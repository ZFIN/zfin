#!/private/bin/perl

# updateMrkrGoEvdTime.pl
# download, decompress and pares the following GAF data files:

# ftp://ftp.ebi.ac.uk/pub/databases/GO/goa/ZEBRAFISH/gene_association.goa_zebrafish.gz
# http://www.geneontology.org/gene-associations/submission/paint/pre-submission/gene_association.paint_zfin.gz
# http://build.berkeleybop.org/view/GAF/job/gaf-check-zfin/lastSuccessfulBuild/artifact/gene_association.zfin.inf.gaf 

# process and update the time fields of marker_go_term_evidence table according to input
# 

use DBI;
use Time::localtime;

use lib "<!--|ROOT_PATH|-->/server_apps/";
use ZFINPerlModules;

$sqlUpdateGAFtime = "update marker_go_term_evidence 
                        set mrkrgoev_date_modified = ?
                      where mrkrgoev_zdb_id = ?;";
                                            
$sqlUpdateGAFtime2 = "update marker_go_term_evidence 
                         set mrkrgoev_date_entered = ?
                       where mrkrgoev_zdb_id = ?;";

$organizationCodeFP = 2;
$organizationCodePAINT = 4;
$organizationCodeGOA = 3;

%alreadyUpdated = ();

# set environment variables

$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";

$dbname = "<!--|DB_NAME|-->";

## remove old files
system("/bin/rm -f gafTimeUpdateList");
system("/bin/rm -f gene_association.zfin.inf.gaf");
system("/bin/rm -f gene_association.goa_zebrafish");
system("/bin/rm -f gene_association.paint_zfin");

open LOG, '>', "gafTimeUpdateList" or die "can not open gafTimeUpdateList: $! \n";

## FP Inference GAF
system("/local/bin/wget http://build.berkeleybop.org/view/GAF/job/gaf-check-zfin/lastSuccessfulBuild/artifact/gene_association.zfin.inf.gaf");

## GOA GAF
system("/local/bin/wget ftp://ftp.ebi.ac.uk/pub/databases/GO/goa/ZEBRAFISH/gene_association.goa_zebrafish.gz");
system("/local/bin/gunzip gene_association.goa_zebrafish.gz");
system("/bin/cat gene_association.goa_zebrafish >> gene_association.zfin.inf.gaf");

## PAINT GAF
system("/local/bin/wget http://www.geneontology.org/gene-associations/submission/paint/pre-submission/gene_association.paint_zfin.gz");
system("/local/bin/gunzip gene_association.paint_zfin.gz");
system("/bin/cat gene_association.paint_zfin >> gene_association.zfin.inf.gaf");

$username = "";
$password = "";

### open a handle on the db
$dbh = DBI->connect ("DBI:Informix:$dbname", $username, $password) 
    or die "Cannot connect to Informix database: $DBI::errstr\n";
    
$sqlTermIdLookup = 'select term_ont_id, term_zdb_id 
                      from term 
                     where term_ontology_id = "3"
                       and term_is_obsolete = "f"
                       and term_is_secondary = "f";';

$cur = $dbh->prepare("$sqlTermIdLookup");
$cur->execute();
$cur->bind_columns(\$ontId,\$termId);

$ctGoTermIds = 0;
%goTermIds = ();
while ($cur->fetch()) {
   $goTermIds{$ontId} = $termId;
   $ctGoTermIds++;
}

%zdbPubIds = ("GO_REF:0000019" => "ZDB-PUB-110127-1",
              "GO_REF:0000020" => "ZDB-PUB-110127-2",
              "GO_REF:0000023" => "ZDB-PUB-110127-3",  
              "GO_REF:0000039" => "ZDB-PUB-120306-2",  
              "GO_REF:0000040" => "ZDB-PUB-120306-4",  
              "GO_REF:0000041" => "ZDB-PUB-130131-1", 
              "GO_REF:0000024" => "ZDB-PUB-110105-1",  
              "GO_REF:0000033" => "ZDB-PUB-110330-1" 
);

$paintGafPub = "ZDB-PUB-110330-1";

$sqlZdbPubIdLookup = 'select accession_no, zdb_id 
                        from publication;';

$cur = $dbh->prepare("$sqlZdbPubIdLookup");
$cur->execute();
$cur->bind_columns(\$pmid,\$pubZdbId);

%pmids = ();
while ($cur->fetch()) {
   $pmids{$pmid} = $pubZdbId;
}    

