#!/usr/bin/env python3

from __future__ import annotations

import subprocess
import sys
import tempfile
import unittest
from pathlib import Path


SCRIPT = Path(__file__).with_name("check_claim_boundaries.py")


def write_fixture(root: Path, kotlin: str, asset: str = "{}") -> None:
    source = root / "app/src/main/java/com/example/service"
    source.mkdir(parents=True)
    (source / "LighthouseRoutingEngine.kt").write_text(kotlin, encoding="utf-8")
    assets = root / "app/src/main/assets"
    assets.mkdir(parents=True)
    (assets / "lighthouse_data.json").write_text(asset, encoding="utf-8")


def run_scan(root: Path) -> subprocess.CompletedProcess[str]:
    return subprocess.run(
        [sys.executable, str(SCRIPT), "--root", str(root)],
        check=False,
        capture_output=True,
        text=True,
    )


class ClaimBoundaryScanTest(unittest.TestCase):
    def test_truthful_unknown_copy_passes(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            write_fixture(
                root,
                'class LighthouseRoutingEngine { val label = "Working status unknown" }',
                '{"note":"Streetlight inventory is not current lamp status"}',
            )
            result = run_scan(root)
            self.assertEqual(0, result.returncode, result.stderr)

    def test_numeric_lux_claim_is_blocked(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            write_fixture(root, 'val label = "CORRIDOR: 98.4% AMBIENT LUX"')
            result = run_scan(root)
            self.assertEqual(2, result.returncode)
            self.assertNotIn("98.4", result.stderr)

    def test_fake_dispatch_claim_is_blocked(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            write_fixture(root, 'val toast = "Emergency SOS dispatched!"')
            result = run_scan(root)
            self.assertEqual(2, result.returncode)

    def test_unvalidated_routing_parser_is_blocked(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            write_fixture(
                root,
                'class LighthouseRoutingEngine { val fields = json.optJSONObject("features") }',
            )
            result = run_scan(root)
            self.assertEqual(2, result.returncode)
            self.assertIn("routing still references", result.stderr)

    def test_neutral_compatibility_words_do_not_trigger(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            write_fixture(
                root,
                'class LighthouseRoutingEngine { val confidenceLabel = "Unavailable" }',
            )
            result = run_scan(root)
            self.assertEqual(0, result.returncode, result.stderr)


if __name__ == "__main__":
    unittest.main()
