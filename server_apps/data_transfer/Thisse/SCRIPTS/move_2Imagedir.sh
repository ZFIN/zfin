#! /bin/csh

#if ("`ls -d Imagesdir`" != "Imagesdir/") mkdir  Imagesdir

foreach d ( CB* )
echo $d:q
#	cp -p $d:r*/* Imagesdir/	#fails when they omit a dot
	cp -p $d:q/* Imagesdir/
end

# find spaces in filenames
ls -1 Imagesdir/ | grep ' '
