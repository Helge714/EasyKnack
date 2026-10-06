"""Regression checks for the read-only distribution comparer; no downloads."""
import importlib.util
import pathlib
import uuid
import zipfile

root = pathlib.Path(__file__).resolve().parent.parent
spec = importlib.util.spec_from_file_location("native_audit", root / "scripts/audit-native-archive.py")
module = importlib.util.module_from_spec(spec)
spec.loader.exec_module(module)
work = root / "target/native-audit-tests" / uuid.uuid4().hex
installed = work / "installed"
installed.mkdir(parents=True)
archive = work / "official.zip"
with zipfile.ZipFile(archive, "w") as z:
    z.writestr("upstream/runtime.dll", b"original")
    z.writestr("upstream/LICENSE", b"license")
(installed / "runtime.dll").write_bytes(b"original")
(installed / "LICENSE").write_bytes(b"license")
assert module.audit(archive, installed)["exactMatch"]
(installed / "runtime.dll").write_bytes(b"tampered")  # Same length, different bytes.
assert module.audit(archive, installed)["changed"] == ["runtime.dll"]
(installed / "cache.pyc").write_bytes(b"cache")
(installed / "LICENSE").unlink()
report = module.audit(archive, installed)
assert report["extra"] == ["cache.pyc"] and report["missing"] == ["LICENSE"]
with zipfile.ZipFile(work / "unsafe.zip", "w") as z:
    z.writestr("root/../outside", b"never extracted")
try:
    module.audit(work / "unsafe.zip", installed)
except ValueError:
    pass
else:
    raise AssertionError("Unsafe archive path accepted")
assert not (work / "outside").exists()
print("Native archive checks passed: exact match, same-size tampering, missing notices, extra caches, unsafe paths.")
