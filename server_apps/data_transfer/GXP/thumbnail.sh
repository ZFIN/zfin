#!/bin/tcsh 
# takes an image in and writes a thumbnail out
# reduces the height to 'given' pixels 
# and lets the width float

if ($#argv < 3) then
	echo "usage: thumbnail.sh int_height in_name out_name"
	exit 1
endif


if ( ($2:e == "jpg") || ($2:e == "jpeg") ) then
	/local/apps/jpeg/bin/djpeg -pnm $2 |\
	/local/apps/netpbm/bin/pnmscale -h $1 |\
	/local/apps/jpeg/bin/cjpeg > $3
	if ($? != 0 ) then
		echo "Error: " $2 " thumbnail generation"
 	endif 
else if ($2:e == "gif") then
	/local/apps/netpbm/bin/giftopnm $2 |\
	/local/apps/netpbm/bin/pnmscale -h $1 |\
	/local/apps/netpbm/bin/ppmtogif > $3
	if ($? != 0 ) then
		echo "Error: " $2 " thumbnail generation"
 	endif
else if ($2:e == "png") then
	/local/apps/netpbm/bin/pngtopnm $2 |\
	/local/apps/netpbm/bin/pnmscale -h $1 |\
	/local/apps/netpbm/bin/pnmtopng > $3
   	if ($? != 0 ) then
		echo "Error: " $2 " thumbnail generation"
 	endif 
else
	# may be worth it to find image type with 'file 
	# if the extension is buggy
	# set type = `file $2` 
	echo "unsupported format must be jpg/jpeg,gif or png (with extension)"
endif
