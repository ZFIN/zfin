#! /private/bin/perl -w 

##
# validatedata.pl
#
# Check the consistence and correctness of data in zfin database
##

use Getopt::Long qw(:config bundling);
use DBI;

#------------------------------------------------------------------------
# Log an error message.
#
# Params 
#  @       List of lines to print out.  List is assumed to be all part of 
#          the same error.
#
# Returns ()

sub logError(@) {

    my $line;
    print "Error:   ";
    foreach $line (@_) {
	print "$line\n";
    }
    print ("\n");
    return ();
}


#------------------------------------------------------------------------
# Log a warning message.
#
# Params 
#  @       List of lines to print out.  List is assumed to be all part of 
#          the same error.
#
# Returns ()

sub logWarning(@) {

    my $line;
    print "Warning: ";
    foreach $line (@_) {
	print "$line\n";
    }
    print ("\n");
    return ();
}

#------------------------------------------------------------------------
# Execute SQL statement(s) and save its results in files.
#  
# Params 
#   $               SQL statement to be executed.
#   $(optional)     Reference of subroutine that does future query if any,
#                   or as 'undef'.
#   @(optional)     Column description for query result.
#  
# Returns 
#   number of rows returned from the query. If column descriptions are defined,
#   the records will be kept in a file. 

sub execSql {

  my $sql = shift;
  my $subSqlRef = shift;
  my @colDesc = @_;
  my $nRecords = 0;
 
  my $sth = $dbh->prepare($sql) or die "Prepare fails";
  
  $sth -> execute() or die "Could not execute $sql";
  
  open RESULT, ">$globalResultFile" or die "Cannot open the file to write check result."
    if @colDesc; 
  
  while (my @row = $sth ->fetchrow_array()) {

    my $valid = 1;
    $valid = $subSqlRef->(@row) if $subSqlRef;
    
    if ($valid) {
      my $i = 0;
      $nRecords ++;

      if (@colDesc) {
	foreach (@row) {
	  $_ = '' unless defined;
	  print RESULT "$colDesc[$i]\t $_\n";
	  $i ++;
	}
      print RESULT "\n";    
      }
    }  
  } 
  close(RESULT) if @colDesc;
  return ($nRecords);
}

sub executeSqlAndPrint {

  my $sql = shift;
  my $subSqlRef = shift;
  my @colDesc = @_;
  my $nRecords = 0;
 
  my $sth = $dbh->prepare($sql) or die "Prepare fails";
  
  $sth -> execute() or die "Could not execute $sql";
  
  open RESULT, ">$globalResultFile" or die "Cannot open the file to write check result."
    if @colDesc; 
  
  while (my @row = $sth ->fetchrow_array()) {

    my $valid = 1;
    $valid = $subSqlRef->(@row) if $subSqlRef;
    
    if ($valid) {
      my $i = 0;
      $nRecords ++;

      if (@colDesc) {
	foreach (@row) {
	  $_ = '' unless defined;
	  print RESULT "$_";
	  if($i + 1 < @colDesc ){
	      print RESULT " : ";
	  }
	  $i ++;
	}
      print RESULT "\n";    
      }
    }  
  } 
  close(RESULT) if @colDesc;
  return ($nRecords);
}


#------------------------------------------------------------------------
#
# Orders column value.  A helper function for orderResults.
#
# Params
#   $		Name of column effect.
#   $   Column data.
# 
# Returns 
#   data string formatted or unformatted. 
sub checkInput(@){

	my $columnLabel = shift  ;
	my $columnData = shift  ;

	my @returnArray  ; 

# if $input starts with $columnLabel
	if( "$columnData" =~ /^$columnLabel/ ){
#    parse tokens around ' ' ; 
		@tokens = split(/ /,$columnData) ; 
		foreach $token (@tokens){
			 if( "$token" =~ /^[0-9]/){
					push(@returnArray,scalar($token)) ; 
			 }
		}
		@returnArray= sort(@returnArray) ;   
		@returnArray = sort { $a <=> $b } @returnArray ; 
		$columnData = $columnLabel . "@returnArray" . "\n" ; 
	}
	return $columnData ; 
}


