#!/private/bin/perl -w

#------------------------------------------------------------------------
# Unloads images from a DB into the ImageFiles directory, gives them
# shorter names containing only the ZDB ID, and then changes their 
# permissions to be readable by more folks.

$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";

#system("rm ZDB-IMAGE-98*");
#system("rm ZDB-IMAGE-99*");
#system("rm ZDB-IMAGE-00*");
#system("rm ZDB-IMAGE-01*");
#system("rm ZDB-IMAGE-02*");
#system("rm ZDB-IMAGE-03*");
#system("rm ZDB-IMAGE-04*");

chdir("/research/zcentral/loadUp/PDFLoadUp/");

system("echo 'select lotofile(pub_file, " . '"/research/zcentral/loadUp/PDFLoadUp/" || zdb_id, "server")' . " from publication where pub_file is not null;' | dbaccess $ENV{DBNAME}");

my @files = `ls`;
my $file;

foreach $file (@files) {

    chop($file);
    my $newFile = $file;
    $newFile =~ s/\..*//;
    $newFile = $newFile.".pdf"; 
    system("mv $file $newFile");
    system("chmod 755 $newFile");
 
}

exit(0);
