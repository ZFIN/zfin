#!/bin/bash
function usage(){
   echo "Need to provide a directory as an argument." ;
   exit ; 
}

if [ -z "$1"]; then
  usage  ; 
fi

./createProductionBlastDatabases.sh $1

# copy existing RNA datbases
# copy the preVega
cp /research/zblastdb/db/Current/vega_transcript.* $1
# copy the Vega
cp /research/zblastdb/db/Current/vega_trans.* $1
# copy another Vega?
cp /research/zblastdb/db/Current/vega_zfin.* $1

# copy microRNA
cp /research/zblastdb/db/Current/zfin_microRNA.* $1


