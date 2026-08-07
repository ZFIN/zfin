#!/usr/bin/env python3
"""Coarser (gene, GO) overlap + the README's specific counter-examples."""
MAP = {}
for line in open("/tmp/gpad-cmp/acc2rec.tsv"):
    acc, rec = line.rstrip("\n").split("\t")
    if "-BAC-" not in rec:
        MAP.setdefault(acc, set()).add(rec)

def mod_pairs(path):
    s = set()
    for line in open(path):
        f = line.rstrip("\n").split("\t")
        if len(f) >= 12 and f[0].startswith("ZFIN:"):
            s.add((f[0][5:], f[3]))
    return s

def uniprot_pairs(path):
    s = set()
    for line in open(path):
        f = line.rstrip("\n").split("\t")
        if len(f) < 12:
            continue
        acc = (f[0].split(":", 1)[1] if ":" in f[0] else f[0]).split("-")[0]
        for g in MAP.get(acc, ()):
            s.add((g, f[3]))
    return s

up = uniprot_pairs("/tmp/gpad-cmp/uniprot.tsv")
m17 = mod_pairs("/tmp/gpad-cmp/current.tsv")
mfx = mod_pairs("/tmp/gpad-cmp/goex.tsv")

print("(gene, GO) pairs, 06-17 vintage")
print(f"  DANRE-uniprot        {len(up):>7}")
print(f"  DANRE-mod            {len(m17):>7}")
print(f"  in both              {len(up & m17):>7}")
print(f"  uniprot ONLY         {len(up - m17):>7}")
print(f"  mod ONLY             {len(m17 - up):>7}")
print(f"\n  uniprot ONLY vs FIXED mod  {len(up - mfx):>7}")

# README's counter-examples: terms claimed present in uniprot but not mod
EX = [("igfbp2a", "GO:0005520"), ("urod", "GO:0004853"), ("dlc", "GO:0030855")]
name2id = {}
for line in open("/tmp/gpad-cmp/gene_ids.tsv"):
    abbrev, zdb = line.rstrip("\n").split("\t")
    name2id[abbrev] = zdb
print("\nREADME counter-examples (claimed: in DANRE-uniprot, absent from DANRE-mod)")
for abbrev, go in EX:
    z = name2id.get(abbrev)
    if not z:
        print(f"  {abbrev:<10} {go}  -- gene id not found")
        continue
    print(f"  {abbrev:<10} {go}  uniprot={(z, go) in up}  "
          f"mod0617={(z, go) in m17}  modFIXED={(z, go) in mfx}")
