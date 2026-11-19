"""Exercise the unchanged loopback protocol without audio hardware."""
import queue
import socket
import subprocess
import sys
import threading
import time
import uuid


def check_host(executable):
    token = uuid.uuid4().hex
    process = subprocess.Popen([executable, token, "--null"], stdout=subprocess.PIPE,
                               stderr=subprocess.PIPE, text=True)
    lines = queue.Queue()
    errors = []
    def read_stdout():
        for line in process.stdout:
            lines.put(line.strip())
    def read_stderr():
        for line in process.stderr:
            if len(errors) < 100:
                errors.append(line)
    threading.Thread(target=read_stdout, daemon=True).start()
    threading.Thread(target=read_stderr, daemon=True).start()
    try:
        ready = lines.get(timeout=20).split()
        assert ready[:2] == ["READY", "13"], ready
        with socket.create_connection(("127.0.0.1", int(ready[2])), timeout=5) as connection:
            stream = connection.makefile("rwb", buffering=0)
            def command(text):
                stream.write((text + "\n").encode())
                answer = stream.readline().decode().strip()
                assert answer.startswith("OK"), (text, answer)
                return answer
            assert command("HELLO 13 " + token) == "OK 13"
            command("GRAPH 2 value value 0.25 @ c - out out 0 0 @ a a")
            command("ENDPOINTS")
            command("MIDI_INPUTS")
            command("PLAY")
            before = int(command("PING").split()[1])
            command("MIDI_KEYBOARD 1 6569104")
            time.sleep(0.1)
            assert int(command("PING").split()[1]) > before
            command("DIAGNOSTICS")
            command("STOP")
            command("QUIT")
            stream.close()
        assert process.wait(timeout=10) == 0, errors
    finally:
        if process.poll() is None:
            process.kill()
            process.wait(timeout=5)
        process.stdout.close()
        process.stderr.close()


if __name__ == "__main__":
    check_host(sys.argv[1])
    print("Host protocol tests passed")