#------------------------------------------------------------------------
#
# Orders array data. 
#
# Params
#   $		Name of column effect.
#   @   Sql result rows.
# 
# Returns 
#   rows from the original query, ordered in the specified column. 
# 
sub orderResults(@) {

	 my $columnLabel = shift ; 
	 my $formattedData = "" ; 

	 open RESULT, "$globalResultFile" or die "Cannot open the result file for read.\n";
	 while ($input =  <RESULT>) {
		 $input = checkInput( $columnLabel, $input) ; 
		 $formattedData = $formattedData . $input ; 
	 }
	 close (RESULT);

	 print "$formattedData" ; 


	 open FORMATTED, ">$globalResultFile" or die "Cannot open the result file for read.\n";
	 print FORMATTED $formattedData ; 
	 close(FORMATTED) ; 

}

#------------------------------------------------------------------------    
# Send the check result 
#
# Params
#   $       Recipient addresses
#   $       Email subject
#   $       Subroutine title
#   $       Error Message 
#   @       Queries for the check
#
# Returns () 

sub sendMail(@) {

     my $sendToAddress = shift;
     my $subject = shift;
     my $rtnName = shift;
     my $msg = shift;
     my @sql = @_; 

     open MAIL, "|/usr/lib/sendmail -t";

     print MAIL "To: $sendToAddress\n";
     print MAIL "Subject: validatedata on <!--|DB_NAME|-->: $subject\n";

     print MAIL "$msg\n";

     # add stats from last run, conditionally
     if ($rtnName ne "void") {
	 # get the checking result from last run
	 my $query = "select vldcheck_count, vldcheck_date
                        from validate_check_history
                       where vldcheck_name = '$rtnName' ";

	 my ($preNum,$preDate) = $dbh->selectrow_array($query);

	 if ($preNum) {
	     print MAIL "(Last run at $preDate got $preNum records.)";
	 }
     }
     print MAIL "\n\n";

     # paste all the result records
     open RESULT, "$globalResultFile" or die "Cannot open the result file for read.\n";
     while (<RESULT>) {
       print MAIL;
     }
     close RESULT;
 
     print MAIL "============================================================\n";
     print MAIL "SQL: \n";
     my $sql;
     foreach $sql (@sql) {
       print MAIL "$sql\n";
     }
     close MAIL;
     
     return();
   }

#------------------------------------------------------------------------
# Record the checking result into validate_check_history
#
# Params
#    $          Checking routine name
#    $          Number of checkouts
#
# Returns ()

