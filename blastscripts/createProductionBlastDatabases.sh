#!/bin/bash
function usage(){
   echo "Need to provide a directory as an argument." ;
   exit ; 
}

if [ -z "$1"]; then
  usage  ; 
fi

# create new nucleotide databases
./createBlastDatabase.sh n unreleasedRNA $1
./createBlastDatabase.sh n publishedRNA $1

# ./create new polypeptide databases
./createBlastDatabase.sh p publishedProtein $1
./createBlastDatabase.sh p unreleasedProtein $1



