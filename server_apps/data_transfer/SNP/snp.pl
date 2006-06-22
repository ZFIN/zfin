#!/private/bin/perl

# FILE: snp.pl
# download, decompress and parse the compressed data files from dbSNP of NCBI
# input files: downloadListSNPncbi.txt, downloadListSNPncbiRS.txt
# output file: snpUnsorted

# set environment variables
$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";
$ENV{"DATABASE"}="<!--|DB_NAME|-->";

# remove old files 
system("/bin/rm -f *.xml") and die "can not rm old xml data file";
system("/bin/rm -f *.fas") and die "can not rm old .fas files";
system("/bin/rm -f *.flat") and die "can not rm old .flat files";
system("/bin/rm -f *.gz") and die "can not rm old .gz files";
system("/bin/rm -f *.bcp") and die "can not rm old .bcp files";
print "\nRemoving old data files is done.\n";

# open the text file containing the list of the xml files to be downloaded
open (INP, "downloadListSNPncbi.txt") || die "Can't open downloadListSNPncbi.txt : $!\n";
@lines=<INP>;
close(INP);

@downloadList = @parseListXML = ();
foreach $line (@lines) {
  $line =~ s/\s+//g;
  if ($line) {
    push @downloadList, $line; 
    $line =~ s/\.gz//g;
    push @parseListXML, $line;
  }
}

# download and unzip the xml files
$ct = 0;
foreach $d (@downloadList) {
  $url = "ftp://ftp.ncbi.nih.gov/snp/organisms/zebrafish_7955/XML/".$d;
  system("/local/bin/wget -q $url -O $d");
  system("/local/bin/gunzip $d");
  ++$ct; print "\n$ct files downloaded and decompressed.";
}
undef @lines; undef @downloadList;
print "\nDownloading and unzipping XML files of SNP data from NCBI is done\n";

# open the text file containing the list of the text files for rs to be downloaded
open (INP, "downloadListSNPncbiRS.txt") || die "Can't open downloadListSNPncbiRS.txt : $!\n";
@lines=<INP>;
close(INP);

@downloadList = @parseListRS = ();
foreach $line (@lines) {
  $line =~ s/\s+//g;
  if ($line) {
    push @downloadList, $line; 
    $line =~ s/\.gz//g;
    push @parseListRS, $line;
  }
}

# download and unzip text files for rs records
$ct = 0;
foreach $d (@downloadList) {
  $url = "ftp://ftp.ncbi.nih.gov/snp/organisms/zebrafish_7955/rs_fasta/".$d;
  system("/local/bin/wget -q $url -O $d");
  system("/local/bin/gunzip $d");
  ++$ct; print "\n$ct files downloaded and decompressed.";
}
undef @lines; undef @downloadList;
print "\nDownloading and unzipping fasta files of RS SNP data from NCBI is done\n";

