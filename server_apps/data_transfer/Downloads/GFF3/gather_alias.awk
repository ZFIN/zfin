#! /usr/bin/nawk -f

#read a fresh row and store it
BEGIN {
	print "##gff-version 3";
	A = $1; B = $4; C = $5;        #  D=; geneid? strand?
	row =substr($0,1,length($0)-1) # chop trailing tab
}
#either the next row matches and append alias or print and start new
{	if ($1==A && $4==B && $5==C){ # LG start, stop all match
		split($9,alias,"=");
		row = row "," alias[4]
	}
	else{
		print row;
		row = substr($0,1,length($0)-1); # chop trailing tab
		A=$1;B=$4;C=$5
	}
}
END {print row}
