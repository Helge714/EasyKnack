using System;
using System.Diagnostics;
using System.IO;
using System.Reflection;
using System.Text;
using System.Windows.Forms;

// Installer prerequisite only: shares discovery/validation with the application launcher.
internal static class JavaSetup {
    [STAThread] static int Main(string[] args) {
        int ui = Array.IndexOf(args, "/ui");
        bool quiet = ui >= 0 && ui + 1 < args.Length && args[ui + 1] != "4" && args[ui + 1] != "5";
        bool checkOnly = Array.IndexOf(args, "/check") >= 0;
        string temporary = Path.Combine(Path.GetTempPath(), "EasyKnack-Java-" + Guid.NewGuid().ToString("N"));
        try {
            Directory.CreateDirectory(temporary);
            foreach (string name in new[] { "JavaPrerequisite.ps1", "JavaSetup.ps1" }) {
                using (var input = Assembly.GetExecutingAssembly().GetManifestResourceStream(name))
                using (var output = File.Create(Path.Combine(temporary, name))) {
                    if (input == null) throw new IOException("Missing Java prerequisite resource: " + name);
                    input.CopyTo(output);
                }
            }
            string command = "& '" + Path.Combine(temporary,"JavaSetup.ps1").Replace("'", "''") + "'" + (quiet ? " -Quiet" : "") + (checkOnly ? " -CheckOnly" : "") + "; exit $LASTEXITCODE";
            var info = new ProcessStartInfo(Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.System),
                "WindowsPowerShell", "v1.0", "powershell.exe"),
                "-NoLogo -NoProfile -NonInteractive -ExecutionPolicy Bypass -EncodedCommand " +
                Convert.ToBase64String(Encoding.Unicode.GetBytes(command)));
            info.UseShellExecute = false; info.CreateNoWindow = true; info.WorkingDirectory = temporary;
            using (var process = Process.Start(info)) { process.WaitForExit(); return process.ExitCode; }
        } catch (Exception failure) {
            if (!quiet) MessageBox.Show(failure.Message,"EasyKnack - Java setup",MessageBoxButtons.OK,MessageBoxIcon.Error);
            return 1603;
        } finally {
            // Only the two files and directory created by this process are removed.
            foreach (string name in new[] { "JavaPrerequisite.ps1", "JavaSetup.ps1" }) {
                try { File.Delete(Path.Combine(temporary,name)); } catch (IOException) { }
            }
            try { Directory.Delete(temporary); } catch (IOException) { }
        }
    }
}
