#!/private/bin/perl -w
#-----------------------------------------------------------------------
# Subroutines used to pull Fish Linesfrom ZIRC.  Initially, this just pulls
# fish line IDs.
#
# ZIRC uses fish line IDs for each line of fish that it maintains.
# ZIRC fish line IDs are NOT the same as FISH ZDB IDs in ZFIN.  They 
# are a distinct set of values.  Howerver, there can be a 1:1 mapping between 
# the two for wildtypes and single mutants.
#
# This file is 'required' by the pullFromZirc.pl script.  This file is not
# meant to be run directly, but only to support the pullFromZirc.pl script.
#
# All subroutines in this file start with the prefix fishline_.
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

sub fishline_parse($$) {

    my $dbh = $_[0];
    my $zircFile = $_[1];

    # See equivalent routine in pullEstsFromZirc.pl for why this routine
    # uses a temp table rather than other approaches.

    $/ = "\r";			# control M is input line separator.

    open (ZIRCFLFILE,"$zircFile") || errorExit ("Failed to open $zircFile");
    my $line;

    my $cur = $dbh->prepare("
          insert into fishline_pulled_from_zirc
              (flpfz_zircfl_id)
            values
              ( ? );");

    my $zircflId;
    while ($zircflId = <ZIRCFLFILE>) {
        chop($zircflId);
        $cur->bind_param(1, $zircflId);
        $cur->execute;
    }
    close (ZIRCFLFILE);

    $/ = '\n';			# restore default input line separator

    return ();
}


#----------------------------------------------------------------------
# Report the number of ZIRC fish lines where their status didn't change.
#
# Params
#  $       DBI Database Handle
#
# Returns ()

sub fishline_reportUnchangedCount($) {

    my $dbh = $_[0];

    my $rowCount = 0;

    # count them
    my $cur = $dbh->prepare("
          select count(*)
            from fishline_pulled_from_zirc, zirc_fish_line
            where flpfz_zircfl_id = zircfl_line_id;");

    $cur->execute();
    $cur->bind_columns(\$rowCount);
    $cur->fetch();
    &writeReport(int($rowCount) . " fish lines continued to be supplied by ZIRC\n");

    if ($rowCount == 0) {
	&errorExit("Aborting run!!!!  Something is probably wrong because the",
		   "  status of EVERY ZIRC supplied fish line is changing.");
    }
    return ();
}


#----------------------------------------------------------------------
# Drop fish lines that are no longer supplied by ZIRC
#
# Params
#  $       DBI Database Handle
#
# Returns ()

sub fishline_dropDiscontinuedLines($) {

    my $dbh = $_[0];

    my $rowCount = 0;

    writeReport("Dropping discontinued ZIRC fish lines ...");

    # report and delete them
    my $cur = $dbh->prepare("
          select zircfl_line_id
            from zirc_fish_line
            where not exists
                    ( select flpfz_zircfl_id 
                        from fishline_pulled_from_zirc
                        where zircfl_line_id = flpfz_zircfl_id)
            order by zircfl_line_id;");

    my $deleteCur = $dbh->prepare("
           delete from zirc_fish_line
             where zircfl_line_id = ? ;");

    $cur->execute;

    my $zircflId;
    $cur->bind_columns(\$zircflId);

    while ($cur->fetch) {
	writeReport($zircflId);
	$deleteCur->bind_param(1, $zircflId);
	$deleteCur->execute();
	$rowCount++;
    }
    
    writeReport("Dropped $rowCount discontinued ZIRC fish line(s)\n");

    return ();
}


#----------------------------------------------------------------------
# Add ZIRC fish lines that were not previously supplied by ZIRC.
#
# Params
#  $       DBI Database Handle
#
# Returns ()

sub fishline_addNewlySuppliedLines($) {

    my $dbh = $_[0];

    my $rowCount = 0;

    writeReport("Adding newly supplied ZIRC Fish Lines ...");

    # report and insert them
    my $cur = $dbh->prepare("
          select flpfz_zircfl_id 
            from fishline_pulled_from_zirc
            where not exists
                    ( select *
                        from zirc_fish_line
                        where flpfz_zircfl_id = zircfl_line_id)
            order by flpfz_zircfl_id;");

    my $insertCur = $dbh->prepare("
           insert into zirc_fish_line
               ( zircfl_line_id )
             values
               ( ? );");

    $cur->execute;

    my $zircflId;
    $cur->bind_columns(\$zircflId);

    while ($cur->fetch) {
	writeReport($zircflId);
	$insertCur->bind_param(1, $zircflId);
	$insertCur->execute();
	$rowCount++;
    }
    
    writeReport("Added $rowCount new ZIRC Fish Line(s)\n");

    return ();
}


#----------------------------------------------------------------------
# Update the list of known ZIRC fish line IDs in ZFIN
#
# Params
#  $       DBI Database Handle
#
# Returns ()

sub fishline_updateZfin($) {

    my $dbh = $_[0];

    &fishline_reportUnchangedCount($dbh);
    &fishline_dropDiscontinuedLines($dbh);
 
    &fishline_addNewlySuppliedLines($dbh);

    return ();
}


#----------------------------------------------------------------------
# fishline_main
#
# download list of ZIRC Fish Line IDs
#
# Params
#  $       DBI Database Handle
#
# Returns ()
#  Does not return if an error is detected.  In case of an error, the 
#    database will not be updated.

sub fishline_main($$) {

    my $dbh = $_[0];
    my $zircflFile = "zircFishLinesID.tab"; # file to download

    &writeReport("****** Pulling fish line ID information from ZIRC.\n");

    system("rm -f $zircflFile");    # remove old downloaded files
    &downloadFiles($zircflFile);    # get new wildtype file

    # create temp table to load list of fish IDs from ZIRC

    my $cur = $dbh->do('
        create temp table fishline_pulled_from_zirc
          ( 
            flpfz_zircfl_id varchar(8) not null unique
          )
          with no log;');

    # parse routine populates the temp table.
    &fishline_parse($dbh, $zircflFile);
    &fishline_updateZfin($dbh);

    return ();
}

return 1;
