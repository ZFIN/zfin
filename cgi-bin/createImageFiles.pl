#!/private/bin/perl -w
#------------------------------------------------------------------------
# Unloads images from a DB into the ImageFiles directory, gives them
# shorter names containing only the ZDB ID, and then changes their 
# permissions to be readable by more folks.

$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";

chdir "/research/zcentral/loadUp/imageLoadUp";
system ("pwd");

system("echo 'select lotofile(fimg_thumbnail, " . '"/research/zcentral/loadUp/imageLoadUp/" || fimg_zdb_id||"_thumb", "server")' . " from fish_image where fimg_thumbnail is not null;' | dbaccess $ENV{DBNAME}");

system("echo 'select lotofile(fimg_image_with_annotation, " . '"/research/zcentral/loadUp/imageLoadUp/" || fimg_zdb_id||"_annot", "server")' . " from fish_image where fimg_image_with_annotation is not null;' | dbaccess $ENV{DBNAME}");

system("echo 'select lotofile(fimg_image, " . '"/research/zcentral/loadUp/imageLoadUp/" || fimg_zdb_id, "server")' . " from fish_image;' | dbaccess $ENV{DBNAME}");

system("echo 'select lotofile(fimgp_image, " . '"/research/zcentral/loadUp/imageLoadUp/" || fimgp_zdb_id, "server")' . " from fx_fish_image_private;' | dbaccess $ENV{DBNAME}");

system("echo 'select lotofile(fimgp_thumbnail, " . '"/research/zcentral/loadUp/imageLoadUp/" || fimgp_zdb_id||"_thumb", "server")' . " from fx_fish_image_private where fimgp_thumbnail is not null;' | dbaccess $ENV{DBNAME}");


my @files = `ls`;
my $file;

foreach $file (@files) {

    chop($file);
    my $newFile = $file;
    $newFile =~ s/\..*//;
    $newFile = $newFile.".jpg" ;
    system("mv $file $newFile");
    system("chmod 755 $newFile");
 
}


exit(0);
