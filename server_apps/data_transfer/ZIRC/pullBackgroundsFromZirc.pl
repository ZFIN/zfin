#!/private/bin/perl -w
#-----------------------------------------------------------------------
# Subroutines used to pull fish line background information from ZIRC.  
# Background information describes what wildtype lines (and in what 
# percentages) are in the genetic background of ZIRC fish lines.
#
# This file is 'required' by the pullFromZirc.pl script.  This file is not
# meant to be run directly, but only to support the pullFromZirc.pl script.
#
# All subroutines in this file start with the prefix background_.
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

sub background_parse($$) {

    my $dbh = $_[0];
    my $zircFile = $_[1];

    # See equivalent routine in pullEstsFromZirc.pl for why this routine
    # uses a temp table rather than other approaches.

    $/ = "\r";			# control M is input line separator.

    open (ZIRCBACKGROUNDFILE,"$zircFile") || errorExit ("Failed to open $zircFile");
    my $line;

    my $cur = $dbh->prepare("
          insert into background_pulled_from_zirc
              (bpfz_zircfl_id, bpfz_wildtype_zdb_id, bpfz_percent)
            values
              ( ?, ?, ? );");

    while ($line = <ZIRCBACKGROUNDFILE>) {
	chop($line);
	my $zircflId;
	my $wildtypeZdbId;
	my $percent;
	($zircflId, $wildtypeZdbId, $percent) = split(/\t/, $line);
	$cur->bind_param(1, $zircflId);
	$cur->bind_param(2, $wildtypeZdbId);
	$cur->bind_param(3, $percent);
	$cur->execute;
    }
    close (ZIRCBACKGROUNDFILE);

    $/ = '\n';			# restore default input line separator

    return ();
}


#----------------------------------------------------------------------
# Report the number of background records where their status didn't change.
#
# Params
#  $       DBI Database Handle
#
# Returns ()

sub background_reportUnchangedCount($) {

    my $dbh = $_[0];

    my $rowCount = 0;

    # count them
    my $cur = $dbh->prepare("
          select count(*)
            from background_pulled_from_zirc, zirc_fish_line_background
            where zircflback_line_id     = bpfz_zircfl_id
              and zircflback_fish_zdb_id = bpfz_wildtype_zdb_id
              and zircflback_percent     = bpfz_percent;");

    $cur->execute();
    $cur->bind_columns(\$rowCount);
    $cur->fetch();
    &writeReport(int($rowCount) . " background records are unchanged\n");

    if ($rowCount == 0) {
	&errorExit("Aborting run!!!!  Something is probably wrong because the",
		   "  status of EVERY ZIRC background record is changing.");
    }
    return ();
}


#----------------------------------------------------------------------
# Report and remove any records with background ZDB IDs that don't exist in
# ZFIN.
#
# Params
#  $       DBI Database Handle
#
# Returns ()

sub background_checkBackgroundZdbIds($) {

    my $dbh = $_[0];

    my $rowCount = 0;

    writeReport("Checking for unknown background ZDB IDs ...");

    # report them
    my $cur = $dbh->prepare("
          select bpfz_wildtype_zdb_id
            from background_pulled_from_zirc
            where not exists
                    ( select zdb_id 
                        from fish
                        where zdb_id = bpfz_wildtype_zdb_id )
            order by bpfz_wildtype_zdb_id;");

    $cur->execute;

    my $wildtypeZdbId;
    $cur->bind_columns(\$wildtypeZdbId);

    while ($cur->fetch) {
	writeReport($wildtypeZdbId);
	$rowCount++;
    }
    
    writeReport("$rowCount unknown background ZDB ID(s) were found.");

    if ($rowCount) {
	writeReport("Background records with unknown ZDB IDs are dropped " .
		"from input.");

	my $deleteCur = $dbh->do("
          delete
            from background_pulled_from_zirc
            where not exists
                    ( select zdb_id 
                        from fish
                        where zdb_id = bpfz_wildtype_zdb_id );");
    }
    writeReport("");
    return ();
}



#----------------------------------------------------------------------
# Report and remove any records with ZIRC fish line IDs that don't exist in
# ZFIN.  Any ZIRC fish line IDs in the backgrounds file should already be
# defined in the zirc_fish_line table.  (See pullFishLinesFromZirc.pl for
# this process.)
#
# Params
#  $       DBI Database Handle
#
# Returns ()

sub background_checkFishLineIds($) {

    my $dbh = $_[0];

    my $rowCount = 0;

    writeReport("Checking for unknown Fish Line IDs ...");

    # report them
    my $cur = $dbh->prepare("
          select bpfz_zircfl_id
            from background_pulled_from_zirc
            where not exists
                    ( select zircfl_line_id
                        from zirc_fish_line
                        where zircfl_line_id = bpfz_zircfl_id )
            order by bpfz_zircfl_id;");

    $cur->execute;

    my $zircflId;
    $cur->bind_columns(\$zircflId);

    while ($cur->fetch) {
	writeReport($zircflId);
	$rowCount++;
    }
    
    writeReport("$rowCount unknown ZIRC Fish Line ID(s) were found.");

    if ($rowCount) {
	writeReport("Background records with unknown ZIRC Fish Line IDs " .
		    "are dropped from input.");

	my $deleteCur = $dbh->do("
          delete
            from background_pulled_from_zirc
            where not exists
                    ( select zircfl_line_id
                        from zirc_fish_line
                        where zircfl_line_id = bpfz_zircfl_id );");
    }
    writeReport("");
    return ();
}


#----------------------------------------------------------------------
# Report and remove any records where the total background percentage for
# a ZIRC fish line does not add up to 100%.  Note that this check is
# performed AFTER other checks, and it is possible that removing records
# in earlier checks will cause this check to fail as well.
#
# Params
#  $       DBI Database Handle
#
# Returns ()

sub background_checkPercentages($) {

    my $dbh = $_[0];

    my $rowCount = 0;

    writeReport("Checking background percentages add up to 100 ...");

    # report them
    $dbh->do("
          select bpfz_zircfl_id, sum(bpfz_percent) percent
            from background_pulled_from_zirc
            group by bpfz_zircfl_id
            having sum(bpfz_percent) <> 100.0
            order by bpfz_zircfl_id
            into temp bad_total_percentage with no log;");

    my $cur = $dbh->prepare("
          select bpfz_zircfl_id, percent
            from bad_total_percentage;");

    $cur->execute;

    my $zircflId;
    my $percent;
    $cur->bind_columns(\$zircflId, \$percent);

    while ($cur->fetch) {
	writeReport($zircflId . " " . $percent);
	$rowCount++;
    }
    
    writeReport("$rowCount ZIRC Fish Lines were found with incorrect " .
		"total percentages.");

    if ($rowCount) {
	writeReport("Background info for these fish lines " .
		    "are dropped from input.");

	$dbh->do("
          delete
            from background_pulled_from_zirc
            where bpfz_zircfl_id in
                    ( select bpfz_zircfl_id 
                        from bad_total_percentage );");
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

sub background_reportAndRemoveErrors($) {

    my $dbh = $_[0];

    &background_checkFishLineIds($dbh);
    &background_checkBackgroundZdbIds($dbh);
    &background_checkPercentages($dbh);

    return ();
}



#----------------------------------------------------------------------
# Replace existing background information in ZFIN.  This deletes all 
# information and then inserts all new information.  We could do this
# in a much more detailed way: report and process deletes, report and
# process updates, and report and process adds, but I'm just not up 
# for it today.  If we need this information later, we can rewrite 
# this routine.
#
# Params
#  $       DBI Database Handle
#
# Returns ()

sub background_updateZfin($) {

    my $dbh = $_[0];

    my $rowCount = 0;

    writeReport("Updating ZIRC fish line background info in ZFIN ...");

    # delete existing data.
    $dbh->do("
          delete from zirc_fish_line_background;");

    # insert new data, which has already been validated
    $dbh->do("
          insert into zirc_fish_line_background
              ( zircflback_line_id, zircflback_fish_zdb_id, zircflback_percent )
            select bpfz_zircfl_id, bpfz_wildtype_zdb_id, bpfz_percent
              from background_pulled_from_zirc;");
    
    writeReport("ZIRC fish line background info updated in ZFIN.\n");

    return ();
}



#----------------------------------------------------------------------
# background_main
#
# download background data from ZIRC and update ZFIN to reflect what it says.
#
# Params
#  $       DBI Database Handle
#
# Returns ()
#  Does not return if an error is detected.  In case of an error, the 
#    database will not be updated.

sub background_main($) {

    my $dbh = $_[0];
    my $zircBackgroundFile = "zircFishLinesBackground.tab"; # file to download

    &writeReport("****** Pulling fish line background information from ZIRC.\n");

    system("rm -f $zircBackgroundFile");    # remove old downloaded files
    &downloadFiles($zircBackgroundFile);    # get new background file

    # create temp table to load list of backgrounds from ZIRC

    my $cur = $dbh->do('
        create temp table background_pulled_from_zirc
          ( 
            bpfz_zircfl_id          varchar(8) not null,
            bpfz_wildtype_zdb_id    varchar(50) not null,
            bpfz_percent            decimal(7,4),
            primary key (bpfz_zircfl_id, bpfz_wildtype_zdb_id)
          )
          with no log;');

    # parse routine populates the temp table.
    &background_parse($dbh, $zircBackgroundFile);
    &background_reportUnchangedCount($dbh);
    &background_reportAndRemoveErrors($dbh);
    &background_updateZfin($dbh);

    return ();
}

return 1;
