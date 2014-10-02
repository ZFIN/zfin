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



#====================== Expression ================================
# Parameter
# $      Email Address for recipients
#
# Check if any expression annotation has a non-gene & non-EFG
# object in xpatex_gene_zdb_id field

sub xpatObjectNotGeneOrEFG ($) {

    my $routineName = "xpatObjectNotGeneOrEFG";
    my $sql = '
         select xpatex_source_zdb_id, xpatex_gene_zdb_id, mrkr_abbrev
           from expression_experiment, marker, marker_type_group_member
          where xpatex_gene_zdb_id = mrkr_zdb_id 
            and not exists (
                  select "t"
                    from marker_type_group_member
                   where mtgrpmem_mrkr_type_group = "GENEDOM_AND_EFG"
                     and mtgrpmem_mrkr_type = mrkr_type)' ;

  my @colDesc = (
		 "Publication ID:     ",
		 "Xpat object ID:     ",
		 "Xpat object name:   "
		);

  my $nRecords = execSql($sql, undef, @colDesc);
  
  if ( $nRecords > 0 ) {
      
      my $sendToAddress = $_[0];
      my $subject = "Gene expression object is not GENE or EFG";
      my $errMsg = "In expression_experiment, $nRecords records with objects  "
	  ."that are neither gene or engineered foreign gene.";
      
      logError ($errMsg);	
      &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql);   
  }
  &recordResult($routineName, $nRecords);
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


#----


#---------------------------------------------------------------
# constructNameNotSubstringOfFeatureName
#
# Parameter
# $ Email Address for recipients

sub constructNameNotSubstringOfFeatureName ($) {
  my $routineName = "constructNameNotSubstringOfFeatureName";
	
  my $sql = "select mrkr_zdb_id, mrkr_name, mrkr_abbrev, feature_zdb_id, feature_name
             from marker, feature_marker_relationship, feature
              where mrkr_type in ('PTCONSTRCT','ETCONSTRCT','GTCONSTRCT','TGCONSTRCT')
             and feature_name not like mrkr_name||'%'
             and feature_zdb_id = fmrel_ftr_zdb_id
             and mrkr_zdb_id = fmrel_mrkr_zdb_id
             and fmrel_type like 'contains%'
             and (feature_name like 'Tg%' or feature_name like 'Pt%' or feature_name like 'Et%' or feature_name like 'Gt%')";

  my @colDesc = ("Marker Zdb ID          ",
                 "Marker Name          ",
                 "Marker Abbrev    ",
                 "Feature Zdb ID         ",
		 "Feature Name       ");

  my $nRecords = execSql ($sql, undef, @colDesc);

  if ( $nRecords > 0 ) {
    my $sendToAddress = $_[0];
    my $subject = "Constructs whose names are not substrings of related features";
    my $errMsg = "The following constructs names are not substrings of their related feature names";
    
    logError ($errMsg);
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql);
  }
  &recordResult($routineName, $nRecords);

}

#---------------------------------------------------------------
# pubClosedGenoHandleDoesNotEqualGenoNickname
#
# Parameter
# $ Email Address for recipients

sub pubClosedGenoHandleDoesNotEqualGenoNickname($) {
  my $routineName = "pubClosedGenoHandleDoesNotEqualGenoNickname";
	
  my $sql = 'select distinct geno_nickname, geno_handle, zdb_id
               from genotype, publication, record_attribution
               where geno_handle !=  geno_nickname
               and geno_zdb_id = recattrib_data_zdb_id
                            and zdb_id = recattrib_source_zdb_id
                            and pub_completion_date is not null';

  my @colDesc = ("Genotype nickname         ",
		 "Genotype handle       ",
                 "Pub id                ");

  my $nRecords = execSql ($sql, undef, @colDesc);

  if ( $nRecords > 0 ) {
    my $sendToAddress = $_[0];
    my $subject = "Genotype(s) have handles that do not equal nicknames";
    my $errMsg = "There are $nRecords genotype records where handle != nickname and pub is closed . ";
    
    logError ($errMsg);
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql);
  }
  &recordResult($routineName, $nRecords);

}

#======================== PUB Attribution ========================

#---------------------------------------------------------------
# associatedDBlinkDataforPUB030905_1
# 
# 
#Parameter
# $      Email Address for recipients
# 

