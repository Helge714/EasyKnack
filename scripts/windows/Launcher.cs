using System;
using System.Diagnostics;
using System.IO;
using System.Text;
using System.Windows.Forms;

// A small Windows launcher, independent of Java and of the caller's directory.
internal static class Launcher {
    [STAThread] static int Main(string[] args) {
        try {
            string root = AppDomain.CurrentDomain.BaseDirectory;
            string script = Path.Combine(root, "app", "Start-EasyKnack.ps1");
            string command = "& '" + script.Replace("'", "''") + "'";
            foreach (string arg in args) command += " '" + arg.Replace("'", "''") + "'";
            var info = new ProcessStartInfo(Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.System),
                "WindowsPowerShell", "v1.0", "powershell.exe"),
                "-NoLogo -NoProfile -NonInteractive -ExecutionPolicy Bypass -EncodedCommand " +
                Convert.ToBase64String(Encoding.Unicode.GetBytes(command)));
            info.UseShellExecute = false;
            info.CreateNoWindow = true;
            info.WorkingDirectory = root;
            using (var process = Process.Start(info)) { process.WaitForExit(); return process.ExitCode; }
        } catch (Exception error) {
            MessageBox.Show(error.Message, "EasyKnack", MessageBoxButtons.OK, MessageBoxIcon.Error);
            return 1;
        }
    }
}