sub recordResult(@) {

  my $rtnName = shift;
  my $rcdNum = shift;

  my $sql = "select vldcheck_name
               from validate_check_history
              where vldcheck_name = '$rtnName' ";

  my $exist = $dbh->selectrow_array($sql);
  if ($exist){
    my $sth = $dbh->do("update validate_check_history 
                        set (vldcheck_count, vldcheck_date) = ('$rcdNum', CURRENT)
                       where vldcheck_name = '$rtnName' " );
  }
  else {
    my $sth = $dbh->do(" insert into validate_check_history
                          (vldcheck_name, vldcheck_count, vldcheck_date) values
                           ('$rtnName', '$rcdNum', CURRENT) ");
    }
  return ();
}


#######################  Checking  ##################################





#=================== Anatomy Item  =================================

#----------------------------------------------------------------
#Parameter
#  $ - Email Address for recipient
#  $ - Curator first name
#
# For each zfin curator, query if there are any close papers by the curator
# having 'unspecified' as an entity in phenotype annotation.

sub phenotypeAnnotationUnspecified ($$) {
	
  my $routineName = "phenotypeAnnotationUnspecified";
       
  my $sql = "select distinct fig_source_zdb_id from figure
        join curation on fig_source_zdb_id = cur_pub_zdb_id
        join person   on cur_curator_zdb_id = zdb_id
        where
        cur_topic='Phenotype'
        AND cur_closed_date is not null
        AND fig_zdb_id in (
        select phenox_fig_zdb_id from phenotype_experiment where not exists
        (select * from phenotype_statement  where phenos_phenox_pk_id = phenox_pk_id))
        and email = '$_[0]'";

  my @colDesc = ("Publication zdb id ");

  my $nRecords = execSql ($sql, undef, @colDesc);

  if ( $nRecords > 0 ) {

    my $sendToAddress = $_[0]; 
    my $curatorFirstName = $_[1];
    my $subject = "Phenotype annotation to 'unspecified' entity";
    my $errMsg = "Dear $curatorFirstName, please check the use of 'unspecified' as an entity in phenotype annotation in the following publication(s).";
      		       
    logError ($errMsg);
    &sendMail($sendToAddress, $subject, "void", $errMsg, $sql);
  }
  
} 



#---------------------------------------------------------------
# strAbbrevContainsGeneAbbrev
#
# Parameter
# $ Email Address for recipients

sub strAbbrevContainsGeneAbbrevBasic($) {
  my $routineName = "strAbbrevContainsGeneAbbrevBasic";
	
  my $sql = "select a.mrkr_abbrev, b.mrkr_abbrev
               from marker a, marker b, marker_relationship c
               where a.mrkr_zdb_id = c.mrel_mrkr_1_zdb_id
               and b.mrkr_zdb_id = c.mrel_mrkr_2_zdb_id
    and exists (Select 'x' from marker_type_group_member where
                              a.mrkr_type = mtgrpmem_mrkr_type
                              and mtgrpmem_mrkr_type_group = 'KNOCKDOWN_REAGENT')
                and b.mrkr_abbrev !=
               (substring(a.mrkr_abbrev 
                            from
                             (length(a.mrkr_abbrev)-length(b.mrkr_abbrev)+1)
                            for
                             (length(b.mrkr_abbrev))
                          )
                )
              order by b.mrkr_abbrev";

  my @colDesc = ("STR abbrev         ",
		 "Gene abbrev       ");

  my $nRecords = execSql ($sql, undef, @colDesc);

  if ( $nRecords > 0 ) {
    my $sendToAddress = $_[0];
    my $subject = "Str abbrev not like gene_abbrev";
    my $errMsg = "There are $nRecords strs without corresponding gene abbrevs. ";
    
    logError ($errMsg);
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql);
  }
  &recordResult($routineName, $nRecords);

}


#======================== PUB Attribution ========================

#---------------------------------------------------------------
# associatedAltDataforPUB030508_1
#
# Only data for gene name, gene symbol
# abbreviation or previous name should be associated with
# ZDB-PUB-030508-1.
# 
# 
#Parameter
# $      Email Address for recipients
# 

sub associatedAltDataforPUB030508_1 ($) {

  my $routineName = "associatedAltDataforPUB030508_1";

  my $sql = "select recattrib_data_zdb_id, feature_name, feature_abbrev
             from   record_attribution, feature
             where  recattrib_source_zdb_id = 'ZDB-PUB-030508-1'
             and recattrib_datA_zdb_id = feature_zdb_id
             
             and    not exists (
                       select mrkr_zdb_id
                       from   marker
                       where  recattrib_data_zdb_id = mrkr_zdb_id
                    )
             and    not exists (
                       select dalias_zdb_id
                       from   data_alias
                       where  recattrib_data_zdb_id = dalias_zdb_id
                    )";

  my @colDesc = ("Attributed ZDB ID       ",
		 "feature name       ",
		 "feature symbol      ");
  
  my $nRecords = execSql ($sql, undef, @colDesc);
	
  if ( $nRecords > 0 ) {
    my $sendToAddress = $_[0];
    my $subject = "Invalid feature data is associated with ZDB-PUB-030508-1.";
    my $errMsg = "$nRecords data are associated with ZDB-PUB-030508-1 "
               . " that are not either: gene name, gene symbol "
               . ", or previous name.";

    logError ($errMsg);
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql);  
  }
  &recordResult($routineName, $nRecords); 
}

