#!/private/bin/perl 
use strict ; 

#
# pre_loadsp.pl
#
 
use MIME::Lite;
use DBI;


#------------------- Download -----------

sub downloadGOtermFiles () {
    &process_vertebrates ;

   system("wget -q http://www.geneontology.org/external2go/spkw2go -O spkw2go");
   system("wget -q http://www.geneontology.org/external2go/interpro2go -O interpro2go");
   system("wget -q http://www.geneontology.org/external2go/ec2go -O ec2go");
 }

# ----------------- Send Error Report -------------
# Parameter
#   $    Error message 

sub sendErrorReport ($) {
  
  my $SUBJECT="Auto SWISS-PROT:".$_[0];
  my $MAILTO="<!--|SWISSPROT_EMAIL_ERR|-->";
  my $TXTFILE="./report.txt";
 
  # Create a new multipart message:
  my $msg1 = new MIME::Lite 
    From    => "$ENV{LOGNAME}",
    To      => "$MAILTO",
    Subject => "$SUBJECT",
    Type    => 'multipart/mixed';
 
  attach $msg1 
   Type     => 'text/plain',   
   Path     => "$TXTFILE";

  # Output the message to sendmail

  open (SENDMAIL, "| /usr/lib/sendmail -t -oi");
  $msg1->print(\*SENDMAIL);
  close (SENDMAIL);

}

#------------------ Send Running Result ----------------
# No parameter
#
sub sendRunningResult {
		
 #----- One mail send out the checking report----

  my $SUBJECT="Auto: SWISS-PROT check report";
  my $MAILTO="<!--|SWISSPROT_EMAIL_REPORT|-->";
  my $TXTFILE="./checkreport.txt";
 
  # Create a new multipart message:
  my $msg2 = new MIME::Lite 
    From    => "$ENV{LOGNAME}",
    To      => "$MAILTO",
    Subject => "$SUBJECT",
    Type    => 'multipart/mixed';
 
  attach $msg2 
   Type     => 'text/plain',   
   Path     => "$TXTFILE";

  # Output the message to sendmail

  open (SENDMAIL, "| /usr/lib/sendmail -t -oi");
  $msg2->print(\*SENDMAIL);

  
 #----- Another mail send out problem files ----

  my $SUBJECT="Auto: SWISS-PROT problem file";
  my $MAILTO="<!--|SWISSPROT_EMAIL_REPORT|-->";     
  my $ATTFILE = "allproblems.txt";

  # Create another new multipart message:
  my $msg3 = new MIME::Lite 
    From    => "$ENV{LOGNAME}",
    To      => "$MAILTO",
    Subject => "$SUBJECT",
    Type    => 'multipart/mixed';

  attach $msg3 
    Type     => 'application/octet-stream',
    Encoding => 'base64',
    Path     => "./$ATTFILE",
    Filename => "$ATTFILE";

  # Output the message to sendmail
  open (SENDMAIL, "| /usr/lib/sendmail -t -oi");
  $msg3->print(\*SENDMAIL);

 #----- Another mail send out problem files ----

  my $SUBJECT="Auto: PubMed not in ZFIN";
  my $MAILTO="<!--|SWISSPROT_EMAIL_REPORT|-->";     
  my $ATTFILE = "pubmed_not_in_zfin";

  # Create another new multipart message:
  my $msg4 = new MIME::Lite 
    From    => "$ENV{LOGNAME}",
    To      => "$MAILTO",
    Subject => "$SUBJECT",
    Type    => 'multipart/mixed';

  attach $msg4 
    Type     => 'application/octet-stream',
    Encoding => 'base64',
    Path     => "./$ATTFILE",
    Filename => "$ATTFILE";

  # Output the message to sendmail
  open (SENDMAIL, "| /usr/lib/sendmail -t -oi");
  $msg4->print(\*SENDMAIL);

 #----- Another mail send out problem files ----

  my $SUBJECT="Auto: report of processing pre_zfin.org";
  my $MAILTO="<!--|SWISSPROT_EMAIL_REPORT|-->";     
  my $ATTFILE = "redGeneReport.txt";

  # Create another new multipart message:
  my $msg5 = new MIME::Lite 
    From    => "$ENV{LOGNAME}",
    To      => "$MAILTO",
    Subject => "$SUBJECT",
    Type    => 'multipart/mixed';

  attach $msg5 
    Type     => 'application/octet-stream',
    Encoding => 'base64',
    Path     => "./$ATTFILE",
    Filename => "$ATTFILE";

  # Output the message to sendmail
  open (SENDMAIL, "| /usr/lib/sendmail -t -oi");
  $msg5->print(\*SENDMAIL);

  close(SENDMAIL);
}


