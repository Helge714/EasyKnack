"""Contract checks for the CMake registry generator, without third-party packages."""
import importlib.util
from pathlib import Path
import tempfile
import unittest

spec = importlib.util.spec_from_file_location("generator", Path(__file__).with_name("generate-native-nodes.py"))
generator = importlib.util.module_from_spec(spec)
spec.loader.exec_module(generator)


class GeneratorTest(unittest.TestCase):
    def test_deterministic_registry_and_invalid_registrations(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            nodes = root / "nodes"
            nodes.mkdir()
            header = nodes / "Value.hpp"
            header.write_text('EASYKNACK_NODE(ValueNode, "value")\n', encoding="utf-8")
            output = root / "generated"
            generator.generate(nodes, output)
            first = (output / "GeneratedNodeRegistry.hpp").read_bytes()
            generator.generate(nodes, output)
            self.assertEqual(first, (output / "GeneratedNodeRegistry.hpp").read_bytes())
            self.assertIn(b"std::make_unique<ValueNode>()", first)
            duplicate = nodes / "Other.hpp"
            duplicate.write_text('EASYKNACK_NODE(OtherNode, "value")\n', encoding="utf-8")
            with self.assertRaisesRegex(ValueError, "Duplicate"):
                generator.generate(nodes, output)
            duplicate.unlink()
            header.write_text('EASYKNACK_NODE(broken)\n', encoding="utf-8")
            with self.assertRaisesRegex(ValueError, "Malformed"):
                generator.generate(nodes, output)
            header.unlink()
            with self.assertRaisesRegex(ValueError, "No native"):
                generator.generate(nodes, output)


if __name__ == "__main__":
    unittest.main()
