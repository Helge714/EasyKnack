package com.vin.easyknack.document;

import java.io.*;
import java.util.Set;
import com.vingame.glswing.document.*;
import com.vingame.glswing.node.document.*;

/** EasyKnack extension, with the unchanged versioned Core payload and migrations. */
public final class KnackDocumentCodec implements GLDocumentCodec<GLNodeGraphDocument> {
    private final GLNodeGraphDocumentCodec core = new GLNodeGraphDocumentCodec();
    public String typeId() { return core.typeId(); }
    public String codecId() { return core.codecId(); }
    public Class<GLNodeGraphDocument> contentType() { return GLNodeGraphDocument.class; }
    public int currentSchemaVersion() { return core.currentSchemaVersion(); }
    public boolean supportsSchemaVersion(int version) { return core.supportsSchemaVersion(version); }
    public Set<String> fileExtensions() { return Set.of("knack", "glgraph"); }
    public GLNodeGraphDocument read(InputStream input, int version) throws IOException { return core.read(input, version); }
    public GLDocumentReadResult<GLNodeGraphDocument> readResult(InputStream input, int version) throws IOException { return core.readResult(input, version); }
    public void write(GLNodeGraphDocument document, OutputStream output) throws IOException { core.write(document, output); }
}