sub associatedDBLinkforPUB030905_1 ($) {

  my $routineName = "associatedDBLinkDataforPUB030905_1";

  my $sql = "select recattrib_data_Zdb_id, dblink_linked_recid, dblink_acc_num
             from   record_attribution, db_link
             where  recattrib_source_zdb_id = 'ZDB-PUB-030905-1'
             and dblink_zdb_id = recattrib_data_zdb_id
             and    recattrib_data_Zdb_id not in (
             select oevdisp_gene_zdb_id
             from   orthologue_evidence_display
             where  exists (
                       select recattrib_data_Zdb_id
                       from   record_attribution
                       where  recattrib_source_zdb_id = 'ZDB-PUB-030905-1'
                       and    oevdisp_gene_zdb_id = recattrib_data_Zdb_id
                    )
             and    exists (
                       select recattrib_data_Zdb_id
                       from   record_attribution
                       where  recattrib_source_zdb_id = 'ZDB-PUB-030905-1'
                       and    oevdisp_zdb_id = recattrib_data_Zdb_id
                    )
             union
             select oevdisp_zdb_id
             from   orthologue_evidence_display
             where  exists (
                       select recattrib_data_Zdb_id
                       from   record_attribution
                       where  recattrib_source_zdb_id = 'ZDB-PUB-030905-1'
                       and    oevdisp_gene_zdb_id = recattrib_data_Zdb_id
                    )
             and    exists (
                       select recattrib_data_Zdb_id
                       from   record_attribution
                       where  recattrib_source_zdb_id = 'ZDB-PUB-030905-1'
                       and    oevdisp_zdb_id = recattrib_data_Zdb_id
                    )
             )";

  my @colDesc = ("Attributed ZDB ID       ",
                 "data ZDB ID             ",
                 "accession number        ");
  
  my $nRecords = execSql ($sql, undef, @colDesc);
	
  if ( $nRecords > 0 ) {
    my $sendToAddress = $_[0];
    my $subject = "Invalid dblink data is associated with ZDB-PUB-030905-1.";
    my $errMsg = "$nRecords dblink data are associated with ZDB-PUB-030905-1"
               . " that either do not have an attributed GENE or"
               . " do not have attributed orthologue evidence.";

    logError ($errMsg);
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql);  
  }
  &recordResult($routineName, $nRecords); 
}

#---------------------------------------------------------------
# associatedAliasDataforPUB030905_1
#
# 
# 
#Parameter
# $      Email Address for recipients
# 

sub associatedAliasDataforPUB030905_1 ($) {

  my $routineName = "associatedAliasDataforPUB030905_1";

  my $sql = "select recattrib_data_Zdb_id, dalias_data_zdb_id, dalias_alias
             from   record_attribution, data_alias
             where  recattrib_source_zdb_id = 'ZDB-PUB-030905-1'
             and dalias_zdb_id = recattrib_data_zdb_id
             and    recattrib_data_Zdb_id not in (
             select oevdisp_gene_zdb_id
             from   orthologue_evidence_display
             where  exists (
                       select recattrib_data_Zdb_id
                       from   record_attribution
                       where  recattrib_source_zdb_id = 'ZDB-PUB-030905-1'
                       and    oevdisp_gene_zdb_id = recattrib_data_Zdb_id
                    )
             and    exists (
                       select recattrib_data_Zdb_id
                       from   record_attribution
                       where  recattrib_source_zdb_id = 'ZDB-PUB-030905-1'
                       and    oevdisp_zdb_id = recattrib_data_Zdb_id
                    )
             union
             select oevdisp_zdb_id
             from   orthologue_evidence_display
             where  exists (
                       select recattrib_data_Zdb_id
                       from   record_attribution
                       where  recattrib_source_zdb_id = 'ZDB-PUB-030905-1'
                       and    oevdisp_gene_zdb_id = recattrib_data_Zdb_id
                    )
             and    exists (
                       select recattrib_data_Zdb_id
                       from   record_attribution
                       where  recattrib_source_zdb_id = 'ZDB-PUB-030905-1'
                       and    oevdisp_zdb_id = recattrib_data_Zdb_id
                    )
             )";

  my @colDesc = ("Attributed ZDB ID       ",
                 "data ZDB ID             ",
                 "alias        ");
  
  my $nRecords = execSql ($sql, undef, @colDesc);
	
  if ( $nRecords > 0 ) {
    my $sendToAddress = $_[0];
    my $subject = "Invalid aliases data is associated with ZDB-PUB-030905-1.";
    my $errMsg = "$nRecords alias data are associated with ZDB-PUB-030905-1"
               . " that either do not have an attributed GENE or"
               . " do not have attributed orthologue evidence.";

    logError ($errMsg);
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql);  
  }
  &recordResult($routineName, $nRecords); 
}
#---------------------------------------------------------------
# associatedMrkrGoevDataforPUB030905_1
#
# 
#Parameter
# $      Email Address for recipients
# 