#---------------------------------------------------------------
# associatedDblinkDataforPUB030508_1
#
# Only data for gene name, gene symbol
# abbreviation or previous name should be associated with
# ZDB-PUB-030508-1.
# 
# 
#Parameter
# $      Email Address for recipients
# 

sub associatedDblinkDataforPUB030508_1 ($) {

  my $routineName = "associatedDblinkDataforPUB030508_1";

  my $sql = "select recattrib_data_zdb_id, dblink_linked_recid, dblink_acc_num
             from   record_attribution, db_link
             where  recattrib_source_zdb_id = 'ZDB-PUB-030508-1'
             and recattrib_datA_zdb_id = dblink_zdb_id
             
             and    not exists (
                       select mrkr_zdb_id
                       from   marker
                       where  recattrib_data_zdb_id = mrkr_zdb_id
                    )
             and    not exists (
                       select dalias_zdb_id
                       from   data_alias
                       where  recattrib_data_zdb_id = dalias_zdb_id
                    )";

  my @colDesc = ("Attributed ZDB ID       ",
		 "marker zdb_id       ",
		 "accession number      ");
  
  my $nRecords = execSql ($sql, undef, @colDesc);
	
  if ( $nRecords > 0 ) {
    my $sendToAddress = $_[0];
    my $subject = "Invalid link data is associated with ZDB-PUB-030508-1.";
    my $errMsg = "$nRecords data are associated with ZDB-PUB-030508-1 "
               . " that are not either: gene name, gene symbol "
               . ", or previous name.";

    logError ($errMsg);
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql);  
  }
  &recordResult($routineName, $nRecords); 
}

#---------------------------------------------------------------
# associatedOevDataforPUB030508_1
#
# Only data for gene name, gene symbol
# abbreviation or previous name should be associated with
# ZDB-PUB-030508-1.
# 
# 
#Parameter
# $      Email Address for recipients
# 

sub associatedOevDataforPUB030508_1 ($) {

  my $routineName = "associatedOevDataforPUB030508_1";

  my $sql = "select recattrib_data_zdb_id, oevdisp_gene_zdb_id, oevdisp_organism_list, oevdisp_evidence_code
             from   record_attribution, orthologue_evidence_display
             where  recattrib_source_zdb_id = 'ZDB-PUB-030508-1'
             and recattrib_datA_zdb_id = oevdisp_zdb_id
             
             and    not exists (
                       select mrkr_zdb_id
                       from   marker
                       where  recattrib_data_zdb_id = mrkr_zdb_id
                    )
             and    not exists (
                       select dalias_zdb_id
                       from   data_alias
                       where  recattrib_data_zdb_id = dalias_zdb_id
                    )";

  my @colDesc = ("Attributed ZDB ID       ",
		 "Gene  ZDB ID       ",
		 "organism      ",
                 "evidence code   ");
  
  my $nRecords = execSql ($sql, undef, @colDesc);
	
  if ( $nRecords > 0 ) {
    my $sendToAddress = $_[0];
    my $subject = "Invalid ortho data is associated with ZDB-PUB-030508-1.";
    my $errMsg = "$nRecords data are associated with ZDB-PUB-030508-1 "
               . " that are not either: gene name, gene symbol "
               . ", or previous name.";

    logError ($errMsg);
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql);  
  }
  &recordResult($routineName, $nRecords); 
}



#---------------------------------------------------------------
# associatedDBLinkDataforPUB030905_2
#
# Data, other than nucleotide sequence accession numbers, associated with ZDB-PUB-030905-2.

# The only data that should be attributed should be sequence accessions (or their markers) 
# of type Genomic, RNA, or Sequence Clusters.
# 
#Parameter
# $      Email Address for recipients
# 

