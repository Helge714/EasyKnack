package com.vin.easyknack;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DocumentLaunchTest {
    @TempDir Path directory;

    @Test void forwardsUnicodePathsDuringStartupWithoutStartingAnotherInstance() throws Exception {
        Path first = directory.resolve("Erster Aufbau ä.knack");
        Path second = directory.resolve("zweiter Aufbau.knack");
        try (var owner = DocumentLaunch.start(directory, new String[]{first.toString()})) {
            assertNotNull(owner);
            assertNull(DocumentLaunch.start(directory, new String[]{second.toString()}));
            var requests = new java.util.HashSet<Path>();
            requests.addAll(owner.poll()); requests.addAll(owner.poll());
            assertEquals(java.util.Set.of(first, second), requests);
            assertNull(owner.poll());
            assertNull(DocumentLaunch.start(directory, new String[0]));
            assertEquals(java.util.List.of(), owner.poll());
        }
        try (var replacement = DocumentLaunch.start(directory, new String[0])) {
            assertNotNull(replacement);
            assertNull(replacement.poll());
        }
    }

    @Test void aSeparateJvmForwardsInsteadOfOwningTheApplication() throws Exception {
        try (var owner = DocumentLaunch.start(directory, new String[0])) {
            var process = new ProcessBuilder(Path.of(System.getProperty("java.home"), "bin", NativePlatform.current().executable("java")).toString(),
                    "-cp", System.getProperty("java.class.path"), Sender.class.getName(), directory.toString()).inheritIO().start();
            assertTrue(process.waitFor(15, java.util.concurrent.TimeUnit.SECONDS));
            assertEquals(0, process.exitValue());
            assertEquals(java.util.List.of(directory.resolve("A B ü.knack")), owner.poll());
        }
    }

    @Test void finderOpenRequestsUseTheExistingAtomicInbox() throws Exception {
        try(var owner=DocumentLaunch.start(directory,new String[0])){
            var file=directory.resolve("Finder ä.knack");
            owner.submit(java.util.List.of(file));
            assertEquals(java.util.List.of(file),owner.poll());
            owner.submit(java.util.List.of());
            assertEquals(java.util.List.of(),owner.poll());
            assertNull(owner.poll());
        }
    }

    public static class Sender {
        public static void main(String[] args) throws Exception {
            Path directory = Path.of(args[0]);
            try (var instance = DocumentLaunch.start(directory, new String[]{directory.resolve("A B ü.knack").toString()})) {
                if (instance != null) throw new AssertionError("Second process acquired instance ownership");
            }
        }
    }
}
