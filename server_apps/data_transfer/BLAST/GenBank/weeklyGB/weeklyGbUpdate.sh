#!/bin/tcsh

cd @BLASTSERVER_FASTA_FILE_PATH@/fasta/GB_daily

echo "== cp the files over from embryonix, and move old files to backup; weeklyCpGenBank.sh =="
# cp the files over from embryonix, and move old files to backup.
@TARGET_PATH@/GenBank/weeklyGB/weeklyCpGenBank.sh

echo "== merge one week's nc files into nonredundant fasta files. weeklyNrdbGenBank.sh =="
# merge one week's nc files into nonredundant fasta files.
@TARGET_PATH@/GenBank/weeklyGB/weeklyNrdbGenBank.sh

echo "== make blastdbs weeklyWudbFormatGenBank.sh =="
# make blastdbs
@TARGET_PATH@/GenBank/weeklyGB/weeklyWudbFormatGenBank.sh

# push new blastdbs to /Current
@TARGET_PATH@/GenBank/weeklyGB/weeklyPushGenBank.sh

exit 0

#=================================
# the above format automatically append the updates to the origin file and keep index
# but redundancy is not automatically eliminated. We hope the redundancy won't affect 
# the search speed very much.





