package com.vin.easyknack;

import java.io.IOException;
import java.nio.channels.*;
import java.nio.file.*;
import java.util.*;

/** Per-user instance ownership and durable, atomic Explorer launch requests. */
public final class DocumentLaunch implements AutoCloseable {
    private final Path inbox;
    private final FileChannel channel;
    private final FileLock lock;

    private DocumentLaunch(Path inbox, FileChannel channel, FileLock lock) {
        this.inbox = inbox; this.channel = channel; this.lock = lock;
    }

    public static DocumentLaunch start(String[] args) throws IOException {
        return start(Path.of(System.getProperty("user.home"), ".easyknack", "instance"), args);
    }

    static DocumentLaunch start(Path directory, String[] args) throws IOException {
        var paths = new ArrayList<String>();
        for (String arg : args) paths.add(Path.of(arg).toAbsolutePath().normalize().toString());
        Files.createDirectories(directory);
        Path inbox = directory.resolve("requests");
        Files.createDirectories(inbox);
        FileChannel channel = FileChannel.open(directory.resolve("owner.lock"), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        FileLock lock = null;
        try {
            try { lock = channel.tryLock(); } catch (OverlappingFileLockException alreadyOwned) { /* Same JVM in tests. */ }
            // Empty requests bring the existing window forward. New instances need no such request.
            if (lock == null || !paths.isEmpty()) {
                var data = new Properties();
                data.setProperty("count", Integer.toString(paths.size()));
                for (int i = 0; i < paths.size(); i++) data.setProperty("path." + i, paths.get(i));
                Path temporary = Files.createTempFile(inbox, "launch-", ".tmp");
                try {
                    try (var out = Files.newOutputStream(temporary)) { data.store(out, "EasyKnack document launch"); }
                    Files.move(temporary, inbox.resolve(String.format(Locale.ROOT, "%019d-%s.request", System.currentTimeMillis(), UUID.randomUUID())), StandardCopyOption.ATOMIC_MOVE);
                } finally { Files.deleteIfExists(temporary); }
            }
            if (lock == null) { channel.close(); return null; }
            return new DocumentLaunch(inbox, channel, lock);
        } catch (IOException | RuntimeException failure) {
            if (lock != null) lock.release();
            channel.close(); throw failure;
        }
    }

    /** Called only by the owning application's UI once it can handle another request. */
    public List<Path> poll() throws IOException {
        try (var entries = Files.list(inbox)) {
            var next = entries.filter(p -> p.getFileName().toString().endsWith(".request")).sorted().findFirst();
            if (next.isEmpty()) return null;
            Path file = next.get();
            try {
                if (Files.size(file) > 1024 * 1024) throw new IOException("Document launch request is too large");
                var data = new Properties();
                try (var input = Files.newInputStream(file)) { data.load(input); }
                int count = Integer.parseInt(data.getProperty("count"));
                if (count < 0 || count > 1000) throw new IOException("Invalid document launch count");
                var paths = new ArrayList<Path>();
                for (int i = 0; i < count; i++) paths.add(Path.of(Objects.requireNonNull(data.getProperty("path." + i))));
                return List.copyOf(paths);
            } finally { Files.deleteIfExists(file); }
        }
    }

    @Override public synchronized void close() {
        try { if (lock.isValid()) lock.release(); } catch (IOException failure) { failure.printStackTrace(); }
        try { channel.close(); } catch (IOException failure) { failure.printStackTrace(); }
    }
}
