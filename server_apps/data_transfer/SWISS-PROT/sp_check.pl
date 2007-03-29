#!/private/bin/perl 

# sp_check.pl
#
# This script reads Swiss-Prot file(which consists of protein records),
# checks each record with corresponding gene record in ZFIN database 
# and accordingly divides the records into several catagories. It checks 
# the Database cross-references(DR) about whether ZFIN accession number is
# provided, whether EMBL GenPept accession numbers are provided and associated 
# with the same marker in ZFIN, it then checks EMBL GenBank accessions for those
# that couldn't be decided by GenPept matching. The scripts also
# checks the Reference cross-reference(s)(RX) to see whether any PubMed 
# number presents and whether they all in ZFIN database. Records with problems 
# are collected and divided into different problem files for biologists to 
# look into.

use DBI;

# Take a SP file as input (content format restricted). 

if (@ARGV == 0) {
  print "Please enter the SP file name.\n" and exit 1;
}

# Create the output files and give them titles. 
init_files();

my $num_ok = 0;    # number of good records that are going to be loaded 
my $num_prob = 0;  # number of problem records
$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";

my $dbname = "<!--|DB_NAME|-->";
my $username = "";
my $password = "";


my $dbh = DBI->connect ("DBI:Informix:$dbname", $username, $password) 
    or die "Cannot connect to Informix database: $DBI::errstr\n";

# if PubMed number not in zfin, output to a single file
open PUB, ">pubmed_not_in_zfin" or die "Cannot open the pubmed_not_in_zfin:$!";

