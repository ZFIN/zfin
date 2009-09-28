#!/private/bin/perl -w
#-----------------------------------------------------------------------
# Subroutines used to pull ATB information from ZIRC.
#
# This file is 'required' by the pullFromZirc.pl script.  This file is not
# meant to be run directly, but only to support the pullFromZirc.pl script.
#
# All subroutines in this file start with the prefix atb_.
#


#----------------------------------------------------------------------
# Parse the downloaded file from ZIRC.
#
# Read the input file.  Insert a record into the temp table for each
# line in the input file.
#
# Params
#  $       DBI Database Handle
#  $       ZDB ID of ZIRC.
#  $       name of file downloaded from ZIRC
#
# Returns ()

sub atb_parse($$$) {

    my $dbh = $_[0];
    my $zircZdbId = $_[1];
    my $zircFile = $_[2];

    # The original plan was to translate the input file into a load
    # file, and then use the load statement to get the data into the
    # temp table.
    #
    # That didn't work because load is only available through dbaccess.
    # Next thing we tried we called dbaccess with a system command.
    # That didn't work becaue dbaccess didn't know about the temp table.
    #
    # Therefore, this routine directly inserts records into the temp table.

    #$/ = "\r";			# control M is input line separator.

    open (ZIRCATBFILE,"$zircFile") || errorExit ("Failed to open $zircFile");

    my $atbZdbID;

    my $cur = $dbh->prepare("
          insert into atb_pulled_from_zirc
              (epfz_atb_zdb_id)
            values
              ( ? );");

    while ($atbZdbId = <ZIRCATBFILE>) {
	chop($atbZdbId);
	$cur->bind_param(1, $atbZdbId);
	$cur->execute;
    }
    close (ZIRCATBFILE);

   # $/ = '\n';			# restore default input line separator

    return ();
}


#----------------------------------------------------------------------
# Report the number of ATB records where their status didn't change.
#
# Params
#  $       DBI Database Handle
#  $       ZDB ID of ZIRC
#
# Returns ()

sub atb_reportUnchangedCount($$) {

    my $dbh = $_[0];
    my $zircZdbId = $_[1];

    my $rowCount = 0;

    # count them
    my $cur = $dbh->prepare("
          select count(*)
            from atb_pulled_from_zirc, int_data_supplier
            where idsup_data_zdb_id = epfz_atb_zdb_id
              and idsup_supplier_zdb_id = '$zircZdbId'
              and get_obj_type(idsup_data_zdb_id) ='ATB';");

    $cur->execute();
    $cur->bind_columns(\$rowCount);
    $cur->fetch();
    &writeReport(int($rowCount) . " ATBs continued to be supplied by ZIRC\n");

    #if ($rowCount == 0) {
	#&errorExit("Aborting run!!!!  Something is probably wrong because the",
		#   "  status of EVERY ZIRC supplied ATB is changing.");
    #}
    return ();
}


#----------------------------------------------------------------------
# Report any ATBs in the list from ZIRC that are not valid ZDB IDs
#
# Params
#  $       DBI Database Handle
#
# Returns ()

sub atb_reportUnknownAtbs($) {

    my $dbh = $_[0];

    my $rowCount = 0;

    # report them
    my $cur = $dbh->prepare("
          select epfz_atb_zdb_id
            from atb_pulled_from_zirc
            where not exists
                    ( select mrkr_zdb_id 
                        from marker
                        where mrkr_zdb_id = epfz_atb_zdb_id )
            and get_obj_type(epfz_atb_zdb_id) ='ATB'         
            order by epfz_atb_zdb_id;");

    $cur->execute;

    my $atbZdbId;
    $cur->bind_columns(\$atbZdbId);

    while ($cur->fetch) {
	writeReport($atbZdbId);
	$rowCount++;
    }
    
    writeReport("$rowCount unknown ATB(s) were found." .
		"  Unknown ATB(s) are not added to ZFIN.\n");

    return ();
}


#----------------------------------------------------------------------
# Drop ATBs that are no longer supplied by ZIRC
#
# Params
#  $       DBI Database Handle
#  $       ZDB ID of ZIRC.
#
# Returns ()

sub atb_dropDiscontinuedAtbs($$) {

    my $dbh = $_[0];
    my $zircZdbId = $_[1];

    my $rowCount = 0;

    # report and delete them
    my $cur = $dbh->prepare("
          select idsup_data_zdb_id
            from int_data_supplier
            where get_obj_type(idsup_data_zdb_id) = 'ATB'
              and idsup_supplier_zdb_id = '$zircZdbId'
              and not exists
                    ( select epfz_atb_zdb_id 
                        from atb_pulled_from_zirc
                        where idsup_data_zdb_id = epfz_atb_zdb_id)
            order by idsup_data_zdb_id;");

    my $deleteCur = $dbh->prepare("
           delete from int_data_supplier
             where idsup_data_zdb_id = ?
               and idsup_supplier_zdb_id = '$zircZdbId';");

    $cur->execute;

    my $atbZdbId;
    $cur->bind_columns(\$atbZdbId);

    while ($cur->fetch) {
	writeReport($atbZdbId);
	$deleteCur->bind_param(1, $atbZdbId);
	$deleteCur->execute();
	$rowCount++;
    }
    
    writeReport("Dropped $rowCount discontinued ATB(s)\n");

    return ();
}


#----------------------------------------------------------------------
# Add ATBs that were not previously supplied by ZIRC.
#
# Params
#  $       DBI Database Handle
#  $       ZDB ID of ZIRC.
#
# Returns ()

sub atb_addNewlySuppliedAtbs($$) {

    my $dbh = $_[0];
    my $zircZdbId = $_[1];

    my $rowCount = 0;

    # report and insert them
    my $cur = $dbh->prepare("
          select epfz_atb_zdb_id 
            from atb_pulled_from_zirc, marker
            where epfz_atb_zdb_id = mrkr_zdb_id
              and get_obj_type(epfz_atb_zdb_id) = 'ATB'
              and not exists
                    ( select idsup_data_zdb_id
                        from int_data_supplier
                        where idsup_data_zdb_id = epfz_atb_zdb_id
                          and idsup_supplier_zdb_id = '$zircZdbId')
            order by epfz_atb_zdb_id;");

    my $insertCur = $dbh->prepare("
           insert into int_data_supplier
               ( idsup_data_zdb_id, idsup_supplier_zdb_id, idsup_acc_num )
             values
               ( ? , '$zircZdbId', ? );");

    $cur->execute;

    my $atbZdbId;
    $cur->bind_columns(\$atbZdbId);

    while ($cur->fetch) {
	writeReport($atbZdbId);
	# ZIRC uses same accession numbers as ZFIN.
	$insertCur->bind_param(1, $atbZdbId);
	$insertCur->bind_param(2, $atbZdbId);
	$insertCur->execute();
	$rowCount++;
    }
    
    writeReport("Added $rowCount new ATB(s)\n");

    return ();
}


#----------------------------------------------------------------------
# Update the ATB / ZIRC data in the db.
#
# Params
#  $       DBI Database Handle
#  $       ZDB ID of ZIRC.
#
# Returns ()

sub atb_updateZfin($$) {

    my $dbh = $_[0];
    my $zircZdbId = $_[1];

    &atb_reportUnchangedCount($dbh, $zircZdbId);
    &atb_reportUnknownAtbs($dbh);
    &atb_dropDiscontinuedAtbs($dbh, $zircZdbId);
 
    &atb_addNewlySuppliedAtbs($dbh, $zircZdbId);

    return ();
}


#----------------------------------------------------------------------
# atb_main
#
# download ATB data from ZIRC and update ZFIN to reflect what it says.
#
# Params
#  $       DBI Database Handle
#  $       ZDB ID of ZIRC.
#
# Returns ()
#  Does not return if an error is detected.  In case of an error, the 
#    database will not be updated.

sub atb_main($$) {

    my $dbh = $_[0];
    my $zircZdbId = $_[1];
    my $zircAtbFile = "resource.txt"; # file to download

    &writeReport("****** Pulling ATB information from ZIRC.\n");
    # since ATB file is the same as GENO, ALT file we don't need to rm
    # and re-download.

    # create temp table to load list of ATBs from ZIRC into

    my $cur = $dbh->do('
        create temp table atb_pulled_from_zirc
          ( epfz_atb_zdb_id  varchar(50) not null unique )
          with no log;');

    # parse routine populates the temp table.
    &atb_parse($dbh, $zircZdbId, $zircAtbFile);
    &atb_updateZfin($dbh, $zircZdbId);

    return ();
}

return 1;
