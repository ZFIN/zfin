#!/private/bin/perl -w
#-----------------------------------------------------------------------
# Subroutines used to pull ZIRC Fish Line alteration/allele information 
# from ZIRC.  
# 
# This information tells what alterations/alleles are in each ZIRC fish
# line.
#
# This file is 'required' by the pullFromZirc.pl script.  This file is not
# meant to be run directly, but only to support the pullFromZirc.pl script.
#
# All subroutines in this file start with the prefix alteration_.
#


#----------------------------------------------------------------------
# Parse the downloaded file from ZIRC.
#
# Read the input file.  Insert a record into the temp table for each
# line in the input file.
#
# Params
#  $       DBI Database Handle
#  $       name of file downloaded from ZIRC
#
# Returns ()

sub alteration_parse($$) {

    my $dbh = $_[0];
    my $zircFile = $_[1];

    # See equivalent routine in pullEstsFromZirc.pl for why this routine
    # uses a temp table rather than other approaches.

    $/ = "\r";			# control M is input line separator.

    open (ZIRCALTERATIONFILE,"$zircFile") || errorExit ("Failed to open $zircFile");
    my $line;

    my $cur = $dbh->prepare("
          insert into alteration_pulled_from_zirc
              (apfz_zircfl_id, apfz_alt_zdb_id)
            values
              ( ?, ? );");

    while ($line = <ZIRCALTERATIONFILE>) {
	chop($line);
	my $zircflId;
	my $altZdbId;
	($zircflId, $altZdbId) = split(/\t/, $line);
	$cur->bind_param(1, $zircflId);
	$cur->bind_param(2, $altZdbId);
	$cur->execute;
    }
    close (ZIRCALTERATIONFILE);

    $/ = '\n';			# restore default input line separator

    return ();
}


#----------------------------------------------------------------------
# Report the number of alteration records where their status didn't change.
#
# Params
#  $       DBI Database Handle
#
# Returns ()

sub alteration_reportUnchangedCount($) {

    my $dbh = $_[0];

    my $rowCount = 0;

    # count them
    my $cur = $dbh->prepare("
          select count(*)
            from alteration_pulled_from_zirc, zirc_fish_line_alteration
            where zircflalt_line_id    = apfz_zircfl_id
              and zircflalt_alt_zdb_id = apfz_alt_zdb_id;");

    $cur->execute();
    $cur->bind_columns(\$rowCount);
    $cur->fetch();
    &writeReport(int($rowCount) . " alteration records are unchanged\n");

    if ($rowCount == 0) {
	&errorExit("Aborting run!!!!  Something is probably wrong because the",
		   "  status of EVERY ZIRC alteration record is changing.");
    }
    return ();
}


#----------------------------------------------------------------------
# Report and remove any records with alteration ZDB IDs that don't exist in
# ZFIN.
#
# Params
#  $       DBI Database Handle
#
# Returns ()

sub alteration_checkAlterationZdbIds($) {

    my $dbh = $_[0];

    my $rowCount = 0;

    writeReport("Checking for unknown alteration ZDB IDs ...");

    # report them
    my $cur = $dbh->prepare("
          select apfz_alt_zdb_id
            from alteration_pulled_from_zirc
            where not exists
                    ( select zdb_id 
                        from alteration
                        where zdb_id = apfz_alt_zdb_id )
            order by apfz_alt_zdb_id;");

    $cur->execute;

    my $altZdbId;
    $cur->bind_columns(\$altZdbId);

    while ($cur->fetch) {
	writeReport($altZdbId);
	$rowCount++;
    }
    
    writeReport("$rowCount unknown alteration ZDB ID(s) were found.");

    if ($rowCount) {
	writeReport("Alteration records with unknown ZDB IDs are dropped " .
		    "from input.");

	my $deleteCur = $dbh->do("
          delete
            from alteration_pulled_from_zirc
            where not exists
                    ( select zdb_id 
                        from alteration
                        where zdb_id = apfz_alt_zdb_id );");
    }
    writeReport("");
    return ();
}



#----------------------------------------------------------------------
# Report and remove any records with ZIRC fish line IDs that don't exist in
# ZFIN.  Any ZIRC fish line IDs in the alterations file should already be
# defined in the zirc_fish_line table.  (See pullFishLinesFromZirc.pl for
# this process.)
#
# Params
#  $       DBI Database Handle
#
# Returns ()

sub alteration_checkFishLineIds($) {

    my $dbh = $_[0];

    my $rowCount = 0;

    writeReport("Checking for unknown Fish Line IDs ...");

    # report them
    my $cur = $dbh->prepare("
          select apfz_zircfl_id
            from alteration_pulled_from_zirc
            where not exists
                    ( select zircfl_line_id
                        from zirc_fish_line
                        where zircfl_line_id = apfz_zircfl_id )
            order by apfz_zircfl_id;");

    $cur->execute;

    my $zircflId;
    $cur->bind_columns(\$zircflId);

    while ($cur->fetch) {
	writeReport($zircflId);
	$rowCount++;
    }
    
    writeReport("$rowCount unknown ZIRC Fish Line ID(s) were found.");

    if ($rowCount) {
	writeReport("Alteration records with unknown ZIRC Fish Line IDs " .
		    "are dropped from input.");

	my $deleteCur = $dbh->do("
          delete
            from alteration_pulled_from_zirc
            where not exists
                    ( select zircfl_line_id
                        from zirc_fish_line
                        where zircfl_line_id = apfz_zircfl_id );");
    }
    writeReport("");
    return ();
}


#----------------------------------------------------------------------
# Report and remove any data that does not pass error checks.
#
# Params
#  $       DBI Database Handle
#
# Returns ()

sub alteration_reportAndRemoveErrors($) {

    my $dbh = $_[0];

    &alteration_checkFishLineIds($dbh);
    &alteration_checkAlterationZdbIds($dbh);

    return ();
}


#----------------------------------------------------------------------
# Drop alterations/zirc fish line combinations that are no longer supplied 
# by ZIRC
#
# Params
#  $       DBI Database Handle
#
# Returns ()

sub alteration_dropDiscontinuedAlterations($) {

    my $dbh = $_[0];

    my $rowCount = 0;

    writeReport("Dropping discontinued alteration/fish line combinations ...");

    # report and delete them
    my $cur = $dbh->prepare("
          select zircflalt_line_id, zircflalt_alt_zdb_id
            from zirc_fish_line_alteration
            where not exists
                    ( select apfz_alt_zdb_id 
                        from alteration_pulled_from_zirc
                        where zircflalt_line_id    = apfz_zircfl_id
                          and zircflalt_alt_zdb_id = apfz_alt_zdb_id)
            order by zircflalt_line_id, zircflalt_alt_zdb_id;");

    my $deleteCur = $dbh->prepare("
           delete from zirc_fish_line_alteration
             where zircflalt_line_id = ?
               and zircflalt_alt_zdb_id = ?;");

    $cur->execute;

    my $lineId;
    my $altZdbId;
    $cur->bind_columns(\$lineId, \$altZdbId);

    while ($cur->fetch) {
        writeReport($lineId . " " . $altZdbId);
        $deleteCur->bind_param(1, $lineId);
        $deleteCur->bind_param(2, $altZdbId);
        $deleteCur->execute();
        $rowCount++;
    }
    
    writeReport("Dropped $rowCount discontinued alteration/fish line combination(s)\n");

    return ();
}


#----------------------------------------------------------------------
# Add alteration/fish line combinations that were not previously supplied by 
# ZIRC.
#
# Params
#  $       DBI Database Handle
#
# Returns ()

sub alteration_addNewlySuppliedAlterations($) {

    my $dbh = $_[0];

    my $rowCount = 0;

    writeReport("Adding newly supplied alteration/fish line combination(s) ...");

    # report and insert them
    my $cur = $dbh->prepare("
          select apfz_zircfl_id, apfz_alt_zdb_id 
            from alteration_pulled_from_zirc
            where not exists
                    ( select *
                        from zirc_fish_line_alteration
                        where zircflalt_alt_zdb_id = apfz_alt_zdb_id
                          and zircflalt_line_id    = apfz_zircfl_id)
            order by apfz_zircfl_id, apfz_alt_zdb_id;");

    my $insertCur = $dbh->prepare("
           insert into zirc_fish_line_alteration
               ( zircflalt_line_id, zircflalt_alt_zdb_id )
             values
               ( ? , ? );");

    $cur->execute;

    my $lineId;
    my $altZdbId;
    $cur->bind_columns(\$lineId, \$altZdbId);

    while ($cur->fetch) {
        writeReport($lineId . " " . $altZdbId);
        $insertCur->bind_param(1, $lineId);
        $insertCur->bind_param(2, $altZdbId);
        $insertCur->execute();
        $rowCount++;
    }
    
    writeReport("Added $rowCount new alteration/fish line combination(s)\n");

    return ();
}


#----------------------------------------------------------------------
# Update alteration / fish line information in ZIRC.
#
# Params
#  $       DBI Database Handle
#
# Returns ()

sub alteration_updateZfin($) {

    my $dbh = $_[0];

    &alteration_dropDiscontinuedAlterations($dbh);
    &alteration_addNewlySuppliedAlterations($dbh);

    return ();
}



#----------------------------------------------------------------------
# alteration_main
#
# download alteration data from ZIRC and update ZFIN to reflect what it says.
#
# Params
#  $       DBI Database Handle
#
# Returns ()
#  Does not return if an error is detected.  In case of an error, the 
#    database will not be updated.

sub alteration_main($) {

    my $dbh = $_[0];
    my $zircAlterationFile = "zircFishLinesAlteration.tab"; # file to download

    &writeReport("****** Pulling fish line alteration information from ZIRC.\n");

    system("rm -f $zircAlterationFile");    # remove old downloaded files
    &downloadFiles($zircAlterationFile);    # get new alteration file

    # create temp table to load list of alterations from ZIRC

    my $cur = $dbh->do('
        create temp table alteration_pulled_from_zirc
          ( 
            apfz_zircfl_id          varchar(8) not null,
            apfz_alt_zdb_id    varchar(50) not null,
            primary key (apfz_zircfl_id, apfz_alt_zdb_id)
          )
          with no log;');

    # parse routine populates the temp table.
    &alteration_parse($dbh, $zircAlterationFile);
    &alteration_reportUnchangedCount($dbh);
    &alteration_reportAndRemoveErrors($dbh);
    &alteration_updateZfin($dbh);

    return ();
}

return 1;
