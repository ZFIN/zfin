# delete first line
1d
# tabs to pipes
s/	/\|/g
# append cDNA name to row
s/\(.*cDNA clone MGC:\)\([0-9]*\)\(.*\)/\1\2\3\|MGC:\2\|MGC:\2|/g
