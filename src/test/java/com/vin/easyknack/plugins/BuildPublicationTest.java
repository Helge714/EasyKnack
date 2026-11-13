package com.vin.easyknack.plugins;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class BuildPublicationTest {
    @TempDir Path root;

    @Test void publishesOnlyAfterTheWindowsDirectoryLockIsReleased() throws Exception {
        org.junit.jupiter.api.Assumptions.assumeTrue(System.getProperty("os.name").startsWith("Windows"));
        Path source = Files.createDirectory(root.resolve("work")), destination = root.resolve("cache");
        // A child process retains its Windows current-directory handle, just as
        // native build/validation processes can briefly retain their build path.
        Path script = root.resolve("hold.ps1");
        Files.writeString(script, "[Console]::WriteLine('ready'); [Console]::ReadLine() | Out-Null");
        var child = new ProcessBuilder("powershell.exe", "-NoProfile", "-ExecutionPolicy", "Bypass", "-File", script.toString())
                .directory(source.toFile()).redirectError(ProcessBuilder.Redirect.INHERIT).start();
        try {
            assertEquals("ready", child.inputReader().readLine());
            assertThrows(FileSystemException.class, () -> Files.move(source, destination));
            var release = new Thread(() -> {
                try { Thread.sleep(250); child.getOutputStream().write('\n'); child.getOutputStream().flush(); }
                catch (Exception failure) { throw new RuntimeException(failure); }
            });
            release.start(); PluginManager.publishDirectory(source, destination); release.join();
            assertTrue(Files.isDirectory(destination)); assertFalse(Files.exists(source));
        } finally { child.destroyForcibly(); child.waitFor(); }
    }

    @Test void doesNotOverwriteAnExistingDestination() throws Exception {
        Path source = Files.createDirectory(root.resolve("work")), destination = Files.createDirectory(root.resolve("cache"));
        Files.writeString(source.resolve("new"), "new"); Files.writeString(destination.resolve("old"), "old");
        assertThrows(FileAlreadyExistsException.class, () -> PluginManager.publishDirectory(source, destination));
        assertEquals("new", Files.readString(source.resolve("new")));
        assertEquals("old", Files.readString(destination.resolve("old")));
    }
}