$/ = "//\n";
while (<>) {
   
    init_var ();     # Initialize the variables and arrays 

    # record in tempfile contains ID, AC, DE, GN, DR, CC, KW, 
    # it goes to "okfile" and "problemfile". Some record from problemfile
    # would be matched out and appended to the okfile for parsing.

    $temprecd = "temp$$.txt"; 
    open TMP, ">$temprecd" or die "Cannot create the temporary file: $!";

    # records in probfile contains AC, RX, DR EMBL lines
    # they go to one of the prob# files for curator review. 
    $probrecd= "prob$$.txt";
    open PROB, ">$probrecd" or die "Cannot create the prob record file: $!";
	
    if (! /DR\s*EMBL;/) {       # if no EMBL line
	open F, ">>prob7" or die "Cannot open prob7 file";
	print F;  close F;
	print PROB; close PROB;
	system ("cat '$probrecd' >> problemfile");
	unlink $probrecd;
	$num_prob ++;
	next;
    }

    if (/DR\s*ZFIN;.*\nDR\s*ZFIN;/) {     # if >1 ZFIN lines
	open F, ">>prob8" or die "Cannot open prob7 file";
	print F;   close F;
	print PROB; close PROB;
	system ("cat '$probrecd' >> problemfile");
	unlink $probrecd;
	$num_prob ++;
	next;
    }

    foreach (split /\n/) {
	$_ = $_."\n";

	if(/^AC/ || /^GN/ || /^CC/|| /^ID/|| /^DE/ ) {  
	    print TMP;  
	}
	
	if (/^ID\s+(\w+)/) {
	    $sp_id = $1; 
	    next;    
	}
	if (/^AC/) {
	    print PROB; 
	    next;    
	}
	
	if (/^RX\s+MEDLINE=\d+.*PubMed=(\d+)/) {   # now only PubMed in zfin
	    push @rx, $_;
	    $num_pub = 1;
	    next;
	}
	
	if (/^DR\s+EMBL;\s+(\w+);\s+(\w+)\./) {   # check for EMBL acc number, parse it  
	       
	    $dr = $_; chop($dr); #the '\n' appended above could only be choped, not chomped. 
	    print PROB "$dr";
	    $embl_exist = 1;
	    $embl_nt = $1;   
	    push @embl_nt, $embl_nt;
	    
            # use GenPept acc for matching, storing results in @EMBL
	    $embl_gp = $2;
	    @embl_match = Embl_Match($embl_gp, "GenPept"); # first try the polypeptide accession
	    $num_match = @embl_match;    # record number of genes directly or indirectly associated
	    
	    if (@embl_match) {
		print PROB "\tGP match: @embl_match\n";
	    }else{
		print PROB "\n";
	    }
	    
	    @EMBL = (@EMBL, @embl_match);  # collect all the matches for each record
	    
	    if ($num_match > 1) {
		$fileno = "1" ;
	    }elsif (!$num_match) {
		print TMP "$dr  GP_NO_MATCH\n";
	    }else{
		$one_match = pop (@embl_match);
		print TMP "$dr  GP match: $one_match\n";   # the gene id is used in the parser
		$count ++;                   # only count the one match
	    }	    
	    $after_embl = 1;
	    next;  
	}

	# after the EMBL lines and ZFIN line, check the GenPept matching, 
	# if GenPept matching is not sufficient, use GenBank to furthur sort 
	# the records.
	if ($after_embl && !$qual_check){
	    ($no, $good) = Embl_Check(); 
 
	    if (!$no && $good) {
		$fileno = "0";

	    }elsif (!$no && !$good) {
		$fileno = "2" ;        #GenPept matching shows conflicts
	    
	    }else {                     #GenBank acc check
		@EMBL = ();
		$count = 0;
		foreach $embl_nt (@embl_nt) {
		    @embl_match = Embl_Match($embl_nt, "GenBank");
		    $num_match = @embl_match;
		    if (@embl_match) {
			push @embl_nt_matched, $embl_nt;
			print PROB "\tGB match: @embl_match\n"; #!!this line is used in the sp_parser.pl
		    }
	    	    @EMBL = (@EMBL, @embl_match);  # collect all the matches for each record
	    	    if ($num_match) {
			$count ++;                   # count for matched ones
			if ($num_match == 1) {
			    $one_match = pop (@embl_match); # record the one match in ZFIN 
			    print TMP "\t\tGB match: $one_match\n";
			}
		    }
		}
	    
		($no, $good) = Embl_Check();
	    
		if($no) {
		    if(!$num_pub) {
			$fileno = "6" ;			
		    }else {
			$fileno  = PubMed_Check() ? "5" : "6";
		    }
		}elsif (!$good){
		    $fileno = "3" ;
		}else {
		    $fileno = Embl_Genomic_Check() ? "4" : "0";		    
		}
	    }
	    $qual_check = 1;
	    	    
	} 	
		
	if (/^DR\s+ZFIN;\s+(.*);/) {              # check for ZFIN acc number, parse it 
	    $fileno = "00" if (!$fileno && $one_match && ($1 ne $one_match));
	    print PROB; 
	    print TMP;
	    next;
	}

	if(/^DR/ || /^KW/) {
	    print TMP;
	    next;
	}

	if (/\/\//) {                   # end of one record    
		
	    print PROB "//\n"; close PROB;
	    print TMP "//\n";   close TMP;     
    
	    if ($fileno eq "0") {
		system ("cat '$temprecd' >> okfile");
		$num_ok ++;

	    }elsif ($fileno eq "00") {   #those disagrees go to both okfile and prob0.
		system ("cat '$temprecd' >> okfile");
		system ("cat '$probrecd' >> prob0 ");
		$num_ok ++;

	    }else {

		$probfile = "prob".$fileno;
		system ("cat '$probrecd' >> '$probfile'");
		system ("cat '$temprecd' >> problemfile");		
		$num_prob ++;
	    }	   
    
	    unlink $temprecd;
	    unlink $probrecd;	
	}
    } # foreach loop for one record
 
}   # while loop for the whole SP file
close PUB;

print "\nFinal report: \n";
print "\t problem records(#) : $num_prob \n";
print "\t ok records(#)  : $num_ok \n";
printf ("\t ok percentage   : %.1f\%\n", 100 - $num_prob/($num_prob+$num_ok) * 100.0);


