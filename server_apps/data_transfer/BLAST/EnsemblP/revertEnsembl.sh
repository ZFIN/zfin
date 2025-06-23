#!/bin/tcsh
#
# Move backup wublast ensemble files back to production area.
# Move .fa files back to production area.

echo "==| rm existing Ensembl .fa files |=="
rm -f /research/zblastfiles/files/blastRegeneration/fasta/Ensembl/ensembl_zf.fa
rm -f /research/zblastfiles/files/blastRegeneration/fasta/Ensembl/cdna.fa

echo "==| mv .fa files for Ensembl back to working file dir |=="
mv /research/zblastfiles/files/blastRegeneration/fasta/Backup/ensembl_zf.fa /research/zblastfiles/files/blastRegeneration/fasta/Ensembl/
mv /research/zblastfiles/files/blastRegeneration/fasta/Backup/cdna.fa /research/zblastfiles/files/blastRegeneration/fasta/Ensembl/

echo "==| revert ensembl by removing current blast dbs and mv the old one to its place. |=="
rm -f /research/zblastfiles/zmore/blastRegeneration/Current/ensembl_zf.xn* 
mv /research/zblastfiles/zmore/blastRegeneration/Backup/ensembl_zf.xn* /research/zblastfiles/zmore/blastRegeneration/Current

#if (watson.zfin.org =~ genomix.cs.uoregon.edu) then
#  /research/zusers/blast/BLAST_load/target/Ensembl/distributeToNodesEnsembl.sh
#endif

exit
