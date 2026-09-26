#!/usr/bin/env python3
"""Fail when production copy or routing code overstates unavailable capabilities."""

from __future__ import annotations

import argparse
import re
import sys
from pathlib import Path


EXACT_CLAIMS = (
    "ambient lux",
    "live telemetry",
    "mesh layer",
    "gps hot",
    "cad-certified",
    "sf emergency cad api",
    "pre-authenticated",
    "path locked on illuminated",
    "offline mesh",
    "105 db",
    "high-candela",
    "dispatched!",
    "emergency sos dispatched",
    "telemetry remains active",
    "live gps & audio stream sent",
    "gemini conf",
    "gemini street assessment",
    "walk comfort scores",
    "recent sf 311",
    "reported sidewalk work",
    "mapped curb ramps",
    "open havens",
    "safe routes",
    "verified safe partners",
    "verified civic evidence",
    "verified footpaths",
    "sfpuc smart lighting",
    "active logs",
    "silent discreet distress ping",
)

PERCENT_CLAIM = re.compile(
    r"(?i)(?:\b\d{1,3}(?:\.\d+)?%\s*(?:ambient\s*)?"
    r"(?:lux|lit|light(?:ing)?|shade(?:d)?|coverage|safe(?:ty)?|monitor(?:ed|ing)?|signal)\b|"
    r"\b(?:lux|lit|light(?:ing)?|shade(?:d)?|coverage|safe(?:ty)?|monitor(?:ed|ing)?|signal)"
    r"[^\n\"]{0,24}\b\d{1,3}(?:\.\d+)?%)"
)

ROUTING_ONLY_FIELDS = re.compile(
    r'''(?ix)
    optJSONObject\(\s*["'](?:features|comfort|explanation)["']\s*\)
    |optDouble\(\s*["'](?:coverage|confidence)["']
    |\bsample\.score\b
    |\broute\.(?:comfort|coverage|confidence|features|explanation|weakest)\b
    '''
)


def production_files(root: Path) -> list[Path]:
    java_root = root / "app" / "src" / "main" / "java"
    assets = root / "app" / "src" / "main" / "assets" / "lighthouse_data.json"
    files = sorted(java_root.rglob("*.kt")) + sorted(java_root.rglob("*.java"))
    if assets.exists():
        files.append(assets)
    return files


def scan(root: Path) -> list[str]:
    findings: list[str] = []
    for path in production_files(root):
        text = path.read_text(encoding="utf-8")
        lowered = text.lower()
        relative = path.relative_to(root)
        for claim in EXACT_CLAIMS:
            if claim in lowered:
                findings.append(f"{relative}: unsupported claim marker: {claim}")
        if PERCENT_CLAIM.search(text):
            findings.append(f"{relative}: unsupported percentage-based condition claim")

    routing = root / "app" / "src" / "main" / "java" / "com" / "example" / "service" / "LighthouseRoutingEngine.kt"
    if routing.exists() and ROUTING_ONLY_FIELDS.search(routing.read_text(encoding="utf-8")):
        findings.append(
            f"{routing.relative_to(root)}: routing still references unvalidated comfort/evidence fields"
        )
    return findings


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", type=Path, default=Path.cwd())
    args = parser.parse_args()
    root = args.root.resolve()
    findings = scan(root)
    if findings:
        print("Claim-boundary scan blocked:", file=sys.stderr)
        for finding in findings:
            print(f"- {finding}", file=sys.stderr)
        return 2
    print("Claim-boundary scan passed.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
