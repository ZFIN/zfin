#!/private/bin/perl -w

my $inputFile = $ARGV[0];

`sort -o $inputFile $inputFile`;
open(INPUT, "$inputFile") or die "couldn't open input $inputFile\n";

my $lastLine = "";
while (my $inputLine = <INPUT>)
    {
    chomp $inputLine;
    if ($inputLine eq $lastLine)
        {
        print "DUP: $inputLine\n";
        }
    $lastLine = $inputLine;
    }
close(INPUT);
