"""Repeatable WASAPI routing soak. Uses private child hosts; never changes device settings.

Default invocation only lists devices. --run requires explicit endpoint IDs from that list.
The last DSP node zeros playback: the real device callback still runs, without a test tone
on the loudspeakers. A scope before that node verifies actual capture signal arrival.
"""
import argparse
import csv
import hashlib
import json
import pathlib
import queue
import re
import socket
import subprocess
import time
import threading
import uuid


class Host:
    def __init__(self, executable):
        token = str(uuid.uuid4())
        self.process = subprocess.Popen([str(executable), token], stdout=subprocess.PIPE,
                                        creationflags=getattr(subprocess, "CREATE_NO_WINDOW", 0))
        self.socket = None
        try:
            ready=queue.Queue()
            threading.Thread(target=lambda: ready.put(self.process.stdout.readline()), daemon=True).start()
            line = ready.get(timeout=12).decode().strip().split()
            if line[:2] != ["READY", "11"]:
                raise RuntimeError(f"Host startup failed: {line}")
            self.socket = socket.create_connection(("127.0.0.1", int(line[2])), 15)
            self.stream = self.socket.makefile("rwb", buffering=0)
            self.command("HELLO 11 " + token)
        except BaseException:
            self.close()
            raise

    def command(self, command):
        self.stream.write((command + "\n").encode())
        result = self.stream.readline().decode().strip()
        if not (result == "OK" or result.startswith("OK ")):
            raise RuntimeError(f"{command.split()[0]}: {result}")
        return result

    def close(self):
        if self.socket:
            try:
                self.command("QUIT")
            except (OSError, RuntimeError):
                pass
            self.stream.close()
            self.socket.close()
        try:
            self.process.wait(timeout=2)
        except subprocess.TimeoutExpired:
            self.process.kill()
            self.process.wait()

    def __enter__(self):
        return self

    def __exit__(self, *_):
        self.close()


