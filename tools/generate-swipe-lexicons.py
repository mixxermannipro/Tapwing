#!/usr/bin/env python3
# SPDX-License-Identifier: GPL-3.0-only
"""Generate deterministic, frequency-ranked assets from the included LatinIME wordlists."""

import argparse
import gzip
import io
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "dictionaries/sources"
ASSETS = ROOT / "app/src/main/assets/swipe"
WORD_LIMIT = 210_000
SOURCES = {
    "de": "de_wordlist.combined.gz",
    "en-US": "en_US_wordlist.combined.gz",
    "en-GB": "en_GB_wordlist.combined.gz",
}


def generate(source):
    entries = []
    with gzip.open(source, "rt", encoding="utf-8") as stream:
        for line in stream:
            if not line.startswith(" word="):
                continue
            fields = dict(part.split("=", 1) for part in line.strip().split(",") if "=" in part)
            word = fields["word"]
            if not 2 <= len(word) <= 32 or not word.isalpha() or word.isupper():
                continue
            if fields.get("possibly_offensive") == "true" or fields.get("not_a_word") == "true":
                continue
            entries.append((int(fields["f"]), word))
    # Python sorting is stable, so ties use original source order.
    entries.sort(key=lambda entry: -entry[0])
    words = list(dict.fromkeys(word for _, word in entries))[:WORD_LIMIT]
    output = io.BytesIO()
    with gzip.GzipFile(fileobj=output, mode="wb", mtime=0) as compressed:
        compressed.write(("\n".join(words) + "\n").encode("utf-8"))
    return output.getvalue()


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--check", action="store_true", help="Verify assets without rewriting them")
    args = parser.parse_args()
    for language, source in SOURCES.items():
        target = ASSETS / f"{language}.lex"
        payload = generate(SOURCE / source)
        if args.check:
            if not target.exists() or target.read_bytes() != payload:
                parser.exit(1, f"{target.relative_to(ROOT)} differs from its source\n")
        else:
            ASSETS.mkdir(parents=True, exist_ok=True)
            target.write_bytes(payload)


if __name__ == "__main__":
    main()