open (GAF, "gene_association.zfin.inf.gaf") ||  die "Cannot open gene_association.zfin.inf.gaf : $!\n";

@gafLines = <GAF>;

$ctLines = $ctGaf = $ctNoWith = $ctTotalGAFprocessed = $ctRecordTimeUpdated = $ctRecordTimeUnchanged = 0;

$ctMultipleWith = 0;

$ctPMID = $ctNotFoundPMIDs = $ctGOpubs = $ctNoGoPubs = $noPubs = $ctPAINTpubs = 0;   

$cur = $dbh->prepare($sqlTermIdLookup);

$ctIEAolderThan1Year = 0;

foreach $gaf (@gafLines) {

  $ctLines++;

  next if $gaf =~ m/\!/;  ## ditch documentation lines
  
  chop($gaf);
  
  @fields = split("\t", $gaf); 
  
  $DB = $fields[0];
    
  $id = $fields[1];  
  
  $qualifier = $fields[3];   ## optional
  
  $qualifier = "noQualifier" if not defined $qualifier;
  $qualifier = "noQualifier" if $qualifier eq "";
  
  $GO = $fields[4];
  next if $GO eq "GO:0005623";        ## cell
  next if !exists($goTermIds{$GO});   ## do nothing if no ZFIN term id found for the GO term Id
  
  $termZdbId = $goTermIds{$GO};  
  
  $reference = $fields[5];
  
  if ($reference =~ m/PAINT\_REF/) {
     $zdbPubId = $paintGafPub;
     $ctPAINTpubs++;
     
  } else {  
      if ($reference =~ m/(ZDB\-PUB\-\d+\-\d+)/) {
          $zdbPubId = $1;
      } elsif ($reference =~ m/PMID:(\d+)/) {
          $ctPMID++;
          if (exists($pmids{$1})) {
              $zdbPubId = $pmids{$1};
          } else {
              $ctNotFoundPMIDs++;
              next;
          }
      } elsif ($reference =~ m/(GO\_REF:\d+)/) {  
          $ctGOpubs++;
          if (exists($zdbPubIds{$1})) {
              $zdbPubId = $zdbPubIds{$1};
          } else {
              $ctNoGoPubs++;
              next;
          }
      } else {
          $noPubs++;
          next;
      }  
  }
  
  $evidenceCode = $fields[6];
  
  $withField = $fields[7];        # Cardinality of the with field could be 0 or 1 or more

  $isThereWithFields = 1;  
  
  $isThereWithFields = 0 if not defined $withField;
  $isThereWithFields = 0 if $withField eq "";
      
  $taxId = $fields[12];
  
  $date = $fields[13];
  
  $assignedBy = $fields[14];
    
  $assignedBy = $assignedBy . "KB" if $assignedBy eq "UniProt";
              
  next if $taxId !~ m/7955/ || $assignedBy =~ m/ZFIN/; 
  
  $ctGaf++;  
  
  if ($assignedBy eq "GOC" && $DB eq "ZFIN") {
       $organizationCode = $organizationCodeFP;
  } elsif ($reference =~ m/PAINT\_REF/) {
       $organizationCode = $organizationCodePAINT;
  } else {
       $organizationCode = $organizationCodeGOA;
  }
  
  if ($isThereWithFields == 0) {
     updateDateGAFtime($organizationCode, "noWith", $termZdbId, $id, $evidenceCode, $zdbPubId, $date, $assignedBy, $DB, $GO, $qualifier);     
     updateDateGAFtime($organizationCode, "noWith", $termZdbId, $id, $evidenceCode, $zdbPubId, $date, "ENSEMBL", $DB, $GO, $qualifier) if $assignedBy eq "Ensembl";
  } else { 
     if ($withField =~ m/\|/) {
         @withFields = split(/\|/, $withField);        
         foreach $with (@withFields) {
           updateDateGAFtime($organizationCode, $with, $termZdbId, $id, $evidenceCode, $zdbPubId, $date, $assignedBy, $DB, $GO, $qualifier);
           updateDateGAFtime($organizationCode, $with, $termZdbId, $id, $evidenceCode, $zdbPubId, $date, "ENSEMBL", $DB, $GO, $qualifier) if $assignedBy eq "Ensembl";
         }
      } else {
         updateDateGAFtime($organizationCode, $withField, $termZdbId, $id, $evidenceCode, $zdbPubId, $date, $assignedBy, $DB, $GO, $qualifier);
         updateDateGAFtime($organizationCode, $withField, $termZdbId, $id, $evidenceCode, $zdbPubId, $date, "ENSEMBL", $DB, $GO, $qualifier) if $assignedBy eq "Ensembl";
     } 
  }
      
  undef $DB;
  undef $id;
  undef $GO;
  undef $termZdbId;
  undef $zdbPubId;
  undef $evidenceCode;
  undef $with;
  undef $date;
  undef $assignedBy;
  undef $withFields;
}

