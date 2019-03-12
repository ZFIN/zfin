#!/private/bin/perl

# FILE: dbSNP.pl

# DESCRIPTION: a control script to download, parse and load dbSNP data into ZFIN
# EFFECT:      dbSNP data inserted into ZFIN snp_download and snp_download_attribution tables
#  also insert record_attribution records for Talbot SNP if they share common dbSNP rs id with Smith or Johson SNPs

use MIME::Lite;
use DBI;

### set environment variables
$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";
$ENV{"DATABASE"}="<!--|DB_NAME|-->";

print "\nRunning new SNP scripts ...\n\n";

$dir = "<!--|ROOT_PATH|-->/server_apps/data_transfer/SNP/";
chdir "$dir";
print "$dir\n";

system("/bin/date");

### remove old files 
system("/bin/rm -f *.report.txt");
system("/bin/rm -f ds*");
system("/bin/rm -f Sub*");

system("/bin/rm -f *.xml");
system("/bin/rm -f *.gz");
system("/bin/rm -f *.bcp");

print "Remove used files done\n";


### open the text file containing the list of the xml files to be downloaded
open (INP, "downloadListSNPncbi.txt") || die "Can't open downloadListSNPncbi.txt : $!\n";
@lines=<INP>;
close(INP);

open (REPORT,">success.report.txt");

@downloadList = @parseListXML = ();
foreach $line (@lines) {
  $line =~ s/\s+//g;
  if ($line) {
    push @downloadList, $line; 
    $line =~ s/\.gz//g;
    push @parseListXML, $line;
  }
}

### download and unzip the xml files
foreach $d (@downloadList) {
  $url = "ftp://ftp.ncbi.nlm.nih.gov/snp/organisms/archive/zebrafish_7955/XML/".$d;
  system("/local/bin/wget -q $url -O $d");
  system("/local/bin/gunzip $d");
}
undef @lines; undef @downloadList;
print "\nDownloaded and unzipped XML files of dbSNP data\n";

### download and unzip the files containing SNP Submitter-Referenced accessions
$ftpDir = "ftp://ftp.ncbi.nlm.nih.gov/snp/organisms/archive/zebrafish_7955/database/organism_data/";
$d = "SubSNP.bcp.gz";
$url = $ftpDir.$d;
system("/local/bin/wget -q $url -O $d");
system("/local/bin/gunzip $d");
print "\n$d downloaded and decompressed\n";

$d = "SubSNPAcc.bcp.gz";
$url = $ftpDir.$d;
system("/local/bin/wget -q $url -O $d");
system("/local/bin/gunzip $d");
print "\n$d downloaded and decompressed\n";

$dbname = "<!--|DB_NAME|-->";
$username = "";
$password = "";

### open a handle on the db
$dbh = DBI->connect ("DBI:Pg:dbname=$dbname;host=localhost", $username, $password)
    or die "Cannot connect to PostgreSQL database: $DBI::errstr\n";
  
