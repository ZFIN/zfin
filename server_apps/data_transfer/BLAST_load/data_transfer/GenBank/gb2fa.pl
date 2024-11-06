#!/usr/bin/env perl
#
# gb2fa.pl 
# 
# Usage:
#       gb2fa.pl  file
# 
# Input:
#    flat file from genbank release
# Output:
#    fasta format mRNA, DNA and others for zebrafish, human, and mouse
#
# Modified 12/29/2003
# add in the generation of _zf_oth_rna.fa, _hs_oth.fa and _ms_oth.fa files 
# which records non-DNA also non-mRNA sequences. We need some of them
# for gb_zf.fa, gb_hs.fa and gb_ms.fa. 

use Getopt::Std;

# Get command line options
getopts ('h');

my $usage = <<EOF;

  Usage:  gb2fast.pl file

     Input: flat file from genbank release
     Output:
         fasta files: mRNA, DNA for zebrafish, human, and mouse
       
EOF
    
if (@ARGV==0 || $opt_h) {
    
    print $usage;
    exit;
}

my ($locus, $bp, $type, $d_type, $definition, $organism, $accession, $gi, $condition1, $condition2, $condition);

while (my $gbfile = shift @ARGV) {
   
    if ($gbfile !~ /\.(seq|flat)$/) {
	print "Error: File must with extension '.seq' or '.flat'. \n";
	exit;
    }

    my @file =  split(/\./, $gbfile);
    my $prefix = shift (@file);
    print "$prefix\n";
    my $fafile = $prefix.".fa";

    open ZF0, ">$prefix"."_zf_oth_rna.fa" or die "Cannot open the file to write: $!.";
    open ZF1, ">$prefix"."_zf_dna.fa" or die "Cannot open the file to write: $!.";
    open ZF2, ">$prefix"."_zf_mrna.fa" or die "Cannot open the file to write: $!.";
    open ZFACC, ">$prefix"."_zf_acc.unl" or die "Cannot open the file to write: $!.";      

    open MS1, ">$prefix"."_ms_dna.fa" or die "Cannot open the file to write: $!.";
    open MS2, ">$prefix"."_ms_mrna.fa" or die "Cannot open the file to write: $!." ;

    open HS1, ">$prefix"."_hs_dna.fa" or die "Cannot open the file to write: $!.";
    open HS2, ">$prefix"."_hs_mrna.fa" or die "Cannot open the file to write: $!.";
 
    open IN, "<$gbfile" or die "Cannot open the $gbfile file to read: $!.";
    $/ = "//\n";

    while (<IN>) {
	next unless /LOCUS\s+(\w+)\s+(\d+)\sbp\s+(\w+)\s+.+/;
	$locus = $1;
	$bp = $2;
	$type = $3;
	/DEFINITION\s+(\w[^\^]+)\.\nACCESSION/ or "DEFINITION unmatched for $locus \n";
	$definition = $1; 
	$definition =~ s/\n\s+/ /g;
        /VERSION\s+(\S+)\s+/ or "VERSION unmatched for $locus \n";
	$accession = $1;
	/ORGANISM\s+(\w.+)\n/ or "ORGANISM unmatched for $locus \n";
	$organism = $1;     
	/ORIGIN[^\n]*\n(.+)$/s or "ORIGIN unmatched for $locus \n";
	$seq = $1; 
	$seq =~ tr/tcag//cd;
	$seq =~ s/(.{60})/$1\n/g;

	if ($organism eq 'Danio rerio'){
	   
	    if ($type eq 'DNA') {
		print ZF1 ">gb|$accession|$locus $definition \n";
		print ZF1 "$seq\n\n";
		print ZFACC substr($accession,0,index($accession, '.'))."|$bp|ZDB-FDBCONT-040412-36|\n";
	    }
	    elsif ($type eq 'mRNA') {
		print ZF2 ">gb|$accession|$locus $definition \n";
		print ZF2 "$seq\n\n";
		print ZFACC substr($accession,0,index($accession, '.'))."|$bp|ZDB-FDBCONT-040412-37|\n";
	    }
	    else {
		print ZF0 ">gb|$accession|$locus $definition \n";
		print ZF0 "$seq\n\n";
	    }
	}	


	if ($organism eq 'Mus musculus'){

	    if ($type eq 'DNA') {
		print MS1 ">gb|$accession|$locus $definition \n";
		print MS1 "$seq\n\n";
	    }
	    elsif ($type eq 'mRNA') {
		print MS2 ">gb|$accession|$locus $definition \n";
		print MS2 "$seq\n\n";
	    }
	    else {	    
		print MS0 ">gb|$accession|$locus $definition \n";
		print MS0 "$seq\n\n";
	    }		
	}	
	

	if ($organism eq 'Homo sapiens'){
	    
	    if ($type eq 'DNA') {
		print HS1 ">gb|$accession|$locus $definition \n";
		print HS1 "$seq\n\n";
	    }
	    elsif ($type eq 'mRNA') {
		print HS2 ">gb|$accession|$locus $definition \n";
		print HS2 "$seq\n\n";
	    }
	    else {
		print HS0 ">gb|$accession|$locus $definition \n";
		print HS0 "$seq\n\n";		
	    }
	}	
    }
    close(ZF0);
    close(ZF1);
    close(ZF2);
    close(ZFACC);
    close(MS1);
    close(MS2);
    close(HS1);
    close(HS2);
    close(IN);
  }