sub associatedMrkrGoevDataforPUB030905_1 ($) {

  my $routineName = "associatedMrkrGoevDataforPUB030905_1";

  my $sql = "select recattrib_data_Zdb_id, mrkrgoev_mrkr_zdb_id, mrkrgoev_evidence_code, mrkrgoev_term_zdb_id
             from   record_attribution, marker_go_term_evidence
             where  recattrib_source_zdb_id = 'ZDB-PUB-030905-1'
             and mrkrgoev_zdb_id = recattrib_data_zdb_id
             and    recattrib_data_Zdb_id not in (
             select oevdisp_gene_zdb_id
             from   orthologue_evidence_display
             where  exists (
                       select recattrib_data_Zdb_id
                       from   record_attribution
                       where  recattrib_source_zdb_id = 'ZDB-PUB-030905-1'
                       and    oevdisp_gene_zdb_id = recattrib_data_Zdb_id
                    )
             and    exists (
                       select recattrib_data_Zdb_id
                       from   record_attribution
                       where  recattrib_source_zdb_id = 'ZDB-PUB-030905-1'
                       and    oevdisp_zdb_id = recattrib_data_Zdb_id
                    )
             union
             select oevdisp_zdb_id
             from   orthologue_evidence_display
             where  exists (
                       select recattrib_data_Zdb_id
                       from   record_attribution
                       where  recattrib_source_zdb_id = 'ZDB-PUB-030905-1'
                       and    oevdisp_gene_zdb_id = recattrib_data_Zdb_id
                    )
             and    exists (
                       select recattrib_data_Zdb_id
                       from   record_attribution
                       where  recattrib_source_zdb_id = 'ZDB-PUB-030905-1'
                       and    oevdisp_zdb_id = recattrib_data_Zdb_id
                    )
             )";

  my @colDesc = ("Attributed ZDB ID       ",
                 "marker id            ",
		 "evidence code           ",
                 "term zdb_id        ");
  
  my $nRecords = execSql ($sql, undef, @colDesc);
	
  if ( $nRecords > 0 ) {
    my $sendToAddress = $_[0];
    my $subject = "Invalid mrkrgoev data is associated with ZDB-PUB-030905-1.";
    my $errMsg = "$nRecords mrkrgoev data are associated with ZDB-PUB-030905-1"
               . " that either do not have an attributed GENE or"
               . " do not have attributed orthologue evidence.";

    logError ($errMsg);
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql);  
  }
  &recordResult($routineName, $nRecords); 
}


#---------------------------------------------------------------
# associatedMrelDataforPUB030905_1
#
# 
# 
#Parameter
# $      Email Address for recipients
# 

sub associatedMrelDataforPUB030905_1 ($) {

  my $routineName = "associatedMrelDataforPUB030905_1";

  my $sql = "select recattrib_data_Zdb_id, mrel_mrkr_1_zdb_id, mrel_mrkr_2_zdb_id, mrel_type
             from   record_attribution, marker_relationship
             where  recattrib_source_zdb_id = 'ZDB-PUB-030905-1'
             and mrel_zdb_id = recattrib_data_zdb_id
             and    recattrib_data_Zdb_id not in (
             select oevdisp_gene_zdb_id
             from   orthologue_evidence_display
             where  exists (
                       select recattrib_data_Zdb_id
                       from   record_attribution
                       where  recattrib_source_zdb_id = 'ZDB-PUB-030905-1'
                       and    oevdisp_gene_zdb_id = recattrib_data_Zdb_id
                    )
             and    exists (
                       select recattrib_data_Zdb_id
                       from   record_attribution
                       where  recattrib_source_zdb_id = 'ZDB-PUB-030905-1'
                       and    oevdisp_zdb_id = recattrib_data_Zdb_id
                    )
             union
             select oevdisp_zdb_id
             from   orthologue_evidence_display
             where  exists (
                       select recattrib_data_Zdb_id
                       from   record_attribution
                       where  recattrib_source_zdb_id = 'ZDB-PUB-030905-1'
                       and    oevdisp_gene_zdb_id = recattrib_data_Zdb_id
                    )
             and    exists (
                       select recattrib_data_Zdb_id
                       from   record_attribution
                       where  recattrib_source_zdb_id = 'ZDB-PUB-030905-1'
                       and    oevdisp_zdb_id = recattrib_data_Zdb_id
                    )
             )";

  my @colDesc = ("Attributed ZDB ID       ",
                 "marker 1            ",
		 "marker 2            ",
                 "type        ");
  
  my $nRecords = execSql ($sql, undef, @colDesc);
	
  if ( $nRecords > 0 ) {
    my $sendToAddress = $_[0];
    my $subject = "Invalid mrel data is associated with ZDB-PUB-030905-1.";
    my $errMsg = "$nRecords mrel data are associated with ZDB-PUB-030905-1"
               . " that either do not have an attributed GENE or"
               . " do not have attributed orthologue evidence.";

    logError ($errMsg);
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql);  
  }
  &recordResult($routineName, $nRecords); 
}

