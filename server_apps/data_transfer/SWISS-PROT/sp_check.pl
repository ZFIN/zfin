#!/local/bin/perl 

# sp_check.pl
#
# This script reads Swiss-Prot file(which consists of protein records),
# checks each record with corresponding gene record in ZFIN database 
# and accordingly divides the records into several catagories. It checks 
# the Database cross-references(DR) about whether ZFIN accession number is
# provided, whether EMBL accession numbers are provided, whether the EMBL 
# accession numbers are in ZFIN and associated with the same marker. It also 
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
 
    if(/^AC/ || /^GN/ || /^CC/|| /^KW/|| /^ID/|| /^DE/ ) {  
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
      print PARSE;            # make up the chomped '\n'
      $dr = $_; chomp ($dr);
      $embl_exist = 1;
      $embl_nt = $1; 
      $embl_gp = $2;

#     print $sp_id . "\t";           
#     print $embl_ac . " ";
#      @embl_match = Embl_Match (); # the matches in ZFIN for each EMBL No.
#      if (@embl_match){
#	print PARSE "\t@embl_match\n\n";
#      #}else{# second chance, to match on Genbank
#         $embl_ac = $embl_nt;
#         print "*** ".$embl_ac . "\n";
#         @embl_match = Embl_Match (); 
#         if (! @embl_match){print "not in zfin\n";}
#      }  


     print $sp_id . "\t";   
     $embl_ac = $embl_gp;          # first try the polypeptide accession
     print $embl_ac;
     @embl_match = Embl_Match ();
     $num_match = @embl_match;    # record number of genes directly or indirectly associated
     print "\t$num_match\t";
     if ($num_match != 1){        # if there is not a unique gene in ZFIN associated w/ the protein   
        $embl_ac = $embl_nt;      # fallback and try with the nucelotide accession
        print $embl_ac ;               
        @embl_match = Embl_Match ();
        $num_match = @embl_match; 
        print "\t$num_match\n";
     }else{print "\n";}   


  
          
      @EMBL = (@EMBL, @embl_match);  # collect all the matches for each record
      $num_match = @embl_match;      # number of matches for each EMBL No. 
      if (!$num_match) {  
	print TMP "$dr\tNO_MATCH\n";
      }else{
	$count ++;                   # count for matched ones
	if ($num_match == 1) {
	  $one_match = pop (@embl_match); # record the one match in ZFIN
	  print TMP "$dr\t$one_match\n";
	}else {
	  print TMP "$dr\tMULTI_MATCHES\n";
	}
      }
      next;      
    }
  
    if (/^DR\s+ZFIN/) {              # check for ZFIN acc number, parse it 
      print PARSE;     
      @zfin = split;              
      $zfin_ac = $zfin[2];
      chop($zfin_ac);
      next;
    }       
    if(/^DR/) {
      print TMP;
    }

    if (/\/\//) {                   # end of one record    
      # checking the recorded information for each SP record,
      if ($zfin_ac) {
	#print "yes zfin\n";      
	($no, $good) = Embl_Check();  
	if ($no) {
	  $fileno = "1" ;
	}elsif ($good) {
	  $fileno = "0" ;
	}else {
	  $fileno = "2" ;
	}
      }else { 
	#print "no zfin\n";
	if (!$embl_exist) {
	  $fileno = "3" ;
	  
	}else {
	  
	  ($no, $good) = Embl_Check();
	  if($no) {
	    if(!$num_med) {
	      $fileno = "4" ;
	      
	    }else {
	      $yes = Medline_Check();
	      if ($yes) {
		$fileno = "5";
		
	      }else {
		$fileno = "6";	      
	      }
	    }
	  }elsif (!$good){
	    $fileno = "7" ;
	  }else {
	    $fileno = "0" ;
	  }
	}
      }
      print PARSE "//\n";    
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
system ("cat appokfile >> okfile");
print "\nFinal report: \n";
print "\t problem records(#) : $num_prob \n";
print "\t ok records(#)  : $num_ok \n";
printf ("\t ok percentage   : %.1f\%\n", 100 - $num_prob/($num_prob+$num_ok) * 100.0);
system ("cp prob1 <!--|ROOT_PATH|-->/home/data_transfer/SWISS-PROT/");
system ("cp prob2 <!--|ROOT_PATH|-->/home/data_transfer/SWISS-PROT/");
system ("cp prob3 <!--|ROOT_PATH|-->/home/data_transfer/SWISS-PROT/");
system ("cp prob4 <!--|ROOT_PATH|-->/home/data_transfer/SWISS-PROT/");
system ("cp prob5 <!--|ROOT_PATH|-->/home/data_transfer/SWISS-PROT/");
system ("cp prob6 <!--|ROOT_PATH|-->/home/data_transfer/SWISS-PROT/");
system ("cp prob7 <!--|ROOT_PATH|-->/home/data_transfer/SWISS-PROT/");
system ("cp index.html <!--|ROOT_PATH|-->/home/data_transfer/SWISS-PROT/");

sub init_var(){

  @rx = (); 
  @medpub = ();
  @embl_match = ();
  @EMBL = ();
  @zfin = ();
  @no_match_embl = ();
  $embl_ac = ''; $zfin_ac = ''; $one_match = '';
  $num_med = 0; $embl_exist = 0;  $num_match = 0; $count = 0;
  $no = 0; $good=0; $fileno = 0; $yes = 0;
}

# Check whether at least one EMBL numbers are in ZFIN database, 
# and whether the matched ZFIN records are the same one. 
# Return two values that denotes the checking result.   

sub Embl_Match () {

  my ($sth, $zdbid);
  @embl_match = () ;
  $sth = $dbh->prepare("
                  select distinct dblink_linked_recid 
                  from db_link 
                  where dblink_acc_num = ?
                  union
                  select mrel_mrkr_1_zdb_id from marker_relationship,db_link
                  where mrel_mrkr_2_zdb_id=dblink_linked_recid and dblink_acc_num= ?");
  $sth ->execute($embl_ac,$embl_ac);
  while ($zdbid = $sth->fetchrow_array ) {
    if ($zdbid =~ /ZDB-GENE/){
      #print "embl: #$zdbid#\n";
      push @embl_match, $zdbid;
    }
  }  
  open EMBL, ">emblmatch" or die "Cannot create the parse file: $!";
  print EMBL "@embl_match"; 
  return (@embl_match);  #matches in ZFIN db for each EMBL number 
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


  open FILE, ">prob1" or die "Cannot open the prob1: $!";
  $title =<<ENDDOC;
#--------------------------------------------
# SP records Problem 1
#    
#   have ZFIN #
#   no EMBL # associated with gene in ZFIN
#
ENDDOC

  print FILE "$title";
  close FILE;


  open FILE, ">prob2" or die "Cannot open the prob2: $!";
  $title =<<ENDDOC;
#--------------------------------------------
# SP records Problem 2
#    
#   have ZFIN#
#   EMBL#  all associated with some gene(s) in ZFIN
#           but not consistent
#
ENDDOC

  print FILE "$title";
  close FILE;


  open FILE, ">prob3" or die "Cannot open the prob3: $!";
  $title =<<ENDDOC;
#--------------------------------------------
# SP records Problem 3
#    
#   no ZFIN#
#   no EMBL#
#
ENDDOC

  print FILE "$title";
  close FILE;


  open FILE, ">prob4" or die "Cannot open the prob4: $!";
  $title =<<ENDDOC;
#--------------------------------------------
# SP records Problem 4
#    
#   no ZFIN#
#   EMBL# present but not associated with any gene in ZFIN
#   no Medline#
#
ENDDOC

  print FILE "$title";
  close FILE;


  open FILE, ">prob5" or die "Cannot open the prob5: $!";
  $title =<<ENDDOC;
#--------------------------------------------
# SP records Problem 5
#    
#   no ZFIN#
#   EMBL# present but not associated with any gene in ZFIN
#   have Medline#(s) and all in ZFIN
#
ENDDOC

  print FILE "$title";
  close FILE; 


  open FILE, ">prob6" or die "Cannot open the prob6: $!";
  $title =<<ENDDOC;
#--------------------------------------------
# SP records Problem 6
#    
#   no ZFIN#
#   EMBL# present but not associated with any gene in ZFIN
#   Medline# present but not (all) in ZFIN
#
ENDDOC

  print FILE "$title";
  close FILE;


  open FILE, ">prob7" or die "Cannot open the prob7: $!";
  $title =<<ENDDOC;
#--------------------------------------------
# SP records Problem 7
#    
#   no ZFIN#
#   EMBL#s all associated with some gene(s) in ZFIN
#              but not consistent
#
ENDDOC

  print FILE "$title";
  close FILE;

}

  
