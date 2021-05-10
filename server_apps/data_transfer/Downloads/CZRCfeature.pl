#!/opt/zfin/bin/perl
#
# CZRCfeature.pl
#
# For DLOAD-633


use DBI;

$dbname = "<!--|DB_NAME|-->";
$username = "";
$password = "";

### open a handle on the db
$dbh = DBI->connect ("DBI:Pg:dbname=$dbname;host=localhost", $username, $password)
    or die "Cannot connect to PostgreSQL database: $DBI::errstr\n";

$cur_feature = $dbh->prepare("select feature_zdb_id, feature_name, ftrtype_type_display, date(now()) from feature, feature_type where feature_type = ftrtype_name;");
$cur_feature->execute();
$cur_feature->bind_columns(\$id, \$name, \$type, \$downloadedTime);

$ctFeatures = 0;
%featureNames = ();   ## feature name, feature ZDB ID
%featureTypes = ();   ## feature type
%downloadedTimes = ();   
while ($cur_feature->fetch()) {
   $featureNames{$name} = $id;
   $featureTypes{$name} = $type;
   $downloadedTimes{$name} = $downloadedTime;
   $ctFeatures++;
}

$cur_feature->finish();

%affectedGeneAbbrevs = ();   # affected gene abrreviation(s)
%affectedGeneIds = ();       # affected gene ID(s)

%featureAliases = ();   # synonym(s)

%featureProtocols = ();  # protocol

%featureComments = ();   # notes

%featureLabNames = ();       # lab of origin (name)
%featureLabIds = ();         # lab of origin (ID)

%featureLocations = ();      # chromosome location

%featureConstructNames = ();  # construct name(s)
%featureConstructIds = ();    # construct ID(s)


$cur_source = $dbh->prepare("select zdb_id, name from lab union select zdb_id, name from company;");
$cur_source->execute();
$cur_source->bind_columns(\$srcid, \$srcname);

%srcs = ();
while ($cur_source->fetch()) {
   $srcs{$srcid} = $srcname;
}

$cur_source->finish();