#---------------------------------------------------------------
# associatedDataforPUB030508_1
#
# Only data for gene name, gene symbol
# abbreviation or previous name should be associated with
# ZDB-PUB-030508-1.
# 
# 
#Parameter
# $      Email Address for recipients
# 

sub associatedDataforPUB030508_1 ($) {

  my $routineName = "associatedDataforPUB030508_1";

  my $sql = "select recattrib_data_zdb_id, recattrib_source_zdb_id
             from   record_attribution
             where  recattrib_source_zdb_id = 'ZDB-PUB-030508-1'
             and get_obj_type(recattrib_datA_zdb_id) not in ('OEVDISP','ALT','DBL
INK')
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

  my @colDesc = ("Data ZDB ID       ",
		 "PUB  ZDB ID       ");
  
  my $nRecords = execSql ($sql, undef, @colDesc);
	
  if ( $nRecords > 0 ) {
    my $sendToAddress = $_[0];
    my $subject = "Invalid data is associated with ZDB-PUB-030508-1.";
    my $errMsg = "$nRecords data are associated with ZDB-PUB-030508-1 "
               . " that are not either: gene name, gene symbol "
               . ", or previous name.";

    logError ($errMsg);
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql);  
  }
  &recordResult($routineName, $nRecords); 
}
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



#======================= ZDB Object Type ================================
#----------------------------------------------------------------------------
# check for duplicate marker_goterm_Evidence, inference_groups.
#
# This statement checks for duplicate marker_Go_term_evidence
# records that have duplicate inference_group_members.
# 
# The first query uses 2 copies of the marker_go_term_evidence table
# each joining with inference_group_member table to find pairs of
# mrkrgoev records that have identical mrkr, term, source,
# and evidence_code. And it counts how many same inference members each of
# the found MRKRGOEV pair has. (we can't put an AK on this table and still avoid
# a join table between inference_group_member and mrkrgoev--in May 2004 we
# decided to remove the join table that did this--called inference_group).
#
# The MRKRGOEV pair and the count for same inference members are passed 
# into a sub-routine where it queries the number of inference members
# for each of the MRKRGOEV id and compares the two numbers with the count
# If the three counts are equal, then we find duplicates in 
# marker_go_term_evidence as well as duplicates in 
# inference_group_members...then this query will return the duplicate
# pairs of mrkrgoev_zdb_ids to Doug and Informix.
#
# Parameter
#  $     Email Address for recipient
#
sub mrkrgoevInfgrpDuplicatesFound ($) {

  my $routineName = "mrkrgoevInfgrpDuplicatesFound";

  my $sql = 'select a.mrkrgoev_zdb_id, 
                    a.mrkrgoev_mrkr_zdb_id,
                    b.mrkrgoev_zdb_id,
                    count(*)
	       from marker_go_term_evidence a, inference_group_member ia,
		    marker_go_Term_evidence b, inference_group_member ib
	      where a.mrkrgoev_mrkr_zdb_id =  b.mrkrgoev_mrkr_zdb_id
	        and a.mrkrgoev_term_zdb_id = b.mrkrgoev_term_zdb_id
		and a.mrkrgoev_source_zdb_id = b.mrkrgoev_sourcE_zdb_id
		and a.mrkrgoev_evidence_code = b.mrkrgoev_evidence_code 
                and a.mrkrgoev_zdb_id = ia.infgrmem_mrkrgoev_zdb_id
                and b.mrkrgoev_zdb_id = ib.infgrmem_mrkrgoev_zdb_id
                and a.mrkrgoev_zdb_id > b.mrkrgoev_zdb_id
                and ia.infgrmem_inferred_from = ib.infgrmem_inferred_from
                and
            (
               ( a.mrkrgoev_gflag_name is null
                  and b.mrkrgoev_gflag_name is null)
               or
               ( ( a.mrkrgoev_gflag_name is not null or b.mrkrgoev_gflag_name is not null)
                  and a.mrkrgoev_gflag_name=b.mrkrgoev_gflag_name
               )
            )
             group by a.mrkrgoev_zdb_id, b.mrkrgoev_zdb_id,
                      a.mrkrgoev_mrkr_zdb_id, b.mrkrgoev_mrkr_zdb_id';
  
  my @colDesc = ("mrkrgoev_zdb_id_1",
                 "mrkrgoev_mrkr_zdb_id_1", 
		 "mrkrgoev_zdb_id_2",
                 "mrkrgoev_mrkr_zdb_id_2",
		 "infgrmem_count   ");

  my $nRecords = execSql ($sql, subMrkrgoevInfgrpDuplicatesFound, @colDesc);

  if ( $nRecords > 0 ) {
    my $sendToAddress = $_[0];
    my $subject = "Possible duplicate records in marker_go_term_evidence, inference_group_member";
    my $errMsg = "$nRecords are possible duplicates in marker_go_term_evidence, inference_group_member";
    logError($errMsg);
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql); 
  }
  &recordResult($routineName, $nRecords);
} 