sub associatedDBLinkDataforPUB030905_2 ($) {

  my $routineName = "associatedDBLinkDataforPUB030905_2";

  my $sql = "select recattrib_data_zdb_id, dblink_linked_recid, dblink_acc_num
             from   record_attribution r1, db_link
             where  r1.recattrib_source_zdb_id = 'ZDB-PUB-030905-2'
             and    r1.recattrib_data_zdb_id = dblink_zdb_id
             and    not exists (
                    -- all nucleotide accession numbers assoc. w/pub via dblink_zdb_id (DBLINK)
                       select recattrib_data_zdb_id
                       from   db_link, record_attribution r2, foreign_db_contains, foreign_db_data_type
                       where  recattrib_source_zdb_id = 'ZDB-PUB-030905-2'
                       and    dblink_zdb_id = recattrib_data_zdb_id
                       and    fdbcont_fdbdt_id = fdbdt_pk_id
                       and    dblink_fdbcont_zdb_id = fdbcont_zdb_id
                       and    fdbdt_data_type in ('Genomic','RNA','Sequence Clusters')
                       and    r1.recattrib_data_zdb_id = r2.recattrib_data_zdb_id
                     union
                    -- all nucleotide accession numbers assoc. w/pub via dblink_linked_recid (GENE)
                       select recattrib_data_zdb_id
                       from   db_link, record_attribution r3, foreign_db_contains, foreign_db_data_type
                       where  recattrib_source_zdb_id = 'ZDB-PUB-030905-2'
                       and    dblink_linked_recid = recattrib_data_zdb_id
                       and    dblink_fdbcont_zdb_id = fdbcont_zdb_id
                       and    fdbcont_fdbdt_id = fdbdt_pk_id
                       and    fdbdt_data_type in ('Genomic','RNA','Sequence Clusters')
                       and    r1.recattrib_data_zdb_id = r3.recattrib_data_zdb_id
                    )  
             order by recattrib_data_zdb_id";


  my @colDesc = ("Attributed ZDB ID       ",
                 "Data ZDB ID             ",
                 "Accession number        ");
  
  my $nRecords = execSql ($sql, undef, @colDesc);
	
  if ( $nRecords > 0 ) {
    my $sendToAddress = $_[0];
    my $subject = "Invalid dblink data is associated with ZDB-PUB-030905-2.";
    my $errMsg = "$nRecords dblink data are associated with ZDB-PUB-030905-2 "
               . " that are not nucleotide sequence accession numbers "
               . " (i.e. not Genomic, RNA or Sequence Clusters.)";

    logError ($errMsg);
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql);  
  }
  &recordResult($routineName, $nRecords); 
}

#---------------------------------------------------------------
# associatedAliasDataforPUB030905_2
#
# Data, other than nucleotide sequence accession numbers, associated with ZDB-PUB-030905-2.

# The only data that should be attributed should be sequence accessions (or their markers) 
# of type Genomic, RNA, or Sequence Clusters.
# 
#Parameter
# $      Email Address for recipients
# 

