# delete first line
1d
# tabs to pipes
s/	/\|/g
# append cDNA name to row
s/\(.*cDNA clone MGC:\)\([0-9]*\)\(.*\)/\1\2\3\|mgc\2\|/g