#----------------------
# Parameters
#     $     mrkrgoev zdb id  1
#     $     mrkrgoev_mrkr_zdb_id 1
#     $     mrkrgoev zdb id 2
#     $     count of how many identical inference members the two mrkrgoev id have

sub subMrkrgoevInfgrpDuplicatesFound($) {
    my @input = @_;
    my $mrkrgoev1 = $input[0];
    my $mrkrgoev1_mrkr = $input[1];
    my $mrkrgoev2 = $input[2];
    my $infgrmem_count = $input[3];

    my $sql = "select count(*) 
               from inference_group_member   
              where infgrmem_mrkrgoev_zdb_id= '$mrkrgoev1'";
 
  
    my @result_a = $dbh->selectrow_array($sql);

    $sql = "select count(*) 
               from inference_group_member   
              where infgrmem_mrkrgoev_zdb_id= '$mrkrgoev2'";
 
  
    my @result_b = $dbh->selectrow_array($sql);

    return ($result_a[0] == $result_b[0] && $result_a[0] == $infgrmem_count) ? 1 : 0 ;
 
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

my $adEmail      = "<!--|VALIDATION_EMAIL_AD|-->";
my $xpatEmail    = "<!--|VALIDATION_EMAIL_XPAT|-->";
my $estEmail     = "<!--|VALIDATION_EMAIL_EST|-->";
my $geneEmail    = "<!--|VALIDATION_EMAIL_GENE|-->";
my $mutantEmail  = "<!--|VALIDATION_EMAIL_MUTANT|-->";
my $dbaEmail     = "<!--|VALIDATION_EMAIL_DBA|-->";
my $goEmail      = "<!--|GO_EMAIL_CURATOR|-->";
my $aoEmail      = "<!--|AO_EMAIL_CURATOR|-->";
my $adminEmail   = "<!--|ZFIN_ADMIN|-->";
my $webAdminEmail = "<!--|WEB_ADMIN_EMAIL|-->";
my $strEmail = "<!--|VALIDATION_EMAIL_MORPHOLINO|-->";
my $genoEmail = "<!--|VALIDATION_EMAIL_GENOCURATOR|-->";


if($daily) {
}
if($weekly) {
  # put these here until we get them down to 0 records.  Then move them to 
  # daily.
	xpatObjectNotGeneOrEFG ($xpatEmail);
	constructNameNotSubstringOfFeatureName($dbaEmail);

	# each bit of the 030905_1 needs different data report to allow curators to clean up.  the generic one goes to DBA.
	associatedMrelDataforPUB030905_1($geneEmail);
	associatedAliasDataforPUB030905_1($geneEmail);
	associatedMrkrGoevDataforPUB030905_1($goEmail);
	associatedDBLinkforPUB030905_1($dbaEmail);

	# each bit of the 030508_1 needs different data report to allow curators to clean up.  the generic one goes to DBA.
	associatedDataforPUB030508_1($dbaEmail);
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
    mrkrgoevInfgrpDuplicatesFound($goEmail);
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