#---------------------------------------------------------------------------------------
#

sub init_var(){

  @rx = ();  @pubmed = ();
  @EMBL = (); @zfin = ();
  @embl_nt = (); @embl_nt_matched = ();  @embl_match = ();
  $embl_ac = ''; $zfin_ac = ''; $one_match = ''; $dr_zfin = '';
  $num_pub = 0; $embl_exist = 0; $num_match = 0; $count = 0; 
  $no = 0; $good=0; $fileno = 0; $after_embl=0; $qual_check=0;
}

# Check whether at least one EMBL numbers are in ZFIN database, 
# and whether the matched ZFIN records are the same one. 
# Return two values that denotes the checking result.   

sub Embl_Match ($$) {

  my ($embl_ac, $dbname, $sth, $geneZdbId, $sql);
  $embl_ac = $_[0];
  $dbname  = $_[1];
  @embl_match = () ;
  $sql = "";
  if ( $dbname eq "GenPept" ) {
      $sql = "  select distinct dblink_linked_recid 
                  from db_link 
                  where dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-42'
                    and dblink_linked_recid like 'ZDB-GENE-%'
                    and dblink_acc_num = ?
                  union
                  select mrel_mrkr_1_zdb_id 
                    from marker_relationship,db_link
                   where dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-42'  
                     and mrel_type = 'gene encodes small segment'
                     and mrel_mrkr_2_zdb_id=dblink_linked_recid 
                     and dblink_acc_num= ?";

  }
  if ($dbname eq "GenBank" ) {
      $sql = "  select distinct dblink_linked_recid 
                  from db_link 
                  where dblink_fdbcont_zdb_id in ('ZDB-FDBCONT-040412-36',
                                                  'ZDB-FDBCONT-040412-37')
                    and dblink_linked_recid like 'ZDB-GENE-%'
                    and dblink_acc_num = ?
                  union
                  select mrel_mrkr_1_zdb_id 
                    from marker_relationship,db_link
                   where dblink_fdbcont_zdb_id in ('ZDB-FDBCONT-040412-37',
                                                  'ZDB-FDBCONT-040412-36')
                     and mrel_type = 'gene encodes small segment'
                     and mrel_mrkr_2_zdb_id=dblink_linked_recid 
                     and dblink_acc_num= ?";
  } 
  $sth = $dbh->prepare($sql);
  $sth ->execute($embl_ac,$embl_ac);

  while ($geneZdbId = $sth->fetchrow_array ) {
    if ($geneZdbId =~ /ZDB-GENE/){
      push @embl_match, $geneZdbId;
    }
  }  
  return (@embl_match);  #matches in ZFIN db for each EMBL number 
}