sub associatedAliasDataforPUB030905_2 ($) {

  my $routineName = "associatedAliasDataforPUB030905_2";

  my $sql = "select recattrib_data_zdb_id, dalias_alias, dalias_data_zdb_id
             from   record_attribution r1, data_alias
             where  recattrib_source_zdb_id = 'ZDB-PUB-030905-2'
             and recattrib_data_zdb_id = dalias_zdb_id
             and    not exists (
                    -- all nucleotide accession numbers assoc. w/pub via dblink_zdb_id (DBLINK)
                       select recattrib_data_zdb_id
                       from   db_link, record_attribution r2, foreign_db_contains, foreign_db_data_type
                       where  recattrib_source_zdb_id = 'ZDB-PUB-030905-2'
                       and    dblink_zdb_id = recattrib_data_zdb_id
                       and    fdbcont_fdbdt_id = fdbdt_pk_id
                       and    dblink_fdbcont_zdb_id = fdbcont_zdb_id
                       and    fdbdt_data_type in ('Genomic','RNA','Sequence Clusters')
                       and    r1.recattrib_data_zdb_id = r2.recattrib_data_zdb_id
                     union
                    -- all nucleotide accession numbers assoc. w/pub via dblink_linked_recid (GENE)
                       select recattrib_data_zdb_id
                       from   db_link, record_attribution r3, foreign_db_contains, foreign_db_data_type
                       where  recattrib_source_zdb_id = 'ZDB-PUB-030905-2'
                       and    dblink_linked_recid = recattrib_data_zdb_id
                       and    dblink_fdbcont_zdb_id = fdbcont_zdb_id
                       and    fdbcont_fdbdt_id = fdbdt_pk_id
                       and    fdbdt_data_type in ('Genomic','RNA','Sequence Clusters')
                       and    r1.recattrib_data_zdb_id = r3.recattrib_data_zdb_id
                    )  
             order by recattrib_data_zdb_id";


  my @colDesc = ("Attributed ZDB ID       ",
                 "Alias          ",
                 "Data ZDB ID        ");
  
  my $nRecords = execSql ($sql, undef, @colDesc);
	
  if ( $nRecords > 0 ) {
    my $sendToAddress = $_[0];
    my $subject = "Invalid alias data is associated with ZDB-PUB-030905-2.";
    my $errMsg = "$nRecords alias data are associated with ZDB-PUB-030905-2 "
               . " that are not nucleotide sequence accession numbers "
               . " (i.e. not Genomic, RNA or Sequence Clusters.)";

    logError ($errMsg);
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql);  
  }
  &recordResult($routineName, $nRecords); 
}

#---------------------------------------------------------------
# associatedMrelDataforPUB030905_2
#
# Data, other than nucleotide sequence accession numbers, associated with ZDB-PUB-030905-2.

# The only data that should be attributed should be sequence accessions (or their markers) 
# of type Genomic, RNA, or Sequence Clusters.
# 
#Parameter
# $      Email Address for recipients
# 

sub associatedMrelDataforPUB030905_2 ($) {

  my $routineName = "associatedMrelDataforPUB030905_2";

  my $sql = "select recattrib_data_zdb_id, mrel_mrkr_1_zdb_id, mrel_mrkr_2_zdb_id, mrel_type
             from   record_attribution r1, marker_relationship
             where  recattrib_source_zdb_id = 'ZDB-PUB-030905-2'
             and recattrib_data_zdb_id = mrel_zdb_id
             and    not exists (
                    -- all nucleotide accession numbers assoc. w/pub via dblink_zdb_id (DBLINK)
                       select recattrib_data_zdb_id
                       from   db_link, record_attribution r2, foreign_db_contains, foreign_db_data_type
                       where  recattrib_source_zdb_id = 'ZDB-PUB-030905-2'
                       and    dblink_zdb_id = recattrib_data_zdb_id
                       and    fdbcont_fdbdt_id = fdbdt_pk_id
                       and    dblink_fdbcont_zdb_id = fdbcont_zdb_id
                       and    fdbdt_data_type in ('Genomic','RNA','Sequence Clusters')
                       and    r1.recattrib_data_zdb_id = r2.recattrib_data_zdb_id
                     union
                    -- all nucleotide accession numbers assoc. w/pub via dblink_linked_recid (GENE)
                       select recattrib_data_zdb_id
                       from   db_link, record_attribution r3, foreign_db_contains, foreign_db_data_type
                       where  recattrib_source_zdb_id = 'ZDB-PUB-030905-2'
                       and    dblink_linked_recid = recattrib_data_zdb_id
                       and    dblink_fdbcont_zdb_id = fdbcont_zdb_id
                       and    fdbcont_fdbdt_id = fdbdt_pk_id
                       and    fdbdt_data_type in ('Genomic','RNA','Sequence Clusters')
                       and    r1.recattrib_data_zdb_id = r3.recattrib_data_zdb_id
                    )  
             order by recattrib_data_zdb_id";


  my @colDesc = ("Attributed ZDB ID       ",
                 "marker 1          ",
                 "marker 2        ",
                 "relationship type     ");
  
  my $nRecords = execSql ($sql, undef, @colDesc);
	
  if ( $nRecords > 0 ) {
    my $sendToAddress = $_[0];
    my $subject = "Invalid mrel data is associated with ZDB-PUB-030905-2.";
    my $errMsg = "$nRecords mrel data are associated with ZDB-PUB-030905-2 "
               . " that are not nucleotide sequence accession numbers "
               . " (i.e. not Genomic, RNA or Sequence Clusters.)";

    logError ($errMsg);
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql);  
  }
  &recordResult($routineName, $nRecords); 
}


