package com.vin.easyknack.audio;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.UUID;
import java.util.concurrent.*;

/** Blocking protocol client. Only used on the audio-control worker, never on the EDT. */
public final class AudioHostClient implements AutoCloseable {
    private Process process;
    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;

    public void start(Path executable, boolean nullDevice) throws Exception {
        String token = UUID.randomUUID().toString();
        var builder = new ProcessBuilder(executable.toAbsolutePath().toString(), token);
        if (nullDevice) builder.command().add("--null");
        builder.redirectError(ProcessBuilder.Redirect.INHERIT);
        process = builder.start();
        try {
            var output = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
            String line = CompletableFuture.supplyAsync(() -> {
                try { return output.readLine(); }
                catch (IOException error) { throw new CompletionException(error); }
            }).get(12, TimeUnit.SECONDS);
            if (line == null || !line.matches("READY 11 [0-9]+")) throw new IOException(com.vin.easyknack.ui.AppText.literal("Audiohost nicht bereit oder veraltet: bitte C++-Host neu bauen"));
            int port = Integer.parseInt(line.substring(9));
            socket = new Socket();
            socket.connect(new InetSocketAddress("127.0.0.1", port), 2000);
            socket.setSoTimeout(15000);
            socket.setTcpNoDelay(true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
            if (!command("HELLO 11 " + token).equals("OK 11")) throw new IOException(com.vin.easyknack.ui.AppText.literal("Protokollversion unpassend"));
        } catch (Exception failure) { close(); throw failure; }
    }

    /** A rejected command leaves the transport and process usable. */
    public static final class CommandRejectedException extends IOException {
        public CommandRejectedException(String response) { super(response); }
    }

    public String command(String command) throws IOException {
        if (writer == null) throw new IOException(com.vin.easyknack.ui.AppText.literal("Audiohost nicht verbunden"));
        writer.write(command); writer.newLine(); writer.flush();
        String response = reader.readLine();
        if (response != null && response.startsWith("ERR ")) throw new CommandRejectedException(response.substring(4));
        if (response == null || !(response.equals("OK") || response.startsWith("OK ")))
            throw new IOException(response == null ? "Audiohost beendet" : response);
        return response;
    }

    public static Path defaultExecutable() {
        var pluginHost=com.vin.easyknack.plugins.PluginManager.activeHost();
        if(pluginHost!=null)return pluginHost;
        String configured = System.getProperty("easyknack.audio.host");
        if (configured != null) return Path.of(configured);
        return com.vin.easyknack.InstallationPaths.audioHost();
    }

    @Override public void close() {
        try { if (socket != null) socket.close(); } catch (IOException ignored) { }
        if (process != null) {
            try {
                if (!process.waitFor(500, TimeUnit.MILLISECONDS)) process.destroyForcibly();
            } catch (InterruptedException interrupted) {
                process.destroyForcibly(); Thread.currentThread().interrupt();
            }
        }
        socket = null; writer = null; reader = null; process = null;
    }
}


