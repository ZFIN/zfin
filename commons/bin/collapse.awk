#! /local/bin/gawk -f
# Tom Conlin
# collapse a number of lines into a single line
# 
# collapse.awk 3 long.data > wide.data

BEGIN {
    N = ARGV[1];
    ARGV[1] = ""
}
{	
	for(i = 1; i < N ;i++){ 
		if (getline row)
			$0 = $0 FS row
		else
			print	
	}
	print
}

