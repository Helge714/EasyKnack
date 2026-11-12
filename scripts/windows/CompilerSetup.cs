using System;
using System.Collections.Generic;
using System.IO;
using System.IO.Compression;
using System.Reflection;
using System.Security.Cryptography;
using System.Text.RegularExpressions;

// MSI transaction helper. The portable image remains fully extracted.
internal static class CompilerSetup {
    const string Manifest = ".easyknack-compiler.manifest", Token = ".easyknack-compiler.token";
    const string Header = "EasyKnack compiler archive v1";
    static string tools, current, journal;
    sealed class Item { public string Path, Hash; public long Size; }
    // MSI impersonates the installing user. Check each existing directory once
    // per operation instead of re-querying all Windows ancestors
    // for every compiler header. Files themselves are checked on every access.
    static readonly HashSet<string> checkedDirectories = new HashSet<string>(StringComparer.OrdinalIgnoreCase);
    static void Safe(string path) {
        if (File.Exists(path) && (File.GetAttributes(path) & FileAttributes.ReparsePoint) != 0) throw new IOException("Linked file: " + path);
        for (var p = new DirectoryInfo(System.IO.Path.GetFullPath(path)); p != null; p = p.Parent) {
            if (checkedDirectories.Contains(p.FullName)) break;
            if (!p.Exists) continue;
            if ((p.Attributes & FileAttributes.ReparsePoint) != 0) throw new IOException("Linked installer path: " + p.FullName);
            checkedDirectories.Add(p.FullName);
        }
    }
    static string Child(string root, string relative) {
        if (String.IsNullOrEmpty(relative) || relative.Contains("\\") || relative.Contains(":")) throw new IOException("Invalid compiler path");
        foreach (string part in relative.Split('/'))
            if (part.Length == 0 || part == "." || part == ".." || part.EndsWith(".") || part.EndsWith(" ") || part.IndexOfAny(System.IO.Path.GetInvalidFileNameChars()) >= 0)
                throw new IOException("Invalid compiler path: " + relative);
        string path = System.IO.Path.GetFullPath(System.IO.Path.Combine(root, relative));
        if (!path.StartsWith(root + System.IO.Path.DirectorySeparatorChar, StringComparison.OrdinalIgnoreCase)) throw new IOException("Compiler path escaped root");
        Safe(path); return path;
    }
    static string Hash(Stream stream) { using (var sha = SHA256.Create()) return BitConverter.ToString(sha.ComputeHash(stream)).Replace("-", ""); }
    static List<Item> ReadManifest(string text) {
        var lines = text.Replace("\r", "").TrimEnd('\n').Split('\n');
        if (lines.Length < 2 || lines[0] != Header) throw new IOException("Invalid compiler manifest");
        var result = new List<Item>(); var names = new HashSet<string>(StringComparer.OrdinalIgnoreCase);
        for (int i = 1; i < lines.Length; i++) {
            string[] fields = lines[i].Split('\t'); long size;
            if (fields.Length != 3 || !Regex.IsMatch(fields[0], "^[A-Fa-f0-9]{64}$") || !Int64.TryParse(fields[1], out size) || size < 0 || !names.Add(fields[2]) || String.Equals(fields[2], Manifest, StringComparison.OrdinalIgnoreCase) || String.Equals(fields[2], Token, StringComparison.OrdinalIgnoreCase))
                throw new IOException("Invalid compiler manifest entry");
            Child(tools, fields[2]);
            result.Add(new Item { Hash = fields[0], Size = size, Path = fields[2] });
        }
        return result;
    }
    static List<Item> Owned(string root) {
        Safe(root); Safe(System.IO.Path.Combine(root, Manifest));
        if (!File.Exists(System.IO.Path.Combine(root, Manifest))) throw new IOException("Refusing to replace an unowned compiler directory: " + root);
        return ReadManifest(File.ReadAllText(System.IO.Path.Combine(root, Manifest)));
    }
    // Remove only manifest-owned files. Extra user files keep their backup directory.
    static void Clean(string root) {
        if (!Directory.Exists(root)) return;
        var entries = Owned(root); var directories = new HashSet<string>(StringComparer.OrdinalIgnoreCase);
        foreach (var entry in entries) {
            string path = Child(root, entry.Path); File.Delete(path);
            for (string p = System.IO.Path.GetDirectoryName(path); p != root; p = System.IO.Path.GetDirectoryName(p)) directories.Add(p);
        }
        var ordered = new List<string>(directories); ordered.Sort((a,b) => b.Length.CompareTo(a.Length));
        foreach (string path in ordered) { Safe(path); if (Directory.Exists(path) && Directory.GetFileSystemEntries(path).Length == 0) Directory.Delete(path); }
        File.Delete(Child(root, Manifest)); File.Delete(Child(root, Token));
        if (Directory.GetFileSystemEntries(root).Length == 0) Directory.Delete(root);
    }
    static string[] Transaction() {
        Safe(journal); if (!File.Exists(journal)) return null;
        var data = File.ReadAllLines(journal);
        if (data.Length != 3 || (data[0] != "install" && data[0] != "remove" && data[0] != "committed-install" && data[0] != "committed-remove") || !Regex.IsMatch(data[1], "^[a-f0-9]{32}$") || (data[2] != "old" && data[2] != "new")) throw new IOException("Invalid compiler transaction");
        return data;
    }
    static string Stage(string id) { return Child(tools, ".ek-compiler-stage-" + id); }
    static string Backup(string id) { return Child(tools, ".ek-compiler-backup-" + id); }
    static void Rollback() {
        var data = Transaction(); if (data == null) return;
        if (data[0].StartsWith("committed-", StringComparison.Ordinal)) { Commit(); return; }
        string stage = Stage(data[1]), backup = Backup(data[1]);
        if (Directory.Exists(current) && File.Exists(Child(current, Token)) && File.ReadAllText(Child(current, Token)) == data[1]) Clean(current);
        if (Directory.Exists(backup)) { if (Directory.Exists(current)) throw new IOException("Cannot restore compiler backup"); Directory.Move(backup, current); }
        Clean(stage); File.Delete(journal);
        if (data[2] == "new") EmptyParents();
    }
    static void Commit() {
        var data = Transaction(); if (data == null) return;
        if (!data[0].StartsWith("committed-", StringComparison.Ordinal)) {
            data[0] = "committed-" + data[0]; File.WriteAllLines(journal, data);
        }
        Clean(Backup(data[1])); Clean(Stage(data[1])); File.Delete(journal);
        // Remove empty custom-action directories which MSI does not own.
        if (data[0] == "committed-remove") EmptyParents();
    }
    static void EmptyParents() {
        string app = System.IO.Path.GetDirectoryName(tools), root = System.IO.Path.GetDirectoryName(app);
        foreach (string path in new[] { tools, app, root }) {
            Safe(path); if (Directory.Exists(path) && Directory.GetFileSystemEntries(path).Length == 0) Directory.Delete(path);
        }
    }
    static void Prepare(string operation) {
        Rollback();
        if (operation == "remove" && Directory.Exists(current)) Owned(current);
        if (operation == "remove" && !Directory.Exists(current)) return;
        Directory.CreateDirectory(tools);
        string id = Guid.NewGuid().ToString("N"), stage = Stage(id), backup = Backup(id);
        using (var stream = new FileStream(journal, FileMode.CreateNew, FileAccess.Write))
        using (var writer = new StreamWriter(stream)) { writer.WriteLine(operation); writer.WriteLine(id); writer.WriteLine(Directory.Exists(current) ? "old" : "new"); }
        if (operation == "install") {
            string archive = Child(tools, "llvm-mingw.zip"), expected;
            using (var reader = new StreamReader(Assembly.GetExecutingAssembly().GetManifestResourceStream("compiler.sha256"))) expected = reader.ReadToEnd().Trim();
            using (var stream = File.OpenRead(archive)) {
                if (!String.Equals(Hash(stream), expected, StringComparison.OrdinalIgnoreCase)) throw new IOException("Compiler archive checksum mismatch");
                stream.Position = 0;
                using (var zip = new ZipArchive(stream, ZipArchiveMode.Read)) {
                    var manifest = zip.GetEntry(Manifest); if (manifest == null) throw new IOException("Missing compiler manifest");
                    string text; using (var reader = new StreamReader(manifest.Open())) text = reader.ReadToEnd();
                    var entries = ReadManifest(text);
                    if (zip.Entries.Count != entries.Count + 1) throw new IOException("Unexpected archive entries");
                    var names = new HashSet<string>(StringComparer.OrdinalIgnoreCase);
                    foreach (var entry in zip.Entries) if (!names.Add(entry.FullName)) throw new IOException("Duplicate archive entry");
                    Directory.CreateDirectory(stage);
                    File.WriteAllText(Child(stage, Manifest), text); File.WriteAllText(Child(stage, Token), id);
                    foreach (var entry in entries) {
                        var source = zip.GetEntry(entry.Path); if (source == null || source.Length != entry.Size) throw new IOException("Missing or invalid compiler file: " + entry.Path);
                        string path = Child(stage, entry.Path); Directory.CreateDirectory(System.IO.Path.GetDirectoryName(path));
                        using (var sha = SHA256.Create())
                        using (var input = source.Open())
                        using (var output = new FileStream(path, FileMode.CreateNew))
                        using (var verified = new CryptoStream(output, sha, CryptoStreamMode.Write)) {
                            input.CopyTo(verified); verified.FlushFinalBlock();
                            if (!String.Equals(BitConverter.ToString(sha.Hash).Replace("-", ""), entry.Hash, StringComparison.OrdinalIgnoreCase)) throw new IOException("Invalid compiler checksum: " + entry.Path);
                        }
                    }
                }
            }
        }
        if (operation == "remove") Directory.Move(current, backup);
    }
    static void Publish() {
        var data = Transaction();
        if (data == null || data[0] != "install") throw new IOException("Missing prepared compiler transaction");
        string stage = Stage(data[1]), backup = Backup(data[1]);
        Owned(stage);
        if (Directory.Exists(current)) { Owned(current); Directory.Move(current, backup); }
        Directory.Move(stage, current);
    }
    static int Main(string[] args) {
        try {
            if (args.Length != 2) throw new ArgumentException("Expected operation and install directory");
            string root = System.IO.Path.GetFullPath(args[1]); Safe(root);
            tools = System.IO.Path.Combine(root, "app", "tools"); Safe(tools);
            current = System.IO.Path.Combine(tools, "llvm-mingw"); Safe(current);
            journal = System.IO.Path.Combine(tools, ".ek-compiler-transaction");
            switch (args[0]) {
                case "install": case "remove": Prepare(args[0]); break;
                case "publish": Publish(); break;
                case "rollback": Rollback(); break;
                case "commit": Commit(); break;
                default: throw new ArgumentException("Unknown compiler operation");
            }
            return 0;
        } catch (Exception e) {
            try { string log = System.IO.Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData), "EasyKnack", "logs"); Directory.CreateDirectory(log); File.AppendAllText(System.IO.Path.Combine(log, "compiler-setup.log"), DateTime.UtcNow.ToString("o") + " " + e + Environment.NewLine); } catch { }
            return 1603;
        }
    }
}
