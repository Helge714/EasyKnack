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

    // Runtime profile: native Windows x64 C/C++, not a cross-platform debugger SDK.
    public static void CopyCompiler(string source, string destination) {
        source = Path.GetFullPath(source);
        destination = Path.GetFullPath(destination);
        if (Directory.Exists(destination) || File.Exists(destination))
            throw new IOException("Compiler destination already exists: " + destination);
        if (destination.StartsWith(source.TrimEnd(Path.DirectorySeparatorChar) + Path.DirectorySeparatorChar, StringComparison.OrdinalIgnoreCase))
            throw new IOException("Compiler destination is inside its source");
        var bin = Path.Combine(source, "bin");
        var binaries = new HashSet<string>(StringComparer.OrdinalIgnoreCase) {
            "clang++.exe", "clang.exe", "ld.lld.exe", "mingw32-common.cfg", "x86_64-w64-windows-gnu.cfg", "llvm-ar.exe", "llvm-ranlib.exe", "llvm-strip.exe",
            "libclang-cpp.dll", "libc++.dll", "libunwind.dll", "libwinpthread-1.dll", "libomp.dll",
            "libclang_rt.asan_dynamic-x86_64.dll"
        };
        string[] drivers = Directory.GetFiles(bin, "clang-*.exe");
        var actualDrivers = Array.FindAll(drivers, path => System.Text.RegularExpressions.Regex.IsMatch(Path.GetFileName(path), @"^clang-\d+\.exe$"));
        if (actualDrivers.Length != 1) throw new IOException("Expected one versioned Clang driver");
        binaries.Add(Path.GetFileName(actualDrivers[0]));
        var llvm = Directory.GetFiles(bin, "libLLVM-*.dll");
        if (llvm.Length != 1) throw new IOException("Expected one LLVM runtime library");
        binaries.Add(Path.GetFileName(llvm[0]));
        foreach (string alias in new[] { "c++.exe", "cc.exe", "g++.exe", "gcc.exe", "ar.exe", "ranlib.exe", "strip.exe", "clang-target-wrapper.exe" }) {
            binaries.Add(alias); binaries.Add("x86_64-w64-mingw32-" + alias);
        }
        foreach (string alias in new[] { "clang++.exe", "clang.exe", "llvm-ar.exe", "llvm-ranlib.exe", "llvm-strip.exe" })
            binaries.Add("x86_64-w64-mingw32-" + alias);
        foreach (string required in new[] { "clang++.exe", "clang.exe", "ld.lld.exe", "mingw32-common.cfg", "x86_64-w64-windows-gnu.cfg", "libclang-cpp.dll", "libc++.dll", "libunwind.dll" })
            if (!File.Exists(Path.Combine(bin, required))) throw new IOException("Missing compiler dependency: " + required);
        foreach (string required in new[] { "include", "lib/clang", "x86_64-w64-mingw32", "share" })
            if (!Directory.Exists(Path.Combine(source, required))) throw new IOException("Missing compiler directory: " + required);
        var contents = Contents(source); // Refuse reparse points before creating any output.
        Directory.CreateDirectory(destination);
        foreach (var item in contents) if (item is FileInfo) {
            string relative = Path.GetRelativePath(source, item.FullName).Replace('\\', '/');
            bool keep = relative.IndexOf('/') < 0 || relative.StartsWith("include/", StringComparison.Ordinal)
                || relative.StartsWith("lib/clang/", StringComparison.Ordinal)
                || relative.StartsWith("x86_64-w64-mingw32/", StringComparison.Ordinal)
                || relative.StartsWith("share/", StringComparison.Ordinal)
                || relative.StartsWith("bin/", StringComparison.Ordinal) && binaries.Contains(relative.Substring(4));
            if (!keep) continue;
            string path = Path.Combine(destination, relative);
            Directory.CreateDirectory(Path.GetDirectoryName(path));
            File.Copy(item.FullName, path, false);
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
