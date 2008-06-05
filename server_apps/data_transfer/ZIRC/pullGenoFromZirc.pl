#!/private/bin/perl -w
#-----------------------------------------------------------------------
# Subroutines used to pull GENO information from ZIRC.
#
# This file is 'required' by the pullFromZirc.pl script.  This file is not
# meant to be run directly, but only to support the pullFromZirc.pl script.
#
# All subroutines in this file start with the prefix geno_.
#


#----------------------------------------------------------------------
# Parse the downloaded file from ZIRC.
#
# Read the input file.  Insert a record into the temp table for each
# line in the input file.
#
# Params
#  $       DBI Database Handle
#  $       ZDB **LAB** ID of ZIRC.
#  $       name of file downloaded from ZIRC
#  $       ZDB of the alteration/allele/feature
#  $       ZDB of the background for the allele/feature
#  $       zygocity of the allele/feature in the background provided.
#
# Returns ()

sub geno_parse($$$) {

    my $dbh = $_[0];
    my $zircZdbId = $_[1];
    my $zircFile = $_[2];
    my $newGenoRequestLine ;
    my $altId;
    my $backgroundId;
    my $zygocity;

    open (ZIRCGENOFILE,"$zircFile") || errorExit ("Failed to open $zircFile");

    my $cur = $dbh->prepare("
          insert into tmp_geno_pulled_from_zirc
              (epfz_alt_zdb_id,
               epfz_background_zdb_id,
               epfz_zygocity)
            values
              ( ?,?,? );");

    while ($newGenoRequestLine = <ZIRCGENOFILE>) {
	chop($newGenoRequestLine);
	($altId,$backgroundId,$zygocity)=split(/\t/,$newGenoRequestLine);

	$cur->bind_param(1, $altId);
	$cur->bind_param(2, $backgroundId);
	$cur->bind_param(3, $zygocity);
	$cur->execute;
   }
    
    close (ZIRCGENOFILE);
    
 
    return ();
}

sub geno_available_parse($$){

    my $newGenoAvailableLine ;
    my $genoId;
    my $dbh = $_[0];
    my $zircResourceFile = $_[1];

   open (ZIRCRESOURCEFILE,"$zircResourceFile") || errorExit ("Failed to open $zircResourceFile");

    my $cur = $dbh->prepare("
          insert into geno_available
              (geno_zdb_id)
            values
              ( ? );");

    while ($newGenoAvailableLine = <ZIRCRESOURCEFILE>) {
	chop($newGenoAvailableLine);
	($genoId)=split(/\t/,$newGenoAvailableLine);

	$cur->bind_param(1, $genoId);
	$cur->execute;
   }
    
    close (ZIRCRESOURCEFILE);

    return ();
}

sub geno_load($) {

    my $dbh = $_[0];
    
    my $insert_temp_1 = $dbh->prepare("insert into geno_pulled_from_zirc (
                                                   epfz_alt_zdb_id,
       	    			                   epfz_background_zdb_id,
       	    			                   epfz_zygocity)
                                         select distinct epfz_alt_zdb_id,
			                                 epfz_background_zdb_id,
       	 		                                 epfz_zygocity
                                           from tmp_geno_pulled_from_zirc;");

    my $insert_temp_clean = $dbh->prepare("update geno_pulled_from_zirc
                                             set epfz_geno_zdb_id =
         (select geno_zdb_id
    	    from genotype a, genotype_feature c, genotype_background
  	    where c.genofeat_geno_zdb_id = a.geno_zdb_id
  	    and c.genofeat_feature_zdb_id = epfz_alt_zdb_id
  	    and a.geno_zdb_id = genoback_geno_zdb_id
  	    and c.genofeat_zygocity = (select zyg_zdb_id
      				    	       from zygocity
			       		       where zyg_name = epfz_zygocity)
            and c.genofeat_mom_zygocity = (Select zyg_zdb_id
	    				     from zygocity
					     where zyg_name = 'unknown')
            and c.genofeat_dad_zygocity = (select zyg_zdb_id
	    				     from zygocity
					     where zyg_name = 'unknown')
            and not exists (select 'x'
	    	    	   	   from genotype_feature b
				   where c.genofeat_geno_zdb_id =
				         b.genofeat_geno_zdb_id
				   and c.genofeat_feature_zdb_id != 
				       b.genofeat_feature_zdb_id)
	    and not exists (select 'x'
	    	    	   	   from genotype_background d
				   where d.genoback_geno_zdb_id = a.geno_zdb_id
				   and d.genoback_background_zdb_id != 
                                         epfz_background_zdb_id));");

    my $insert_genos_found_not_supplied_yet = $dbh->prepare(
	"insert into int_data_supplier (idsup_data_zdb_id, 
                                        idsup_supplier_zdb_id, 
                                        idsup_acc_num)
           select epfz_geno_zdb_id, 'ZDB-LAB-991005-53', epfz_geno_zdb_id
             from geno_pulled_From_zirc
             where not exists (select 'x' 
                                 from int_data_supplier
		                 where idsup_data_zdb_id = epfz_geno_zdb_id
		                 and idsup_supplier_zdb_id = 'ZDB-LAB-991005-53'
                                 and idsup_acc_num = epfz_geno_zdb_id)
             and epfz_geno_zdb_id is not null
             and exists (select 'x' from geno_available
                             where epfz_geno_zdb_id = geno_zdb_id);");

    my $insert_data_from_resource_file = $dbh->prepare(
	"insert into int_data_supplier (idsup_data_zdb_id, 
                                        idsup_supplier_zdb_id, 
                                        idsup_acc_num)
           select geno_zdb_id, 'ZDB-LAB-991005-53', geno_zdb_id
             from geno_available b
             where exists (Select 'x' from zdb_active_data
                                 where b.geno_zdb_id = zactvd_zdb_id)
             and not exists (select 'x' 
                                 from int_data_supplier
		                 where idsup_data_zdb_id = geno_zdb_id
		                 and idsup_supplier_zdb_id = 'ZDB-LAB-991005-53'
                                 and idsup_acc_num = geno_zdb_id);");


    my $delete_existing_where_supplied = $dbh->prepare("delete from geno_pulled_from_zirc
                                                          where epfz_geno_zdb_id is not null;");

    my $update_genofeat=$dbh->prepare("update geno_pulled_from_zirc
                                              set epfz_genofeat_zdb_id = get_id('GENOFEAT');");

    my $zactvd_genofeat=$dbh->prepare("insert into zdb_active_data
                                               select epfz_genofeat_zdb_id
                                                 from geno_pulled_From_zirc
                                                 where not exists (Select 'x'
    	      	     	                                              from zdb_active_data
			                                               where epfz_genofeat_Zdb_id = zactvd_zdb_id);");

    my $add_new_genoids = $dbh->prepare("update geno_pulled_from_zirc
                                          set epfz_geno_zdb_id = get_id('GENO')
                                          where epfz_geno_zdb_id is null;");

    my $to_active_data = $dbh->prepare("insert into zdb_active_data
                                          select epfz_geno_zdb_id
                                             from geno_pulled_From_zirc
                                             where not exists (Select 'x'
    	      	     	                                         from zdb_active_data
			                                         where epfz_geno_Zdb_id = zactvd_zdb_id);");

    my $to_genotype = $dbh->prepare("insert into genotype (geno_zdb_id, 
       	    	                                           geno_display_name,
		                                           geno_handle,
		                                           geno_date_entered,
		                                           geno_nickname)
                                       select epfz_geno_zdb_id,
 	                                      epfz_geno_zdb_id,
	                                      epfz_geno_zdb_id,
	                                      current year to second,
	                                      epfz_geno_zdb_id
                                         from geno_pulled_from_zirc
                                         where not exists (select 'x'
  	    	   	                                     from genotype
			                                     where geno_zdb_id = epfz_geno_zdb_id);");

    my $to_genofeat = $dbh->prepare(
	"insert into genotype_feature (genofeat_zdb_id,
				genofeat_geno_zdb_id,
				genofeat_feature_zdb_id,
				genofeat_zygocity,
				genofeat_mom_zygocity,
				genofeat_dad_zygocity)
            select epfz_genofeat_zdb_id,
  	           epfz_geno_zdb_id,
	           epfz_alt_zdb_id,
	           (select zyg_zdb_id
		       from zygocity 
		       where zyg_name = epfz_zygocity),
	           (select zyg_zdb_id
		      from zygocity
		      where zyg_name ='unknown'),
	           (select zyg_zdb_id
		      from zygocity
		      where zyg_name ='unknown')
              from geno_pulled_from_zirc
              where not exists (Select 'x' from genotype_feature
                                  where genofeat_geno_zdb_id = epfz_geno_zdb_id
                                    and genofeat_feature_zdb_id = epfz_alt_zdb_id
                                    and genofeat_zygocity = (select zyg_zdb_id from zygocity where zyg_name =epfz_zygocity)
                                    and genofeat_mom_zygocity = 'ZDB-ZYG-070117-7'
                                    and genofeat_dad_zygocity = 'ZDB-ZYG-070117-7');");

    my $to_genoback=$dbh->prepare(
	"insert into genotype_background (genoback_geno_zdb_id,
					genoback_background_zdb_id)
           select epfz_geno_zdb_id, epfz_background_zdb_id
             from geno_pulled_from_zirc
             where not exists (Select 'x' from genotype_background
                                    where genoback_geno_zdb_id = epfz_geno_zdb_id
                                    and genoback_background_zdb_id = epfz_background_zdb_id);");

    my $to_recattrib=$dbh->prepare(
	"insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id)
             select distinct(epfz_geno_zdb_id), 'ZDB-PUB-080110-2'
               from geno_pulled_from_zirc
               where not exists (select 'x'
    	      	     	           from record_attribution 
			           where recattrib_data_zdb_id = epfz_geno_zdb_id
			           and recattrib_source_zdb_id = 'ZDB-PUB-080110-2'
			           and recattrib_source_type = 'standard');");

    my $to_recattrib_genofeat=$dbh->prepare(
	"insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id)
             select distinct(epfz_genofeat_zdb_id), 'ZDB-PUB-080110-2'
               from geno_pulled_from_zirc
               where not exists (select 'x'
    	      	     	           from record_attribution 
			           where recattrib_data_zdb_id = epfz_genofeat_zdb_id
			           and recattrib_source_zdb_id = 'ZDB-PUB-080110-2'
			           and recattrib_source_type = 'standard');");

    my $to_data_supplier_geno=$dbh->prepare(
	"insert into int_data_supplier (idsup_data_zdb_id, idsup_acc_num, 
       	    		      			  idsup_supplier_zdb_id)
           select epfz_geno_zdb_id, epfz_geno_zdb_id, 'ZDB-LAB-991005-53'
             from geno_pulled_from_zirc
             where not exists (Select 'x'
  	    	   	         from int_datA_supplier
			         where idsup_data_zdb_id = epfz_geno_zdb_id
			         and idsup_Acc_num = epfz_geno_zdb_id
			         and idsup_supplier_zdb_id = 'ZDB-LAB-991005-53')
             and exists (Select 'x' from geno_available
                            where geno_zdb_id = epfz_geno_zdb_id);");

    my $to_data_supplier_feature=$dbh->prepare(
    "insert into int_data_supplier (idsup_data_zdb_id, idsup_acc_num, 
       	    		      			  idsup_supplier_zdb_id)
               select distinct epfz_alt_zdb_id, epfz_alt_zdb_id, 'ZDB-LAB-991005-53'
                 from geno_pulled_from_zirc
                 where not exists (Select 'x'
  	    	   	            from int_datA_supplier
			            where idsup_data_zdb_id = epfz_alt_zdb_id
			            and idsup_Acc_num = epfz_alt_zdb_id
			            and idsup_supplier_zdb_id = 'ZDB-LAB-991005-53')
                 and exists (Select 'x' from geno_available
                            where geno_zdb_id = epfz_geno_zdb_id);");
 
    my $update_geno_handle=$dbh->prepare(
	"update geno_pulled_from_zirc
               set epfz_geno_handle = get_genotype_handle(epfz_geno_zdb_id)
               where epfz_geno_handle is null;");

    my $update_geno_display=$dbh->prepare(
          "update geno_pulled_from_zirc
                 set epfz_geno_display_name = get_genotype_display(epfz_geno_zdb_id)
                 where epfz_geno_display_name is null;");



    my $update_genotype_handle=$dbh->prepare(
                    "update genotype
                       set geno_handle = (Select epfz_geno_handle
      		                            from geno_pulled_from_zirc
		                            where epfz_geno_zdb_id = geno_zdb_id)
                       where exists (Select 'x'
       	      	                       from geno_pulled_From_zirc
		                       where geno_zdb_id = epfz_geno_zdb_id)
                       and geno_handle like 'ZDB-GENO-%';");

    my $update_genotype_display=$dbh->prepare(
               "update genotype
                  set geno_display_name =  (Select epfz_geno_display_name
      		                              from geno_pulled_from_zirc
		                              where epfz_geno_zdb_id = geno_zdb_id)
                  where exists (Select 'x'
       	      	                  from geno_pulled_from_zirc
		                  where geno_zdb_id = epfz_geno_zdb_id)
                                  and geno_display_name like 'ZDB-GENO-%';");

 
   die "Couldn't prepare queries; aborting"
	unless defined 
	$insert_temp_1 &&
	$insert_temp_clean &&
	$insert_genos_found_not_supplied_yet &&
	$insert_data_from_resource_file &&
	$delete_existing_where_supplied &&
	$update_genofeat &&
	$zactvd_genofeat &&
	$add_new_genoids &&
	$to_active_data &&
	$to_genotype &&
	$to_genofeat &&
	$to_genoback &&
	$to_recattrib &&
	$to_data_supplier_geno &&
	$to_data_supplier_feature &&
	$update_geno_handle &&
	$update_geno_display &&
	$update_genotype_handle &&
	$update_genotype_display &&
	$to_recattrib_genofeat;
 
  &writeReport("*** Queries prepaired. *** \n"); 

# execute the queries

    $insert_temp_1->execute;
    $insert_temp_clean->execute;
    $insert_genos_found_not_supplied_yet->execute;
    $insert_data_from_resource_file->execute;
    $delete_existing_where_supplied->execute;
    $update_genofeat->execute;
    $zactvd_genofeat->execute; 
    $add_new_genoids->execute;
    $to_active_data->execute;
    $to_genotype->execute;
    $to_genofeat->execute;
    $to_genoback->execute;
    $to_recattrib->execute;
    $to_data_supplier_geno->execute;
    $to_data_supplier_feature->execute;
    $update_geno_handle->execute;
    $update_geno_display->execute;
    $update_genotype_handle->execute;
    $update_genotype_display->execute;
    $to_recattrib_genofeat->execute;


    &writeReport("*** Queries executed. ***\n"); 

    return();
}
sub geno_dropDiscontinuedGenos($$) {

    my $dbh = $_[0];
    my $zircZdbId = $_[1];

    my $rowCount = 0;

    # report and delete them
    my $cur = $dbh->prepare("
          select idsup_data_zdb_id
            from int_data_supplier
            where get_obj_type(idsup_data_zdb_id) = 'GENO'
              and idsup_supplier_zdb_id = '$zircZdbId'
              and idsup_acc_num = idsup_data_zdb_id
              and not exists (select 'x'
                        from geno_available
                        where idsup_data_zdb_id = geno_zdb_id
                        and geno_zdb_id like 'ZDB-GENO%')
              and not exists (select 'x' from geno_pulled_from_zirc
                                where epfz_geno_zdb_id = idsup_Data_zdb_id)
                    
            order by idsup_data_zdb_id
   ;");

    my $deleteCur = $dbh->prepare("
           delete from int_data_supplier
             where idsup_supplier_zdb_id = '$zircZdbId'
               and idsup_data_zdb_id like 'ZDB-GENO%'
               and idsup_data_zdb_id = ?;");

    $cur->execute;

    my $genoZdbId;
    $cur->bind_columns(\$genoZdbId);

    while ($cur->fetch) {
	writeReport($genoZdbId);
	$deleteCur->bind_param(1, $genoZdbId);
	$deleteCur->execute();
	$rowCount++;
    }
    
    writeReport("Dropped $rowCount discontinued Genotypes(s)\n");

    return ();
}

sub geno_dropDiscontinuedAlts($$) {

    my $dbh = $_[0];
    my $zircZdbId = $_[1];

    my $rowCount = 0;

    # report and delete them
    my $cur = $dbh->prepare("
          select idsup_data_zdb_id
            from int_data_supplier
            where get_obj_type(idsup_data_zdb_id) = 'ALT'
              and idsup_supplier_zdb_id = '$zircZdbId'
              and idsup_acc_num = idsup_data_zdb_id
              and not exists
                    ( select 'z'
                        from geno_available
                        where idsup_data_zdb_id = geno_zdb_id
                        and geno_zdb_id like 'ZDB-ALT%')
              
            order by idsup_data_zdb_id;");

    my $deleteCur = $dbh->prepare("
           delete from int_data_supplier
             where idsup_supplier_zdb_id = '$zircZdbId'
               and idsup_data_zdb_id = ?
               and idsup_data_zdb_id = idsup_acc_num
               and idsup_data_zdb_id like 'ZDB-ALT%';");

    $cur->execute;

    my $altZdbId;
  
    $cur->bind_columns(\$altZdbId);

    while ($cur->fetch) {
	writeReport($altZdbId);
	$deleteCur->bind_param(1, $altZdbId);
	$deleteCur->execute();
	$rowCount++;
    }    
    writeReport("Dropped $rowCount discontinued Feature(s)\n");

    return ();
}


#----------------------------------------------------------------------
# Report the number of GENO records where their status didn't change.
#
# Params
#  $       DBI Database Handle
#  $       ZDB ID of ZIRC
#
# Returns ()

sub geno_altSuppliedByZFIN_count($$) {

    my $dbh = $_[0];
    my $zircZdbId = $_[1];

    my $rowCount = 0;

    # count them
    my $cur = $dbh->prepare("select count(*)
            from int_data_supplier
            where idsup_data_zdb_id like 'ZDB-ALT-%'
              and idsup_supplier_zdb_id = '$zircZdbId'
              and idsup_data_zdb_id = idsup_acc_num;");

    $cur->execute();
    $cur->bind_columns(\$rowCount);
    $cur->fetch();
    &writeReport(int($rowCount) . " Features from ZIRC already being 'supplied' at ZFIN.\n");

    if ($rowCount == 0) {
	&errorExit("Aborting run!!!!  Something is probably wrong because the",
		   "  status of EVERY ZIRC supplied ALT is changing.");
    }
    return ();
}

sub geno_GenoSuppliedByZFIN_count($$) {

    my $dbh = $_[0];
    my $zircZdbId = $_[1];

    my $rowCount = 0;

    # count them
    my $cur = $dbh->prepare("select count(*)
            from int_data_supplier
            where idsup_data_zdb_id like 'ZDB-GENO-%'
              and idsup_supplier_zdb_id = '$zircZdbId'
              and idsup_acc_num = idsup_data_zdb_id;");

    $cur->execute();
    $cur->bind_columns(\$rowCount);
    $cur->fetch();
    &writeReport(int($rowCount) . " Genotypes from ZIRC already being 'supplied' at ZFIN.\n");

    if ($rowCount == 0) {
	&errorExit("Aborting run!!!!  Something is probably wrong because the",
		   "  status of EVERY ZIRC supplied GENO is changing.");
    }
    return ();
}


#----------------------------------------------------------------------
# Report any GENOs/ALTs in the list from ZIRC that are not valid ZDB IDs
#
# Params
#  $       DBI Database Handle
#
# Returns ()

sub geno_reportUnknownGenos($) {

    my $dbh = $_[0];

    my $rowCount = 0;

    my $unknownGenoCounter;
    # report them
    my $cur = $dbh->prepare("select count(*) from geno_pulled_from_zirc
                              where epfz_geno_zdb_id is not null;");

    $cur->execute;   
    $cur->bind_columns(\$rowCount);
    $cur->fetch();
    &writeReport(int($rowCount) . " New genotypes added to ZFIN.\n");

    return ();
}

sub geno_reportSuppliedByZircGenos($) {

    my $dbh = $_[0];

    my $rowCount = 0;

    # report them
    my $cur = $dbh->prepare("select count(*) from geno_available
                               where geno_zdb_id like 'ZDB-GENO-%';");

    $cur->execute;   
    $cur->bind_columns(\$rowCount);
    $cur->fetch();
    &writeReport(int($rowCount) ." GENOs currently supplied by ZIRC.\n");

    return ();
}

sub geno_reportSuppliedByZircAlts($) {

    my $dbh = $_[0];

    my $rowCount = 0;

    # report them
    my $cur = $dbh->prepare("select count(*) from geno_available where geno_zdb_id like 'ZDB-ALT-%';");

    $cur->execute;   
    $cur->bind_columns(\$rowCount);
    $cur->fetch();
    &writeReport(int($rowCount) . " Features currently supplied by ZIRC.\n");

    return ();
}

#----------------------------------------------------------------------
# geno_main
#
# download GENO data from ZIRC and update ZFIN to reflect what it says.
#
# Params
#  $       DBI Database Handle
#  $       ZDB ID of ZIRC.
#
# Returns ()
#  Does not return if an error is detected.  In case of an error, the 
#    database will not be updated.

sub geno_main($$) {

    my $dbh = $_[0];
    my $zircZdbId = $_[1];
    my $zircGenoFile = "need.txt"; # geno file to download
    my $zircResourceFile = "resource.txt"; # which genos supplied from ZIRC

    system("rm -f $zircGenoFile");    # remove old downloaded files
    system ("rm -f $zircResourceFile"); 

    &downloadFiles($zircGenoFile,"Genos");    # get new Geno file
    &downloadFiles ($zircResourceFile,"Genos"); # get Resource file

    &writeReport("\n*** need.txt and resource.txt files downloaded from ZIRC. ***\n");

    # create temp table to load list of GENOs from ZIRC into

    my $curFirstTable = $dbh->do('create temp table tmp_geno_pulled_from_zirc
          ( epfz_alt_zdb_id  varchar(50),
            epfz_background_zdb_id varchar(50),
            epfz_zygocity  varchar(40))
            with no log;
        ');


    my $curSecondTable = $dbh->do('create temp table geno_pulled_from_zirc
          ( epfz_alt_zdb_id  varchar(50),
            epfz_background_zdb_id varchar(50),
            epfz_zygocity  varchar(40),
            epfz_geno_zdb_id varchar(50),
            epfz_zirc_is_submitter boolean,
	    epfz_geno_handle varchar(150),
	    epfz_geno_display_name varchar(150),
            epfz_genofeat_zdb_id varchar(50))
           with no log; ');

    my $resourceTable = $dbh->do('create temp table geno_available
          ( geno_zdb_id  varchar(50))
            with no log; ');

    &writeReport("*** Temp tables created. ***\n");

    # parse routines populate the temp tables.

    &geno_parse($dbh,$zircZdbId,$zircGenoFile);
    &geno_available_parse($dbh,$zircResourceFile);

    &writeReport("*** Files parsed and loaded to temp tables. ***\n"); 

    &geno_reportSuppliedByZircAlts($dbh);
    &geno_reportSuppliedByZircGenos($dbh);
    &geno_altSuppliedByZFIN_count($dbh,$zircZdbId);
    &geno_GenoSuppliedByZFIN_count($dbh,$zircZdbId);
 
    &geno_load($dbh);

    &writeReport("*** Data Loaded. ***\n");

    &geno_dropDiscontinuedGenos($dbh,$zircZdbId);
    &geno_dropDiscontinuedAlts($dbh,$zircZdbId);

    &geno_reportUnknownGenos($dbh);

    # run these again post load.

    &geno_altSuppliedByZFIN_count($dbh,$zircZdbId);
    &geno_GenoSuppliedByZFIN_count($dbh,$zircZdbId);

    return ();
}

return 1;
