#!/bin/tcsh -e 

#$1 db name to dump

set pth=/research/zunloads/databases/$1
set dirname=`date +"%Y.%m.%d.1"`

# increment until we get name which has not been taken
while ( -d $pth/$dirname )
	set z=$dirname:e
	set y=$dirname:r
@ x = $z + 1
	set dirname=$y.$x
end

echo $dirname
