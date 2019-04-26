#!/private/bin/perl
#
# reportPubsForGeneAndFeature.pl
#
# For JENK-428


use DBI;

$dbname = "<!--|DB_NAME|-->";
$username = "";
$password = "";

system("/bin/rm -f feature_data_report.txt") if (-e "feature_data_report.txt");

### open a handle on the db
$dbh = DBI->connect ("DBI:Pg:dbname=$dbname;host=localhost", $username, $password)
    or die "Cannot connect to PostgreSQL database: $DBI::errstr\n";

$cur_feature = $dbh->prepare("select feature_abbrev, feature_zdb_id, feature_type from feature;");
$cur_feature->execute();
$cur_feature->bind_columns(\$abbrev, \$id, \$type);

$ctFeatures = 0;
%featureAbbrevs = ();
%featureTypes = ();
while ($cur_feature->fetch()) {
   $featureAbbrevs{$abbrev} = $id;
   $featureTypes{$abbrev} = $type;
   $ctFeatures++;
}

print "total number of features: $ctFeatures\n";

$cur_feature->finish();

$cur_feature_noassay = $dbh->prepare("select feature_abbrev, feature_zdb_id from feature where not exists(select 1 from feature_assay where featassay_feature_zdb_id = feature_zdb_id);");
$cur_feature_noassay->execute();
$cur_feature_noassay->bind_columns(\$abbrev, \$id);

$ctFeaturesNoAssay = 0;
%featureAbbrevsNoAssay = ();
while ($cur_feature_noassay->fetch()) {
   $featureAbbrevsNoAssay{$abbrev} = $id;
   $ctFeaturesNoAssay++;
}

print "total number of features with no assay: $ctFeaturesNoAssay\n";

$cur_feature_noassay->finish();

$cur_feature_assay = $dbh->prepare("select featassay_feature_zdb_id, featassay_mutagen, featassay_mutagee from feature_assay;");
$cur_feature_assay->execute();
$cur_feature_assay->bind_columns(\$id, \$mutagen, \$mutagee);

$ctFeaturesAssays = 0;
%featureMutagens = ();
%featureMutagees = ();
while ($cur_feature_assay->fetch()) {
   $featureMutagens{$id} = $mutagen;
   $featureMutagees{$id} = $mutagee;
   $ctFeaturesAssays++;
}

print "total number of feature_assay records: $ctFeaturesAssays\n";

$cur_feature_assay->finish();

$cur_feature_nolab = $dbh->prepare("select feature_abbrev, feature_zdb_id from feature where not exists(select 1 from int_data_source where ids_data_zdb_id = feature_zdb_id);");
$cur_feature_nolab->execute();
$cur_feature_nolab->bind_columns(\$abbrev, \$id);

$ctFeaturesNoLab = 0;
%featureAbbrevsNoLab = ();
while ($cur_feature_nolab->fetch()) {
   $featureAbbrevsNoLab{$abbrev} = $id;
   $ctFeaturesNoLab++;
}

print "total number of features with no lab of origin: $ctFeaturesNoLab\n";

$cur_feature_nolab->finish();

$cur_feature_source = $dbh->prepare("select ids_data_zdb_id, ids_source_zdb_id from int_data_source where ids_data_zdb_id like 'ZDB-ALT-%';");
$cur_feature_source->execute();
$cur_feature_source->bind_columns(\$id, \$srcid);

$ctFeaturesSrcs = 0;
%featureSrcs = ();
while ($cur_feature_source->fetch()) {
   $featureSrcs{$id} = $srcid;
   $ctFeaturesSrcs++;
}

print "total number of feature sources: $ctFeaturesSrcs\n";

$cur_feature_source->finish();

$cur_source = $dbh->prepare("select zdb_id, name from lab union select zdb_id, name from company;");
$cur_source->execute();
$cur_source->bind_columns(\$srcid, \$name);

$ctSrcs = 0;
%srcs = ();
while ($cur_source->fetch()) {
   $srcs{$srcid} = $name;
   $ctSrcs++;
}

print "total number of labs and companies: $ctSrcs\n";

$cur_source->finish();

$cur_feature_loc9 = $dbh->prepare("select distinct sfcl_feature_zdb_id from sequence_feature_chromosome_location where sfcl_assembly like '%9%' and sfcl_feature_zdb_id like 'ZDB-ALT%' and sfcl_chromosome is not null and sfcl_start_position is not null;");
$cur_feature_loc9->execute();
$cur_feature_loc9->bind_columns(\$id);

$ctFeaturesLocs9 = 0;
%featureLocs9 = ();
while ($cur_feature_loc9->fetch()) {
  $featureLocs9{$id} = "Y";
  $ctFeaturesLocs9++;
}

print "total number of features with coordinate on Zv9 (from sequence_feature_chromosome_location table): $ctFeaturesLocs9\n";

$cur_feature_loc9->finish();


$cur_feature_loc9_generated = $dbh->prepare("select distinct sfclg_data_zdb_id from sequence_feature_chromosome_location_generated where sfclg_assembly like '%9%' and sfclg_data_zdb_id like 'ZDB-ALT%' and sfclg_chromosome is not null and sfclg_start is not null;");
$cur_feature_loc9_generated->execute();
$cur_feature_loc9_generated->bind_columns(\$id);

$ctFeaturesLocs9_generated = 0;
%featureLocs9_generated = ();
while ($cur_feature_loc9_generated->fetch()) {
  $featureLocs9_generated{$id} = 'Y';
  $ctFeaturesLocs9_generated++;
}

print "total number of features with coordinate on Zv9 (from sequence_feature_chromosome_location_generated table): $ctFeaturesLocs9_generated\n";

$cur_feature_loc9_generated->finish();

