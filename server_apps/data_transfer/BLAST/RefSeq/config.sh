BLAST_PATH="/opt/zfin/blastdb"

log_message() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" | tee -a "refseq_process.log"
}

