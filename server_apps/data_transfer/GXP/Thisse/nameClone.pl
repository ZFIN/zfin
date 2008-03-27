#!/private/bin/perl -w
#
# FILE: nameClone.pl
#
# Usage:  nameClone.pl  dbname
#
# Read in: probes.raw
# 
# Use the accession number to query zfin database
# to get exist gene id and/or clone name. If clone 
# is not already exist, use image clone name in the 
# description to replace FR#. In case no image clone
# name is provided, use xdget to get fasta file from 
# gbk_zf_all and parse out image clone name from defline,
# and write the accession and name into acc_imClone.unl.
# In case no gene id is available, output accession to 
# acc4blast.txt for blasting. 
# 
# Output : probes.unl        file for unload
#          nameClone.err     error entries
#          acc4blast.txt     accessions need blast efforts 
#          (acc_imClone.unl   image clone name definition from xdget )
#
use strict;
use DBI;

#====================================
# subfunction getImageCloneName
#
# execute xdget to fetch fasta file, 
# and parse out image clone name
#
# input:  $acc4imname file
# output: acc_imClone.unl for loading
#
sub getImageCloneName ($) {
    my $accfile = shift;

    my $currentPath = $ENV{PATH};
    $ENV{PATH} = "/private/apps/wublast/:$currentPath";
    $ENV{BLASTDB} = "/research/zblastdb/db/wu-db";
    
    open ACC_IMCLONE, ">acc_imClone.unl" or die "Cannot open acc_imClone.unl for write.";
    
    open DEFLINE, "xdget -n -f -Tgb1 -e probe_retrieve_for_defline.log gbk_zf_all $accfile |" 
	or die "Error executing xdget in nameClone.pl .";
    
    while (<DEFLINE>) {
	print ACC_IMCLONE "$1|$2|\n" if ((/^>.*gb\|(\w+)\..+(IMAGE:\d+)/)||(/^>.*gb\|(\w+)\..+(cssl:\w+)/));
    }

    close (ACC_IMCLONE);
}

#=======================================================
# Main

die "Db name is required.\n" if (@ARGV < 1);

# inherit Infomrix environment variables from the shell

my $dbname = $ARGV[0];
my $username = "";
my $password = "";

my $dbh = DBI->connect ("DBI:Informix:$dbname", $username, $password) 
    or die "Cannot connect to Informix database: $DBI::errstr\n";

open PROBE_IN, "<probes.raw" or die "Cannot open probes.raw to read";
open PROBE_OUT, ">probes.unl" or die "Cannot open probes.unl to write";
open ACC4BLAST, ">acc4blast.txt" or die "Cannot open acc4blast.txt to write";
open ERR, ">nameClone.err" or die "Cannot open nameClone.err to write";

my $acc4imname = "acc4imname$$";
open ACC4IMNAME, ">$acc4imname" or die "Cannot open $acc4imname to write"; 
my $getImNameNeeded = 0;

while (<PROBE_IN>) {
    my $im_clone_name = $1 if ((/(IMAGE:\d+)/)||(/(cssl:\w+)/)); 
    my @row = split (/\|/);
    my $clone_col   = $row[1];
    my $gene_id_col = $row[2];
    my $acc_col     = $row[3];

    # report invalid accession number
    # this was added here since we had uncovered mistaken accession#.
    if ( $acc_col !~ /^\w\w\d{6}$/ && $acc_col !~ /^\w\d{5}$/ && $acc_col !~ /^NM_\d+$/ ) { 
	print ERR "Invalid accession number $acc_col for $clone_col \n";
	next;
    }
 
    # get back clone name and gene id for accessions in ZFIN. 
    # if it is a cDNA accession it would be a GenBank number
    # RefSeq NM_# are only associated with genes in ZFIN. See below.
    my ($gene_zdb, $clone_name) = $dbh->selectrow_array ("
                                  select g.mrkr_zdb_id, e.mrkr_name
                                    from db_link, marker g, marker_relationship, marker e
                                   where dblink_acc_num = '$acc_col'
                                     and dblink_linked_recid = mrel_mrkr_2_zdb_id
                                     and mrel_mrkr_1_zdb_id = g.mrkr_zdb_id
                                     and g.mrkr_type like 'GENE%'
                                     and mrel_mrkr_2_zdb_id = e.mrkr_zdb_id
                                     and dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-37'
                                ");
    if ($clone_name) {
	
	$clone_col = $clone_name;
	$gene_id_col = $gene_zdb;
    }
    else {

	# if clone not exist and image clone name is not given, 
	# we will use xdget to find out image clone names to replace the fr#
	if (!$im_clone_name && $acc_col !~ /^NM_\d+$/) {
	    print ACC4IMNAME "$acc_col\n";
	    $getImNameNeeded = 1;
	}

	# replace FR# with image clone name if available
	if ($im_clone_name) {
	    $clone_col = $im_clone_name;
	}

	# when accession is not associated with a clone, try gene
   
	# find out the gb/refseq acc related to which gene
	my $sth = $dbh->prepare ("select mrkr_zdb_id
                                    from db_link, marker
                                   where dblink_acc_num = '$acc_col'
                                     and dblink_linked_recid = mrkr_zdb_id
                                     and mrkr_type like 'GENE%'
                                     and dblink_fdbcont_zdb_id in ('ZDB-FDBCONT-040412-37',
                                                                   'ZDB-FDBCONT-040412-38')
                                ");
	$sth->execute();
	my $array_ref = $sth->fetchall_arrayref();
	if (@$array_ref > 1) {            # gb acc matches >1 zfin genes

	    print ERR join("    ", $acc_col, $clone_col, ">1 ZFIN genes")."\n";
	    next;

	}elsif (@$array_ref == 1) {       # gb acc matches one zfin gene

	    my ($result) = @$array_ref;
	    ($gene_id_col) = @$result; 
     
	}else {
	    print  ACC4BLAST "$acc_col\n";
	}
    }

    $row[1] = $clone_col;
    $row[2] = $gene_id_col;
    $row[3] = $acc_col;
    print PROBE_OUT join("\|", @row);
}

close (PROBE_IN);
close (PROBE_OUT);
close (ACC4BLAST);
close (ACC4IMNAME);

&getImageCloneName($acc4imname) if $getImNameNeeded;
    
#unlink $acc4imname;	
close (ERR);