$cur_feature_loc10 = $dbh->prepare("select distinct sfcl_feature_zdb_id from sequence_feature_chromosome_location where sfcl_assembly like '%10%' and sfcl_feature_zdb_id like 'ZDB-ALT%' and sfcl_chromosome is not null and sfcl_start_position is not null;");
$cur_feature_loc10->execute();
$cur_feature_loc10->bind_columns(\$id);

$ctFeaturesLocs10 = 0;
%featureLocs10 = ();
while ($cur_feature_loc10->fetch()) {
  $featureLocs10{$id} = "Y";
  $ctFeaturesLocs10++;
}

print "total number of features with coordinate on GRCz10 (from sequence_feature_chromosome_location table): $ctFeaturesLocs10\n";

$cur_feature_loc10->finish();

$cur_feature_loc10_generated = $dbh->prepare("select distinct sfclg_data_zdb_id from sequence_feature_chromosome_location_generated where sfclg_assembly like '%10%' and sfclg_data_zdb_id like 'ZDB-ALT%' and sfclg_chromosome is not null and sfclg_start is not null;");
$cur_feature_loc10_generated->execute();
$cur_feature_loc10_generated->bind_columns(\$id);

$ctFeaturesLocs10_generated = 0;
%featureLocs10_generated = ();
while ($cur_feature_loc10_generated->fetch()) {
  $featureLocs10_generated{$id} = 'Y';
  $ctFeaturesLocs10_generated++;
}

print "total number of features with coordinate on Zv10 (from sequence_feature_chromosome_location_generated table): $ctFeaturesLocs10_generated\n";

$cur_feature_loc10_generated->finish();

$cur_feature_loc11 = $dbh->prepare("select distinct sfcl_feature_zdb_id from sequence_feature_chromosome_location where sfcl_assembly like '%11%' and sfcl_feature_zdb_id like 'ZDB-ALT%' and sfcl_chromosome is not null and sfcl_start_position is not null;");
$cur_feature_loc11->execute();
$cur_feature_loc11->bind_columns(\$id);

$ctFeaturesLocs11 = 0;
%featureLocs11 = ();
while ($cur_feature_loc11->fetch()) {
  $featureLocs11{$id} = "Y";
  $ctFeaturesLocs11++;
}

print "total number of features with coordinate on GRCz11 (from sequence_feature_chromosome_location table): $ctFeaturesLocs11\n";

$cur_feature_loc11->finish();

$cur_feature_loc11_generated = $dbh->prepare("select distinct sfclg_data_zdb_id from sequence_feature_chromosome_location_generated where sfclg_assembly like '%11%' and sfclg_data_zdb_id like 'ZDB-ALT%' and sfclg_chromosome is not null and sfclg_start is not null;");
$cur_feature_loc11_generated->execute();
$cur_feature_loc11_generated->bind_columns(\$id);

$ctFeaturesLocs11_generated = 0;
%featureLocs11_generated = ();
while ($cur_feature_loc11_generated->fetch()) {
  $featureLocs11_generated{$id} = 'Y';
  $ctFeaturesLocs11_generated++;
}

print "total number of features with coordinate on Zv11 (from sequence_feature_chromosome_location_generated table): $ctFeaturesLocs11_generated\n";

$cur_feature_loc11_generated->finish();


$dbh->disconnect
    or warn "Disconnection failed: $DBI::errstr\n";


open (FEATUREDATAREPORT, ">feature_data_report.txt") || die "Cannot open feature_data_report.txt : $!\n";

print FEATUREDATAREPORT "Feature ZDB ID\tFeature Symbol\tFeature Type\tMutagen\tMutagee\tLab of Origin\tOn Zv9\tOn GRCz10\tOn GRCz11\n";

$ctRows = 0;
foreach $abbrev (sort keys %featureAbbrevs) {
  $id = $featureAbbrevs{$abbrev};
  $type = $featureTypes{$abbrev};
  print FEATUREDATAREPORT "$id\t$abbrev\t$type\t";
  if(exists($featureAbbrevsNoAssay{$abbrev})) {
     print FEATUREDATAREPORT " \t \t";
  } else {
     if(exists($featureMutagens{$id})) {
        print FEATUREDATAREPORT "$featureMutagens{$id}\t";
     } else {
        print FEATUREDATAREPORT " \t";
     }
     if(exists($featureMutagees{$id})) {
        print FEATUREDATAREPORT "$featureMutagees{$id}\t";
     } else {
        print FEATUREDATAREPORT " \t";
     }
  }
  if(exists($featureAbbrevsNoLab{$abbrev})) {
     print FEATUREDATAREPORT " \t";
  } else {
     if(exists($featureSrcs{$id})) {
        $srcID = $featureSrcs{$id};
        if(exists($srcs{$srcID})) {
           print FEATUREDATAREPORT "$srcs{$srcID}\t";
        } else {
           print FEATUREDATAREPORT " \t";
        }
     } else {
        print FEATUREDATAREPORT " \t";
     }  
  }
  if(exists($featureLocs9{$id}) || exists($featureLocs9_generated{$id})) {
     print FEATUREDATAREPORT "Y\t";
  } else {
     print FEATUREDATAREPORT "N\t";
  }
  if(exists($featureLocs10{$id}) || exists($featureLocs10_generated{$id})) {
     print FEATUREDATAREPORT "Y\t";
  } else {
     print FEATUREDATAREPORT "N\t";
  }  
  if(exists($featureLocs11{$id}) || exists($featureLocs11_generated{$id})) {
     print FEATUREDATAREPORT "Y\n";
  } else {
     print FEATUREDATAREPORT "N\n";
  }  
  $ctRows++;
}

close FEATUREDATAREPORT;

print "ctRows = $ctRows\n";

exit;