def endpoints(host):
    tokens = host.command("ENDPOINTS").split()
    result = []
    for i in range(int(tokens[2])):
        direction, kind, identity, name, available, route = tokens[3+i*6:9+i*6]
        result.append(dict(direction=direction, kind=int(kind), id=bytes.fromhex(identity).decode(),
                           name=bytes.fromhex(name).decode(), available=available == "1", route=route == "1"))
    return result


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--host", type=pathlib.Path, default=pathlib.Path("target/native/easyknack-audio-host.exe"))
    parser.add_argument("--run", action="store_true")
    parser.add_argument("--cable-playback")
    parser.add_argument("--cable-capture")
    parser.add_argument("--output")
    parser.add_argument("--seconds", type=float, default=120)
    parser.add_argument("--live-seconds", type=float, default=300)
    parser.add_argument("--directory", type=pathlib.Path, default=pathlib.Path("target/reference-routing"))
    args = parser.parse_args()
    if args.seconds <= 0 or args.live_seconds <= 0:
        parser.error("Measurement durations must be positive")
    args.host = args.host.resolve()
    with Host(args.host) as consumer:
        devices = endpoints(consumer)
        print(json.dumps(devices, indent=2), flush=True)
        if not args.run:
            return
        for identifier, direction in [(args.cable_playback, "o"), (args.cable_capture, "i"), (args.output, "o")]:
            if not any(d["id"] == identifier and d["direction"] == direction and d["available"] for d in devices):
                raise ValueError(f"Unavailable endpoint: {identifier!r}")
        args.directory.mkdir(parents=True, exist_ok=True)
        metadata = dict(devices=devices, host=str(args.host), host_sha256=hashlib.sha256(args.host.read_bytes()).hexdigest(),
                        started=time.strftime("%Y-%m-%dT%H:%M:%S%z"), arguments=vars(args),
                        initial_clock=consumer.command("IO_CLOCK"), silent_physical_output=True)
        (args.directory / "metadata.json").write_text(json.dumps(metadata, indent=2, default=str), encoding="utf8")
        (args.directory / "summary.json").write_text("[]", encoding="utf8")
        # Scope contract comes from the Java-generated reference, not a duplicated fingerprint.
        original = pathlib.Path("target/reference-v1/analysis.original.txt").read_text()
        payload = re.search(r"\bscope (.*?) @", original).group(1).split()
        payload[1] = "2"
        scope = " ".join(payload)
        correlation = re.search(r"\bautocorrelation (.*?) @", original).group(1).split()
        correlation[1] = "2"
        correlation = " ".join(correlation)
        def graph(gain=0.5, bypass=False):
            # All processing remains live upstream of zero. Only the final physical signal is silenced.
            return (f"GRAPH 7 capture input {args.cable_capture.encode().hex()} copy stereo @ a - "
                    f"scale math 0 0 -1 {gain} multiply 0 @ a aa "
                    f"offset math {0 if bypass else 1} 0 -1 0.01 add 0 @ a aa "
                    f"scope scope {scope} @ a a "
                    f"correlation autocorrelation {correlation} @ c a "
                    f"quiet math 2 0 -1 0 multiply 0 @ a aa "
                    f"output deviceout 5 {args.output.encode().hex()} @ a a")
        with Host(args.host) as producer:
            producer.command(f"GRAPH 3 sine osc -1 440 @ a a level math 0 0 -1 0.05 multiply 0 @ a aa output deviceout 1 {args.cable_playback.encode().hex()} @ a a")
            producer.command("PLAY")
            consumer.command(graph())
            consumer.command("PLAY")
            for label, host in [("consumer",consumer),("producer",producer)]:
                status=host.command("ROUTING_STATUS")
                if status != "OK -":
                    raise RuntimeError(label+": "+bytes.fromhex(status.split()[1]).decode())
                metadata[label+"_clock"]=host.command("IO_CLOCK")
            (args.directory / "metadata.json").write_text(json.dumps(metadata, indent=2, default=str), encoding="utf8")
            warmup=time.monotonic()
            while time.monotonic()-warmup<5:
                consumer.command("DIAGNOSTICS");producer.command("DIAGNOSTICS")
                time.sleep(.25)
            reports=[]
            for run, seconds in [("steady-1", args.seconds), ("steady-2", args.seconds), ("steady-3", args.seconds), ("live", args.live_seconds)]:
                consumer.command("DIAGNOSTICS_RESET")
                producer.command("DIAGNOSTICS_RESET")
                start=time.monotonic();samples=0;signal_peak=0.;edits=0;last_edit=-1;last={};tone_frames=0
                with (args.directory / (run+".csv")).open("w", newline="") as file:
                    writer=csv.writer(file);writer.writerow(["elapsed_s", "consumer_diagnostics", "producer_diagnostics", "capture_peak"])
                    while time.monotonic()-start < seconds:
                        elapsed=time.monotonic()-start
                        if run == "live" and int(elapsed*10) != last_edit:
                            last_edit=int(elapsed*10)
                            consumer.command(graph(.2+.6*((last_edit%100)/100), (last_edit//50)%2 == 1));edits+=1
                        diagnostic=consumer.command("DIAGNOSTICS");producer_diagnostic=producer.command("DIAGNOSTICS")
                        analysis=consumer.command("ANALYSIS").split()[1:]
                        peak=0.
                        for i in range(0,len(analysis),260):
                            if analysis[i] == "correlation" and abs(float(analysis[i+1])-440)<5 and float(analysis[i+2])>.9:
                                tone_frames+=1
                            values=analysis[i+4:i+260]
                            if values and analysis[i] == "scope":
                                peak=max(peak,max(map(abs,map(float,values))))
                        signal_peak=max(signal_peak,peak)
                        writer.writerow([elapsed,diagnostic,producer_diagnostic,peak]);file.flush();samples+=1
                        last=dict(consumer=diagnostic,producer=producer_diagnostic)
                        time.sleep(.1 if run == "live" else .25)
                result=dict(run=run,elapsed_s=time.monotonic()-start,polls=samples,edits=edits,signal_peak=signal_peak,tone_frames=tone_frames,**last)
                reports.append(result)
                (args.directory/"summary.json").write_text(json.dumps(reports,indent=2))
                print(json.dumps(result),flush=True)
                # Windows/session volume can attenuate the probe. Periodicity at 440 Hz is
                # the signal witness; do not impose an arbitrary absolute amplitude floor.
                if tone_frames == 0:
                    raise RuntimeError("Capture test signal was not observed")
            consumer.command("STOP");producer.command("STOP")
            metadata["completed_runs"]=len(reports)
            (args.directory / "metadata.json").write_text(json.dumps(metadata, indent=2, default=str), encoding="utf8")


if __name__ == "__main__":
    main()
