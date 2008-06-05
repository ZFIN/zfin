#!/private/bin/perl -w
#-----------------------------------------------------------------------
# Subroutines used to pull EST information from ZIRC.
#
# This file is 'required' by the pullFromZirc.pl script.  This file is not
# meant to be run directly, but only to support the pullFromZirc.pl script.
#
# All subroutines in this file start with the prefix est_.
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

sub est_parse($$$) {

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

    $/ = "\r";			# control M is input line separator.

    open (ZIRCESTFILE,"$zircFile") || errorExit ("Failed to open $zircFile");

    my $estZdbID;

    my $cur = $dbh->prepare("
          insert into est_pulled_from_zirc
              (epfz_est_zdb_id)
            values
              ( ? );");

    while ($estZdbId = <ZIRCESTFILE>) {
	chop($estZdbId);
	$cur->bind_param(1, $estZdbId);
	$cur->execute;
    }
    close (ZIRCESTFILE);

    $/ = '\n';			# restore default input line separator

    return ();
}


#----------------------------------------------------------------------
# Report the number of EST records where their status didn't change.
#
# Params
#  $       DBI Database Handle
#  $       ZDB ID of ZIRC
#
# Returns ()

sub est_reportUnchangedCount($$) {

    my $dbh = $_[0];
    my $zircZdbId = $_[1];

    my $rowCount = 0;

    # count them
    my $cur = $dbh->prepare("
          select count(*)
            from est_pulled_from_zirc, int_data_supplier
            where idsup_data_zdb_id = epfz_est_zdb_id 
              and idsup_supplier_zdb_id = '$zircZdbId';");

    $cur->execute();
    $cur->bind_columns(\$rowCount);
    $cur->fetch();
    &writeReport(int($rowCount) . " ESTs continued to be supplied by ZIRC\n");

    if ($rowCount == 0) {
	&errorExit("Aborting run!!!!  Something is probably wrong because the",
		   "  status of EVERY ZIRC supplied EST is changing.");
    }
    return ();
}


#----------------------------------------------------------------------
# Report any ESTs in the list from ZIRC that are not valid ZDB IDs
#
# Params
#  $       DBI Database Handle
#
# Returns ()

sub est_reportUnknownEsts($) {

    my $dbh = $_[0];

    my $rowCount = 0;

    # report them
    my $cur = $dbh->prepare("
          select epfz_est_zdb_id
            from est_pulled_from_zirc
            where not exists
                    ( select mrkr_zdb_id 
                        from marker
                        where mrkr_zdb_id = epfz_est_zdb_id )
            order by epfz_est_zdb_id;");

    $cur->execute;

    my $estZdbId;
    $cur->bind_columns(\$estZdbId);

    while ($cur->fetch) {
	writeReport($estZdbId);
	$rowCount++;
    }
    
    writeReport("$rowCount unknown EST(s) were found." .
		"  Unknown EST(s) are not added to ZFIN.\n");

    return ();
}


#----------------------------------------------------------------------
# Drop ESTs that are no longer supplied by ZIRC
#
# Params
#  $       DBI Database Handle
#  $       ZDB ID of ZIRC.
#
# Returns ()

sub est_dropDiscontinuedEsts($$) {

    my $dbh = $_[0];
    my $zircZdbId = $_[1];

    my $rowCount = 0;

    # report and delete them
    my $cur = $dbh->prepare("
          select idsup_data_zdb_id
            from int_data_supplier
            where get_obj_type(idsup_data_zdb_id) = 'EST'
              and idsup_supplier_zdb_id = '$zircZdbId'
              and not exists
                    ( select epfz_est_zdb_id 
                        from est_pulled_from_zirc
                        where idsup_data_zdb_id = epfz_est_zdb_id)
            order by idsup_data_zdb_id;");

    my $deleteCur = $dbh->prepare("
           delete from int_data_supplier
             where idsup_data_zdb_id = ?
               and idsup_supplier_zdb_id = '$zircZdbId';");

    $cur->execute;

    my $estZdbId;
    $cur->bind_columns(\$estZdbId);

    while ($cur->fetch) {
	writeReport($estZdbId);
	$deleteCur->bind_param(1, $estZdbId);
	$deleteCur->execute();
	$rowCount++;
    }
    
    writeReport("Dropped $rowCount discontinued EST(s)\n");

    return ();
}


#----------------------------------------------------------------------
# Add ESTs that were not previously supplied by ZIRC.
#
# Params
#  $       DBI Database Handle
#  $       ZDB ID of ZIRC.
#
# Returns ()

sub est_addNewlySuppliedEsts($$) {

    my $dbh = $_[0];
    my $zircZdbId = $_[1];

    my $rowCount = 0;

    # report and insert them
    my $cur = $dbh->prepare("
          select epfz_est_zdb_id 
            from est_pulled_from_zirc, marker
            where epfz_est_zdb_id = mrkr_zdb_id
              and not exists
                    ( select idsup_data_zdb_id
                        from int_data_supplier
                        where idsup_data_zdb_id = epfz_est_zdb_id
                          and idsup_supplier_zdb_id = '$zircZdbId')
            order by epfz_est_zdb_id;");

    my $insertCur = $dbh->prepare("
           insert into int_data_supplier
               ( idsup_data_zdb_id, idsup_supplier_zdb_id, idsup_acc_num )
             values
               ( ? , '$zircZdbId', ? );");

    $cur->execute;

    my $estZdbId;
    $cur->bind_columns(\$estZdbId);

    while ($cur->fetch) {
	writeReport($estZdbId);
	# ZIRC uses same accession numbers as ZFIN.
	$insertCur->bind_param(1, $estZdbId);
	$insertCur->bind_param(2, $estZdbId);
	$insertCur->execute();
	$rowCount++;
    }
    
    writeReport("Added $rowCount new EST(s)\n");

    return ();
}


#----------------------------------------------------------------------
# Update the EST / ZIRC data in the db.
#
# Params
#  $       DBI Database Handle
#  $       ZDB ID of ZIRC.
#
# Returns ()

sub est_updateZfin($$) {

    my $dbh = $_[0];
    my $zircZdbId = $_[1];

    &est_reportUnchangedCount($dbh, $zircZdbId);
    &est_reportUnknownEsts($dbh);
    &est_dropDiscontinuedEsts($dbh, $zircZdbId);
 
    &est_addNewlySuppliedEsts($dbh, $zircZdbId);

    return ();
}


#----------------------------------------------------------------------
# est_main
#
# download EST data from ZIRC and update ZFIN to reflect what it says.
#
# Params
#  $       DBI Database Handle
#  $       ZDB ID of ZIRC.
#
# Returns ()
#  Does not return if an error is detected.  In case of an error, the 
#    database will not be updated.

sub est_main($$) {

    my $dbh = $_[0];
    my $zircZdbId = $_[1];
    my $zircEstFile = "zircESTs.tab"; # file to download

    &writeReport("****** Pulling EST information from ZIRC.\n");

    system("rm -f $zircEstFile");    # remove old downloaded files
    &downloadFiles($zircEstFile);    # get new EST file

    # create temp table to load list of ESTs from ZIRC into

    my $cur = $dbh->do('
        create temp table est_pulled_from_zirc
          ( epfz_est_zdb_id  varchar(50) not null unique )
          with no log;');

    # parse routine populates the temp table.
    &est_parse($dbh, $zircZdbId, $zircEstFile);
    &est_updateZfin($dbh, $zircZdbId);

    return ();
}

return 1;
