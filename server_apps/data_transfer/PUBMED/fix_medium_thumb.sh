#!/bin/bash                                                                                                       
while IFS="|" read -r regularFile thumbnailFile mediumFile remainder
do
    echo $value1
    echo $value2
    /bin/convert -thumbnail 1000x64 $regularFile $thumbnailFile 
    /bin/convert -thumbnail 500x550 $regularFile $mediumFile

done
