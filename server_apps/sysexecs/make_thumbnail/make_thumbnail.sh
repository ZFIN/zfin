#!/local/bin/perl -wT
$ENV{PATH} = "/local/apps/netpbm/bin:/bin";
$ARGV[0] =~  m/([A-Za-z\d\-\_\$\+\=\~\.\,\ \/]+)/;
my $fullImageFile = $1;
$ARGV[1] =~  m/([A-Za-z\d\-\_\$\+\=\~\.\,\ \/]+)/;
my $thumbnailFile = $1;

system("/local/apps/netpbm/bin/anytopnm $fullImageFile | " .
       "/local/apps/netpbm/bin/pnmscale -h 64 | " .
       "/local/apps/netpbm/bin/ppmtojpeg > $thumbnailFile");
system("/bin/chmod 644 $thumbnailFile");
exit(0);
