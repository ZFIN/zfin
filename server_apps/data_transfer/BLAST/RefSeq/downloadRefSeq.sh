#!/bin/bash -e
#
# The script download RefSeq zebrafish files
#
source "config.sh"

REFSEQ_FTP_DIR="ftp://ftp.ncbi.nih.gov/refseq/D_rerio/mRNA_Prot"

# NCBI ships the D_rerio release as an unpredictable number of chunks
# (zebrafish.1.rna.fna.gz, zebrafish.2.rna.fna.gz, ...) and adds more as the
# release grows, so probe upwards instead of hard-coding the count. Only
# grabbing chunk 1 silently dropped ~78% of RefSeq (ZFIN-10452).
MAX_CHUNKS=50

log_message "Starting RefSeq download..."

# download_chunks <ncbi file suffix> <output fasta>
#
# Concatenates every available chunk into one uncompressed fasta. Stops at the
# first chunk NCBI does not publish; errors out if not even chunk 1 exists.
download_chunks() {
    local suffix="$1"
    local outfile="$2"
    local chunk=1
    local file

    : > "$outfile"
    while [[ $chunk -le $MAX_CHUNKS ]]; do
        file="zebrafish.$chunk.$suffix.gz"
        if ! wget -Nq "$REFSEQ_FTP_DIR/$file"; then
            break
        fi
        if [[ ! -s "$file" ]]; then
            error_exit "Downloaded file $file is empty"
        fi
        gunzip -c "$file" >> "$outfile"
        chunk=$((chunk + 1))
    done

    if [[ $chunk -eq 1 ]]; then
        error_exit "No chunks found at $REFSEQ_FTP_DIR/zebrafish.1.$suffix.gz"
    fi
    log_message "Downloaded $((chunk - 1)) chunk(s) of $suffix into $outfile ($(grep -c '^>' "$outfile") sequences)"
}

log_message "Download and unzip the RefSeq fasta files"

download_chunks "protein.faa" "refseq_zf_aa.fa"
download_chunks "rna.fna" "refseq_zf_rna.fa"

# Rewrite each defline into the tpe|<accession>| form that xdformat -I indexes,
# dropping the version suffix so xdget can find sequences by the unversioned
# accessions ZFIN stores in db_link. Anchored at ^> so sequence lines are left
# alone, and the version pattern allows more than one digit (XM_685006.11).
for fasta in refseq_zf_aa.fa refseq_zf_rna.fa; do
    sed -i -E \
        -e 's/^>([A-Za-z0-9_]+)\.[0-9]+/>\1/' \
        -e 's/^>([A-Za-z0-9_]+)/>tpe|\1|/' \
        "$fasta"
done

log_message "Done with download"
exit
