#!/private/bin/perl -w

my %category;
my $inputFile = $ARGV[0];

open(INPUT, "$inputFile") or die "couldn't open input $inputFile\n";
while (my $inputLine = <INPUT>)
    {
    chomp $inputLine;
    if ($inputLine =~ /html?\z/)
        {
        $category{"html"}++;
        }
    elsif ($inputLine =~ /aa-([a-zA-Z1-9_]+)\./)
        {
        $category{$1}++;
        }
    elsif ($inputLine =~ /([a-zA-Z1-9_]+)\.cgi\?/)
        {
        $category{$1}++;
        }
    elsif ($inputLine =~ /(ZFIN_jump)/)
        {
        $category{$1}++;
        }
    elsif ($inputLine =~ /action\/([a-zA-Z1-9_]+)/)
        {
        $category{$1}++;
        }
    else
        {
        $category{$inputLine}++;
        }
    }
close(INPUT);
    
    
    
my @keys = sort {$category{$a} <=> $category{$b}} keys %category;

foreach $key (@keys)
    {
    print "$key = $category{$key}\n";
    }
    
