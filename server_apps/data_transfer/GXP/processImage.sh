#!/bin/tcsh
#
# FILE: processImages.sh
#
# Check image files sent are consistent with the entries in 
# image.csv file. Make thumbnail images and annotation images if
# applicable.
#
# INPUT: 
#      labname
# optional:
#      release type
#
# OUTPUT:
#      images/images, thumbnails, annotations
#      images.dim : image name, height, width
#
#      missing image file if any
#      missing entry in image.csv if any
#      error output from thumbnail generation
#      missing thumbnail file if any 
#
# EFFECT:
# image file name extension are forced to lower case "jpg", space in name 
# are changed to "__". All images and annotation files are under images/
#      

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
#----------------------------------------------------------
# Create IMGTMP/, move image files into it
#
# replace extension JPG with jpg, replace space with '__'
#----------------------------------------------------------
echo "...create new IMGTMP/, move .jpg (corresponding .txt) into it...";
/bin/rm -rf IMGTMP
mkdir IMGTMP

if ($1 == "Thisse")  then
  foreach item (images/*)
    if (-d "$item") then   # quote preserves names with space

        /bin/cp "$item"/* IMGTMP/

    else if (-f "$item") then
	      /bin/cp "$item" IMGTMP/
    endif
  end

  cd IMGTMP/
  foreach file (`ls -1 .`)    
     set new_name = `echo "$file" | sed 's/JPG/jpg/' | sed 's/ /__/g' `;
     if ($file != $new_name ) then
         /bin/mv "$file" $new_name;
     endif
  end
  cd ..

else if ($1 == "Talbot")  then

  foreach jpg (images/*)
     set new_name = `echo "$jpg" | cut -d\/ -f2 | sed 's/JPG/jpg/' | sed 's/ /__/g' `;
     /bin/cp "$jpg" IMGTMP/$new_name;      
  end

endif


#-------------------------------------------------------------
# Generate image name list from images.csv(txt), move to IMGTMP
#
# replace extension JPG with jpg, replace space with '__'
#-------------------------------------------------------------
if ($1 == "Thisse")  then
    cat images.csv | cut -d, -f2 | cut -f1 -d\. | tr -d \" | sed 's/JPG/jpg/' | sed 's/ /__/g' | sort >! imgname.list

else if ($1 == "Talbot") then
    sed -n '/^[0-9]/ p' images.txt | cut -f2 | cut -f1 -d\. | sed 's/ /__/g' | sed 's/JPG/jpg/' | sort >! imgname.list
endif

/bin/mv imgname.list IMGTMP/


#=======================================================================
#   at IMGTMP/
#=======================================================================

cd IMGTMP

#------------------------------------------------------------
# Check image consistency
#------------------------------------------------------------

ls -1 *.jpg | cut -d. -f1| sort >! imgjpg.list
echo ".jpg sent but not in images.csv(txt)"
diff imgname.list imgjpg.list  |grep '^>' | cut -c 3-

echo "#\!/bin/sh" > rmAdditionalImages.sh
diff imgname.list imgjpg.list  |grep '^>' | cut -c 3- | sed 's/^/\/bin\/rm -f /' | \
                 sed 's/$/\.jpg/' >> rmAdditionalImages.sh
/bin/chmod u+x  rmAdditionalImages.sh
 
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
if ($goahead == 'n') then   #if abort, drop IMGTMP
    cd ..
    /bin/rm -rf IMGTMP
    exit;
endif


#------------------------------------------------------------
# Generate thumbnail images, and annotation images if any
#------------------------------------------------------------
echo "delete additional images ..."
./rmAdditionalImages.sh

echo "image generation ...."
foreach file (*.jpg)

    set imgdim = `/local/apps/jpeg/bin/djpeg -pnm $file | /local/apps/netpbm/bin/pnmfile | cut -f3,5 -d' ' | sed 's/ /\|/' `;

    # this line writes out the image name,width,height
    echo "$file:r|$imgdim|" >> images.dim

    if ($1 == "Thisse" && $2 == "cb") then   # if image annotation exist

	# the java program will put the annotation text on top of the image
        # and name the final image end with --C.jpg
	/bin/java -ms50m -mx100m -cp \
        /research/zusers/bsprunge/Annotator/annotator/annotator.jar \
        Annotator "$file";

        ../thumbnail.sh 64 $file:r--C.jpg $file:r--t.jpg ;

    else	
	../thumbnail.sh 64 $file $file:r--t.jpg ;
    endif
end 


#------------------------------------------------------------
# Check success of thumnail and annotation image generation
#------------------------------------------------------------

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


#--------------------------------------------------------------
# Refresh images/ with new images, mv images.dim out
# clean out IMGTMP 
#----------------------------------------------------------------

cd .. 

/bin/rm -rf images/*

/bin/mv IMGTMP/*.jpg IMGTMP/*.txt images/
/bin/chmod 644 images/*
/bin/mv IMGTMP/images.dim .

/bin/rm -rf IMGTMP

exit;
