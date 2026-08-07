#!/usr/bin/env python3
"""Does DANRE-uniprot carry content DANRE-mod lacks, once accessions are
collapsed to ZFIN genes and duplicates removed?"""
import collections

MAP = {}
for line in open("/tmp/gpad-cmp/acc2rec.tsv"):
    acc, rec = line.rstrip("\n").split("\t")
    if "-BAC-" not in rec:
        MAP.setdefault(acc, set()).add(rec)

def key(gene, f):           # (gene, relation, GO, ref, ECO)
    return (gene, f[2], f[3], f[4], f[5])

def mod_keys(path):
    ks = set()
    for line in open(path):
        f = line.rstrip("\n").split("\t")
        if len(f) < 12 or not f[0].startswith("ZFIN:"):
            continue
        ks.add(key(f[0][5:], f))
    return ks

def uniprot_keys(path):
    ks, unmapped_rows, unmapped_acc = set(), 0, set()
    for line in open(path):
        f = line.rstrip("\n").split("\t")
        if len(f) < 12:
            continue
        acc = f[0].split(":", 1)[1] if ":" in f[0] else f[0]
        acc = acc.split("-")[0]          # strip isoform suffix (P12345-2)
        genes = MAP.get(acc)
        if not genes:
            unmapped_rows += 1
            unmapped_acc.add(acc)
            continue
        for g in genes:
            ks.add(key(g, f))
    return ks, unmapped_rows, unmapped_acc

def report(name, a, b, label_a, label_b):
    print(f"\n=== {name} ===")
    print(f"  {label_a:<26} {len(a):>8}")
    print(f"  {label_b:<26} {len(b):>8}")
    print(f"  {'in both':<26} {len(a & b):>8}")
    print(f"  {label_a + ' ONLY':<26} {len(a - b):>8}")
    print(f"  {label_b + ' ONLY':<26} {len(b - a):>8}")

up, un_rows, un_acc = uniprot_keys("/tmp/gpad-cmp/uniprot.tsv")
mod17 = mod_keys("/tmp/gpad-cmp/current.tsv")
modfix = mod_keys("/tmp/gpad-cmp/goex.tsv")

print("DANRE-uniprot rows unmapped to a ZFIN gene: "
      f"{un_rows} rows / {len(un_acc)} distinct accessions")
print(f"accession->gene map entries: {len(MAP)}")

report("ALL annotations, same 06-17 vintage", up, mod17,
       "DANRE-uniprot", "DANRE-mod")
report("ALL annotations, uniprot(06-17) vs mod(FIXED 08-04)", up, modfix,
       "DANRE-uniprot", "DANRE-mod-fixed")

for ref, nm in (("GO_REF:0000002", "InterPro2GO"), ("GO_REF:0000003", "EC2GO")):
    u = {k for k in up if k[3] == ref}
    m17 = {k for k in mod17 if k[3] == ref}
    mfx = {k for k in modfix if k[3] == ref}
    print(f"\n=== {nm} ({ref}) ===")
    print(f"  DANRE-uniprot (gene-collapsed) {len(u):>8}")
    print(f"  DANRE-mod 06-17                {len(m17):>8}")
    print(f"  DANRE-mod FIXED                {len(mfx):>8}")
    print(f"  uniprot-only vs fixed mod      {len(u - mfx):>8}")
    print(f"  fixed-mod-only vs uniprot      {len(mfx - u):>8}")

print("\n=== raw row counts (no gene collapse, no dedup) ===")
for nm, p in (("DANRE-uniprot", "/tmp/gpad-cmp/uniprot.tsv"),
              ("DANRE-mod 06-17", "/tmp/gpad-cmp/current.tsv"),
              ("DANRE-mod FIXED", "/tmp/gpad-cmp/goex.tsv")):
    n = sum(1 for _ in open(p))
    print(f"  {nm:<18} {n:>8}")
