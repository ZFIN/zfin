#!/bin/tcsh
#
# Move images(.jpg) and annotation files if exist 
# to a new Imagesdir/. Then check for image consistency,
# and make thumbnail image, and annotation image. 
#
set usage = <<EOF
Usage:  processImage.sh  labname  [release_type] 
  lab_name:    Thisse  Talbot                  
  release_type:                            
     Thisse   cb            with annotation  
              fr            default, without annotation   
EOF

if (($#argv < 1) || ($#argv == 1 && $1 == "Thisse")) then
  echo ""
  echo "Usage:  processImage.sh  labname  [release_type]" 
  echo "  lab_name:    Thisse / Talbot " 
  echo "  release_type:               " 
  echo "       Thisse    cb            with annotation" 
  echo "                 fr            without annotation"     
  exit 1
endif

echo "== process images for '$1' lab with option '$2' =="
echo "...create Imagesdir/, move .jpg (corresponding .txt) into it...";
rm -rf Imagesdir
mkdir Imagesdir

if ($1 == "Thisse")  then
  foreach item (*)
    # directory item happened to have space in name, quotes help.
    if (-d "$item" && "$item" != "Imagesdir") then 

        cp "$item"/*.jpg Imagesdir/
	if ($2 == "cb") then
	    cp "$item"/*.txt Imagesdir/
	endif 

    else if (-f $item && $item:e == "jpg") then
	cp $item  Imagesdir/
    else
    endif
  end

else if ($1 == "Talbot")  then

  foreach jpg (images/*/large/*)
     set new_name = `echo "$jpg" | cut -d\/ -f4 | sed 's/ /__/g' | sed 's/JPG/jpg/' `;
     #set old_name = `echo "$jpg" | sed 's/ /\\ /g'`;
     cp "$jpg" Imagesdir/$new_name;         #quote helps to preserve the name which has space in it
  end

endif


# generate image name list from the images.csv(txt).(space replaced by "__")
if ($1 == "Thisse")  then
    cat images.csv | cut -d, -f2 | cut -f1 -d\. | tr -d \" | sort >! imgname.list
else if ($1 == "Talbot") then
    sed -n '/^[0-9]/ p' images.txt | cut -f2 | cut -f1 -d\. | sed 's/ /__/g' | sed 's/JPG/jpg/' | sort >! imgname.list
endif
mv imgname.list Imagesdir/

echo "in Imagesdir, check bad image names with space in"
cd Imagesdir
ls -1 | grep ' '
echo "---"

#try to rename ...
# foreach file (*)
#    mv $file `echo $file | sed 's/ /__/g'`
# end

# check image consistency
ls -1 *.jpg | cut -d. -f1| sort >! imgjpg.list
echo ".jpg sent but not in images.csv(txt)"
diff imgname.list imgjpg.list  |grep '^>' | cut -c 3-
echo "---"
echo "rows in images.csv(txt) that don't have .jpg files"
diff imgname.list imgjpg.list  |grep '^<' | cut -c 3-
echo "---"

if ($1 == "Thisse" && $2 == "cb") then
  ls -1 *.txt | cut -d. -f1 | sort >! imgtxt.list
  echo "rows in images.csv(txt) that don't have .txt files"
  diff imgname.list imgtxt.list  |grep '^<' | cut -c 3-
  echo "---"
endif

echo "Ready to generate thumbnail and annotation images? (y or n)"
set goahead = $< 
if ($goahead == 'n') then

    rm -rf Imagesdir
    exit;
endif

echo "image generation ...."
foreach file (*.jpg)

    # this line writes out the image name,width,height
    echo "$file:r `/local/apps/jpeg/bin/djpeg -pnm '$file'|/local/apps/netpbm/bin/pnmfile|cut -f 3,5 -d ' '`" >> imagedim.raw

    sed  '/^$/d; s/ /\|/g; s/$/\|/' imagedim.raw > images.dim

    if ($1 == "Thisse" && $2 == "cb") then   # if image annotation exist

	# the java program will put the annotation text on top of the image
        # and name the final image end with --C.jpg
	/bin/java -ms50m -mx100m -cp \
        /research/zusers/bsprunge/Annotator/annotator/annotator.jar \
        Annotator "$file";

        ../thumbnail.sh 64 $file:r--C.jpg > $file:r--t.jpg ;

    else	
	../thumbnail.sh 64 $file > $file:r--t.jpg ;
 
    endif

end 

# images size into images.dim
sed  '/^$/d; s/ /\|/g; s/$/\|/' imagedim.raw > images.dim

# check for success of thumnail and annotation image generation
if ($1 == "Thisse" && $2 == "cb") then 
    ls -1 *--C.jpg | cut -d\- -f1| sort >! img_C_jpg.list
    echo "missed --C.jpg files"
    diff imgname.list img_C_jpg.list  |grep '^<' | cut -c 3-
    echo "---"
endif

ls -1 *--t.jpg | cut -d\. -f1 | sed 's/--t$//' | sort >! img_t_jpg.list

echo "missed --t.jpg files"
diff imgname.list img_t_jpg.list  |grep '^<' | cut -c 3-
echo "---"

echo "Ready to finish up and clean up? (y or n)"
set goahead = $< 
if ($goahead == 'n') then
    exit;
endif

mv *.jpg *.txt ../
mv images.dim ../

cd ../
rm -rf Imagesdir
if ($1 == "Thisse") then 
    set argv2_upper = `echo $2 | tr '[a-z]' '[A-Z]'`
    foreach dir ($2* $argv2_upper*) 
	if (-d $dir) then
	    rm -rf $dir
	endif
    end
else if ($1 == "Talbot") then 
    rm -rf images/
endif
