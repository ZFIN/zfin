#!/private/bin/perl -wT
$ENV{PATH} = "/local/apps/netpbm/bin:/bin";
my $fullImageFile = $1;
my $thumbnailFile = $1;
my $mediumImageFile = $1;

#the 1000px is a maximum that no sensible thumbnail should achieve
system("/local/bin/convert -thumbnail 1000x64 $fullImageFile $thumbnailFile");
system("/local/bin/convert -thumbnail 500x550 $fullImageFile $mediumImageFile");

system("/bin/chmod 644 $thumbnailFile");
system("/bin/chmod 644 $mediumImageFile");
exit(0);
