#! /bin/csh
# this script expects to be run in in a directory containing
# Bernard's raw unanotated images (and associated .txt) only
# generates the anotated image and a thumbnail from it.
setenv DISPLAY bionix:1.0

foreach file (CB*.jpg)
	/bin/java -ms50m -mx100m -cp \
	/research/zfin/users/bsprunge/Annotator/annotator/annotator.jar \
	Annotator "$file";
	thumbnail.sh 64 $file:r--C.jpg > $file:r--t.jpg ;
end
mv CB*--?.* ..
cp -p CB*.jpg ..
cp -p CB*.txt ..