$cur = $dbh->prepare("select distinct dblink_acc_num, dblink_linked_recid
                        from db_link
                       where substring(dblink_linked_recid from 1 for 8) in ('ZDB-BAC-', 'ZDB-PAC-', 'ZDB-FOSM');");

$cur->execute();

$cur->bind_columns(\$acc,\$markerZDBid);
    
### get all ZFIN BAC/PAC/fosmid marker_zdb_id and their accession 
%markerZDBids = ();
$ctMarkers = 0;
while ($cur->fetch) {
  $markerZDBids{$acc} = $markerZDBid;
  $ctMarkers++;
}

$cur->finish();

print "\n ctMarkers ----- $ctMarkers \n";

$curDeleteSnpDownloadAttr = $dbh->prepare('delete from snp_download_attribution;');
$curDeleteSnpDownloadAttr->execute();
$curDeleteSnpDownloadAttr->finish();

$curDeleteSnpDownload = $dbh->prepare('delete from snp_download;');
$curDeleteSnpDownload->execute();
$curDeleteSnpDownload->finish();

$cur = $dbh->prepare('select snpd_rs_acc_num, snpd_mrkr_zdb_id
                        from snp_download;');

$cur->execute();

$cur->bind_columns(\$rsZFIN,\$mrkrZFIN);

### get current records in snp_download table    
%rsAndMrkrIDs = ();
$ct = 0;
while ($cur->fetch) {
  $rsAndMrkrIDs{"$rsZFIN"."$mrkrZFIN"} = 1;
  $ct++;
}

$cur->finish();

print "Number of rows in snp_download: $ct\n";

$cur = $dbh->prepare('select snpdattr_snpd_pk_id, snpdattr_pub_zdb_id
                        from snp_download_attribution;');

$cur->execute;

$cur->bind_columns(\$pk,\$pubIdZFIN);

### get current records in snp_download_attribution table     
%pkAndPubIDs = ();
$ct = 0;
while ($cur->fetch) {
  $pkAndPubIDs{"$pk"."$pubIdZFIN"} = 1;
  $ct++;
}

$cur->finish();

print "Number of rows in snp_download_attribution: $ct\n\n";

### clean up mapped_marker table
## $cur = $dbh->prepare("delete from mapped_marker
#                            where marker_type = "SNP"
#                              and marker_id in (select mrel_mrkr_2_zdb_id
#                                                  from marker_relationship
#                                                 where mrel_type = 'contains polymorphism'
#                                                   and mrel_mrkr_2_zdb_id like = 'ZDB-SNP-%');
#                       ");
#  $cur->execute;

### the datafile sent from J. Smith, which contains SNP name, clone name, etc.
open (JSMITH, "/research/zprod/data/JSmithSNPCloneInfo.txt") || die "Cannot open /research/zprod/data/JSmithSNPCloneInfo.txt : $!\n";
@lines=<JSMITH>;
close(JSMITH);
%cloneNames = (); 
$ct = 0;
foreach $line (@lines) {
  $ct++;
  next if $ct == 1;
  if ($line) {
    chop($line);
    undef (@fields);
    @fields = split(/\s+/, $line); 
    $zsnp = uc $fields[0];
    $cloneName = $fields[2];
    $cloneNames{$zsnp} = $cloneName;
  }
}
undef @lines;

print "Number of records in the datafile from J. Smith: $ct\n";

open (ACC1, "SubSNP.bcp") || die "Cannot open SubSNP.bcp : $!\n";
@lines=<ACC1>;
close(ACC1);
%corrspZSNPs = (); 
$ct = 0;
foreach $line (@lines) {
  if ($line) {
    undef (@fields);
    @fields = split(/\s+/, $line); 
    $corrsp = $fields[0];
    $ZSNP = uc $fields[3];
    $corrspZSNPs{$corrsp} = $ZSNP;
    $ct++;
  }
}
print "\nNumber of rows from SubSNP.bcp: $ct\n";

open (ACC2, "SubSNPAcc.bcp") || die "Can't open SubSNPAcc.bcp : $!\n";
@lines=<ACC2>;
close(ACC2);
open (SNPNOTEST,  ">snpNotEST.txt") || die "Can't open: snpNotEST.txt $!\n";
$ct = $ct2 = $ct3 = $ct4 = 0;
%dbsnpAccs = ();
foreach $line (@lines) {
  if ($line) {
    undef (@fields);
    @fields = split(/\s+/, $line); 
    $corrsp = $fields[0];
    $accnum = $fields[2];
    if ( exists($corrspZSNPs{$corrsp}) ) {
      $ZSNP = $corrspZSNPs{$corrsp};
      if ($accnum && $accnum ne "UNKNOWNACC") {
        $dbsnpAccs{$ZSNP} = $accnum;
        $ct++;
      } else {
        $ct2++;
      }
    } else {
      $ct3++;
    }
    $ct4++;
  }
}
print "\nNumber of ZSNPs found in SubSNPAcc.bcp that are also in SubSNP.bcp: $ct\n";
print "\nNumber of UNKNOWNACC: $ct2\n";
print "\nNumber of ZSNPs found in SubSNP.bcp that are not in SubSNPAcc.bcp: $ct3\n";
print "\nNumber of rows from SubSNPAcc.bcp: $ct4\n";

open (OUTPUT1,  ">forSNPDtable.unl") || die "Can't open: forSNPDtable.unl $!\n";
open (OUTPUT2,  ">outOfZFINSmith.unl") || die "Can't open: outOfZFIN.unl $!\n";  
# open (OUTPUT3,  ">nonNCBI.unl") || die "Can't open: nonNCBI.unl $!\n";        # mostly Johson SNPs
open (OUTPUT5,  ">nonZFIN.unl") || die "Can't open: nonZFIN.unl $!\n";

### parse the xml files and do output for loading snp_download table
$ctNew = $nonNCBIct = $ctNoZFINmrkr = $ctOutSmith = $ctOutJohson = $ctOutTalbot = $ctSS = $ctRedn1 = $ctElse = 0;
%outZFINacc = ();
foreach $p (@parseListXML) {
  open (INP, "$p") || next;
  @lines=<INP>;
  close(INP);
  foreach $line (@lines) {  
    $line =~ s/>\n+//g;          
    if ($line =~ m/<Rs\s/) {  # beginning of a rs record
      undef (@fields);
      @fields = split(/"/, $line);
      $rsId = "rs" . $fields[1];
      undef (@fields);  
    } elsif ($line =~ m/<Ss\s/) { # beginning of a ss record, could be multiple ss for 1 rs
      $ctSS++;   
      
      undef (@fields);
      @fields = split(/locSnpId="/, $line);
      $rightpart = $fields[1];
      undef (@fields);
      @fields = split(/"/, $rightpart);
      $loc = uc $fields[0];

      undef (@fields);
      @fields = split(/handle="/, $line);           
      $rightpart = $fields[1];
      undef (@fields);
      @fields = split(/"/, $rightpart);        
      $lab = $fields[0];

      if ($loc && exists($dbsnpAccs{$loc})) {  ## there is associated acc in NCBI
        $accNum = $dbsnpAccs{$loc};
        if (exists($markerZDBids{$accNum})) {  ## there is associated ZFIN mrkr ID
          $markerZDBid = $markerZDBids{$accNum};   
          $gotoZFIN = 1;
        } else {   # special cases
          $gotoZFIN = 1;
          if ($cloneNames{$loc} eq "ZH356B10") {
            $markerZDBid = "ZDB-BAC-050218-605"; 
          } elsif ($cloneNames{$loc} eq "ZK92F12A") {
            $markerZDBid = "ZDB-BAC-070608-386";   
          } elsif ($cloneNames{$loc} eq "ZK251O17B") {
            $markerZDBid = "ZDB-BAC-050218-806";   
          } elsif ($cloneNames{$loc} eq "ZK92F12B") {
            $markerZDBid = "ZDB-BAC-070608-386";   
          } elsif ($cloneNames{$loc} eq "ZK148M7A" || $cloneNames{$loc} eq "ZK148M7B") {
            $markerZDBid = "ZDB-BAC-060503-1327";   
          } else {
            $gotoZFIN = 0;
            print OUTPUT5 "$rsId|$loc|$accNum\n";
            $outZFINacc{$accNum} = "$loc    $rsId";
            $ctNoZFINmrkr++;
          }
        }
      } elsif($loc && !exists($dbsnpAccs{$loc})) {  ## there is associated acc in NCBI, mostly Johson SNPs     
      # print OUTPUT3 "$rsId|$loc\n";
        $nonNCBIct++;
        $gotoZFIN = 0;
      } else {
        $ctElse++;
        $gotoZFIN = 0;
      }
     
      if ( $gotoZFIN == 1 ) {
        $ZFINsnpd = "$rsId"."$markerZDBid";        
        if ( !exists($rsAndMrkrIDs{$ZFINsnpd}) ) {
          $rsAndMrkrIDs{$ZFINsnpd} = 1;
          print OUTPUT1 "$rsId|$markerZDBid\n";
          $ctNew++;
        } else {
          $ctRedn1++;
        }
      } elsif ($gotoZFIN == 0 && $lab eq "VU_JRS") {
        print OUTPUT2 "$rsId|$loc\n";
        $ctOutSmith++;
      } elsif ($gotoZFIN == 0 && $lab eq "FGG_NIOB") {
        $ctOutJohson++;
      } elsif ($gotoZFIN == 0 && $lab eq "WTALBOT") {
        $ctOutTalbot++;
      }
    }
  }
  undef (@lines);
}

close(OUTPUT1);
close(OUTPUT2);
# close(OUTPUT3);
close(OUTPUT5);

print "\n ctNew: $ctNew \t ctSS: $ctSS \nctRedn1: $ctRedn1 \t nonNCBIct: $nonNCBIct \t ctNoZFINmrkr: $ctNoZFINmrkr \n\n";
print "\n ctElse: $ctElse\n\n";
print "\n ctOutSmith: $ctOutSmith \t ctOutJohson: $ctOutJohson\t ctOutTalbot: $ctOutTalbot\n\n";

if ($ctNew > 0) {
  system( "psql -d <!--|DB_NAME|--> -a -f loadNewSNPs.sql > newSNPs.report.txt" ) and &emailError("failed to load snp_download table");
  &createReport("has added $ctNew new records into snp_download table");
}

$cur = $dbh->prepare('select snpd_pk_id,snpd_rs_acc_num,snpd_mrkr_zdb_id
                        from snp_download;');

$cur->execute();

$cur->bind_columns(\$snpdPk,\$snpdRs,\$snpdMrkr);
    
%pksPerRs = ();
while ($cur->fetch) {
  if ( !exists($pksPerRs{$snpdRs}) ) {
    $pksPerRs{$snpdRs} = $snpdPk;
  } else {
    $pksPerRs{$snpdRs} = $pksPerRs{$snpdRs}.";".$snpdPk;
  }
}

$cur->finish();

$cur = $dbh->prepare("select distinct mrkr_name, mrkr_zdb_id, recattrib_source_zdb_id 
                        from marker, record_attribution 
                       where recattrib_data_zdb_id = mrkr_zdb_id and mrkr_type = 'SNP';");
                       
$cur->execute();

$cur->bind_columns(\$snpRsName,\$snpMrkr,\$snpPub);
    
%snpRsPubs = %snpMrkrs = ();
while ($cur->fetch) {
  $snpRsPubs{"$snpRsName"."$snpPub"} = 1;
  $snpMrkrs{$snpRsName} = $snpMrkr;
}

$cur->finish();

$dbh->disconnect();   

open (OUTPUT4,  ">forsnpdattrtable.unl") || die "Can't open: forsnpdattrtable.unl $!\n";
open (OUTPUT6,  ">forrecordattrtable.unl") || die "Can't open: forrecordattrtable.unl $!\n";

### parse the xml files and do output for loading snp_download_attribution table
$ctNew2 = $ctNew3 = 0;
$ctJohson = $ctSmith = $ctTalbot = $ctRedn2 = $ctRedn3 = $ctSS = 0;
%outZFINacc = ();
foreach $p (@parseListXML) {
  open (INP, "$p") || next;
  @lines=<INP>;
  close(INP);
  foreach $line (@lines) {  
    $line =~ s/>\n+//g;          
    if ($line =~ m/<Rs\s/) {  # beginning of a rs record
      undef (@fields);
      @fields = split(/"/, $line);
      $rsId = "rs" . $fields[1];
      undef (@fields);  
    } elsif ($line =~ m/<Ss\s/) { # beginning of a ss record, could be multiple ss for 1 rs
      $ctSS++;  
      
      undef (@fields);
      @fields = split(/handle="/, $line);           
      $rightpart = $fields[1];
      undef (@fields);
      @fields = split(/"/, $rightpart);        
      $lab = $fields[0];
      if ($lab eq "FGG_NIOB") {
        $pub = "ZDB-PUB-060323-11";
        $ctJohson++;
      } elsif ($lab eq "VU_JRS") {
        $pub = "ZDB-PUB-070427-10";
        $ctSmith++;
      } elsif ($lab eq "WTALBOT") {
        $pub = "ZDB-PUB-021213-2";
        $ctTalbot++;
      }
      
      # add attribution to output file for snp_download_attribution if it is not Johson SNP
      if ( $lab ne "FGG_NIOB" && exists($pksPerRs{$rsId}) ) {
        undef (@pks);
        @pks = split(/;/, $pksPerRs{$rsId});
        foreach $p (@pks) {
          $PkPub = "$p"."$pub";  
          if ( !exists($pkAndPubIDs{$PkPub}) ) {
            $pkAndPubIDs{$PkPub} = 1;
            print OUTPUT4 "$p|$pub\n";
            $ctNew2++;
          } else {
            $ctRedn2++;
          }
        }
      }
      
      # add attribution to output file for record_attribution if it is Talbot SNP
      if ( exists($snpMrkrs{$rsId}) ) {
        $rsPub = "$rsId"."$pub";  
        if ( !exists($snpRsPubs{$rsPub}) ) {
          $snpRsPubs{$rsPub} = 1;
          $snpMrkrId = $snpMrkrs{$rsId};
          print OUTPUT6 "$snpMrkrId|$pub\n";
          $ctNew3++;
        } else {
          $ctRedn3++;
        }
      } 
      
    }
    
  }
  undef (@lines);
}

close(OUTPUT4);
close(OUTPUT6);

print "\n ctNew2: $ctNew2 \t   ctRedn2: $ctRedn2 \t ctNew3: $ctNew3 \t   ctRedn3: $ctRedn3 \t $ctSS: $ctSS \n\n";
print "\n ctJohson = $ctJohson \t ctSmith = $ctSmith \t ctTalbot: $ctTalbot \n\n";

if ($ctNew2 > 0) {
  system( "psql -d <!--|DB_NAME|--> -a -f loadNewSNPAttrs.sql > newSNPattribution.report.txt" ) and &emailError("failed to load snp_download_attribution table");
  &createReport("has added $ctNew2 new records into snp_download_attribution table");
}

if ($ctNew3 > 0) {
  system( "psql -d <!--|DB_NAME|--> -a -f addTalbotSNPAttr.sql > talbotAttribution.report.txt" ) and &emailError("failed to insert record_attribution table");
  &createReport("has added $ctNew3 new records into record_attribution table for Talbot SNPs");
} 

close REPORT;

system("/bin/date");

print "Done\n";

exit;

#=========================================
# emailError
#
# INPUT:
#    string ::  error message
# OUTPUT:
#    none
# EFFECT:
#    error message is sent to xshao@zfin.org.
#

sub emailError($)
  {
    
    open(MAIL, "| /usr/lib/sendmail -t -oi" || die "Cannot open mailprog");
    print MAIL "To: xshao\@zfin.org\n";
    print MAIL "Subject: dbSNP.pl $_[0]\n";
    print MAIL "Error:\n";                  
    print MAIL "$_[0]";
    close MAIL;       
    exit;            
  }   

sub createReport($)
  {
      print REPORT "$_[0]\n";
  }

