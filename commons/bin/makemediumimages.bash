#!/bin/bash

# make medium sized images from all images in the imageLoadUp directory
#
# Example usage: 
# % cd imageLoadUp
# % makemediumimages.bash ZDB-IMAGE-09*    
#
#


FILES="$@"
for i in $FILES
do
echo "Prcoessing image $i ..."
/local/bin/convert -thumbnail 500x550 $i medium/$i
done
