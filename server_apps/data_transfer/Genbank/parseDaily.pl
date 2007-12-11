#!/private/bin/perl
#
# The script reads GenBank daily update flat file, translate it 
# into sets of fasta files, including gb set, mRNA set, DNA set,
# EST zf, GSS zf, HTG zf. An zf accession unload file is also 
# generated.
# 

use Getopt::Std;

# Get command line options
getopts ('h');

my $usage = <<EOF;

  Usage:  parseDaily.pl file

    Input file : Gb daily update file nc*.flat
    Output file: fasta format gb set, mRNA set, DNA set,  EST zf, GSS zf, HTG zf, and accession unload file nc_zf_acc.unl.
EOF
    
if (@ARGV==0 || $opt_h) {
    
    print $usage;
    exit;
}

my ($locus, $bp, $type, $division,  $definition, $organism, $accession, $gi, $dbsource, $condition1, $condition2, $condition);

while (my $gbfile = shift @ARGV) {
   
    if ($gbfile !~ /\.(seq|flat)$/) {
	print "Error: File must with extension '.seq' or '.flat'. \n";
	exit;
    }

    my @file =  split(/\./, $gbfile);
    my $prefix = shift (@file);
    #print "$prefix\n";
 
    open ZF, ">$prefix"."_zf_all.fa" or die "Cannot open the file to write: $!.";
    open ZF_DNA, ">$prefix"."_zf_dna.fa" or die "Cannot open the file to write: $!.";
    open ZF_mRNA, ">$prefix"."_zf_mrna.fa" or die "Cannot open the file to write: $!.";
    open ZF_GB, ">$prefix"."_gb_zf.fa" or die "Cannot open the file to write: $!.";

    open ZF_EST, ">$prefix"."_est_zf.fa" or die "Cannot open the file to write: $!.";
    open ZF_GSS, ">$prefix"."_gss_zf.fa" or die "Cannot open the file to write: $!.";
    open ZF_HTG, ">$prefix"."_htg_zf.fa" or die "Cannot open the file to write: $!.";


    open MS_DNA, ">$prefix"."_ms_dna.fa" or die "Cannot open the file to write: $!.";
    open MS_mRNA, ">$prefix"."_ms_mrna.fa" or die "Cannot open the file to write: $!." ;
    open MS_GB, ">$prefix"."_gb_ms.fa" or die "Cannot open the file to write: $!.";
    open MS_EST, ">$prefix"."_est_ms.fa" or die "Cannot open the file to write: $!.";
    
    open HS_DNA, ">$prefix"."_hs_dna.fa" or die "Cannot open the file to write: $!.";
    open HS_mRNA, ">$prefix"."_hs_mrna.fa" or die "Cannot open the file to write: $!.";
    open HS_GB, ">$prefix"."_gb_hs.fa" or die "Cannot open the file to write: $!.";
    open HS_EST, ">$prefix"."_est_hs.fa" or die "Cannot open the file to write: $!.";

    open ZFACC, ">$prefix"."_zf_acc.unl" or die "Cannot open the file to write: $!.";

    open IN, "<$gbfile" or die "Cannot open the $gbfile file to read: $!.";
    $/ = "//\n";

    while (<IN>) {
	next unless /LOCUS\s+(\w+)\s+(\d+)\sbp\s+(\w+)\s+\w+\s+(\w+).+/;
	$locus = $1;
	$bp = $2;
	$type = $3;
	$division = $4;
	/DEFINITION\s+(\w[^\^]+)\.\nACCESSION/ or "DEFINITION unmatched for $locus \n";
	$definition = $1; 
	$definition =~ s/\n\s+/ /g;
        /VERSION\s+(\S+)\s+GI:(\d+)/ or "VERSION unmatched for $locus \n";
	$accession = $1;
	$gi = $2;
	/ORGANISM\s+(\w.+)\n/ or "ORGANISM unmatched for $locus \n";
	$organism = $1; 
	$dbsource = gb ;
	$dbsource = emb if /Center code: SC/;
	/ORIGIN[^\n]*\n(.+)$/s or "ORIGIN unmatched for $locus \n";
	$seq = $1; 
	$seq =~ tr/tcag//cd;
	$seq =~ s/(.{60})/$1\n/g;

	if ($organism eq 'Danio rerio'){
	    
	    print ZF ">gi|$gi|$dbsource|$accession|$locus $definition \n";
	    print ZF "$seq\n";

	    if ($type eq "mRNA") {
		print ZFACC substr($accession,0,index($accession, '.'))."|$bp|ZDB-FDBCONT-040412-37|\n";
	    }
	    elsif ($type eq "DNA") {
		print ZFACC substr($accession,0,index($accession, '.'))."|$bp|ZDB-FDBCONT-040412-36|\n";
	    }
	    else {
		print "Attention: $accession has type $type";
	    }	   

	    if ($type eq 'DNA') {
		  	print ZF_DNA ">gi|$gi|$dbsource|$accession|$locus $definition \n";
			print ZF_DNA "$seq\n";
		      }
	    if ($type eq 'mRNA') {
		  	print ZF_mRNA ">gi|$gi|$dbsource|$accession|$locus $definition \n";
			print ZF_mRNA "$seq\n";
		      }
	    if ($division eq 'VRT' || $division eq 'HTC' || $division eq 'PAT') {
		    print ZF_GB ">gi|$gi|$dbsource|$accession|$locus $definition \n";
		    print ZF_GB "$seq\n";
		}

	    if ($division eq 'EST') {
		print ZF_EST ">gi|$gi|$dbsource|$accession|$locus $definition \n";
		print ZF_EST "$seq\n";
	    }
	    if ($division eq 'GSS') {
		print ZF_GSS ">gi|$gi|$dbsource|$accession|$locus $definition \n";
		print ZF_GSS "$seq\n";
	    }
	    if ($division eq 'HTG') {
		print ZF_HTG ">gi|$gi|$dbsource|$accession|$locus $definition \n";
		print ZF_HTG "$seq\n";
	    }


	}	

	if ($organism eq 'Mus musculus'){
	  
		if ($type eq 'DNA') {
		  	print MS_DNA ">gi|$gi|$dbsource|$accession|$locus $definition \n";
			print MS_DNA "$seq\n";
		      }
		if ($type eq 'mRNA') {
		  	print MS_mRNA ">gi|$gi|$dbsource|$accession|$locus $definition \n";
			print MS_mRNA "$seq\n";
		      }
		if ($division eq 'ROD' || $division eq 'HTC' || $division eq 'PAT') {
		    print MS_GB ">gi|$gi|$dbsource|$accession|$locus $definition \n";
		    print MS_GB "$seq\n";
		}
		if ($division eq 'EST') {
		    print MS_EST ">gi|$gi|$dbsource|$accession|$locus $definition \n";
		    print MS_EST "$seq\n";
		}
	    }	

	if ($organism eq 'Homo sapiens'){
	  
	
		if ($type eq 'DNA') {
		  	print HS_DNA ">gi|$gi|$dbsource|$accession|$locus $definition \n";
			print HS_DNA "$seq\n";
		      }
		if ($type eq 'mRNA') {
		  	print HS_mRNA ">gi|$gi|$dbsource|$accession|$locus $definition \n";
			print HS_mRNA "$seq\n";
		      }
		if ($division eq 'PRI' || $division eq 'HTC' || $division eq 'PAT') {
		    print HS_GB ">gi|$gi|$dbsource|$accession|$locus $definition \n";
		    print HS_GB "$seq\n";
		}
		if ($division eq 'EST') {
		    print HS_EST ">gi|$gi|$dbsource|$accession|$locus $definition \n";
		    print HS_EST "$seq\n";
		}
		
	    }	
    }
    close(ZF);
    close(ZFACC);
    close(ZF_DNA);
    close(ZF_mRNA);
    close(ZF_GB);
    close(ZF_EST);
    close(ZF_GSS);
    close(ZF_HTG);
    close(MS_DNA);
    close(MS_mRNA);
    close(MS_GB);
    close(MS_EST);
    close(HS_DNA);
    close(HS_mRNA);
    close(HS_GB);
    close(HS_EST);
    close(IN);
  }

