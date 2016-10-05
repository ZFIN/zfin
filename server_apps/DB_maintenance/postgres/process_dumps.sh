#!/bin/bash

unloadDirectory=$1

if [[ -d $1 ]]; then
    echo "$1 is a directory"
   
else
    echo "$1 is not valid"
    exit 1
fi

cd $unloadDirectory

rm unload_*
rm *.err
rm *.out
rm btsfse*

data_files=`ls *`

ls --hide="*.*" > filenames.txt

for f in $data_files
do
    echo $f
    sed 's/\r/\\r/g' $f > $f.txt2
    sed 's/|$//' $f.txt2 > $f.txt
    rm f.txt2
done


