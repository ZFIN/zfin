#!/local/bin/perl
my $fullImageFile = $ARGV[0];
my $thumbnailFile = $ARGV[1];

$ENV{PATH} = "/local/apps/netpbm/bin:$ENV{PATH}";

system("/local/apps/netpbm/bin/anytopnm $fullImageFile | " .
       "/local/apps/netpbm/bin/pnmscale -h 64 | " .
       "/local/apps/netpbm/bin/ppmtojpeg > $thumbnailFile");
system("/bin/chmod 644 $thumbnailFile");
exit(0);
