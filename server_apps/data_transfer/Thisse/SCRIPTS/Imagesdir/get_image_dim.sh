#! /bin/csh
# this script expects to be run in in a directory containing
# Bernard's raw unanotated images (and associated .txt) only
# generates the images sizes. also used to detect missing/abundant files
rm -f imagedim.1
foreach file (CB*.jpg)
	# we found the image sizes are not constant
	# this line writes out the image name,width,height
	echo "$file:r `/local/apps/jpeg/bin/djpeg -pnm '$file'|/local/apps/netpbm/bin/pnmfile|cut -f 3,5 -d ' '`" >> imagedim.1
end
# transforms the image dim list into an informix .unl format file 
cat imagedim.1 | grep -v '^$' | tr '\ ' '\|' >! imagedim.2
~tomc/bin/tailpipe  imagedim.2 >! imagedim.unl
# get rid of blank lines and move to parent dir 
grep -v '^$' imagedim.unl >! ../imagedim.unl 
