package com.vin.easyknack.plugins;

import com.vin.easyknack.nodes.*;
import com.vin.easyknack.audio.AudioHostClient;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Pattern;

/** Transactional source installation and content-addressed, matched Java/native builds. */
public final class PluginManager {
    public record Build(Path directory, List<BasicNode> definitions, Map<String,String> sources, boolean cached) {
        public Path host() { return directory.resolve("easyknack-audio-host.exe"); }
    }
    public record Installation(Build build, boolean restartRequired) { }
    private static final PluginManager DEFAULT = new PluginManager(Path.of(System.getProperty("easyknack.plugins",
            Path.of(System.getProperty("user.home"), ".easyknack", "plugins").toString())));
    private static volatile Path activeHost;
    private final Path home;
    private Build active;
    // Classloaders must remain alive for as long as any workspace/history references their definitions.
    private final List<URLClassLoader> loaders = new ArrayList<>();
    public PluginManager(Path home) { this.home = home.toAbsolutePath().normalize(); }
    public static PluginManager current() { return DEFAULT; }
    public static Path activeHost() { return activeHost; }
    public Path sourcesDirectory() { return home.resolve("sources"); }
    public com.vin.easyknack.formula.FormulaNativeCompiler formulaCompiler()throws IOException {
        return new com.vin.easyknack.formula.FormulaNativeCompiler(Path.of(compiler(readIfPresent(home.resolve("toolchain.properties")))),home.resolve("formula-cache"));
    }
    /** Build a local workspace bundle together with installed plugins, without
     * installing sources or publishing persistent plugin state. */
    public synchronized Build buildEphemeral(Map<String,String> files,Consumer<String> progress) throws Exception {
        Files.createDirectories(home.resolve("work"));
        var staging=Files.createTempDirectory(home.resolve("work"),"bake-");
        try {
            for(var entry:files.entrySet()){
                var path=staging.resolve(entry.getKey()).normalize();
                if(!path.startsWith(staging))throw new IOException(com.vin.easyknack.i18n.AppText.literal("Invalid bake source path"));
                Files.createDirectories(path.getParent());Files.writeString(path,entry.getValue());
            }
            var manifest=PluginManifest.read(staging);
            // Keep the already active plugin ABI; baking must not implicitly
            // activate manual edits to ordinary installed plugin sources.
            Map<String,Path> candidates=new TreeMap<>();
            if(active==null)candidates.putAll(installed(readIfPresent(home.resolve("active.properties"))));
            else for(String id:active.sources().keySet())candidates.put(id,active.directory().resolve("sources").resolve(id));
            if(candidates.putIfAbsent(manifest.id(),staging)!=null)throw new IOException(com.vin.easyknack.i18n.AppText.literal("Reserved bake bundle ID is already installed"));
            return build(candidates,progress);
        } finally {
            try(var paths=Files.walk(staging)){for(var path:paths.sorted(Comparator.reverseOrder()).toList())Files.deleteIfExists(path);}
        }
    }
    public synchronized Build startup(Consumer<String> progress) throws Exception {
        if (!Files.isRegularFile(home.resolve("active.properties"))) return null;
        var state = read(home.resolve("active.properties"));
        var candidates = installed(state);
        if(candidates.isEmpty()){Files.deleteIfExists(home.resolve("active.properties"));active=null;if(this==DEFAULT)activeHost=null;return null;}
        Build result;
        try { result = build(candidates, progress); }
        catch (Exception failure) {
            // Manual source edits may fail. Only a still-compatible previous build can be retained.
            var prior = cachePath(state.getProperty("build", ""));
            if (!valid(prior, sdkKey()) || !load(prior,true).sources().keySet().equals(candidates.keySet())) throw failure;
            progress.accept(com.vin.easyknack.i18n.AppText.literal("Plugin rebuild failed; keeping previous build: ") + failure.getMessage());
            result = load(prior, true);
        }
        publishState(result); active = result; return result;
    }
    public synchronized Installation install(Path archive, Consumer<String> progress) throws Exception {
        progress.accept(com.vin.easyknack.i18n.AppText.literal("Unpacking and checking plugin"));
        Files.createDirectories(home.resolve("work"));
        var staging = Files.createTempDirectory(home.resolve("work"), "import-");
        try (var input = Files.newInputStream(archive)) { PluginSdk.unpack(input, staging); }
        var manifest = PluginManifest.read(staging);
        var candidates = installed(readIfPresent(home.resolve("active.properties")));
        candidates.put(manifest.id(), staging);
        var result = build(candidates, progress);
        // Working sources are separate from immutable cached classes/native artifacts.
        Files.createDirectories(sourcesDirectory());
        var destination = sourcesDirectory().resolve(manifest.id());
        if (Files.exists(destination)) Files.move(destination, home.resolve("work").resolve("previous-" + UUID.randomUUID()));
        Files.move(staging, destination);
        publishState(result);
        return new Installation(result, requiresRestart(result));
    }
    public synchronized Installation rebuild(Consumer<String> progress) throws Exception {
        var result = build(installed(readIfPresent(home.resolve("active.properties"))), progress);
        publishState(result); return new Installation(result, requiresRestart(result));
    }
    private boolean requiresRestart(Build next) {
        return active != null && active.sources().entrySet().stream().anyMatch(e -> !e.getValue().equals(next.sources().get(e.getKey())));
    }
    /** Activate on the EDT before refreshing catalogs; the host service then reopens this executable. */
    public synchronized void activate(Build build) {
        NodeRegistry.installPlugins(build.definitions());
        active = build;
        if (this == DEFAULT) activeHost = build.host();
    }
    private Map<String,Path> installed(Properties state) throws IOException {
        var result = new TreeMap<String,Path>();
        for (String id : state.getProperty("plugins", "").split(",")) if (!id.isBlank()) {
            if (!id.matches("[a-z][a-z0-9_-]{0,79}")) throw new IOException(com.vin.easyknack.i18n.AppText.literal("Invalid installed plugin ID"));
            var source = sourcesDirectory().resolve(id);
            if(!Files.exists(source))continue;
            if (!PluginManifest.read(source).id().equals(id)) throw new IOException(com.vin.easyknack.i18n.AppText.literal("Plugin ID changed in ") + source);
            result.put(id, source);
        }
        return result;
    }
    private Path cachePath(String key) throws IOException {
        if (!key.matches("[a-f0-9]{64}")) throw new IOException(com.vin.easyknack.i18n.AppText.literal("Invalid plugin cache key"));
        return home.resolve("cache").resolve(key);
    }
    private String sdkKey() throws Exception {
        var digest = MessageDigest.getInstance("SHA-256");
        digest.update(PluginSdk.bytes());
        for (var type : List.of(BasicNode.class, com.vingame.glswing.node.model.GLNodeValue.class)) {
            var path = Path.of(type.getProtectionDomain().getCodeSource().getLocation().toURI());
            if(type==BasicNode.class && Files.isDirectory(path)) hashPath(digest,path.resolve("com/vin/easyknack/nodes"));
            else hashPath(digest, path);
        }
        digest.update(("plugin-build-1/" + System.getProperty("os.name") + "/" + System.getProperty("os.arch")
                + "/" + System.getProperty("java.version")) .getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(digest.digest());
    }
    private Build build(Map<String,Path> candidates, Consumer<String> progress) throws Exception {
        progress.accept(com.vin.easyknack.i18n.AppText.literal("Checking build cache"));
        if (candidates.isEmpty()) throw new IOException(com.vin.easyknack.i18n.AppText.literal("No source plugins installed"));
        if (!System.getProperty("os.name").startsWith("Windows") || !Set.of("amd64", "x86_64").contains(System.getProperty("os.arch")))
            throw new IOException(com.vin.easyknack.i18n.AppText.literal("Plugin builds currently support Windows x64"));
        String sdk = sdkKey(); var hashes = new TreeMap<String,String>();
        for (var entry : candidates.entrySet()) hashes.put(entry.getKey(), hash(entry.getValue()));
        var tools = readIfPresent(home.resolve("toolchain.properties"));
        String cxx=compiler(tools);
        String javac=tools.getProperty("javac",Path.of(System.getProperty("java.home"),"bin","javac.exe").toString());
        String toolchain=cxx+":"+hash(Path.of(cxx))+":"+javac+":"+hash(Path.of(javac));
        String key = digest((sdk + hashes + toolchain).getBytes(StandardCharsets.UTF_8));
        var destination = cachePath(key);
        if (valid(destination, sdk)) { progress.accept(com.vin.easyknack.i18n.AppText.literal("Loading cached plugins")); return load(destination, true); }
        Files.createDirectories(home.resolve("work"));
        var work = Files.createTempDirectory(home.resolve("work"), "build-");
        var log = work.resolve("build.log");
        progress.accept(com.vin.easyknack.i18n.AppText.literal("Preparing plugin build"));
        try {
            var sdkRoot = work.resolve("sdk");
            PluginSdk.unpack(new ByteArrayInputStream(PluginSdk.bytes()), sdkRoot);
            var sourceRoot = sdkRoot.resolve("src"); var generated = work.resolve("generated");
            Files.createDirectories(generated); Files.createDirectories(work.resolve("classes"));
            var manifests = new ArrayList<PluginManifest>(); var javaSources = new ArrayList<String>();
            for (var entry : candidates.entrySet()) {
                var manifest = PluginManifest.read(entry.getValue()); manifests.add(manifest);
                var snapshot=work.resolve("sources").resolve(entry.getKey());
                copyTree(entry.getValue(), snapshot);
                if(!hash(snapshot).equals(hashes.get(entry.getKey())))throw new IOException(com.vin.easyknack.i18n.AppText.literal("Plugin sources changed during import; retry the build"));
                var sources = files(snapshot.resolve("java"));
                for (var source : sources) if (source.toString().endsWith(".java")) javaSources.add(source.toString());
                var nativeRoot = snapshot.resolve("native");
                for (var source : files(nativeRoot)) {
                    String name = source.getFileName().toString();
                    if (!(name.endsWith(".hpp") || name.endsWith(".cpp") || name.endsWith(".h"))) continue;
                    var target = sourceRoot.resolve("nodes").resolve(nativeRoot.relativize(source));
                    Files.createDirectories(target.getParent()); Files.copy(source, target); // Never overwrite built-ins/other plugins.
                }
            }
            if (javaSources.isEmpty()) throw new IOException(com.vin.easyknack.i18n.AppText.literal("Plugin contains no Java source"));
            var command = new ArrayList<>(List.of(javac, "--release", "17", "-proc:none", "-encoding", "UTF-8", "-classpath", classpath(), "-d", work.resolve("classes").toString()));
            command.addAll(javaSources);progress.accept(com.vin.easyknack.i18n.AppText.literal("Compiling Java nodes"));run(command, work, log);
            var definitions = definitions(work, manifests);
            validateDefinitions(definitions);
            var all = new ArrayList<>(NodeRegistry.builtinDefinitions()); all.addAll(definitions);
            Files.writeString(generated.resolve("ParameterContracts.hpp"), NativeContractGenerator.generate(all));
            generateRegistry(sourceRoot.resolve("nodes"), generated, all);
            run(List.of(cxx, "--version"), work, log);
            var nativeSources = new ArrayList<Path>();nativeSources.add(sourceRoot.resolve("main.cpp"));
            for (var source : files(sourceRoot.resolve("nodes"))) if (source.toString().endsWith(".cpp")) nativeSources.add(source);
            var objects = new ArrayList<String>();
            var nativeBuild = new NativeBuildSettings(sdkRoot);
            for (int i=0;i<nativeSources.size();i++) {
                var source=nativeSources.get(i);var object=work.resolve("host-"+i+".o");objects.add(object.toString());
                command = new ArrayList<>(List.of(cxx));command.addAll(nativeBuild.compile());
                command.addAll(List.of("-I",sdkRoot.resolve("third_party").toString(),
                        "-I",sourceRoot.toString(),"-I",generated.toString(),"-c",source.toString(),"-o",object.toString()));
                String stage="C++ "+(i+1)+"/"+nativeSources.size()+": "+sourceRoot.relativize(source)+(i==0?" + nodes":"");
                run(command,work,log,progress,stage);
            }
            command=new ArrayList<>(List.of(cxx));command.addAll(nativeBuild.arguments("plugin.link.flags"));command.addAll(objects);
            command.addAll(List.of("-o",work.resolve("easyknack-audio-host.exe").toString()));command.addAll(nativeBuild.libraries());
            run(command,work,log,progress,"Linking C++ audio host");
            // Start the actual isolated host and validate every Java/native parameter contract.
            progress.accept(com.vin.easyknack.i18n.AppText.literal("Checking audio host and parameter contracts"));
            try (var host = new AudioHostClient()) {
                host.start(work.resolve("easyknack-audio-host.exe"), true);
                for (var node : definitions) {
                    String rates = "a".repeat(node.processor().inputs().size());
                    host.command("GRAPH 1 plugin " + node.processor().id() + " !" + NativeContractGenerator.fingerprint(node) + " "
                            + node.encode(new NodeState(node.defaults()), p -> -1) + " @ a " + (rates.isEmpty() ? "-" : rates));
                }
            }
            var info = new Properties(); info.setProperty("sdk", sdk); info.setProperty("host", hash(work.resolve("easyknack-audio-host.exe")));
            info.setProperty("classes", hash(work.resolve("classes")));
            info.setProperty("toolchain",digest(toolchain.getBytes(StandardCharsets.UTF_8)));
            hashes.forEach((id, value) -> info.setProperty("source." + id, value));
            write(work.resolve("build.properties"), info);
            Files.createDirectories(destination.getParent());
            if (Files.exists(destination)) Files.move(destination, home.resolve("work").resolve("invalid-" + UUID.randomUUID()));
            Files.move(work, destination);
            progress.accept(com.vin.easyknack.i18n.AppText.literal("Plugin build complete")); return load(destination, false);
        } catch (Exception failure) {
            throw new IOException(com.vin.easyknack.i18n.AppText.literal("Plugin build failed: ") + failure.getMessage() + "\nBuild log: " + log, failure);
        }
    }
    private static String compiler(Properties configuration) throws IOException {
        String configured = System.getProperty("easyknack.plugin.cxx", configuration.getProperty("cxx"));
        if (configured != null && !configured.isBlank()) return configured;
        var roots=com.vin.easyknack.InstallationPaths.compilerRoots();
        for (Path root : roots) if (Files.isDirectory(root))
            try (var paths = Files.walk(root, 4)) {
                var found = paths.filter(p -> p.getFileName().toString().equalsIgnoreCase("clang++.exe")).sorted().findFirst();
                if (found.isPresent()) return found.get().toAbsolutePath().toString();
            }
        throw new IOException(com.vin.easyknack.i18n.AppText.literal("C++ toolchain not found. Configure cxx in plugins/toolchain.properties (LLVM-MinGW Windows x64)."));
    }
    public static String classpath() throws Exception {
        var paths = new LinkedHashSet<String>(List.of(System.getProperty("java.class.path").split(java.io.File.pathSeparator)));
        for (var type : List.of(BasicNode.class, com.vingame.glswing.node.model.GLNodeValue.class)) {
            var path = Path.of(type.getProtectionDomain().getCodeSource().getLocation().toURI()); paths.add(path.toString());
            var lib = path.getParent().resolve("lib");
            if (Files.isDirectory(lib)) for (var p : files(lib)) if (p.toString().endsWith(".jar")) paths.add(p.toString());
        }
        return String.join(java.io.File.pathSeparator, paths.stream().map(p -> Path.of(p).toAbsolutePath().toString()).toList());
    }
    private List<BasicNode> definitions(Path build, List<PluginManifest> manifests) throws Exception {
        var loader = new URLClassLoader(new URL[]{build.resolve("classes").toUri().toURL()}, BasicNode.class.getClassLoader());
        loaders.add(loader); var result = new ArrayList<BasicNode>();
        for (var manifest : manifests) for (String name : manifest.nodes()) {
            try {
                var type = Class.forName(name, true, loader);
                if (type.getClassLoader() != loader) throw new IOException(com.vin.easyknack.i18n.AppText.literal("Plugin shadows application class: ") + name);
                result.add(type.asSubclass(BasicNode.class).getConstructor().newInstance());
            }catch(ReflectiveOperationException|LinkageError error){throw new IOException(com.vin.easyknack.i18n.AppText.literal("Cannot load plugin node: ")+name,error);}
        }
        return result;
    }
    private static void validateDefinitions(List<BasicNode> definitions) throws IOException {
        var ids = new HashSet<String>(); var processors = new HashSet<String>();
        for (var d : NodeRegistry.builtinDefinitions()) { ids.add(d.registration().id()); processors.add(d.processor().id()); }
        for (var d : definitions) {
            if (!ids.add(d.registration().id()) || !processors.add(d.processor().id())) throw new IOException(com.vin.easyknack.i18n.AppText.literal("Duplicate plugin node/processor: ") + d.registration().id());
            d.layout(new NodeState(d.defaults())); NativeContractGenerator.fingerprint(d);
        }
    }
    private Build load(Path path, boolean cached) throws Exception {
        var manifests = new ArrayList<PluginManifest>();
        for (var source : directories(path.resolve("sources"))) manifests.add(PluginManifest.read(source));
        var defs = definitions(path, manifests); validateDefinitions(defs);
        var info = read(path.resolve("build.properties")); var sources = new TreeMap<String,String>();
        for (var manifest : manifests) sources.put(manifest.id(), info.getProperty("source." + manifest.id()));
        return new Build(path, List.copyOf(defs), Map.copyOf(sources), cached);
    }
    private boolean valid(Path path, String sdk) {
        try {
            var info = read(path.resolve("build.properties"));
            if(!sdk.equals(info.getProperty("sdk")) || !hash(path.resolve("classes")).equals(info.getProperty("classes"))
                    || !hash(path.resolve("easyknack-audio-host.exe")).equals(info.getProperty("host")))return false;
            for(var source:directories(path.resolve("sources")))if(!hash(source).equals(info.getProperty("source."+source.getFileName())))return false;
            return true;
        } catch (Exception e) { return false; }
    }
    private void publishState(Build build) throws IOException {
        var p = new Properties(); p.setProperty("build", build.directory().getFileName().toString());
        p.setProperty("plugins", String.join(",", new TreeSet<>(build.sources().keySet())));
        var temporary = Files.createTempFile(home, "active-", ".tmp"); write(temporary, p);
        Files.move(temporary, home.resolve("active.properties"), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
    }
    private static void generateRegistry(Path nodes, Path generated, List<BasicNode> definitions) throws IOException {
        var pattern = Pattern.compile("(?m)^\\s*EASYKNACK_NODE\\(\\s*([A-Za-z_][A-Za-z0-9_:]*)\\s*,\\s*\"([a-z][a-z0-9_-]*)\"\\s*\\)\\s*$");
        var registrations = new TreeMap<String,String>(); var headers = new TreeSet<String>();
        for (var header : files(nodes)) if (header.toString().endsWith(".hpp")) {
            var matcher = pattern.matcher(Files.readString(header));
            while (matcher.find()) {
                if (registrations.putIfAbsent(matcher.group(2), matcher.group(1)) != null) throw new IOException(com.vin.easyknack.i18n.AppText.literal("Duplicate native processor: ") + matcher.group(2));
                headers.add("nodes/" + nodes.relativize(header).toString().replace('\\', '/'));
            }
        }
        for (var d : definitions) if (!registrations.containsKey(d.processor().id())) throw new IOException(com.vin.easyknack.i18n.AppText.literal("Native processor missing: ") + d.processor().id());
        var s = new StringBuilder("#pragma once\n#include <memory>\n#include <string>\n#include <stdexcept>\n");
        for (var header : headers) s.append("#include \"").append(header).append("\"\n");
        s.append("namespace easyknack { inline std::unique_ptr<NodeProcessor> createNodeProcessor(const std::string& id){\n");
        registrations.forEach((id, type) -> s.append("if(id==\"").append(id).append("\")return std::make_unique<").append(type).append(">();\n"));
        Files.writeString(generated.resolve("GeneratedNodeRegistry.hpp"), s.append("throw std::runtime_error(\"Unknown processor: \"+id);}}\n").toString());
    }
    private static void run(List<String> command, Path directory, Path log) throws Exception {
        run(command,directory,log,null,null);
    }
    private static void run(List<String> command, Path directory, Path log, Consumer<String> progress, String stage) throws Exception {
        Files.writeString(log, "\n" + String.join(" ", command) + "\n", StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        long started=System.nanoTime();if(progress!=null)progress.accept("0 s — "+stage);
        var process = new ProcessBuilder(command).directory(directory.toFile()).redirectErrorStream(true).redirectOutput(ProcessBuilder.Redirect.appendTo(log.toFile())).start();
        try {
            while(!process.waitFor(1,TimeUnit.SECONDS)) {
                long seconds=TimeUnit.NANOSECONDS.toSeconds(System.nanoTime()-started);
                if(progress!=null)progress.accept(seconds+" s — "+stage);
                if(seconds>=300)throw new IOException(com.vin.easyknack.i18n.AppText.literal("Compiler timed out: ")+(stage==null?command.get(0):stage));
            }
            if (process.exitValue() != 0) throw new IOException(com.vin.easyknack.i18n.AppText.literal("Compiler exited with ") + process.exitValue());
        } finally { if (process.isAlive()) process.destroyForcibly(); }
    }
    private static List<Path> files(Path root) throws IOException {
        if (!Files.isDirectory(root)) return List.of();
        try (var paths = Files.walk(root)) {
            var all = paths.sorted().toList();
            if (all.stream().anyMatch(Files::isSymbolicLink)) throw new IOException(com.vin.easyknack.i18n.AppText.literal("Plugin sources must not contain symbolic links"));
            return all.stream().filter(Files::isRegularFile).toList();
        }
    }
    private static List<Path> directories(Path root) throws IOException { try (var paths = Files.list(root)) { return paths.filter(Files::isDirectory).sorted().toList(); } }
    private static void copyTree(Path from, Path to) throws IOException {
        for (var source : files(from)) { var dest = to.resolve(from.relativize(source)); Files.createDirectories(dest.getParent()); Files.copy(source, dest); }
    }
    private static void hashPath(MessageDigest d, Path path) throws IOException {
        if (Files.isDirectory(path)) for (var file : files(path)) {
            d.update(path.relativize(file).toString().replace('\\', '/').getBytes(StandardCharsets.UTF_8)); d.update((byte)0);
            try (var in = Files.newInputStream(file)) { in.transferTo(new DigestOutputStream(OutputStream.nullOutputStream(), d)); }
        }
        else try (var in = Files.newInputStream(path)) { in.transferTo(new DigestOutputStream(OutputStream.nullOutputStream(), d)); }
    }
    private static String hash(Path path) throws Exception { var d = MessageDigest.getInstance("SHA-256"); hashPath(d, path); return HexFormat.of().formatHex(d.digest()); }
    private static String digest(byte[] bytes) throws Exception { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes)); }
    private static Properties readIfPresent(Path path) throws IOException { return Files.exists(path) ? read(path) : new Properties(); }
    private static Properties read(Path path) throws IOException { var p = new Properties(); try (var reader = Files.newBufferedReader(path)) { p.load(reader); } return p; }
    private static void write(Path path, Properties p) throws IOException { try (var writer = Files.newBufferedWriter(path)) { p.store(writer, "EasyKnack plugin cache"); } }
}