#---------------------------------------------------------------
# associatedOrthoEvidenceDataforPUB030905_2
#
# Data, other than nucleotide sequence accession numbers, associated with ZDB-PUB-030905-2.

# The only data that should be attributed should be sequence accessions (or their markers) 
# of type Genomic, RNA, or Sequence Clusters.
# 
#Parameter
# $      Email Address for recipients
# 

sub associatedOrthoEvidenceDataforPUB030905_2 ($) {

  my $routineName = "associatedOrthoEvidenceDataforPUB030905_2";

  my $sql = "select recattrib_data_zdb_id, oevdisp_gene_zdb_id, oevdisp_evidence_code, oevdisp_organism_list
             from   record_attribution r1, orthologue_evidence_display
             where  recattrib_source_zdb_id = 'ZDB-PUB-030905-2'
             and recattrib_data_zdb_id = oevdisp_zdb_id
             and    not exists (
                    -- all nucleotide accession numbers assoc. w/pub via dblink_zdb_id (DBLINK)
                       select recattrib_data_zdb_id
                       from   db_link, record_attribution r2, foreign_db_contains, foreign_db_data_type
                       where  recattrib_source_zdb_id = 'ZDB-PUB-030905-2'
                       and    dblink_zdb_id = recattrib_data_zdb_id
                       and    fdbcont_fdbdt_id = fdbdt_pk_id
                       and    dblink_fdbcont_zdb_id = fdbcont_zdb_id
                       and    fdbdt_data_type in ('Genomic','RNA','Sequence Clusters')
                       and    r1.recattrib_data_zdb_id = r2.recattrib_data_zdb_id
                     union
                    -- all nucleotide accession numbers assoc. w/pub via dblink_linked_recid (GENE)
                       select recattrib_data_zdb_id
                       from   db_link, record_attribution r3, foreign_db_contains, foreign_db_data_type
                       where  recattrib_source_zdb_id = 'ZDB-PUB-030905-2'
                       and    dblink_linked_recid = recattrib_data_zdb_id
                       and    dblink_fdbcont_zdb_id = fdbcont_zdb_id
                       and    fdbcont_fdbdt_id = fdbdt_pk_id
                       and    fdbdt_data_type in ('Genomic','RNA','Sequence Clusters')
                       and    r1.recattrib_data_zdb_id = r3.recattrib_data_zdb_id
                    )  
             order by recattrib_data_zdb_id";


  my @colDesc = ("Attributed ZDB ID       ",
                 "gene zdb_id          ",
                 "evidence code     ",
                 "organism list     ");
  
  my $nRecords = execSql ($sql, undef, @colDesc);
	
  if ( $nRecords > 0 ) {
    my $sendToAddress = $_[0];
    my $subject = "Invalid ortho data is associated with ZDB-PUB-030905-2.";
    my $errMsg = "$nRecords ortho data are associated with ZDB-PUB-030905-2 "
               . " that are not nucleotide sequence accession numbers "
               . " (i.e. not Genomic, RNA or Sequence Clusters.)";

    logError ($errMsg);
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql);  
  }
  &recordResult($routineName, $nRecords); 
}



