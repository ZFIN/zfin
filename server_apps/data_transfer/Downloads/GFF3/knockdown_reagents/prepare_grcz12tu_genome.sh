#!/bin/bash -e
#
# One-time script to prepare the GRCz12tu genome for bowtie alignment.
# This script:
#   1. Downloads/copies the GRCz12tu FASTA
#   2. Renames NCBI accession names to simple chromosome numbers (1-25)
#   3. Builds bowtie v1 and bowtie2 indexes
#
# The resulting indexes are stored at $GLOBALSTORE/GRCz12tu
#
# Prerequisites:
#   - /opt/misc/bowtie/bowtie-build
#   - /opt/misc/bowtie2/bowtie2-build
#   - GRCz12tu FASTA available (GCF_049306965.1_GRCz12tu_genomic.fna)

GLOBALSTORE="/research/zprodmore/gff3"
INDEXDIR="$GLOBALSTORE/GRCz12tu"
FASTA_SOURCE="${1:-/research/zprodmore/gff3/GCF_049306965.1_GRCz12tu_genomic.fna}"

if [ ! -f "$FASTA_SOURCE" ]; then
    echo "Error: FASTA file not found: $FASTA_SOURCE"
    exit 1
fi

mkdir -p "$INDEXDIR"

echo "Renaming chromosomes from NCBI accessions to simple numbers..."
# NC_133176.1 -> 1, NC_133177.1 -> 2, ..., NC_133200.1 -> 25, NC_002333.2 -> MT
sed \
    -e 's/^>NC_133176\.1.*/>1/' \
    -e 's/^>NC_133177\.1.*/>2/' \
    -e 's/^>NC_133178\.1.*/>3/' \
    -e 's/^>NC_133179\.1.*/>4/' \
    -e 's/^>NC_133180\.1.*/>5/' \
    -e 's/^>NC_133181\.1.*/>6/' \
    -e 's/^>NC_133182\.1.*/>7/' \
    -e 's/^>NC_133183\.1.*/>8/' \
    -e 's/^>NC_133184\.1.*/>9/' \
    -e 's/^>NC_133185\.1.*/>10/' \
    -e 's/^>NC_133186\.1.*/>11/' \
    -e 's/^>NC_133187\.1.*/>12/' \
    -e 's/^>NC_133188\.1.*/>13/' \
    -e 's/^>NC_133189\.1.*/>14/' \
    -e 's/^>NC_133190\.1.*/>15/' \
    -e 's/^>NC_133191\.1.*/>16/' \
    -e 's/^>NC_133192\.1.*/>17/' \
    -e 's/^>NC_133193\.1.*/>18/' \
    -e 's/^>NC_133194\.1.*/>19/' \
    -e 's/^>NC_133195\.1.*/>20/' \
    -e 's/^>NC_133196\.1.*/>21/' \
    -e 's/^>NC_133197\.1.*/>22/' \
    -e 's/^>NC_133198\.1.*/>23/' \
    -e 's/^>NC_133199\.1.*/>24/' \
    -e 's/^>NC_133200\.1.*/>25/' \
    -e 's/^>NC_002333\.2.*/>MT/' \
    "$FASTA_SOURCE" > "$INDEXDIR/GRCz12tu.fa"

echo "Building bowtie v1 index..."
/opt/misc/bowtie/bowtie-build "$INDEXDIR/GRCz12tu.fa" "$INDEXDIR/GRCz12tu"

echo "Building bowtie2 index..."
/opt/misc/bowtie2/bowtie2-build "$INDEXDIR/GRCz12tu.fa" "$INDEXDIR/GRCz12tu"

echo "Done. Indexes created at $INDEXDIR/GRCz12tu"