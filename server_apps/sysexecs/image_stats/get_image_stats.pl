#!/opt/zfin/bin/perl -wT
#------------------------------------------------------------------------
#
# Script to get stats of a file stored in an image.
#
# Calls anytopnm and pnmfile (part of the netpbm package) to get the 
# width and height of an image.  pnmfile outputs something like the following:
#
# STDOUT:
#  stdin:  PPM raw, 98 by 59  maxval 255
#
# If the expected output is not given then this script outputs "0 0".
#
# Usage:
#
#   get_image_stats.pl imageFile
#
#     imageFile  Name of file containing an image.
#
#   Returns:
#
#     0  Stats were successfully obtained from the file.  The width and
#        height values were written to STDOUT as "widthValue heightValue"
#    >0  Stats were not successfully obtained from the file.  In this
#        case "0 0" is written to STDOUT.
#

use English;
$ENV{PATH} = "/bin:/local/apps/netpbm/bin";

$ARGV[0] =~  m/([A-Za-z\d\-\_\$\+\=\~\.\,\ \/]+)/;
my $imageFile = $1;

my $tmpDir     = "/tmp/get_image_stats.$PROCESS_ID";
my $pnmFile    = "$tmpDir/pnmfile";
my $statsFile  = "$tmpDir/stats";
my $stderrFile = "$tmpDir/err";
my $width  = 0;
my $height = 0;
my $retVal = 0;

my $dirPerms = oct(777);
mkdir($tmpDir, $dirPerms);



system("/local/apps/netpbm/bin/anytopnm $imageFile > $pnmFile 2> /dev/null");
system("/local/apps/netpbm/bin/pnmfile $pnmFile > $statsFile 2> $stderrFile");


# If anything was written to stderr then give up.

if (-s $stderrFile) {
    $retVal = 1;
}
else {
    # Parse standard out looking for width and height

    open(STATSFILE, $statsFile);

    my $line;

    while ( defined(STATSFILE) && ($line = <STATSFILE>) && ! $width) {
	if ($line =~ / by /) {
	    my @tokens1 = split(/,/, $line);
	    my @tokens2 = split(/\s+/, pop(@tokens1));
	    $width  = $tokens2[1];
	    $height = $tokens2[3];
	}
    }
}

print(STDOUT "$width $height");

system("/bin/rm -rf $tmpDir");

exit ($retVal);