print "\nctLines = $ctLines\nctGaf = $ctGaf\nctMultipleWith = $ctMultipleWith \n\n";

print "\n Summary: $ctTotalGAFprocessed  \n updated: $ctRecordTimeUpdated \t not changed: $ctRecordTimeUnchanged \n ctNoWith = $ctNoWith \n ctPAINTpubs = $ctPAINTpubs \n\n";

print "ctPMID = $ctPMID \t ctNotFoundPMIDs = $ctNotFoundPMIDs \t ctGOpubs = $ctGOpubs \t ctNoGoPubs = $ctNoGoPubs \t noPubs = $noPubs\n ctIEAolderThan1Year = $ctIEAolderThan1Year \n\n"; 
 
close(GAF);

$cur->finish(); 

$dbh->disconnect(); 

close(LOG);

$subject = "Auto from $dbname: " . "updateMrkrGoEvdTime.pl";
ZFINPerlModules->sendMailWithAttachedReport("<!--|GO_EMAIL_CURATOR|-->","$subject","gafTimeUpdateList");

exit;

sub compareDates {
  my ($dateValue1, $dateString2FromDB) = @_;
  
  @dateFileds = split("\s+", $dateString2FromDB);
  $dateValue2 = $dateFileds[0];
  $dateValue2 =~ s/\-//g;

  $result = $dateValue1 - $dateValue2;

  return $result;

}

sub makeDateToUpdate {
  my $dateString = $_[0];
  $dateYear = ZFINPerlModules->getYear($dateString);
  $dateMonth = ZFINPerlModules->getMonth($dateString);
  $dateDay = ZFINPerlModules->getDay($dateString);
  $dateToUpdate = $dateYear . '-' . $dateMonth . '-' . $dateDay . ' ' . '00:00:00';
  
  return $dateToUpdate;
}

