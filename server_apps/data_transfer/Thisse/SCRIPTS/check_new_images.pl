#! /local/bin/perl

# This file finds images that do not have thumb or annotated images.
# The file runs in the current directory. Each image name in images.unl
# is checked for existing thubmnail and annotated images. Missing names
# are printed to standard out.
#
# Assumes that thumbnails have format imagename--t.jpg and annotated
# images have format imagename--C.jpg. Imagename formats are generated
# by get_image_dim.sh.

# Get image names
die "Error: images.unl was not found.\n" if !(-e "images.unl");
open (IMG, "images.unl") or die "cannot open images.unl";
while ($img = <IMG>)
  {
    ($img_id, $img_name, $junk) = split /\|/,$img,3;
    # Print unfound
    print "$img_name.jpg\n" if !(-e "$img_name.jpg");
    print "$img_name--C.jpg\n" if !(-e "$img_name--C.jpg");
    print "$img_name--t.jpg\n" if !(-e "$img_name--t.jpg");
  }
close (IMG);
exit;
