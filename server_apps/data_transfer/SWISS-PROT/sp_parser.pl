#!/local/bin/perl

#
#  sp_parser.pl
#
#  This script parse the checked SWISS-PROT records, get each field in 
#  the record into different files with informix loadable format. 

use DBI;
use Cwd;

my $cwd = getcwd();
my $first = 1;       #indicate the beginning of DR(the end of CC)
my @zdb_gname = ();
my @ext_dr = ();
my @cc = (); $cc = '';
my @de = (); $de = '';
my @sp_ac=(); $sp_ac='';
my @zdbid_spac = ();
$desc="";


# Files to be loaded into zfin.
open DBLINK, ">dr_dblink.unl" or die "Cannot open dr_dblink.unl:$!";
open ACC, ">ac_dalias.unl" or die "Cannot open ac_dalias.unl:$!";
open GNAME, ">gn_dalias.unl" or die "Cannot open gn_dalias.unl:$!";
open COMMT, ">cc_external.unl" or die "Cannot open cc_external.unl:$!";
open KEYWD, ">kd_spkeywd.unl" or die "Cannot open kd_spkeywd.unl:$!";
open DESC, ">sp_desc.unl";
open LEN, ">sp_len.unl";
open DESCNEW, ">spdesc.unl";
open GENELEN, ">splen.unl";
open ECNUM, ">ecgene.unl";
open SPGO, ">spgo.unl";

my $dbname = "<!--|DB_NAME|-->";
my $username = "";
my $password = "";



my $dbh = DBI->connect ("DBI:Informix:$dbname", $username, $password) 
          or die "Cannot connect to Informix database: $DBI::errstr\n";

while (<>) {
 if (/^ID\s+(.*)/) {
     $id=$1;
     @sp_length = split(/PRT; /,$id);
     $length = $sp_length[1] ;             #chop period sign
     @len=split(' ',$length);
     $len=$len[0];
     print LEN "$len|\n";
     next;
  }
  
  #AC   O12990; O73880;
  if (/^AC\s+(.*)/) {
    @sp_ac = split(' ',$1); 
    $prm_ac = shift @sp_ac;
    chop $prm_ac;
    while ($sp_ac = shift @sp_ac){
      chop $sp_ac;
      print ACC "$prm_ac|$sp_ac|\n";
    }  
    next;
  }
  #GN   rag1 OR RAG1 OR RAG-1.
  if (/^GN\s+(.*)/) {
    $gn = $1;  chop($gn);              #chop period sign
    @sp_gname = split(/\s+OR\s+/i, "$gn"); 
    next;
  }
  
  if (/^DE\s+(.*)/) {   
    push (@de, $de);    #put each item of the comments into array     
    $de = $de.' '.$1;
    @ec=split(/EC/,$de);
    @ecnum=split(' ',$ec[1]);
    $ecnumber= $ecnum[0];
    $ecnumber=~s/\).//;
    $ecnumber=~s/\)//;
    chomp $ecnumber;
    next;
  }
  
  #CC   -!- SUBCELLULAR LOCATION: Nuclear (By similarity).
  #CC   -!- SIMILARITY: CONTAINS 2 LIM DOMAINS. THE LIM DOMAIN BINDS 2 ZINC
  #CC       IONS.
  if (/^CC\s+-!-\s(.*)/) {
    push (@cc, $cc);    #put each item of the comments into array     
    $cc = $1.' ';            #concatenate and form one item
    next;
  }
  if (/^CC\s+(.*)/) {   
    $cc = $cc.$1.' ';
    next;
  }
 
  #DR   EMBL; U71094; AAC60366.1; -.	ZDB-GENE-990415-235                
  #DR   ZFIN; ZDB-GENE-990415-235; rag2.
  #DR   InterPro; IPR004321; RAG2.
  #DR   Pfam; PF03089; RAG2; 1.
  if (/^DR/ ) {
    @dr = split;
    $dbname = $dr[1]; chop($dbname);
    $acc_no = $dr[2]; chop($acc_no);
    
    if ($first){             #first DR is always EMBL record with ZDB id
       if ($cc) { push (@cc, $cc);}
      # print COMMT "$prm_ac|@cc|\n";         
       $first = 0;
     }
    if ($dbname eq "EMBL") {
       $_ = pop(@dr);     #get zfin accession number for the SP record  
       if (/NO_MATCH/) {
	push(@embl_unl, $acc_no);
       }elsif(/ZDB/ ){
	 $gene= $_;
       }
       next;
     }else{
       #$attribute = Get_attribute();
       print DBLINK "$gene|$dbname|$acc_no| |\n";
       #print ACTV "$attribute|\n";
       next;
     }
  }  

  #KW   DNA-binding; Nuclear protein; Transcription regulation; Activator;
  #KW   Neurogenesis; Developmental protein; Differentiation.
  if (/^KW\s+(.*)/) {
    $kw = $1;  chop($kw);
    @sp_kw = split(/; /, $kw);
    while ($sp_kw = shift @sp_kw) {
      print KEYWD "$gene|$sp_kw|\n";
    }
    next;
  } 
  if(/\/\//) {
    if (@cc) {
     
      open CC, ">$cwd/ccnote/$prm_ac" or die "Cannot open the $prm_ac file:$!";
      print CC "@cc";
      close CC;
      print COMMT "$gene|$cwd/ccnote/$prm_ac|\n";  
    }
    
    print DBLINK "$gene|SWISS-PROT|$prm_ac|$len|\n";
    print SPGO "$gene|$prm_ac|\n";

    while ($embl_unl = shift @embl_unl) {
      print DBLINK "$gene|Genbank|$embl_unl| |\n";
    }
       
    my $get_abbrv = $dbh->selectrow_array("
                 select mrkr_abbrev from marker 
                 where mrkr_zdb_id = \"$gene\"
                " );    
    push (@zdb_gname, $get_abbrv);
    
    my $sth = $dbh->prepare("
                  select dalias_alias from data_alias
                  where dalias_data_zdb_id = ?
                " ); 
    $sth->execute( $gene);
    while ( my $get_alias = $sth->fetchrow_array) {
      	push (@zdb_gname, $get_alias);
      }
 
    #print "@zdb_gname\n\n";
    @temp_gname = @zdb_gname;
    while ($sp_gname = shift @sp_gname) {
      $new = 1;
      while ($zdb_gname = shift @zdb_gname) {
	#print "sp: $sp_gname#, zdb: $zdb_gname#\n";
	if (lc($sp_gname) eq lc($zdb_gname) ) {
	  $new = 0;
	  last;
	}
      }
      @zdb_gname = @temp_gname;
      if ($new) {
	print GNAME "$prm_ac|$gene|$sp_gname|\n";
      }
    }
    print DESC "$de|$gene|\n"; 
    print GENELEN "$gene|$len|\n";
    if (length($ecnumber)>0){
       print DBLINK "$gene|EC-ENZYME|$ecnumber| |\n"; 
    }
    # reinitiate the variables for loop
    @sp_ac=();
    $gn=''; $cc=''; $dbname=''; $acc_no=''; $kw=''; $gene=''; $prm_ac = '';
    @cc = ();  @dr = (); @zfin = ();@zdb_gname = (); @info = (); @de=();$de='';
    @sp_ac=(); $sp_ac='';
    $first = 1;$one = 1;
  }
}