# ====================================
#
# Extracts only zfin data from vertebrates.
#
sub process_vertebrates{
    system("wget -q ftp://ftp.uniprot.org/pub/databases/uniprot/current_release/knowledgebase/taxonomic_divisions/uniprot_trembl_vertebrates.dat.gz -O uniprot_trembl_vertebrates.dat.gz");
    system("gunzip uniprot_trembl_vertebrates.dat.gz");
    system("cp uniprot_trembl_vertebrates.dat pre_pre_zfin.dat");
    system("wget -q ftp://ftp.uniprot.org/pub/databases/uniprot/current_release/knowledgebase/taxonomic_divisions/uniprot_sprot_vertebrates.dat.gz -O uniprot_sprot_vertebrates.dat.gz");
    system("gunzip uniprot_sprot_vertebrates.dat.gz");
    system("cat uniprot_sprot_vertebrates.dat >> pre_pre_zfin.dat");

    open(DAT, "pre_pre_zfin.dat") || die("Could not open file!");

    open OUTPUT, ">pre_zfin.dat" or die "Cannot open zfin.dat";

    # find each "//\n" and test to see if its a zfin record.
    my $buffer = "" ; 
    my $line ; 
    foreach $line(<DAT>){
       if( 
           $line !~ m/CC   -------/
           and 
           $line !~ m/CC   Copyrighted/ 
           and 
           $line !~ m/CC   Distributed/ 
           and
           $line !~ m/DR   ZFIN; ZDB-GENE/
           ){
           $buffer = $buffer .  $line  ; 
       }
       if($line=~ m/DR   ZFIN; ZDB-GENE/){
           $buffer = $line .  $buffer; 
       }
       if($line=~ m/\/\/\n/){
           if($buffer=~ m/OS   Danio rerio/){
               print OUTPUT $buffer; 
           }
           $buffer = "" ;  # reset the buffer
       }
    }

    close(DAT) ; 
    close(OUTPUT) ; 
}

#=======================================================
#
#   Main
#


#set environment variables
$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";



chdir "<!--|ROOT_PATH|-->/server_apps/data_transfer/SWISS-PROT/";


#remove old files
 
system("rm -f ./ccnote/*");
system("rmdir ./ccnote");
system("rm -f *.ontology");
system("rm -f *2go");
system("rm -f prob*");
system("rm -f okfile");
system("rm -f pubmed_not_in_zfin");
system("rm -f *.unl");
system("rm -f *.txt");
system("rm -f *.dat");
system("mkdir ./ccnote");


&downloadGOtermFiles();


my $dbname = "<!--|DB_NAME|-->";
my $username = "";
my $password = "";

########################################################################################################
#
#  for FB case 8042 UniProt load: DR lines redundancy in the input file
#
########################################################################################################

### open a handle on the db
my $dbh = DBI->connect ("DBI:Informix:$dbname", $username, $password) 
    or die "Cannot connect to Informix database: $DBI::errstr\n";
    
my $cur;

$/ = "//\n";
open(PREDAT, "pre_zfin.dat") || die("Could not open pre_zfin.dat !");
my @blocks = <PREDAT>;
close(PREDAT);
    
open ZFINDAT, ">zfin.dat" || die ("Cannot open zfin.dat !");
open ZFINDATDELETED, ">zfinGeneDeleted.dat" || die ("Cannot open zfinGeneDeleted.dat !");
open ZFINGENES, ">zfinGenes.dat" || die ("Cannot open zfinGenes.dat !");
open DBG, ">debugfile.dat" || die ("Cannot open debugfile.dat !");
    