sub Embl_Genomic_Check () {

    my ($gb_acc, $all_genomic, $isGenomic);
    $all_genomic = 1;
    foreach $gb_acc (@embl_nt_matched) {
	($isGenomic) = $dbh->selectrow_array("select 1
                                         from accession_bank
                                        where accbk_acc_num = '$gb_acc'
                                          and accbk_db_name = 'GenBank'
                                          and accbk_data_type = 'Genomic'");
        if (!$isGenomic) {
	    $all_genomic = 0;
	    last;
	}
    }
    return ($all_genomic);
}
    

#check whether at least one EMBL# is in ZFIN,
#and for those have match(es), if every one has multiple matches,
#that is a problem. Only if there is at least one has one match and
#that match also appears in other EMBL#'s matches, the record is ok. 

sub Embl_Check () {

    my $none = 0;     #whether at least one EMBL# matches in ZFIN
    my $same = 0;     #whether all EMBL#s in ZFIN associated with the same marker
    if(!@EMBL){
	$none = 1;
    }else{
	if ($one_match) {
	    
	    while ($match = pop @EMBL) {
		if ($match eq $one_match) { #whether the one-match appears in all the
		    $count --;                #other matches.
		}
	    }
	    if (!$count ) {
		$same = 1;
	    }
	}
    }
    return ($none, $same);
}


# Check whether at least one PubMed number is in ZFIN db.
# Return 0/1 that denote this result.
sub PubMed_Check( ) {
  
  my $match = 0; 
  my ($sth, $pubmed, $qpubmed, $pub_match);

  foreach my $rx (@rx) {
      chop ($rx);    # the added '\n' could only be chopped not chompped. 
      print PROB "$rx";

      $pubmed = $1 if ($rx =~ /^RX\s+MEDLINE=\d+.*PubMed=(\d+)/);
      $qpubmed = $dbh->quote ($pubmed);
      $pub_match = $dbh->selectrow_array("
                 select zdb_id
                 from publication
                 where accession_no = $qpubmed
               " );
 
      if($pub_match){
	  
	  print PROB "\t$pub_match";
	  $match = 1;
      }else {

	  print PUB "$pubmed\n";
      }
      print PROB "\n";
  }

  return $match;
}


# Initialize the final output files for the checked SP records
sub init_files () {

  my $title;
  open FILE, ">okfile" or die "Cannot open the okfile: $!";
  close FILE;
  
  open FILE, ">problemfile" or die "Cannot open the problemfile: $!";
  close FILE;


  open FILE, ">prob0" or die "Cannot open the prob0: $!";
  $title =<<ENDDOC;
#--------------------------------------------
# SP records Problem 0
#    
#  DR ZFIN line doesn't agree with the matched gene. 
#
#  These records are already in okfile with the matched gene. 
#
ENDDOC

  print FILE "$title";
  close FILE;


  open FILE, ">prob1" or die "Cannot open the prob1: $!";
  $title =<<ENDDOC;
#--------------------------------------------
# SP records Problem 1
#    
#   at least one GenPept Acc#  associated with >1  genes in ZFIN
#   
ENDDOC

  print FILE "$title";
  close FILE;


  open FILE, ">prob2" or die "Cannot open the prob2: $!";
  $title =<<ENDDOC;
#--------------------------------------------
# SP records Problem 2
#  
#   GenPept Acc#s associated with different genes
#
ENDDOC

  print FILE "$title";
  close FILE;


  open FILE, ">prob3" or die "Cannot open the prob3: $!";
  $title =<<ENDDOC;
#--------------------------------------------
# SP records Problem 3
#    
#   at least one GenBank Acc# in ZFIN, but not consistent
#   
ENDDOC

  print FILE "$title";
  close FILE;


  open FILE, ">prob4" or die "Cannot open the prob4: $!";
  $title =<<ENDDOC;
#--------------------------------------------
# SP records Problem 4
#    
#   GenBank Acc# in ZFIN, consistent, but all genomic 
#
ENDDOC

  print FILE "$title";
  close FILE;


  open FILE, ">prob5" or die "Cannot open the prob5: $!";
  $title =<<ENDDOC;
#--------------------------------------------
# SP records Problem 5
#    
#   GenBank #s not in ZFIN
#   at least one PubMed # in ZFIN
#
ENDDOC

  print FILE "$title";
  close FILE;


  open FILE, ">prob6" or die "Cannot open the prob6: $!";
  $title =<<ENDDOC;
#--------------------------------------------
# SP records Problem 6
#    
#   GenBank #s not in ZFIN
#   PubMed # not present, or not in ZFIN
#
ENDDOC

  print FILE "$title";
  close FILE; 


  open FILE, ">prob7" or die "Cannot open the prob7: $!";
  $title =<<ENDDOC;
#--------------------------------------------
# SP records Problem 7
#    
#   No EMBL line
#
ENDDOC

  print FILE "$title";
  close FILE;


  open FILE, ">prob8" or die "Cannot open the prob8: $!";
  $title =<<ENDDOC;
#--------------------------------------------
# SP records Problem 8
#    
#   >1 DR ZFIN lines
#
ENDDOC

  print FILE "$title";
  close FILE;
}