sub updateDateGAFtime {

    my $orgCode = $_[0];
    my $withString = $_[1];
    my $term = $_[2];
    my $objId = $_[3];
    my $evidence = $_[4];
    my $publication = $_[5];
    my $newDate = $_[6];
    my $createdBy = $_[7];
    my $db = $_[8];
    my $GOid = $_[9];
    my $qualifierString = $_[10];
    
    my $mrkrgoevdZdbId = " ";
    my $lastModDate;

    my $currentTime = localtime;
    my $currentYear = $currentTime->year;
    my $currentMonth = $currentTime->mon;
    my $currentDay = $currentTime->mday;
    my $today = ($currentYear + 1900)*10000 + ($currentMonth + 1)*100 + $currentDay;

    my $sqlGetZFINtimeFrom = "select mrkrgoev_zdb_id, mrkrgoev_date_modified 
                                from inference_group_member, marker_go_term_evidence ";
                                                    

    my $sqlGetZFINtimeFromNoWith = "select mrkrgoev_zdb_id, mrkrgoev_date_modified 
                                      from marker_go_term_evidence ";
                        
    my $sqlGetZFINtimeWhere;
    
    my $sqlGetZFINtimeWhereNoWith;
    
    my %mrkrGoTermEvdIds = ();
    
    if ($objId !~ m/ZDB/) {
       $sqlGetZFINtimeFrom = $sqlGetZFINtimeFrom . " , db_link ";
       
       $sqlGetZFINtimeFromNoWith = $sqlGetZFINtimeFromNoWith . " , db_link ";
       
       $sqlGetZFINtimeWhere = " where mrkrgoev_annotation_organization = ? 
                                  and infgrmem_mrkrgoev_zdb_id = mrkrgoev_zdb_id 
                                  and infgrmem_inferred_from = ? 
                                  and mrkrgoev_term_zdb_id = ?
                                  and mrkrgoev_evidence_code = ?
                                  and mrkrgoev_source_zdb_id = ?
                                  and mrkrgoev_annotation_organization_created_by = ? 
                                  and mrkrgoev_mrkr_zdb_id = dblink_linked_recid 
                                  and dblink_acc_num = ? ";  
                                  
       $sqlGetZFINtimeWhereNoWith = " where mrkrgoev_annotation_organization = ? 
                                        and mrkrgoev_term_zdb_id = ?
                                        and mrkrgoev_evidence_code = ?
                                        and mrkrgoev_source_zdb_id = ?
                                        and mrkrgoev_annotation_organization_created_by = ? 
                                        and mrkrgoev_mrkr_zdb_id = dblink_linked_recid 
                                        and dblink_acc_num = ? ";                                    
    } else {
       $sqlGetZFINtimeWhere = " where mrkrgoev_annotation_organization = ? 
                                  and infgrmem_mrkrgoev_zdb_id = mrkrgoev_zdb_id 
                                  and infgrmem_inferred_from = ?
                                  and mrkrgoev_term_zdb_id = ? 
                                  and mrkrgoev_evidence_code = ?
                                  and mrkrgoev_source_zdb_id = ?
                                  and mrkrgoev_annotation_organization_created_by = ? 
                                  and mrkrgoev_mrkr_zdb_id = ? ";    
                                  
        $sqlGetZFINtimeWhereNoWith = " where mrkrgoev_annotation_organization = ? 
                                         and mrkrgoev_term_zdb_id = ? 
                                         and mrkrgoev_evidence_code = ?
                                         and mrkrgoev_source_zdb_id = ?
                                         and mrkrgoev_annotation_organization_created_by = ? 
                                         and mrkrgoev_mrkr_zdb_id = ? ";                                  
    }
    
    my $sql;
    
    if ($withString eq "noWith") {
        $sql = $sqlGetZFINtimeFromNoWith . $sqlGetZFINtimeWhereNoWith; 
        $ctNoWith++;
    } else {
        $sql = $sqlGetZFINtimeFrom . $sqlGetZFINtimeWhere;
        $ctMultipleWith++;
    }
    
    if ($qualifierString ne "noQualifier") {
      $sql = $sql . " and mrkrgoev_gflag_name = ? ";
    }
    
    $cur = $dbh->prepare($sql);
    
    if ($withString eq "noWith") { 
        if ($qualifierString eq "noQualifier") {
             $cur->execute($orgCode, $term, $evidence, $publication, $createdBy, $objId);
        } else {
             $cur->execute($orgCode, $term, $evidence, $publication, $createdBy, $objId, $qualifierString);
        }
    } else {
        if ($qualifierString eq "noQualifier") {
             $cur->execute($orgCode, $withString, $term, $evidence, $publication, $createdBy, $objId);
        } else {
             $cur->execute($orgCode, $withString, $term, $evidence, $publication, $createdBy, $objId, $qualifierString);
        }
    }    

    $cur->bind_columns(\$mrkrgoevdZdbId,\$lastModDate);  
    while ($cur->fetch()) {
	$mrkrGoTermEvdIds{$mrkrgoevdZdbId} = $lastModDate;	
    }                  

    foreach $mrkrGoTermEvdId (keys %mrkrGoTermEvdIds) {
	$ctTotalGAFprocessed++;
	
	$dateStored = $mrkrGoTermEvdIds{$mrkrGoTermEvdId};
	
        if (!exists($alreadyUpdated{$mrkrGoTermEvdId}) && compareDates($newDate, $dateStored) > 0) {
               $updatedDate = makeDateToUpdate($newDate);
           
               if ($evidence eq "IEA" && compareDates($today, $dateStored) > 10000) {
                 $ctIEAolderThan1Year++;
                 print LOG "$mrkrgoevdZdbId\t$db\t$objId\t$GOid\t$term\t$publication\t$evidence\t$withString\t$createdBy\t$updatedDate\t$dateStored\n";
               }
           
               $ctRecordTimeUpdated++;
           
               $cur = $dbh->prepare($sqlUpdateGAFtime);
               $cur->execute($updatedDate, $mrkrgoevdZdbId);
           
               $cur = $dbh->prepare($sqlUpdateGAFtime2);
               $cur->execute($updatedDate, $mrkrgoevdZdbId);
           
               $alreadyUpdated{$mrkrgoevdZdbId} = $newDate;
           
         } else {
               $ctRecordTimeUnchanged++;
         }
    }  
        
    undef $mrkrgoevdZdbId;
    undef $lastModDate;
    undef $orgCode;
    undef $withString;
    undef $term;
    undef $objId;
    undef $evidence;
    undef $publication;
    undef $newDate;
    undef $createdBy;
    undef $db;
    undef $GOid; 
    undef %mrkrGoTermEvdIds;
}