# parse the xml files
%rsNums = %rsZSNPs = ();
%allNames = %snpNameXML = ();
$total = $uniqRsNums = $reduRsNum = $ctIdsName = $ctMultIdsName = 0;
foreach $p (@parseListXML) {
  open (INP, "$p") || die "Can't open $p : $!\n";
  $p =~ s/\.xml//g;
  $p =~ s/ds_//g;
  @lines=<INP>;
  foreach $line (@lines) {  # beginning of a rs record
    $line =~ s/>\n+//g;          
    if ($line =~ m/<Rs\s/) {
      @fields = split(/"/, $line);
      $rsId = "rs" . $fields[1];
      undef (@fields);
      $ct = 0;
      undef (@oriNames);
      @submitNames = (); 
      $total++;
      if (exists($rsNums{$rsId})) {
        $reduRsNum++;
      } else {
        $rsNums{$rsId} = 1;
        $uniqRsNums++;
      }
    } elsif ($line =~ m/<Ss\s/) { # ss record, could be multiple ss for 1 rs
      @fields = split(/locSnpId="/, $line);
      $ssName = $fields[1];
      undef (@fields);
      @fields = split(/"/, $ssName);
      $ssName = $fields[0];
      $rsZSNPs{$ssName} = $rsId;
      $snpNameXML{$ssName} = $ssName;
      if (exists($allNames{$rsId})) {
        $ctMultIdsName++;
        $allNames{$rsId} = $allNames{$rsId} . " ANDDDDD " . $ssName;
      } else {
        $ctIdsName++;
        $allNames{$rsId} = $ssName;
      }
      push @submitNames, $ssName;
      if ($ssName eq "ZSNP1952") {
        print "\nFound $ssName with $rsId\n";
      } elsif ($ssName eq "ZSNP1314") {
        print "\nFound $ssName with $rsId\n";
      } elsif ($ssName eq "ZSNP1455") {
        print "\nFound $ssName with $rsId\n";
      } elsif ($ssName eq "ZSNP1952") {
        print "\nFound $ssName with $rsId\n";
      } elsif ($ssName eq "ZSNP2035") {
        print "\nFound $ssName with $rsId\n";
      }
      undef (@fields);  
      $ct++;
    } elsif ($line =~ m/<\/Rs/) {  # end of a rs record
      undef $ssName; undef $rsId;
    }
  }
  undef @lines;
  close(INP);
}

print "\n$total rs numbers from xml files\n";
print "\n$uniqRsNums unique rs Ids and $reduRsNum redundancy\n";
print "\n$ctIdsName have unique submitted name and $ctMultIdsName redundancy\n";

# parse the text files for rs records
%seqs = %variations = %poses = ();
$uniqRsNums = $reduRsNum = 0;
%rsNums = ();
$/ = ">";
$total = 0;
foreach $p (@parseListRS) {
  open (INP, "$p") || die "Can't open $p : $!\n";
  @blocks=<INP>;
  $ct = 0;
  foreach $block (@blocks) {
    $ct++; next if ($ct == 1);
    @lines = split(/\n/, $block);
    foreach $line (@lines) {  
      next if ($line =~ m/#/);
      $line =~ s/\s+//g;
      @fields = split(/>/, $line);
      $line = $fields[0];
      undef (@fields);
      if ($line =~ m/dbSNP/) {
        $seq = "";
        @fields = split(/rs=/, $line);
        $rs = $fields[1]; 
        undef (@fields);
        @fields = split(/\|/, $rs);
        $rs = "rs" . $fields[0];
      if (exists($allrsNums{$rs})) {
        $reduRsNum++;
      } else {
        $allrsNums{$rs} = 1;
        $uniqRsNums++;
      }        
        $total++;
        undef (@fields);
        @fields = split(/alleles="/, $line);
	$variation = $fields[1];
	undef (@fields);
        @fields = split(/"/, $variation);
	$variation = $fields[0];
	undef (@fields);  
        $variations{$rs} = $variation;
        @fields = split(/pos=/, $line);
	$pos = $fields[1];
	undef (@fields);
	@fields = split(/\|/, $pos);
	$pos = $fields[0];
        undef (@fields);  
        $poses{$rs} = $pos;
      } else {
        $seq .= $line;
      }
    } 
    $seqs{$rs} = $seq;
    undef @lines;
  }
  undef @blocks;
  close(INP);
}

print "\n$total rs numbers from rs fasta files\n";
print "\n$uniqRsNums unique rs Ids and $reduRsNum redundancy\n";


# download and unzip the files containing SNP Submitter-Referenced accessions
$ftpDir = "ftp://ftp.ncbi.nih.gov/snp/organisms/zebrafish_7955/database/organism_data/";
$d = "SubSNP.bcp.gz";
$url = $ftpDir.$d;
system("/local/bin/wget -q $url -O $d");
system("/local/bin/gunzip $d");
print "\n$d is downloaded and unzipped\n";

$d = "SubSNPAcc.bcp.gz";
$url = $ftpDir.$d;
system("/local/bin/wget -q $url -O $d");
system("/local/bin/gunzip $d");
print "\n$d is downloaded and unzipped\n";

open (RESULT,  ">snpUnsorted") || die "Can't open: snpUnsorted $!\n";
$ct = 0;
%ctRSid = ();
foreach $z (sort keys %rsZSNPs) {
  $rs = $rsZSNPs{$z};
  $ctRSid{$rs} = 1;
  print RESULT "$rs\t$z\t$variations{$rs}\t$poses{$rs}\t$seqs{$rs}\n";
  $ct++;
}
close(RESULT);

print "\nParsing dbSNP data is partially done.\n$ct number of data output to snpUnsorted\n";

$ct = 0;
foreach $k (keys %ctRSid) {
  $ct++;
}
print "\ntotal of $ct unique RS Ids\n";