my $totalOnZfinDat = 0;
my $totalOnDeleted = 0;
my $ttt = 0;
my @lines = ();
my %toNewInput = ();
my %deletes = ();
my $ct = 0;
my %ZDBgeneIDgeneAbbrevs = ();
my $line;
my $lineKey = 0;
my @fields = ();
my $ZFINgeneId;
my $geneAbbrev;
my $block;
my $newLineNumber;
my $key;
foreach $block (@blocks) {
   $ttt++;
   if($block =~ m/OS   Danio rerio/) {
        @lines = split(/\n/, $block);
        %toNewInput = ();   
        %deletes = (); 
        $ct = 0;
        %ZDBgeneIDgeneAbbrevs = ();
        foreach $line (@lines) {
           if($line !~ m/CC   -------/ && 
             $line !~ m/CC   Copyrighted/ &&
             $line !~ m/CC   Distributed/)  {
                   
               ## add 10000 to pad so that the sorting would be right
               $lineKey = 10000 + $ct;
               $toNewInput{$lineKey} = $line; 
               $deletes{$lineKey} = 0;
                   
               if ($line =~ m/DR   ZFIN; ZDB-GENE-/) {
                   @fields = split(/;/, $line);
                   $ZFINgeneId = $fields[1];
                   $ZFINgeneId =~ s/^\s+//; 
                   $ZFINgeneId =~ s/\s+$//;                                          
                     
                   $geneAbbrev = $fields[2];
                   $geneAbbrev =~ s/^\s+//; 
                   $geneAbbrev =~ s/\s+$//;
                   $geneAbbrev =~ s/\.$//;
                          
                   if ($ttt < 1000)  {
                             print DBG "ZFINgeneId : $ZFINgeneId \t geneAbbrev : $geneAbbrev  \n";
                             
                             if (exists($ZDBgeneIDgeneAbbrevs{$ZFINgeneId})) {
                                                              print DBG "exists  $ZDBgeneIDgeneAbbrevs{$ZFINgeneId} \n";
                             }
   
                             if (!exists($ZDBgeneIDgeneAbbrevs{$ZFINgeneId})) {
                                                                 print DBG "Not exists  ZDBgeneIDgeneAbbrevs{ZFINgeneId} \n";
                             }
   
   
                   }
                          

                   if (!exists($ZDBgeneIDgeneAbbrevs{$ZFINgeneId})) {
                       $cur = $dbh->prepare('select mrkr_abbrev from marker where mrkr_zdb_id = ?;');

                       $cur->execute($ZFINgeneId);
                       my ($ZFINgeneAbbrev);
                       $cur->bind_columns(\$ZFINgeneAbbrev);
                       while ($cur->fetch()) {
                           $ZDBgeneIDgeneAbbrevs{$ZFINgeneId} = $ZFINgeneAbbrev;
                       }
                       $cur->finish(); 
                    ###       $line = $fields[0] . " " . ";" . $fields[1] . " " . ";" . $ZFINgeneAbbrev . ".";
                       $line =~ s/$geneAbbrev/$ZFINgeneAbbrev/g;
                       $toNewInput{$lineKey} = $line;
                   } else {
                       $deletes{$lineKey} = 1;
                   }

               }
                                                        
               $ct++;
           }
        }
            
        foreach $newLineNumber (sort keys %toNewInput) {
           if ($deletes{$newLineNumber} == 0) {
               print ZFINDAT "$newLineNumber ::: $toNewInput{$newLineNumber}\n";
               $totalOnZfinDat = $totalOnZfinDat + 1;  
           } else {
               print ZFINDATDELETED "$toNewInput{$newLineNumber}\n";
               $totalOnDeleted++;
           }
        }   
            
        foreach $key (sort keys %ZDBgeneIDgeneAbbrevs) {
               print ZFINGENES "$ZDBgeneIDgeneAbbrevs{$key}\n";
        } 
            
   }

}
    
close(ZFINDAT); 
close(ZFINDATDELETED); 
close(ZFINGENES); 
close(DBG);

$dbh->disconnect();  


