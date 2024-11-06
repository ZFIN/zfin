#!/bin/sh

# writes an ant command to command.sh and executees it based on a prefix


COMMAND_TARGETS="" ;

if [ -z $1 ]; then
   echo "Need to provide a command to distribute." ; 
   exit ; 
fi

# ant p lists all possible commands in this project; the rest
# greps for the target of interest ($1) that is provided and cuts off
# the rest of the output from ant -p

# for each of the commands found, it puts them into a command list
# (COMMAND_TARGET), then tells ant to execute the COMMAND_TARGET

for i in `ant -p | cut -c 1-40 | fgrep ${1} `; do 
#  echo "$i"
  $COMMAND_TARGETS ="${COMMAND_TARGETS}${i} " ;
done ;

echo $COMMAND_TARGETS  ; 
ant $COMMAND_TARGETS  ; 


