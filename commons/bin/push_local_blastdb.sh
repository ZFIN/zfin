#! /bin/bash

# originally to make the tempoary blast file for transcripts in reno available

# for PREVEGA this name should be    'vega_transcript.fa'
# for Vega_trans the name should be  'vega_zfin.fa'
#   and ...    'vega_withdrawn.fa'

fname="$1"
# would be interested in not having this step
sanspath=${fname##*/}  
basename=${sanspath%.*}

#setenv PATH /local/bin:/bin:$PATH
HOSTNAME=`hostname`

#on development the blastdb path is... /research/zblastfiles/zmore/dev_blastdb/

#on production  the blastdb path is ...
# not /research/zprodmore/blast/[watson|crick|hapdb|impdb] ...
# nor /research/zprodmore/blast/hapdb/wu-db/ which links to Current?
# ... somewhere  /kinetix/zprod/blastdb
# but that path in not available from zygotix

# extract the blast paths for  machine  we are on
# ahhh ... still not that regular... 
# i.e. dont have both correct paths in any development enviroment

eval `grep -i blast /private/ZfinLinks/Commons/env/blast-default.properties | grep "/"`
# not setting WEBHOST_FASTA_FILE_PATH
# keep hardcoding till someone gets it right
WEBHOST_FASTA_FILE_PATH="/research/zblastfiles/files"

if [ "kinetix" == ${HOSTNAME} ] ; then 
	# idealy, this line by itself would allways do the right thing on any machine
	eval `grep -i blast /private/ZfinLinks/Commons/env/${HOSTNAME}.properties | grep "/"`
fi

#echo ${WEBHOST_FASTA_FILE_PATH}
#echo ${WEBHOST_BLAST_DATABASE_PATH}

CURRENT=${WEBHOST_BLAST_DATABASE_PATH}/Current
BACKUP=${WEBHOST_BLAST_DATABASE_PATH}/Backup
LOCAL=${WEBHOST_FASTA_FILE_PATH}/LOCAL

if [ 'informix' != `whoami` ] ; then
	echo "needs to be run as user informix. "
	echo "        Would have:"
	echo "cp ${fname} ${LOCAL}/${basename}.fa"
	echo "cd ${LOCAL}"
	echo "/private/apps/wublast/xdformat -n -q3 -o ${CURRENT}/$basename -I ${basename}.fa"
	echo "rm ${BACKUP}/${basename}.xn*"
	echo "cp ${CURRENT}/${basename}.xn* ${BACKUP}/"		
	
	exit -1
fi

echo "== copy sequence files $name to $LOCAL =="
echo ""
echo "cp ${fname} ${LOCAL}/${basename}.fa"

cp ${fname} ${LOCAL}/${basename}.fa
echo "cd ${LOCAL}"
cd ${LOCAL}
echo ""
echo "== Format $fname into blast db ${basename} == "
echo ""
echo "/private/apps/wublast/xdformat -n -q3 -o ${CURRENT}/$basename -I ${basename}.fa"
/private/apps/wublast/xdformat -n -q3 -o ${CURRENT}/$basename -I ${basename}.fa
chmod 664 ${CURRENT}/${basename}.xn*
echo ""
echo "== Move ${basename} to db dir $WEBHOST_BLAST_DATABASE_PATH/ =="
echo ""
echo "rm ${BACKUP}/${basename}.xn*"
rm ${BACKUP}/${basename}.xn*
echo "cp ${CURRENT}/${basename}.xn* ${BACKUP}/"
cp ${CURRENT}/${basename}.xn* ${BACKUP}/
chmod 664 ${BACKUP}/${basename}.xn*
echo ""
echo "   == Finish ==\n"
