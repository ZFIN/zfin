#!/local/bin/perl -wT
$ENV{PATH} = "/local/apps/netpbm/bin:/bin";
$ARGV[0] =~  m/([A-Za-z\d\-\_\$\+\=\~\.\,\ \/]+)/;
my $fullImageFile = $1;
$ARGV[1] =~  m/([A-Za-z\d\-\_\$\+\=\~\.\,\ \/]+)/;
my $thumbnailFile = $1;

# image files will be in a subdirectory called "medium/" and
# have the same name as the full sized image
my $mediumImageFile = $fullImageFile;
$mediumImageFile =~ s/ZDB-IMAGE/medium\/ZDB-IMAGE/;

#the 1000px is a maximum that no sensible thumbnail should achieve
system("/local/bin/convert -thumbnail 1000x64 $fullImageFile $thumbnailFile");
system("/local/bin/convert -thumbnail 500x550 $fullImageFile $mediumImageFile");

system("/bin/chmod 644 $thumbnailFile");
system("/bin/chmod 644 $mediumImageFile");
exit(0);
