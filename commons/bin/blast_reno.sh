#! /bin/tcsh

# who knows , might still be valid
source /private/ZfinLinks/Commons/env/blast-default.env
set nt_fasta=$1;
set db_path="${WEBHOST_BLAST_DATABASE_PATH}/Current/"
set wz_est="/research/zblastfiles/files/LOCAL/wz_est"

set blastn = "/private/apps/wublast/blastn"

echo "check target DB modified date"
ls -lh $db_path/zfin_cdna_seq* 
ls -lh $db_path/vega_withdrawn*
ls -lh $db_path/unreleasedRNA*
echo ""

rm -f $nt_fasta:r.out
nice +10 $blastn \
"$db_path/zfin_cdna_seq $db_path/unreleasedRNA $db_path/vega_withdrawn $wz_est" \
 $nt_fasta -E e-50 -B 100 > $nt_fasta:r.out

echo ""
