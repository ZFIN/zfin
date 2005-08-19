#!/private/bin/perl -w
#
# FILE: blast2tab.pl
#
# Parse the blast output into table format  
#
# INPUT: 
#       STDIN blast result file
# OUTPUT:
#        table format blast result
#
use strict;

use LWP::Simple;
use Getopt::Std;
use vars qw($opt_p $opt_b $opt_e $opt_m $opt_n $opt_h $opt_l);
getopts('p:b:e:m:n:l:h');

my $usage =<<END;
Usage :
       blast2unl.pl [options]  < inputfile 

 Options:
  -p   percentage threshold
  -b   bits threshold
  -e   expect value threshold
  -m   start position 
  -n   end position
  -l   alignment length threshold 
  -h   print usage 
END

die "$usage \n" if ($opt_h);

my $PERCENT = $opt_p ? $opt_p : 0;
my $BITS    = $opt_b ? $opt_b : 0;
my $EXPECT  = $opt_e ? $opt_e : 1e30;
my $START   = $opt_m ? $opt_m : 0;
my $END     = $opt_n ? $opt_n : 1e30;
my $LENGTH  = $opt_l ? $opt_l : 0;

my ($Query, $Sbjct, $queryLength, $sbjctLength, $subjectdef, $sbjctAccession, @sbjctArray);
my $HSP = "";

while (<>) {
    
    if (/^Query=\s+(\S+)/) {
	my $querydef = $1;  # save var right away
	outputHSP(  ); 
	
	($Query) = $querydef =~ /gb\|(\w+)/;   #gb accession
    }
    elsif (/^\s+\((\d+)\s+letters/) {
	$queryLength = $1;
    }
    elsif (/^>(.+)$/) {
	$subjectdef = $1; chomp $subjectdef; 
	outputHSP(  ); 
	
	while (<>) {
	    if (/Length = (\d+)/) {
		$sbjctLength = $1;
		last;
	    }else {
		s/^\s*//; s/\s*$//;
		$subjectdef .= " ".$_;
	    }
	}
	($sbjctAccession) = split(/ /, $subjectdef); #get the accession part
	@sbjctArray = split(/[\|\.]/,$sbjctAccession);
	$Sbjct = $sbjctArray[1] ? $sbjctArray[1] : $sbjctArray[0] ;  	
    }
    elsif (/^ Score = /) {
        outputHSP(  );
        my @stat = ($_);
        while (<>) {
            last unless /\S/;
            push @stat, $_;
        }
        my $stats = join("", @stat);
        my ($bits) = $stats =~ /(\d\S+) bits/;
        my ($expect) = $stats =~ /Expect\S* = ([\d\-\.e]+)/;
        $expect = "1$expect" if $expect =~ /^e/;
        my ($match, $total, $percent)
            = $stats =~ /Identities = (\d+)\/(\d+) \((\d+)%\)/;
        my $mismatch = $total - $match;
        
        $HSP = {bits => $bits, expect => $expect, mismatch => $mismatch,
            percent => $percent, q_begin => 0, q_end => 0, q_align => "",
            s_begin => 0, s_end => 0, s_align => ""};
    }
    elsif (/^Query:\s+(\d+)\s+(\S+)\s+(\d+)/) {
        $HSP->{q_begin}  = $1 unless $HSP->{q_begin};
        $HSP->{q_end}    = $3;
        $HSP->{q_align} .= $2;
    }
    elsif (/^Sbjct:\s+(\d+)\s+(\S+)\s+(\d+)/) {
        $HSP->{s_begin}  = $1 unless $HSP->{s_begin};
        $HSP->{s_end}    = $3;
        $HSP->{s_align} .= $2;
    }
}
outputHSP(  );

sub outputHSP {
    return unless $HSP;
    return if $HSP->{percent}  < $PERCENT;
    return if $HSP->{bits}     < $BITS;
    return if $HSP->{expect}   > $EXPECT;
    return if ($HSP->{q_begin} < $START or $HSP->{q_end} < $START);
    return if ($HSP->{q_begin} > $END   or $HSP->{q_end} > $END);
	return if (length($HSP->{q_align}) < $LENGTH);

    print join("|", $Query, $Sbjct, $HSP->{percent},
        length($HSP->{q_align}), $queryLength, $sbjctLength, 
		$HSP->{mismatch},
        countGaps($HSP->{q_align}) + countGaps($HSP->{s_align}),
        $HSP->{q_begin}, $HSP->{q_end}, $HSP->{s_begin}, $HSP->{s_end},
        $HSP->{expect}, $HSP->{bits})."|\n";
    $HSP = "";
}

sub countGaps {
    my ($string) = @_;
    my $count = 0;
    while ($string =~ /\-+/g) {$count++}
    return $count;
}



    

    
