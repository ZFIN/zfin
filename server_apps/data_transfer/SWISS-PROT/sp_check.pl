#!/local/bin/perl 

# sp_check.pl
#
# This script reads Swiss-Prot file(which consists of protein records),
# checks each record with corresponding gene record in ZFIN database 
# and accordingly divides the records into several catagories. It checks 
# the Database cross-references(DR) about whether ZFIN accession number is
# provided, whether EMBL GenPept accession numbers are provided and associated 
# with the same marker in ZFIN, it then checks EMBL Genbank accessions for those 
# that couldn't be decided by GenPept matching. The scripts also
# checks the Reference cross-reference(s)(RX) to see whether any Medline 
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

while (<>) {
    
    
    $record = "recd$$.txt"; # Store the whole record, might go to problemfile
    open REC, ">$record" or die "Cannot create the record file: $!";
    do{
	print REC;
	$_ =<>;
    }until ($_ eq "//\n");   # identify one record
    print REC;
    
    init_var ();     # Initialize the variables and arrays 
    
    ## Parsefile stores AC, DR(EMBL, ZFIN) and/or RX(Medline), goes to 
    ## divided problem file. 
    $parsefile = "parse$$.txt";
    open PARSE, ">$parsefile" or die "Cannot create the parse file: $!";
    
    ## Tempfile stores ID, AC, DE, GN, DR, CC, KW might go to Ok file.
    $tempfile = "temp$$.txt"; 
    open TMP, ">$tempfile" or die "Cannot create the temporary file: $!";
    
    open REC, "$record" or die "Cannot open the record file:$!";
    while (<REC>){               #read each SP record
	
	if(/^AC/ || /^GN/ || /^CC/|| /^ID/|| /^DE/ ) {  
	    print TMP;
	}
	
	if (/^ID\s+(\w+)/) {
	    $sp_id = $1; 
	    next;    
	}
	if (/^AC/) {
	    print PARSE; 
	    next;    
	}
	
	if (/^RX\s+MEDLINE=(\d+).*PubMed=(\d+)/) {   # parse for checking 
	    push @rx, $_;
	    $num_med ++;
	    push @medpub, $1;  
	    push @medpub, $2;
	    next;
	}
	
	if (/^DR\s+EMBL;\s+(\w+);\s+(\w+)\./) {   # check for EMBL acc number, parse it  
	    print PARSE;            
	    $dr = $_; chomp ($dr);
	    $embl_exist = 1;
	    $embl_nt = $1;   
	    push @embl_nt, $embl_nt;
	    
            # use GenPept acc for matching, storing results in @EMBL
	    $embl_gp = $2;
	    @embl_match = Embl_Match($embl_gp, "GenPept"); # first try the polypeptide accession
	    $num_match = @embl_match;    # record number of genes directly or indirectly associated
	    
	    if (@embl_match) {
		print PARSE "\tGP match: @embl_match\n";
	    }
	    
	    @EMBL = (@EMBL, @embl_match);  # collect all the matches for each record
	    
	    if ($num_match > 1) {
		$fileno = "1A" ;
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
	
	if (/^DR\s+ZFIN/) {              # check for ZFIN acc number, parse it 

	    print PARSE;  
	    $dr_zfin = $_;
	    @zfin = split;              
	    $zfin_ac = $zfin[2];
	    chop($zfin_ac);
	    next;
	}  
     
        # after the EMBL lines and ZFIN line, check the GenPept matching, 
	# if GenPept matching is not sufficient, use Genbank to furthur sort 
        # the records.

	if ($after_embl && !$qual_check){
	    ($no, $good) = Embl_Check();  
	    if (!$no && $good) {
		$fileno = "0";
	    }elsif (!$no && !$good) {
		$fileno = "1A" ;        #GenPept matching shows conflicts
	    }else {                     #Genbank acc check
		@EMBL = ();
		$count = 0;
		@embl_nt_reserve = @embl_nt;
		while ($embl_nt = shift @embl_nt) {
		    @embl_match = Embl_Match($embl_nt, "Genbank");
		    $num_match = @embl_match;
		    if (@embl_match) {
			print PARSE "\tGB match: @embl_match\n"; #!!this line is used in the sp_parser.pl
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
		if ($zfin_ac) {		
		    if ($no) {
			$fileno = "1B" ;
		    }elsif (!$good) {
			$fileno = "2B" ;
		    }else {
			$fileno = "0" ;
		    }
		    
		}else { 		    
		    if($no) {
			if(!$num_med) {
			    $fileno = "4B" ;			
			}else {
			    $yes = Medline_Check();
			    if ($yes) {
				$fileno = "5B";
				
			    }else {
				$fileno = "6B";	      
			    }
			}
		    }elsif (!$good){
			$fileno = "7B" ;
		    }else {
			$fileno = Embl_Genomic_Check() ? "8B" : "0";		    
		    }
		}
	    }
	    $qual_check = 1;
	    print TMP $dr_zfin;
	} 	
	
	# output DR ZFIN line into okfile, so that when the record doesn't 
	# have GenPept or Genbank matched ZDB ID, we still know which gene.(
	# this is very rare case though)
	if ($dr_zfin) {
	     print TMP $dr_zfin;
	     $dr_zfin = '';
	 }

	if(/^DR/ || /^KW/) {
	    print TMP;
	}

	if (/\/\//) {                   # end of one record    
	    if (!$zfin_ac && !$embl_exist) {
		$fileno = "3B" ;
	    }
		
	    print PARSE "//\n"; close PARSE;
	    print TMP "//\n";   close TMP;     
	    if ($fileno eq "0") {
		system ("cat '$tempfile' >> okfile");
		$num_ok ++;
	    }else {
		system ("cat '$record' >> problemfile"); 
		$num_prob ++;
		$probfile = "prob".$fileno;
		system ("cat '$parsefile' >> '$probfile'");
	    }
	    unlink $tempfile;
	    unlink $parsefile;
	    unlink $record;
	}    
    }
}

print "\nFinal report: \n";
print "\t problem records(#) : $num_prob \n";
print "\t ok records(#)  : $num_ok \n";
printf ("\t ok percentage   : %.1f\%\n", 100 - $num_prob/($num_prob+$num_ok) * 100.0);


#---------------------------------------------------------------------------------------
#

sub init_var(){

  @rx = (); 
  @medpub = ();
  @embl_match = ();
  @EMBL = ();
  @zfin = ();
  @no_match_embl = ();
  @embl_nt = (); @embl_nt_reserve = ();
  $embl_ac = ''; $zfin_ac = ''; $one_match = ''; $dr_zfin = '';
  $num_med = 0; $embl_exist = 0; $num_match = 0; $count = 0; $qual_check = 0;
  $no = 0; $good=0; $fileno = 0; $yes = 0; $after_embl = 0;
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
                    and dblink_acc_num = ?
                  union
                  select mrel_mrkr_1_zdb_id 
                    from marker_relationship,db_link
                   where dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-42'  
                     and mrel_type = 'gene encodes small segment'
                     and mrel_mrkr_2_zdb_id=dblink_linked_recid 
                     and dblink_acc_num= ?";

  }
  if ($dbname eq "Genbank" ) {
      $sql = "  select distinct dblink_linked_recid 
                  from db_link 
                  where dblink_fdbcont_zdb_id in ('ZDB-FDBCONT-040412-36',
                                                  'ZDB-FDBCONT-040412-37')
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
    while (my $gb_acc = shift @embl_nt_reserve) {
	($isGenomic) = $dbh->selectrow_array("select 1
                                         from accession_bank
                                        where accbk_acc_num = '$gb_acc'
                                          and accbk_db_name = 'Genbank'
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


# Check whether the Medline number are in ZFIN db.
# Return a value that denote this result.
sub Medline_Check( ) {
  
  my $rx = shift @rx;
  chomp ($rx);
  print PARSE "$rx";
  
  my $match = 0; 
  my $all = 0;  
  my ($sth, $medline, $qmedline, $med_match, $pub_match);
  while ($medline = shift @medpub ) {
   
    $qmedline = $dbh->quote ($medline);
    $med_match = $dbh->selectrow_array("
                 select zdb_id
                 from publication
                 where accession_no = $qmedline
               " );
    
    if($med_match){
      
      print PARSE "\tYN\t$med_match";
      $match ++;
      $medline = shift @medpub;      
      }else {
	$medline = shift @medpub;
	$qmedline = $dbh->quote ($medline);
	$pub_match = $dbh->selectrow_array("
                 select zdb_id
                 from publication
                 where accession_no = $qmedline
               " );
	if($pub_match){
	  
	  print PARSE "\tNY\t$pub_match";
	  $match ++;
	}
      }     
    print PARSE "\n";
    $rx = shift @rx; chomp ($rx);   
    print PARSE "$rx";
  }
  if ($match == $num_med) {    #check whether all the Medline# are in ZFIN
    $all = 1;
  }
  return $all;
}


# Initialize the final output files for the checked SP records
sub init_files () {

  my $title;
  open FILE, ">okfile" or die "Cannot open the okfile: $!";
  close FILE;
  
  open FILE, ">problemfile" or die "Cannot open the problemfile: $!";
  close FILE;


  open FILE, ">prob1A" or die "Cannot open the prob1A: $!";
  $title =<<ENDDOC;
#--------------------------------------------
# SP records Problem 1A
#    
#   at least 1 GenPept#  associated with >1  genes in ZFIN
#   OR GenPept #s associated with different genes
#
ENDDOC

  print FILE "$title";
  close FILE;


  open FILE, ">prob1B" or die "Cannot open the prob1B: $!";
  $title =<<ENDDOC;
#--------------------------------------------
# SP records Problem 1B
#  
#   have ZFIN #
#   GenPept #(s) not in ZFIN, or problematic
#   none Genbank # associated with gene in ZFIN
#
ENDDOC

  print FILE "$title";
  close FILE;


  open FILE, ">prob2B" or die "Cannot open the prob2B: $!";
  $title =<<ENDDOC;
#--------------------------------------------
# SP records Problem 2B
#    
#   have ZFIN#
#   GenPept #(s) not in ZFIN, or problematic
#   1 Genbank # associated with >1 genes in ZFIN 
#   OR >1 Genbank #s  associated with different genes
#   
ENDDOC

  print FILE "$title";
  close FILE;


  open FILE, ">prob3B" or die "Cannot open the prob3B: $!";
  $title =<<ENDDOC;
#--------------------------------------------
# SP records Problem 3B
#    
#   no ZFIN#
#   GenPept #(s) not in ZFIN, or problematic
#   no Genbank# present in SP file
#
ENDDOC

  print FILE "$title";
  close FILE;


  open FILE, ">prob4B" or die "Cannot open the prob4B: $!";
  $title =<<ENDDOC;
#--------------------------------------------
# SP records Problem 4B
#    
#   no ZFIN#
#   GenPept #(s) not in ZFIN, or problematic
#   Genbank #s not associated with any gene in ZFIN
#   no Medline#
#
ENDDOC

  print FILE "$title";
  close FILE;


  open FILE, ">prob5B" or die "Cannot open the prob5B: $!";
  $title =<<ENDDOC;
#--------------------------------------------
# SP records Problem 5B
#    
#   no ZFIN#
#   GenPept #(s) not in ZFIN, or problematic
#   Genbank #s not associated with any gene in ZFIN
#   have Medline#(s) and all in ZFIN
#
ENDDOC

  print FILE "$title";
  close FILE; 


  open FILE, ">prob6B" or die "Cannot open the prob6B: $!";
  $title =<<ENDDOC;
#--------------------------------------------
# SP records Problem 6B
#    
#   no ZFIN#
#   GenPept #(s) not in ZFIN, or problematic
#   Genbank #s not associated with any gene in ZFIN
#   Medline# present but not (all) in ZFIN
#
ENDDOC

  print FILE "$title";
  close FILE;


  open FILE, ">prob7B" or die "Cannot open the prob7B: $!";
  $title =<<ENDDOC;
#--------------------------------------------
# SP records Problem 7B
#    
#   no ZFIN#
#   GenPept #(s) not in ZFIN, or problematic
#   Genbank #s associated with different genes in ZFIN
#             
ENDDOC

  print FILE "$title";
  close FILE;

  
  open FILE, ">prob8B" or die "Cannot open the prob8B: $!";
  $title =<<ENDDOC;
#--------------------------------------------
# SP records Problem 8B
#    
#   no ZFIN#
#   GenPept #(s) not in ZFIN, or problematic
#   all Genbank #s are genomic, though no conflict in ZFIN gene association
#             
ENDDOC

  print FILE "$title";
  close FILE;

}