#######################  Main ###########################################
#
# Define Usage 
# 

$usage = "Usage: validatedata.pl <dbname> [-d|-w|-m|-o] ";

$document = <<ENDDOC;

$usage

Command line parameters:

  dbname   Name of database to validate. This must be a ZFIN database.
  -d       Excute the checks supposed to run daily.  
  -w       Excute the checks supposed to run weekly.
  -m       Excute the checks supposed to run monthly.
  -y       Execute the checks supposed to run yearly.

ENDDOC

if (@ARGV < 2) {
  print $document and exit 1;
}

GetOptions (
	    "d"    => \$daily,
	    "w"    => \$weekly,
	    "m"    => \$monthly,
            "y"    => \$yearly
	    );

#
# Define GLOBALS
#
			
$globalDbName = $ARGV[0]; #"<!--|DB_NAME|-->";
$globalUsername = "";
$globalPassword = "";

$globalResultFile = "/tmp/<!--|DB_NAME|-->"."checkresult.txt";

#set environment variables
$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";

$dbh = DBI->connect("DBI:Informix:$globalDbName", 
			      $globalUsername, 
			      $globalPassword) 
       or die "Cannot connect to Informix database $globalDbName:$DBI::errstr\n";


#$globalWorkingDir = "/tmp/validatedata";  # Created & deleted by this script.
#my $dirPerms = oct(770);
#mkdir($globalWorkingDir, $dirPerms);

# Each routine below:
#  runs some SQL
#  checks results -- usually the number of row
#  if results are NOT as expected
#    print error message
#    send error message and query results to sb.
#  endif

my $geneEmail    = "<!--|VALIDATION_EMAIL_GENE|-->";
my $dbaEmail     = "<!--|VALIDATION_EMAIL_DBA|-->";
my $strEmail = "<!--|VALIDATION_EMAIL_MORPHOLINO|-->";


if($daily) {
}
if($weekly) {
  # put these here until we get them down to 0 records.  Then move them to 
  # daily.
	# each bit of the 030508_1 needs different data report to allow curators to clean up.  the generic one goes to DBA.
	associatedAltDataforPUB030508_1($geneEmail);
	associatedOevDataforPUB030508_1($geneEmail);
	associatedDblinkDataforPUB030508_1($geneEmail);

	# each bit of the 030905_2 needs different data report to allow curators to clean up.  the generic one goes to DBA.
	associatedOrthoEvidenceDataforPUB030905_2($geneEmail);
	associatedAliasDataforPUB030905_2($geneEmail);
	associatedMrelDataforPUB030905_2($geneEmail);
	associatedDBLinkDataforPUB030905_2($geneEmail);


	# put these here until we get them down to 0 records.  Then move them to 
	# daily.
	# changed to monthly strAbbrevContainsGeneAbbrev($strEmail);

}
if($monthly) {
    # for each zfin curator, run phenotypeAnnotationUnspecified() check
    my $sql = " select email, full_name
                from int_person_lab 
                     join person on source_id = zdb_id
                     join lab_position on position_id =  labpos_pk_id
               where target_id = 'ZDB-LAB-000914-1'
                 and labpos_position = 'Research Staff'";
    my $sth = $dbh->prepare ($sql) or die "Prepare fails";
    $sth->execute();
    
    while (my ($curatorEmail, $curatorName) = $sth->fetchrow_array()) {
	
	if (!$curatorEmail){
	    $curatorEmail = "<!--|VALIDATION_EMAIL_OTHER|-->";
	}
	my @curatorName = split(/,/,$curatorName);
	my $curatorFirstName = $curatorName[$#curatorName];
	$curatorFirstName =~ s/^\s+//;
	phenotypeAnnotationUnspecified ($curatorEmail, $curatorFirstName);
    }
}
if ($yearly) {
    
    strAbbrevContainsGeneAbbrevBasic($strEmail);
}
	   

#rmdir($globalWorkingDir);

