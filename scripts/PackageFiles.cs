using System;
using System.Collections.Generic;
using System.IO;
using System.Security.Cryptography;

// Packaging-only filesystem work. Never modifies the source tree or overwrites files.
public static class EasyKnackPackageFiles {
    public sealed class Entry {
        public string path { get; set; }
        public long bytes { get; set; }
        public string sha256 { get; set; }
    }

    private static List<FileSystemInfo> Contents(string root) {
        var result = new List<FileSystemInfo>();
        var pending = new Stack<DirectoryInfo>();
        pending.Push(new DirectoryInfo(root));
        while (pending.Count > 0) {
            var directory = pending.Pop();
            if ((directory.Attributes & FileAttributes.ReparsePoint) != 0)
                throw new IOException("Linked packaging directory: " + directory.FullName);
            foreach (var item in directory.EnumerateFileSystemInfos()) {
                if ((item.Attributes & FileAttributes.ReparsePoint) != 0)
                    throw new IOException("Linked packaging entry: " + item.FullName);
                result.Add(item);
                if (item is DirectoryInfo child) pending.Push(child);
            }
        }
        result.Sort((a, b) => StringComparer.Ordinal.Compare(a.FullName, b.FullName));
        return result;
    }

    public static void CopyTree(string source, string destination) {
        source = Path.GetFullPath(source).TrimEnd(Path.DirectorySeparatorChar);
        destination = Path.GetFullPath(destination).TrimEnd(Path.DirectorySeparatorChar);
        if (Directory.GetParent(source) == null || !Directory.Exists(source))
            throw new IOException("Invalid packaging source: " + source);
        if (File.Exists(destination) || Directory.Exists(destination))
            throw new IOException("Packaging destination already exists: " + destination);
        if (destination.StartsWith(source + Path.DirectorySeparatorChar, StringComparison.OrdinalIgnoreCase))
            throw new IOException("Packaging destination is inside the source tree");
        var contents = Contents(source);
        Directory.CreateDirectory(destination);
        foreach (var item in contents) {
            string path = Path.Combine(destination, Path.GetRelativePath(source, item.FullName));
            if (item is DirectoryInfo) Directory.CreateDirectory(path);
            else {
                Directory.CreateDirectory(Path.GetDirectoryName(path));
                File.Copy(item.FullName, path, false);
            }
        }
    }

    public static Entry[] Inventory(string root) {
        root = Path.GetFullPath(root);
        var result = new List<Entry>();
        foreach (var item in Contents(root)) if (item is FileInfo file) {
            using (var stream = File.OpenRead(file.FullName)) {
                result.Add(new Entry {
                    path = Path.GetRelativePath(root, file.FullName), bytes = file.Length,
                    sha256 = Convert.ToHexString(SHA256.HashData(stream))
                });
            }
        }
        return result.ToArray();
    }
}