foreach $featureName (sort keys %featureNames) {
  $featureID = $featureNames{$featureName};
  
  $cur_feature_relations = $dbh->prepare_cached("select feature_name, mrkr_abbrev, mrkr_zdb_id 
                                                   from feature, feature_marker_relationship, feature_marker_relationship_type, marker 
                                                  where feature_zdb_id = ? 
                                                    and feature_zdb_id = fmrel_ftr_zdb_id
                                                    and fmrel_type = fmreltype_name
                                                    and fmreltype_produces_affected_marker = 'f'
                                                    and fmrel_mrkr_zdb_id = mrkr_zdb_id
                                                    and mrkr_zdb_id like 'ZDB-GENE%'
                                                  order by mrkr_abbrev;");
  $cur_feature_relations->execute($featureID);
  $cur_feature_relations->bind_columns(\$name, \$affectedGeneAbbrev, \$affectedGeneId);

  $ctAffectedGenes = 0;
  $affectedGeneAbbrevsString = "undefined";
  $affectedGeneIdsString = "undefined";
  while ($cur_feature_relations->fetch()) {
    if ($affectedGeneAbbrevsString eq "undefined") {               
       $affectedGeneAbbrevsString = $affectedGeneAbbrev;
    } else {
       $affectedGeneAbbrevsString = $affectedGeneAbbrevsString . ', ' . $affectedGeneAbbrev;      
    }
    if ($affectedGeneIdsString eq "undefined") {                   
       $affectedGeneIdsString = $affectedGeneId;
    } else {
       $affectedGeneIdsString = $affectedGeneIdsString . ', ' . $affectedGeneId;      
    }
    $ctAffectedGenes++;
  }
  
  $cur_feature_relations->finish();

  if ($ctAffectedGenes == 0) {
     $cur_feature_known_insertion = $dbh->prepare_cached("select feature_name  
                                                            from feature 
                                                           where feature_zdb_id = ?
                                                             and feature_known_insertion_site = 't';");   
     $cur_feature_known_insertion->execute($featureID);
     $cur_feature_known_insertion->bind_columns(\$name);
     $ctKnownInsertion = 0;
     while ($cur_feature_known_insertion->fetch()) {
       $ctKnownInsertion++;      
     }
     $cur_feature_known_insertion->finish();
     if ($ctKnownInsertion > 0) {
        $affectedGeneAbbrevs{$featureName} = "This feature is representative of one or more unknown insertion sites.";        
     } else {
        $affectedGeneAbbrevs{$featureName} = "";
     }
     $affectedGeneIds{$featureName} = "";
  } else {
     $affectedGeneAbbrevs{$featureName} = $affectedGeneAbbrevsString;
     $affectedGeneIds{$featureName} = $affectedGeneIdsString;
  }
  
  $cur_feature_alias = $dbh->prepare_cached("select feature_name, dalias_alias 
                                               from feature, data_alias 
                                              where feature_zdb_id = ? 
                                                and feature_zdb_id = dalias_data_zdb_id
                                           order by dalias_alias;");
  $cur_feature_alias->execute($featureID);
  $cur_feature_alias->bind_columns(\$name, \$alias);


  $ctAlias = 0;
  $aliasString = "undefined";
  while ($cur_feature_alias->fetch()) {
    if ($aliasString eq "undefined") {                
       $aliasString = $alias;
    } else {
       $aliasString = $aliasString . ', ' . $alias;      
    }
    $ctAlias++;
  }
  
  $aliasString = "" if $ctAlias == 0 || $aliasString eq "undefined";
  
  $featureAliases{$featureName} = $aliasString;
  
  $cur_feature_alias->finish();
  

  $cur_feature_protocol = $dbh->prepare_cached("select feature_name, featassay_mutagen, featassay_mutagee 
                                                  from feature, feature_assay 
                                                 where feature_zdb_id = ? 
                                                   and feature_zdb_id = featassay_feature_zdb_id;");
  $cur_feature_protocol->execute($featureID);
  $cur_feature_protocol->bind_columns(\$name, \$mutagen, \$mutagee);


  $ctAssay = 0;
  while ($cur_feature_protocol->fetch()) {
    $ctAssay++;
  }
  
  $cur_feature_protocol->finish();
   
  if ($ctAssay == 0 || $mutagen eq "not specified" || $mutagen eq "") { 
      $featureProtocols{$featureName} = "";
  } else {         
      $cur_feature_marker = $dbh->prepare_cached("select feature_name, mrkr_abbrev 
                                                from feature, feature_marker_relationship, marker 
                                                 where feature_zdb_id = ? 
                                                   and feature_zdb_id = fmrel_ftr_zdb_id
                                                   and fmrel_mrkr_zdb_id = mrkr_zdb_id
                                                   and mrkr_type in ('CRISPR', 'TALEN')
                                                   order by mrkr_abbrev;");
      $cur_feature_marker->execute($featureID);
      $cur_feature_marker->bind_columns(\$name, \$createdBy);

      $ctCreatedBy = 0;
      $createdByString = "undefined";
      while ($cur_feature_marker->fetch()) {
        if ($createdByString eq "undefined") {                
            $createdByString = $createdBy;
        } else {
            $createdByString = $createdByString . ', ' . $createdBy;      
        }        
        $ctCreatedBy++;
      }
      
      $createdByString = "" if $ctCreatedBy == 0 || $createdByString eq "undefined";
  
      $cur_feature_marker->finish();  

      if ($mutagee eq "not specified" || $mutagee eq "") {
          $featureProtocols{$featureName} = $mutagen . " " . $createdByString;
      } else {
          if ($ctCreatedBy > 0) {
             $featureProtocols{$featureName} = $mutagee . " treated with " . $createdByString;
          } else {
             $featureProtocols{$featureName} = $mutagee . " treated with " . $mutagen;
          }
      }
      
  }
  
  $cur_feature_comments = $dbh->prepare_cached("select feature_name, extnote_note 
                                                  from feature, external_note 
                                                 where feature_zdb_id = ? 
                                                   and feature_zdb_id = extnote_data_zdb_id;");
  $cur_feature_comments->execute($featureID);
  $cur_feature_comments->bind_columns(\$name, \$comment);


  $ctComments = 0;
  $commentString = "undefined";
  while ($cur_feature_comments->fetch()) {
    if ($commentString eq "undefined") {                
       $commentString = $comment;
    } else {
       $commentString = $commentString . '; ' . $comment;      
    }
    $ctComments++;
  }
  
  $commentString = "" if $ctComments == 0 || $commentString eq "undefined";
  
  $featureComments{$featureName} = $commentString;
  
  $cur_feature_comments->finish();  
  
  
  $cur_feature_labs = $dbh->prepare_cached("select feature_name, ids_source_zdb_id 
                                              from feature, int_data_source 
                                             where feature_zdb_id = ? 
                                               and feature_zdb_id = ids_data_zdb_id;");
  $cur_feature_labs->execute($featureID);
  $cur_feature_labs->bind_columns(\$name, \$labId);


  $ctLabs = 0;
  @labIDs = ();
  while ($cur_feature_labs->fetch()) {
    push @labIDs, $labId;
    $ctLabs++;
  }
  
  $cur_feature_labs->finish();
    
  if ($ctLabs == 0) {
     $featureLabNames{$featureName}  = "";  
     $featureLabIds{$featureName}  = "";
  } else {    
     %labNames = ();
     for $labID (@labIDs) {
       $labName = $srcs{$labID};
       $labNames{$labName} = $labID;    
     }
     $labNameString = "undefined";
     $labIdString = "undefined";
     for $labOfOriginName (sort keys %labNames) {
       if ($labNameString eq "undefined") {                
           $labNameString = $labOfOriginName;
           $labIdString = $labNames{$labOfOriginName};
       } else {
           $labNameString = $labNameString . ', ' . $labOfOriginName;
           $labIdString = $labIdString . ', ' . $labNames{$labOfOriginName};
       }    
     }     
     $featureLabNames{$featureName}  = $labNameString;  
     $featureLabIds{$featureName}  = $labIdString;     
     
  } 
  
  $cur_feature_location = $dbh->prepare_cached("select feature_name, sfclg_chromosome 
                                                  from feature, sequence_feature_chromosome_location_generated 
                                                 where feature_zdb_id = ? 
                                                   and feature_zdb_id = sfclg_data_zdb_id;");
  $cur_feature_location->execute($featureID);
  $cur_feature_location->bind_columns(\$name, \$location);


  $ctLocations = 0;
  $firstLocation = "undefined";
  $isAmbiguous = 0;
  while ($cur_feature_location->fetch()) {
    if ($firstLocation eq "undefined") {                
       $firstLocation = $location;
    } else {
       $isAmbiguous = 1 if $location ne $firstLocation;      
    }
    $ctLocations++;
  }
  
  if ($ctLocations == 0 || $firstLocation eq "undefined") {
      $featureLocations{$featureName} = "Unmapped";
  } else {
      if ($isAmbiguous > 0) {
         $featureLocations{$featureName} = "Ambiguous";
      } else {
         $featureLocations{$featureName} = "Chr: " . $firstLocation;
      }
  
  }
    
  $cur_feature_location->finish();   

  $cur_feature_constructs = $dbh->prepare_cached("select feature_name, fmrel_mrkr_zdb_id 
                                                    from feature, feature_marker_relationship 
                                                   where feature_zdb_id = ? 
                                                     and feature_zdb_id = fmrel_ftr_zdb_id
                                                     and fmrel_type in ('contains innocuous sequence feature', 'contains phenotypic sequence feature');");
  $cur_feature_constructs->execute($featureID);
  $cur_feature_constructs->bind_columns(\$name, \$constructId);

  $ctConstructs = 0;
  @constructIDs = ();
  while ($cur_feature_constructs->fetch()) {
    push @constructIDs, $constructId;
    $ctConstructs++;
  }
  
  if ($ctConstructs == 0) {
     $featureConstructNames{$featureName}  = "";  
     $featureConstructIds{$featureName}  = "";
  } else {
     %constructNames = ();
     for $id (@constructIDs) {
       $cur_construct = $dbh->prepare_cached("select mrkr_name from marker where mrkr_zdb_id = ?;");
       $cur_construct->execute($id);
       $cur_construct->bind_columns(\$cnstName); 
       while ($cur_construct->fetch()) {
         $constructNames{$cnstName} = $id;  
       }
       $cur_construct->finish();
     }
     $constructNameString = "undefined";
     $constructIdString = "undefined";
     for $constructName (sort keys %constructNames) {
       if ($constructNameString eq "undefined") {                
           $constructNameString = $constructName;
           $constructIdString = $constructNames{$constructName};
       } else {
           $constructNameString = $constructNameString . ', ' . $constructName;
           $constructIdString = $constructIdString . ', ' . $constructNames{$constructName};
       }    
     }
     
     $featureConstructNames{$featureName}  = $constructNameString;  
     $featureConstructIds{$featureName}  = $constructIdString;          
  }
    
  $cur_feature_constructs->finish(); 
  
}

$featureFileForCZRC = "<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/CZRCfeature.txt";
open (CZRCFEATURES, ">$featureFileForCZRC") || die "can not open $featureFileForCZRC: $!\n";

foreach $featureName (sort keys %featureNames) {
  print CZRCFEATURES "$featureNames{$featureName}|$featureName|$affectedGeneAbbrevs{$featureName}|$affectedGeneIds{$featureName}|";    
  print CZRCFEATURES "$featureAliases{$featureName}|$featureTypes{$featureName}|$featureProtocols{$featureName}|$featureComments{$featureName}|";
  print CZRCFEATURES "$featureLabNames{$featureName}|$featureLabIds{$featureName}|$featureLocations{$featureName}|";
  print CZRCFEATURES "$featureConstructNames{$featureName}|$featureConstructIds{$featureName}|$downloadedTimes{$featureName}\n";
}

close CZRCFEATURES;
  
exit;