open(PRE, "pre_zfin.dat") || die("Could not open pre_zfin.dat !");
my @blocksPRE = <PRE>;
close(PRE);
my $prezfin = 0;
my $totalprezfin = 0;
    
foreach $block (@blocksPRE){
   $totalprezfin++;
   if($block =~ m/OS   Danio rerio/) {
      $prezfin++;
   } 
}



open(ZF, "zfin.dat") || die("Could not open zfin.dat !");
my @blocksZF = <ZF>;
close(ZF);
my $zfin = 0;
my $totalzfin = 0;
foreach $block (@blocksZF){
   $totalzfin++;
   if($block =~ m/OS   Danio rerio/) {
      $zfin++;
   }  
}


$/ = "\n";

open(PRE, "pre_zfin.dat") || die("Could not open pre_zfin.dat !");
my @blocksPRE = <PRE>;
close(PRE);
my $prezfinGene = 0;
my $prezfinLines = 0;
foreach $block (@blocksPRE){
   $prezfinLines++;
   if($block =~ m/DR   ZFIN; ZDB-GENE-/) {
      $prezfinGene++;
   }
}



open(ZF, "zfin.dat") || die("Could not open zfin.dat !");
my @blocksZF = <ZF>;
close(ZF);
my $zfinGene = 0;
my $zfinLines = 0;
foreach $block (@blocksZF){
   $zfinLines++;
   if($block =~ m/DR   ZFIN; ZDB-GENE-/) {
      $zfinGene++;
   }  
}


print "totalOnZfinDat = $totalOnZfinDat\t totalOnDeleted = $totalOnDeleted\n\n\n";
print "prezfin = $prezfin\t";
print "totalprezfin = $totalprezfin\n";
print "zfin = $zfin\t";
print "totalzfin = $totalzfin\n";

print "\n\n\n";
print "prezfinGene = $prezfinGene\t";
print "prezfinLines = $prezfinLines\n";
print "zfinGene = $zfinGene\t";
print "zfinLines = $zfinLines\n";

open(REDGENERPT, ">redGeneReport.txt") || die("Could not open redGeneReport.txt !");
print REDGENERPT "totalOnZfinDat = $totalOnZfinDat\t totalOnDeleted = $totalOnDeleted\n\n\n";
print REDGENERPT "prezfin = $prezfin\t";
print REDGENERPT "totalprezfin = $totalprezfin\n";
print REDGENERPT "zfin = $zfin\t";
print REDGENERPT "totalzfin = $totalzfin\n";

print REDGENERPT "\n\n\n";
print REDGENERPT "prezfinGene = $prezfinGene\t";
print REDGENERPT "prezfinLines = $prezfinLines\n";
print REDGENERPT "zfinGene = $zfinGene\t";
print REDGENERPT "zfinLines = $zfinLines\n";


#--------------- Delete records from last SWISS-PROT loading-----
print "\n delete records source from last SWISS-PROT loading.\n";
system ("$ENV{'INFORMIXDIR'}/bin/dbaccess <!--|DB_NAME|--> sp_delete.sql >out 2>report.txt");
open F, "out" or die "Cannot open out file";
if (<F>) {
 
  &sendErrorReport("Failed to delete old records");
  exit;
}
close F;
 
# --------------- Check SWISS-PROT file --------------
# good records for loading are placed in "okfile"
print "\n sp_check.pl zfin.dat >checkreport.txt \n";
system ("<!--|ROOT_PATH|-->/server_apps/data_transfer/SWISS-PROT/sp_check.pl zfin.dat >checkreport.txt" );

my $count = 0;
my $retry = 1;
# wait till checking is finished
while( !( -e "okfile" && 
          -e "problemfile")) {

  $count++;
  if ($count > 10)
  {
    if ($retry) 
    {
      $count = 0;
      $retry = 0;
      print "retry sp_check.pl\n";
      system("sp_check.pl zfin.dat >checkreport.txt ");
    }
    else
    {
      &sendErrorReport("Failed to run sp_check.pl");
      exit;
    }
  }  
}

# concatenate all the sub problem files
system("cat prob1 prob2 prob3 prob4 prob5 prob6 prob7 prob8 > allproblems.txt");

&sendRunningResult();

exit;



