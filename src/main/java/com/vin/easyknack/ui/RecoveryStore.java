package com.vin.easyknack.ui;

import java.io.*;
import java.nio.channels.*;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import java.util.zip.*;
import com.vingame.glswing.node.document.GLNodeGraphDocument;

/** Atomic crash snapshots, separate from user files. All calls belong to one I/O worker. */
final class RecoveryStore implements AutoCloseable {
    static final Duration RETENTION=Duration.ofDays(7);
    record Entry(Path file,Path original,Instant saved,GLNodeGraphDocument document) {}
    private final Path directory,file,lockFile;
    private final FileChannel channel;
    private final FileLock lock;
    private final KnackDocumentCodec codec=new KnackDocumentCodec();
    RecoveryStore(Path directory)throws IOException{
        this.directory=directory;Files.createDirectories(directory);
        String id=UUID.randomUUID().toString();file=directory.resolve(id+".recovery");lockFile=directory.resolve(id+".lock");
        channel=FileChannel.open(lockFile,StandardOpenOption.CREATE,StandardOpenOption.WRITE);lock=channel.lock();
    }
    void write(GLNodeGraphDocument document,Path original)throws IOException{
        Path temporary=Files.createTempFile(directory,"snapshot-",".tmp");
        try{
            try(var out=new ZipOutputStream(Files.newOutputStream(temporary))){
                var metadata=new Properties();metadata.setProperty("schema",Integer.toString(codec.currentSchemaVersion()));
                if(original!=null)metadata.setProperty("original",original.toAbsolutePath().normalize().toString());
                out.putNextEntry(new ZipEntry("metadata.properties"));metadata.store(out,"EasyKnack recovery");out.closeEntry();
                out.putNextEntry(new ZipEntry("document.knack"));codec.write(document,out);out.closeEntry();
            }
            try(var data=FileChannel.open(temporary,StandardOpenOption.WRITE)){data.force(true);}
            Files.move(temporary,file,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);
        }finally{Files.deleteIfExists(temporary);}
    }
    void touch()throws IOException{if(Files.exists(file))Files.setLastModifiedTime(file,java.nio.file.attribute.FileTime.from(Instant.now()));}
    void clear()throws IOException{Files.deleteIfExists(file);}
    List<Entry> scan(Instant now)throws IOException{return scan(now,RETENTION);}
    List<Entry> scan(Instant now,Duration retention)throws IOException{
        if(retention.isZero()||retention.isNegative())throw new IllegalArgumentException("Retention must be positive");
        var result=new ArrayList<Entry>();
        try(var entries=Files.list(directory)){
            for(var candidate:entries.filter(p->p.toString().endsWith(".recovery")).toList()){
                if(candidate.equals(file))continue;
                Path otherLock=candidate.resolveSibling(candidate.getFileName().toString().replace(".recovery",".lock"));
                try(var other=FileChannel.open(otherLock,StandardOpenOption.CREATE,StandardOpenOption.WRITE)){
                    FileLock claimed;try{claimed=other.tryLock();}catch(OverlappingFileLockException active){continue;}
                    if(claimed==null)continue;
                    try(claimed){
                        Instant saved=Files.getLastModifiedTime(candidate).toInstant();
                        if(saved.isBefore(now.minus(retention))){Files.deleteIfExists(candidate);continue;}
                        try(var zip=new ZipFile(candidate.toFile())){
                            var metadata=new Properties();try(var in=zip.getInputStream(zip.getEntry("metadata.properties"))){metadata.load(in);}
                            String original=metadata.getProperty("original");
                            try(var in=zip.getInputStream(zip.getEntry("document.knack"))){result.add(new Entry(candidate,original==null?null:Path.of(original),saved,codec.read(in,Integer.parseInt(metadata.getProperty("schema")))));}
                        }catch(Exception invalid){System.err.println("Cannot read recovery snapshot "+candidate+": "+invalid.getMessage());}
                    }
                }
            }
        }
        result.sort(Comparator.comparing(Entry::saved).reversed());return List.copyOf(result);
    }
    void delete(Entry entry)throws IOException{
        if(!entry.file().toAbsolutePath().normalize().getParent().equals(directory.toAbsolutePath().normalize()))throw new IOException("Invalid recovery location");
        Files.deleteIfExists(entry.file());
    }
    @Override public void close()throws IOException{lock.release();channel.close();Files.deleteIfExists(lockFile);}
}
