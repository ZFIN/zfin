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
  
  $sth -> execute();
  
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
     print MAIL "Subject: $subject\n";

     print MAIL "$msg\n";

     # get the checking result from last run
     my $query = "select vldcheck_count, vldcheck_date
                   from validate_check_history
                  where vldcheck_name = '$rtnName' ";

     my ($preNum,$preDate) = $dbh->selectrow_array($query);

     if ($preNum) {
       print MAIL "(Last run at $preDate got $preNum records.)";
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
# $      Email Address for recipients
#
# For fx interface, we store the source (pub) zdb_id in both the figure
# table and in the expression_experiment table.
# We then relate the two, figure and expression, in fx_expression_pattern_figure
# We want the two sources to match--otherwise, we'd have figures from 
# one paper associated with expression_patterns from other papers.  This 
# would be incorrect. 
# These attributions are also stored in record_attribution, but that
# table is not verified here.

sub checkFigXpatexSourceConsistant ($) {
	
  my $routineName = "checkFigXpatexSourceConsistant";
	
  my $sql = 'select xpatfig_fig_zdb_id, xpatfig_xpatres_zdb_id
               from figure, expression_pattern_figure,
               expression_result, expression_experiment
               where xpatfig_fig_zdb_id = fig_zdb_id
               and xpatfig_xpatres_zdb_id = xpatres_zdb_id
               and xpatres_xpatex_zdb_id = xpatex_zdb_id
               and xpatex_source_zdb_id != fig_source_zdb_id   
              ';
  	
  my @colDesc = ("xpatfig_fig_zdb_id ",
		 "xpatfig_xpatres_zdb_id "
		);

  my $nRecords = execSql ($sql, undef, @colDesc);

  if ( $nRecords > 0 ) {

    my $sendToAddress = $_[0];
    my $subject = "FigXpatEx Source Inconsistant";
    my $errMsg = "$nRecords records' use different sources for xpatex
                   records and figure/xpatex records";
      		       
    logError ($errMsg);
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql);
  }
  &recordResult($routineName, $nRecords);
} 

#----------------------------------------------------------------
#Parameter
# $      Email Address for recipients
#
# For apato interface, we store the source (pub) zdb_id in both the figure
# table and in the atomic_phenotype table.
# We then relate the two, figure and phenotype, in apato_figure
# We want the two sources to match--otherwise, we'd have figures from 
# one paper associated with phenotypes from other papers.  This 
# would be incorrect. 
# These attributions are also stored in record_attribution, but that
# table is not verified here.

sub checkFigApatoSourceConsistant ($) {
	
  my $routineName = "checkFigXpatexSourceConsistant";
	
  my $sql = 'select apatofig_fig_zdb_id,apato_zdb_id 
      from atomic_phenotype
      join apato_figure
        on apato_zdb_id = apatofig_apato_zdb_id
      join figure
        on apatofig_fig_zdb_id = fig_zdb_id
      where apato_pub_zdb_id <> fig_source_zdb_id';

  	
  my @colDesc = ("apatofig_fig_zdb_id ",
		 "apato_zdb_id "
		);

  my $nRecords = execSql ($sql, undef, @colDesc);

  if ( $nRecords > 0 ) {

    my $sendToAddress = $_[0];
    my $subject = "FigPato Source Inconsistant";
    my $errMsg = "$nRecords records' use different sources for apato
                   records and figure/apato records";
      		       
    logError ($errMsg);
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql);
  }
  &recordResult($routineName, $nRecords);
} 

#---------------------------------------------------------------
#Parameter
# $      Email Address for recipients
# 
# Use the SPL routine anatitem_overlaps_stg_window() to check if
# any record in expression_result violates the rule.
# 
sub expressionResultStageWindowOverlapsAnatomyItem ($) {
	
  my $routineName = "expressionResultStageWindowOverlapsAnatomyItem";
	
  my $sql = '
            select xpatres_zdb_id,
                   s1.stg_name_long, s2.stg_name_long,
                   xpatres_anat_item_zdb_id, anatitem_name,
                   s3.stg_name_long, s4.stg_name_long
	      from expression_result 
                   join anatomy_item
                        on xpatres_anat_item_zdb_id = anatitem_zdb_id
                   join stage s1
                        on xpatres_start_stg_zdb_id = s1.stg_zdb_id
                   join stage s2
                        on xpatres_end_stg_zdb_id = s2.stg_zdb_id
                   join stage s3
                        on anatitem_start_stg_zdb_id = s3.stg_zdb_id
                   join stage s4
                        on anatitem_end_stg_zdb_id = s4.stg_zdb_id
             where anatitem_overlaps_stg_window(
                                     xpatres_anat_item_zdb_id,
                                     xpatres_start_stg_zdb_id,
                                     xpatres_end_stg_zdb_id
                                     ) = "f"';

   my @colDesc = ( "Xpatres ZDB ID      ",
		   "Xpatres start stage ",
		   "Xpatres end stage   ",
		   "Anatitem ZDB ID     ",
		   "Anatitem name       ",
		   "Anatitem start stage",
		   "Anatitem end stage  " );

  my $nRecords = execSql($sql, undef, @colDesc);
  
  if ( $nRecords > 0 ) {
      
      my $sendToAddress = $_[0];
      my $subject = "Xpat's stage window not overlaps with anatomy item's";
      my $errMsg = "In expression_result, $nRecords records' stage "
	  ."window don't overlap with the anatomy item's stage window.";
      
      logError ($errMsg);	
      &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql);   
  }
  &recordResult($routineName, $nRecords);
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



#----------------------------------------------------------------
#Parameter
# $      Email Address for recipients
#
# Finds figures with images from Elsevier publications which have not finished
# being reviewed (are open) and have PATO data but have no expression patterns.  
# This raises a flag that we should look at these again as they have been checked
# already (because there is PATO data), but no expression data was found.
# Per our agreement with Elsevier we are not supposed to show these images.

sub checkOpenElsevierFigureNoExpresWithPATO($) {
	
  my $routineName = "checkOpenElsevierFigureNoExpresWithPATO";
	
  my $sql = '
			select p.zdb_id as Pub_Id, j.jrnl_name as Journal, p.title as Title, p.pub_date as Pub_Date, concatenate( replace( f.fig_label ,"Fig. ","" ) ) as Figures
				from 
					image i , figure f, publication p, journal j
				where 
					j.jrnl_publisher like "Elsevier%"
					and j.jrnl_zdb_id=p.pub_jrnl_zdb_id
					and f.fig_source_zdb_id=p.zdb_id 
					and i.img_fig_zdb_id=f.fig_zdb_id
					and p.pub_completion_date is null 
					and not exists
					(
					select *
						from expression_pattern_figure expr
						where f.fig_zdb_id=expr.xpatfig_fig_zdb_id
					)
					and exists
					(
					select * 
						from apato_figure a
						where 
						a.apatofig_fig_zdb_id=f.fig_zdb_id 
					 )
				 group by p.zdb_id, j.jrnl_name, p.title, p.pub_date  
				 order by p.zdb_id desc ;
		'
						;
		
  	
  my @colDesc = (
		 "Publication ID:    ",
		 "Journal name:      ",
		 "Publication title: ",
		 "Publication date:  ",
		 "Figures:                        ",
		);

  my $nRecords = execSql ($sql, undef, @colDesc);

	orderResults($colDesc[4],$nRecords) ; 

  if ( $nRecords > 0 ) {
    my $sendToAddress = $_[0];
    my $subject = "Contains Elsevier images from open publications with no expression pattern but existing PATO.";
    my $errMsg = "$nRecords open publications use Elsevier images with no expression patterns but existing PATO values." ; 
      		       
    logError ($errMsg);
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql);
  }
  &recordResult($routineName, $nRecords);
} 


#----------------------------------------------------------------
#Parameter
# $      Email Address for recipients
# Finds figures with images from Elseview publications which have been 
# reviewed (are closed) and have no expression patterns.  Per our agreement
# with Elsevier we are not supposed to show these images.
#
#
#----------------------------------------------------------------
sub checkClosedElsevierFigureNoExpressions($) {
	
  my $routineName = "checkClosedElsevierFigureNoExpressions";
	
  my $sql = '
		select  p.zdb_id as Pub_Id, j.jrnl_name as Journal, p.title as Title, p.pub_date as Pub_Date, concatenate( replace(f.fig_label ,"Fig. ","") ) as Figures 
			from 
				image i , figure f, publication p, journal j
			where 
				j.jrnl_publisher like "Elsevier%"
				and j.jrnl_zdb_id=p.pub_jrnl_zdb_id
				and f.fig_source_zdb_id=p.zdb_id 
				and i.img_fig_zdb_id=f.fig_zdb_id
				and p.pub_completion_date is not null 
				and not exists
				(
				select *
					from expression_pattern_figure expr
					where f.fig_zdb_id=expr.xpatfig_fig_zdb_id
				)
			 group by p.zdb_id, j.jrnl_name, p.title, p.pub_date 
			 order by p.zdb_id desc
			 ;
		'
						;
		
  	
  my @colDesc = (
		 "Publication ID:    ",
		 "Journal name:      ",
		 "Publication title: ",
		 "Publication date:  ",
		 "Figures:                        ",
		);

  my $nRecords = execSql ($sql, undef, @colDesc);

	orderResults($colDesc[4],$nRecords) ; 

  if ( $nRecords > 0 ) {
    my $sendToAddress = $_[0];
    my $subject = "Contains Elsevier images from closed publications with no expression pattern";
    my $errMsg = "$nRecords closed publications use Elsevier images with no expression patterns." ; 
      		       
    logError ($errMsg);
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql);
  }
  &recordResult($routineName, $nRecords);
} 


#========================  Features  ================================
#
#---------------------------------------------------------------
# featureAssociatedWithGenotype
#
# After curation is done, each feature record should be associated
# with a genotype as an entry in genotype_feature table. Since a 
# feature is created first and then associated with a genotype, we
# couldn't make this a database constraint. 
#
# Parameter
# $ Email Address for recipients

sub featureAssociatedWithGenotype($) {
  my $routineName = "featureAssociatedWithGenotype";
	
  my $sql = 'select feature_name, feature_zdb_id
               from feature
              where not exists
                    (select "t"
                       from genotype_feature
                      where genofeat_feature_zdb_id = feature_zdb_id)';

  my @colDesc = ("Feature name         ",
		 "Feature zdb id       ");

  my $nRecords = execSql ($sql, undef, @colDesc);

  if ( $nRecords > 0 ) {
    my $sendToAddress = $_[0];
    my $subject = "Features not in any genotype";
    my $errMsg = "There are $nRecords feature record(s) that are not associated with any genotype. ";
    
    logError ($errMsg);
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql);
  }
  &recordResult($routineName, $nRecords);

}

#---------------------------------------------------------------
# morpholinoAbbrevContainsGeneAbbrev
#
# Parameter
# $ Email Address for recipients

sub morpholinoAbbrevContainsGeneAbbrev($) {
  my $routineName = "morpholinoAbbrevContainsGeneAbbrev";
	
  my $sql = "select a.mrkr_abbrev, b.mrkr_abbrev
               from marker a, marker b, marker_relationship
               where a.mrkr_zdb_id = mrel_mrkr_1_zdb_id
               and b.mrkr_zdb_id = mrel_mrkr_2_zdb_id
               and get_obj_type(a.mrkr_zdb_id) = 'MRPHLNO'
                and b.mrkr_abbrev !=
               (substring(a.mrkr_abbrev 
                            from
                             (length(a.mrkr_abbrev)-length(b.mrkr_abbrev)+1)
                            for
                             (length(b.mrkr_abbrev))
                          )
                )
              order by b.mrkr_abbrev";

  my @colDesc = ("Morpholino abbrev         ",
		 "Gene abbrev       ");

  my $nRecords = execSql ($sql, undef, @colDesc);

  if ( $nRecords > 0 ) {
    my $sendToAddress = $_[0];
    my $subject = "Morpholino abbrev not like gene_abbrev";
    my $errMsg = "There are $nRecords morpholinos without corresponding gene abbrevs. ";
    
    logError ($errMsg);
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql);
  }
  &recordResult($routineName, $nRecords);

}


#---------------------------------------------------------------
# featureIsAlleleOfOrMrkrAbsent
#
# features can either be alleles of markers or have markers absent
# not both.  we can not do this in the database via a trigger or
# constraint because the two data are stored in different tables.
#
# Parameter
# $ Email Address for recipients

sub featureIsAlleleOfOrMrkrAbsent($) {
  my $routineName = "featureAssociatedWithGenotype";
	
  my $sql = 'select feature_name, feature_zdb_id
               from feature, feature_marker_relationship, mapped_deletion
               where fmrel_ftr_zdb_id = feature_zdb_id
               and fmrel_mrkr_zdb_id = marker_id
               and feature_name = allele
               and present_t = "f"';

  my @colDesc = ("Feature name         ",
		 "Feature zdb id       ");

  my $nRecords = execSql ($sql, undef, @colDesc);

  if ( $nRecords > 0 ) {
    my $sendToAddress = $_[0];
    my $subject = "Feature are both alleles of a gene and are missing that gene";
    my $errMsg = "There are $nRecords feature record(s) that are both alleles of a gene and are missing that gene. ";
    
    logError ($errMsg);
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql);
  }
  &recordResult($routineName, $nRecords);

}

#---------------------------------------------------------------
# genotypesHaveNoNames
#
# Parameter
# $ Email Address for recipients

sub genotypesHaveNoNames($) {
  my $routineName = "genotypesHaveNoNames";
	
  my $sql = 'select geno_display_name, geno_handle
               from genotype
               where geno_display_name = geno_zdb_id';

  my @colDesc = ("Genotype name         ",
		 "Genotype handle       ");

  my $nRecords = execSql ($sql, undef, @colDesc);

  if ( $nRecords > 0 ) {
    my $sendToAddress = $_[0];
    my $subject = "Genotypes are incomplete";
    my $errMsg = "There are $nRecords genotype records with ZDB-ids for names. ";
    
    logError ($errMsg);
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql);
  }
  &recordResult($routineName, $nRecords);

}


#======================== PUB Attribution ========================
#
#---------------------------------------------------------------
# associatedDataforPUB030905_1
#
# Only orthology evidence code data should be associated with
# ZDB-PUB-030905-1.
#
# In record attribution, each record should have both a GENE and
# OEVDISP data associated (they should appear in pairs) for
# this PUB.  These pairs should correspond to a record in 
# orthologue_evidence_display.
#
# This test identifies any PUB data that does not fit this
# criteria.
# 
# 
#Parameter
# $      Email Address for recipients
# 

sub associatedDataforPUB030905_1 ($) {

  my $routineName = "associatedDataforPUB030905_1";

  my $sql = "select recattrib_data_Zdb_id
             from   record_attribution
             where  recattrib_source_zdb_id = 'ZDB-PUB-030905-1'
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

  my @colDesc = ("Data ZDB ID       ");
  
  my $nRecords = execSql ($sql, undef, @colDesc);
	
  if ( $nRecords > 0 ) {
    my $sendToAddress = $_[0];
    my $subject = "Invalid data is associated with ZDB-PUB-030905-1.";
    my $errMsg = "$nRecords data are associated with ZDB-PUB-030905-1"
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
# associatedDataforPUB030905_2
#
# Only data for gene name, gene symbol, 
# or previous name should be associated with
# ZDB-PUB-030508-1.
# 
# 
#Parameter
# $      Email Address for recipients
# 

sub associatedDataforPUB030905_2 ($) {

  my $routineName = "associatedDataforPUB030905_2";

  my $sql = "select recattrib_data_zdb_id
             from   record_attribution r1
             where  recattrib_source_zdb_id = 'ZDB-PUB-030905-2'
             and    not exists (
                    -- all nucleotide accession numbers assoc. w/pub via dblink_zdb_id (DBLINK)
                       select recattrib_data_zdb_id
                       from   db_link, record_attribution r2, foreign_db_contains
                       where  recattrib_source_zdb_id = 'ZDB-PUB-030905-2'
                       and    dblink_zdb_id = recattrib_data_zdb_id
                       and    dblink_fdbcont_zdb_id = fdbcont_zdb_id
                       and    fdbcont_fdbdt_data_type in ('Genomic','cDNA','Sequence Clusters')
                       and    r1.recattrib_data_zdb_id = r2.recattrib_data_zdb_id
                     union
                    -- all nucleotide accession numbers assoc. w/pub via dblink_linked_recid (GENE)
                       select recattrib_data_zdb_id
                       from   db_link, record_attribution r3, foreign_db_contains
                       where  recattrib_source_zdb_id = 'ZDB-PUB-030905-2'
                       and    dblink_linked_recid = recattrib_data_zdb_id
                       and    dblink_fdbcont_zdb_id = fdbcont_zdb_id
                       and    fdbcont_fdbdt_data_type in ('Genomic','cDNA','Sequence Clusters')
                       and    r1.recattrib_data_zdb_id = r3.recattrib_data_zdb_id
                    )  
             order by recattrib_data_zdb_id";


  my @colDesc = ("Data ZDB ID       ");
  
  my $nRecords = execSql ($sql, undef, @colDesc);
	
  if ( $nRecords > 0 ) {
    my $sendToAddress = $_[0];
    my $subject = "Invalid data is associated with ZDB-PUB-030905-2.";
    my $errMsg = "$nRecords data are associated with ZDB-PUB-030905-2 "
               . " that are not nucleotide sequence accession numbers "
               . " (i.e. not Genomic, cDNA or Sequence Clusters.)";

    logError ($errMsg);
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql);  
  }
  &recordResult($routineName, $nRecords); 
}



#======================== Marker Relationships ========================
#
#---------------------------------------------------------------
# containedInRelationshipsInEST
#
# This test identifies segments where "contained in" 
# relationships are associated with ESTs.
# 
# 
#Parameter
# $      Email Address for recipients
# 

sub containedInRelationshipsInEST ($) {

  my $routineName = "containedInRelationshipsInEST";

  my $sql = "select mrel_mrkr_1_zdb_id, mrel_mrkr_2_zdb_id, mrkr_type, mrel_type
             from   marker, marker_relationship, marker_relationship_type
             where  mrkr_type = 'EST'
             and    (mrkr_zdb_id = mrel_mrkr_2_zdb_id
                     and    mrel_type = mreltype_name
                     and    mreltype_2_to_1_comments = 'Contained in')
             union
             select mrel_mrkr_1_zdb_id, mrel_mrkr_2_zdb_id, mrkr_type, mrel_type
             from   marker, marker_relationship, marker_relationship_type
             where  mrkr_type = 'EST'
             and    (mrkr_zdb_id = mrel_mrkr_1_zdb_id
                     and    mrel_type = mreltype_name
                     and    mreltype_1_to_2_comments = 'Contains')";

  my @colDesc = ("Marker ID 1       ",
		 "Marker ID 2       ",
		 "Marker Type       ",
		 "Relationship type ");
  
  my $nRecords = execSql ($sql, undef, @colDesc);
	
  if ( $nRecords > 0 ) {
    my $sendToAddress = $_[0];
    my $subject = "ESTs have 'contained in' relationships.";
    my $errMsg = "$nRecords segments with 'contained in' relationships "
                 . "are associated with ESTs.";

    logError ($errMsg);
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql);  
  }
  &recordResult($routineName, $nRecords); 
}



#---------------------------------------------------------------
# encodesRelationshipsInBACorPAC
#
# This test identifies segments where "encodes" 
# relationships are associated with BACs or PACs.
# 
# 
#Parameter
# $      Email Address for recipients
# 

sub encodesRelationshipsInBACorPAC ($) {

  my $routineName = "encodesRelationshipsInBACorPAC";

  my $sql = "select mrel_mrkr_1_zdb_id, mrel_mrkr_2_zdb_id, mrkr_type, mrel_type
             from   marker, marker_relationship, marker_relationship_type
             where  mrkr_type = 'BAC'
             and    (mrkr_zdb_id = mrel_mrkr_2_zdb_id
                     and    mrel_type = mreltype_name
                     and    mreltype_2_to_1_comments = 'Is encoded by')
             union
             select mrel_mrkr_1_zdb_id, mrel_mrkr_2_zdb_id, mrkr_type, mrel_type
             from   marker, marker_relationship, marker_relationship_type
             where  mrkr_type = 'BAC'
             and    (mrkr_zdb_id = mrel_mrkr_1_zdb_id
                     and    mrel_type = mreltype_name
                     and    mreltype_1_to_2_comments = 'Encodes')
             union
             select mrel_mrkr_1_zdb_id, mrel_mrkr_2_zdb_id, mrkr_type, mrel_type
             from   marker, marker_relationship, marker_relationship_type
             where  mrkr_type = 'PAC'
             and    (mrkr_zdb_id = mrel_mrkr_2_zdb_id
                     and    mrel_type = mreltype_name
                     and    mreltype_2_to_1_comments = 'Is encoded by')
             union
             select mrel_mrkr_1_zdb_id, mrel_mrkr_2_zdb_id, mrkr_type, mrel_type
             from   marker, marker_relationship, marker_relationship_type
             where  mrkr_type = 'PAC'
             and    (mrkr_zdb_id = mrel_mrkr_1_zdb_id
                     and    mrel_type = mreltype_name
                     and    mreltype_1_to_2_comments = 'Encodes')";

  my @colDesc = ("Marker ID 1       ",
		 "Marker ID 2       ",
		 "Marker Type       ",
		 "Relationship type ");
  
  my $nRecords = execSql ($sql, undef, @colDesc);
	
  if ( $nRecords > 0 ) {
    my $sendToAddress = $_[0];
    my $subject = "BACs or PACs have 'encodes' relationships.";
    my $errMsg = "$nRecords segments with 'encodes' relationships "
                 . "are associated with either a BAC or PAC.";

    logError ($errMsg);
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql);  
  }
  &recordResult($routineName, $nRecords); 
}



#======================== Gene - EST relationships =====================
#
# All ESTs in ZFIN are supposed to be associated with Genes.
# These routines check that those relationships are correctly defined.

#---------------------------------------------------------------
# estsHave1Gene
#
# Each EST should be associated with 1 and only 1 gene
# 
#Parameter
# $      Email Address for recipients
# 
sub estsHave1Gene ($) {

  my $routineName = "estsHave1Gene";

  my $sql = 'select mrkr_zdb_id, mrkr_name, mrkr_abbrev
               from marker m2
               where mrkr_type = "EST"
                 and 1 <> 
                     ( select count(*) 
                         from marker m1, marker_relationship
                         where mrel_mrkr_1_zdb_id = m1.mrkr_zdb_id
                           and mrel_mrkr_2_zdb_id = m2.mrkr_zdb_id
                           and m1.mrkr_type in (select mtgrpmem_mrkr_type
				                  from marker_type_group_member
						 where mtgrpmem_mrkr_type_group="GENEDOM"))
               order by mrkr_name';

  my @colDesc = ("EST ZDB ID        ",
		 "EST Name          ",
		 "EST Abbrev        ");
  
  my $nRecords = execSql ($sql, undef, @colDesc);
	
  if ( $nRecords > 0 ) {
    my $sendToAddress = $_[0];
    my $subject = "ESTs have 0 or > 1 associated genes";
    my $errMsg = "$nRecords ESTs had 0 or more than 1 associated genes. ";

    logError ($errMsg);
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql);   
  }
  &recordResult($routineName, $nRecords);
} 


#---------------------------------------------------------------
# prefixedGenesHave1Est
#
# Genes that have a 2 character prefix, followed by a : should always have
# a corresponding EST.
#
# This test excludes genes that start with "id:".  See the description of
# the following test for more info.
# 
#Parameter
# $      Email Address for recipients
# 

sub prefixedGenesHave1Est ($) {

  my $routineName = "prefixedGenesHave1Est";

  my $sql = 'select mrkr_zdb_id, mrkr_name, mrkr_abbrev
               from marker m1
               where mrkr_type = "GENE"
                 and mrkr_name[3]  == ":"
                 and mrkr_name[1,2] not in ("id","si")
                 and 1 <> 
                     ( select count(*) 
                         from marker m2, marker_relationship
                         where mrel_mrkr_1_zdb_id = m1.mrkr_zdb_id
                           and mrel_mrkr_2_zdb_id = m2.mrkr_zdb_id
                           and mrel_type = "gene encodes small segment" )
               order by mrkr_name';

  my @colDesc = ("Gene ZDB ID       ",
		 "Gene Name         ",
		 "Gene Abbrev       ");
  
  my $nRecords = execSql ($sql, undef, @colDesc);
	
  if ( $nRecords > 0 ) {
    my $sendToAddress = $_[0];
    my $subject = "Prefixed genes have 0 or > 1 associated ESTs";
    my $errMsg = "$nRecords prefixed genes had 0 or more than 1 associated ESTs. ";

    logWarning ($errMsg);
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql);  
  }
  &recordResult($routineName, $nRecords); 
} 


#---------------------------------------------------------------
# xpatHasConsistentMarkerRelationship
#
# Temporary expression experiments are sometimes not updated 
# when an actual marker relationship is found.
#
# This test identifies any expresssion_experiment (probe,gene) pairs
# that need to be updated based on marker_relationship.
# 
# 
#Parameter
# $      Email Address for recipients
# 

sub xpatHasConsistentMarkerRelationship ($) {

  my $routineName = "xpatHasConsistentMarkerRelationship";

  my $sql = 'select xpatex_zdb_id, xpatex_probe_feature_zdb_id, xpatex_gene_zdb_id
               from expression_experiment
               where xpatex_probe_feature_zdb_id is not null
               and not exists
                (
                   select * 
                     from marker_relationship
                     where xpatex_probe_feature_zdb_id = mrel_mrkr_2_zdb_id
                     and xpatex_gene_zdb_id = mrel_mrkr_1_zdb_id
                 )';

  my @colDesc = ("xpat ZDB ID       ",
		 "probe ZDB ID      ",
		 "xpat gene         ");
  
  my $nRecords = execSql ($sql, undef, @colDesc);
	
  if ( $nRecords > 0 ) {
    my $sendToAddress = $_[0];
    my $subject = "For a given probe, expression_experiment gene is out-of-sync.";
    my $errMsg = "$nRecords genes in expression_experiment need to be synchronized "
		. "with marker_relationship.";

    logError ($errMsg);
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql);  
  }
  &recordResult($routineName, $nRecords); 
} 



#---------------------------------------------------------------
# prefixedIbdGenesHave1Est
#
# This test esists solely to report prefixed genes that start with "id:"
# but done have a corresponding EST.  This data is treated differently
# from all other prefixed genes (see previous test) because, unlike 
# other prefixed genes, ZFIN is not going to create ESTs for the "id:"
# genes.  The hope is that over time the biology community will determine
# what genes these actually are, and then these records will slowly go away.
#
# Therefore, the output of this test should not be considered as stuff
# that needs to be fixed this month, but rather as a report on the progress
# towards the overall goal of making these genes disappear.
# 
#Parameter
# $      Email Address for recipients
# 

sub prefixedIbdGenesHave1Est ($) {

  my $routineName = "prefixedIbdGenesHave1Est";

  my $sql = 'select mrkr_zdb_id, mrkr_name, mrkr_abbrev
               from marker m1
               where mrkr_type = "GENE"
                 and mrkr_name like "id:%"
                 and 1 <> 
                     ( select count(*) 
                         from marker m2, marker_relationship
                         where mrel_mrkr_1_zdb_id = m1.mrkr_zdb_id
                           and mrel_mrkr_2_zdb_id = m2.mrkr_zdb_id
                           and mrel_type = "gene encodes small segment" )
               order by mrkr_name';

  my @colDesc = ("Gene ZDB ID       ",
		 "Gene Name         ",
		 "Gene Abbrev       ");
  
  my $nRecords = execSql ($sql, undef, @colDesc);
	
  if ( $nRecords > 0 ) {
    my $sendToAddress = $_[0];
    my $subject = "id: genes that have 0 or > 1 associated ESTs";
    my $errMsg = "$nRecords 'id:' genes had 0 or more than 1 associated ESTs. ";

    logError ($errMsg);
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql);  
  }
  &recordResult($routineName, $nRecords);
} 

#---------------------------------------------------------------
# estsWithoutClonesHaveXxGenes
#
# If an EST does not have a corresponding clone record, then its corresponding
# gene record must begin with xx.
# 
# This test explicitly excludes 6 ESTs from the Thisse's.  ZFIN has these 6 ESTs
# beacuse ZIRC carries them.  However, we don't have any expression data
# for them yet.
#
#Parameter
# $      Email Address for recipients
# 
sub estsWithoutClonesHaveXxGenes ($) {

  my $routineName = "estsWithoutClonesHaveXxGenes";

  my $sql = 'select mrkr_zdb_id, mrkr_name, mrkr_abbrev
               from marker est
               where mrkr_type = "EST"
                 and not exists 
                     ( select * 
                         from clone
                         where clone_mrkr_zdb_id = est.mrkr_zdb_id )
                 and not exists
                     ( select * 
                         from marker m1, marker_relationship
                         where mrel_mrkr_1_zdb_id = m1.mrkr_zdb_id
                           and mrel_mrkr_2_zdb_id = est.mrkr_zdb_id
                           and mrel_type = "gene encodes small segment"
                           and m1.mrkr_name like "xx:%" )
                 and mrkr_name not in ("cb23", "cb42", "cb70", "cb104",
                                       "cb109", "cb114")
               order by mrkr_name';

  my @colDesc = ("EST ZDB ID        ",
		 "EST Name          ",
		 "EST Abbrev        ");
  
  my $nRecords = execSql ($sql, undef, @colDesc);
	
  if ( $nRecords > 0 ) {
    my $sendToAddress = $_[0];
    my $subject = "ESTs without clones missing corresponding xx: gene record";
    my $errMsg = "$nRecords ESTs without clone records do not have corresponding xx: genes. ";

    logError ($errMsg);
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql); 
  }
  &recordResult($routineName, $nRecords);
} 


#---------------------------------------------------------------
# xxGenesHaveNoClones
#
# Genes that are prefixed with xx: exist as placeholder genes for ESTs
# for which we don't have clone information.
# 
#Parameter
# $      Email Address for recipients
# 

sub xxGenesHaveNoClones ($) {

  my $routineName = "xxGenesHaveNoClones";

  my $sql = 'select mrkr_zdb_id, mrkr_name, mrkr_abbrev
               from marker m1
               where mrkr_type = "GENE"
                 and mrkr_name like "xx:%"
                 and exists
                     ( select * 
                         from marker m2, marker_relationship
                         where mrel_mrkr_1_zdb_id = m1.mrkr_zdb_id
                           and mrel_mrkr_2_zdb_id = m2.mrkr_zdb_id
                           and mrel_type = "gene encodes small segment" 
                           and exists
                               ( select * 
                                   from clone
                                   where clone_mrkr_zdb_id = m2.mrkr_zdb_id ) )
               order by mrkr_name';

  my @colDesc = ("Gene ZDB ID       ",
		 "Gene Name         ",
		 "Gene Abbrev       ");
  
  my $nRecords = execSql ($sql, undef, @colDesc);
	
  if ( $nRecords > 0 ) {
    my $sendToAddress = $_[0];
    my $subject = "'xx:' genes had a corresponding clone record";
    my $errMsg = "$nRecords xx: genes had a corresponding clone record\n";

    logError ($errMsg);
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql); 
  }
  &recordResult($routineName, $nRecords); 
} 


#==================== Detect Gene Merge Candidates ======================

# These tests could be filed under the gene-est tests above, or under the
# db_link-orthology tests below.  However, they fall in between so they
# get thier own listing.


#---------------------------------------------------------------
# Detect candidates for gene merging by identifying different genes that point
# at the same DB_link records in other databases.
#
# Parameter
# $      Email Address for recipients
# 

sub genesWithCommonDblinks ($) {

  my $routineName = "genesWithCommonDblinks";
	
  my $sql = '
      -- cDNA/Polypeptide acc connects anything directly to anything
      -- (no marker relationships)
      select fdbcont_fdb_db_name, adbl.dblink_acc_num, 
	     a.mrkr_abbrev, b.mrkr_abbrev
	from marker a, marker b,
	     db_link adbl, db_link bdbl,
	     foreign_db_contains,
	     marker_type_group_member amtgm, marker_type_group_member bmtgm
       where a.mrkr_type = amtgm.mtgrpmem_mrkr_type
	 and b.mrkr_type = bmtgm.mtgrpmem_mrkr_type
	 and amtgm.mtgrpmem_mrkr_type_group in ("GENEDOM","SMALLSEG")
	 and bmtgm.mtgrpmem_mrkr_type_group in ("GENEDOM","SMALLSEG")
	 and a.mrkr_abbrev < b.mrkr_abbrev
	 and a.mrkr_zdb_id = adbl.dblink_linked_recid
	 and b.mrkr_zdb_id = bdbl.dblink_linked_recid
	 and adbl.dblink_acc_num = bdbl.dblink_acc_num
	 and adbl.dblink_acc_num[1,3] <> "NC_" -- temp kludge
	 and fdbcont_fdbdt_data_type in ("cDNA", "Polypeptide")
	 and adbl.dblink_fdbcont_zdb_id =  fdbcont_zdb_id
	 and bdbl.dblink_fdbcont_zdb_id =  fdbcont_zdb_id
    union
      -- cDNA/Polypeptide acc connects genes
      -- via marker relationships to both
      select fdbcont_fdb_db_name, adbl.dblink_acc_num, 
	     a.mrkr_abbrev, b.mrkr_abbrev
	from marker a, marker b,
	     marker_relationship amr, marker_relationship bmr,
	     db_link adbl, db_link bdbl,
	     foreign_db_contains,
	     marker_type_group_member amtgm, marker_type_group_member bmtgm
       where a.mrkr_type = amtgm.mtgrpmem_mrkr_type
	 and b.mrkr_type = bmtgm.mtgrpmem_mrkr_type
	 and amtgm.mtgrpmem_mrkr_type_group ="GENEDOM"
	 and bmtgm.mtgrpmem_mrkr_type_group ="GENEDOM"
	 and a.mrkr_abbrev < b.mrkr_abbrev
	 and amr.mrel_mrkr_1_zdb_id = a.mrkr_zdb_id
	 and amr.mrel_mrkr_2_zdb_id = adbl.dblink_linked_recid
	 and bmr.mrel_mrkr_1_zdb_id = b.mrkr_zdb_id
	 and bmr.mrel_mrkr_2_zdb_id = bdbl.dblink_linked_recid
	 and adbl.dblink_acc_num = bdbl.dblink_acc_num
	 and adbl.dblink_acc_num[1,3] <> "NC_" -- temp kludge
	 and fdbcont_fdbdt_data_type in ("cDNA", "Polypeptide")
	 and adbl.dblink_fdbcont_zdb_id = fdbcont_zdb_id
	 and bdbl.dblink_fdbcont_zdb_id = fdbcont_zdb_id
    union
      -- cDNA/Polypeptide acc connected gene to smallseg
      -- via marker relationship on first gene only
      select fdbcont_fdb_db_name, adbl.dblink_acc_num, 
	     a.mrkr_abbrev, b.mrkr_abbrev
	from marker a, marker b,
	     marker_relationship bmr,
	     db_link adbl, db_link bdbl,
	     foreign_db_contains,
	     marker_type_group_member amtgm, marker_type_group_member bmtgm
       where a.mrkr_type = amtgm.mtgrpmem_mrkr_type
	 and b.mrkr_type = bmtgm.mtgrpmem_mrkr_type
	 and amtgm.mtgrpmem_mrkr_type_group ="GENEDOM"
	 and bmtgm.mtgrpmem_mrkr_type_group ="GENEDOM"
	 and a.mrkr_abbrev < b.mrkr_abbrev
	 and a.mrkr_zdb_id = adbl.dblink_linked_recid
	 and bmr.mrel_mrkr_1_zdb_id = b.mrkr_zdb_id
	 and bmr.mrel_mrkr_2_zdb_id = bdbl.dblink_linked_recid
	 and adbl.dblink_acc_num = bdbl.dblink_acc_num
	 and adbl.dblink_acc_num[1,3] <> "NC_" -- temp kludge
	 and fdbcont_fdbdt_data_type in ("cDNA", "Polypeptide")
	 and adbl.dblink_fdbcont_zdb_id = fdbcont_zdb_id
	 and bdbl.dblink_fdbcont_zdb_id = fdbcont_zdb_id
    union
      -- cDNA/Polypeptide acc connected gene to smallseg
      -- via marker relationship on second gene only
      select fdbcont_fdb_db_name, adbl.dblink_acc_num, 
	     a.mrkr_abbrev, b.mrkr_abbrev
	from marker a, marker b,
	     marker_relationship amr,
	     db_link adbl, db_link bdbl,
	     foreign_db_contains,
	     marker_type_group_member amtgm, marker_type_group_member bmtgm
       where a.mrkr_type = amtgm.mtgrpmem_mrkr_type
	 and b.mrkr_type = bmtgm.mtgrpmem_mrkr_type
	 and amtgm.mtgrpmem_mrkr_type_group ="GENEDOM"
	 and bmtgm.mtgrpmem_mrkr_type_group ="GENEDOM"
	 and a.mrkr_abbrev < b.mrkr_abbrev
	 and amr.mrel_mrkr_1_zdb_id = a.mrkr_zdb_id
	 and amr.mrel_mrkr_2_zdb_id = adbl.dblink_linked_recid
	 and b.mrkr_zdb_id = bdbl.dblink_linked_recid
	 and adbl.dblink_acc_num = bdbl.dblink_acc_num
	 and adbl.dblink_acc_num[1,3] <> "NC_" -- temp kludge
	 and fdbcont_fdbdt_data_type in ("cDNA", "Polypeptide")
	 and adbl.dblink_fdbcont_zdb_id =  fdbcont_zdb_id
	 and bdbl.dblink_fdbcont_zdb_id =  fdbcont_zdb_id
    order by 1,2,3,4';

  my @colDesc = ("Database     ",
		 "Accession num",
		 "First marker ",
		 "Second marker");
  
  my $nRecords = execSql ($sql, undef, @colDesc);
	
  if ( $nRecords > 0 ) {
    my $sendToAddress = $_[0];
    my $subject = "Markers that are candidates for merging";
    my $errMsg = "$nRecords pairs of markers are candidates for merging, based on common links to other databases.";
    logWarning ($errMsg);
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql); 
  }
  &recordResult($routineName, $nRecords); 
} 




#============================ Linkage ===================================
#---------------------------------------------------------------
#Parameter
# $      Email Address for recipients
# 
sub linkageHasMembers ($) {

  my $routineName = "linkageHasMembers";
	
  my $sql = '
             select lnkg_zdb_id,
                    lnkg_or_lg,
                    recattrib_source_zdb_id
               from linkage, record_attribution
	      where lnkg_zdb_id not in (
	            	      select lnkgmem_linkage_zdb_id 
                                from linkage_member)
                and lnkg_zdb_id = recattrib_data_zdb_id
             ';

  my @colDesc = ("Lnkg ZDB ID       ",
		 "Lnkg or lg        ",
		 "Lnkg source ZDB ID" );
  
  my $nRecords = execSql ($sql, undef, @colDesc);
	
  if ( $nRecords > 0 ) {
    my $sendToAddress = $_[0];
    my $subject = "linkage has no member(s)";
    my $errMsg = "In linkage table, $nRecords records have no members in linkage_member. ";
    
    logError ($errMsg);
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql); 
  }
  &recordResult($routineName, $nRecords); 
} 

#---------------------------------------------------------------
#Parameter
# $      Email Address for recipients
# 
sub linkagePairHas2Members ($) {
	
  my $routineName = "linkagePairHas2Members";
	
  my $sql = ' 
              select lpmem_linkage_pair_zdb_id
		     lpmem_member_zdb_id

                from linkage_pair, 
                     linkage_pair_member
 
	       where lnkgpair_zdb_id = lpmem_linkage_pair_zdb_id 
                     group by lpmem_linkage_pair_zdb_id
                     having count(*) != 2
             ';

  my @colDesc = ("Lpmem linkage pair ZDB ID",
		 "Lpmem member ZDB ID      " );
  
  my $nRecords = execSql ($sql, undef, @colDesc);

  if ( $nRecords > 0 ) {

    my $sendToAddress = $_[0];
    my $subject = "linkage pair has one or more than 2 members";
    my $errMsg = "In linkage_pair table, $nRecords records have less than or"
                   ." more than two members in linkage_pair_member. ";
    logError ($errMsg); 
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql);  
  }
  &recordResult($routineName, $nRecords); 
} 

#=========================== Orthology, DB Link ===========================

#-------------------------------------------------------------
#Parameter
# $      Email Address for recipients
# 
sub orthologueHasDblink ($) {

  my $routineName = "orthologueHasDblink";

  my $sql = '
             select zdb_id,
                    c_gene_id,
                    ortho_name,
                    entry_time
               from orthologue
              where zdb_id not in (
                                select dblink_linked_recid 
                                  from db_link) 
             ';
 
  my @colDesc = ("Ortho ZDB ID",
		 "Gene ID     ",
		 "Ortho name  ",
		 "Entry time  " );

  my $nRecords = execSql ($sql, undef, @colDesc);

  if ( $nRecords > 0 ) {
    my $sendToAddress = $_[0];
    my $subject = "Orthologue(s) without links to any other database";
    my $errMsg = "$nRecords orthologue(s) have 0 links to other databases.  ";
    logError ($errMsg); 
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql);
  }
  &recordResult($routineName, $nRecords);
} 



#-------------------------------------------------------------
# orthologueNomenclatureValid 
# 
# Do minimal nomenclature rule checking on orthologue names and symbols.
# This routine checks rules that are easy to check (such as "must be all caps").
# The first version of this routine also excluded common exceptions to the
# rules such as "C%orf%" orthologues in humans and Riken orthologues in Mouse.
# However, Ken requested that the report include these, as most/all of them
# will change over time.  
#
# Names and abbrevs reported by this check are not necessarily 
# in error -- they might be errors, or they just might be exceptions to 
# the standard nomenclature.  These exceptions are why this check is not
# done with a trigger.  See case 314 for more details.
#
# There are some rules that we know about that we don't enforce:
# Everything, from Ken
#   Dashes and periods are frowned upon in symbols, but there are exceptions.
# Yeast, from Ceri:
#   all symbols (gene abbreviations in the yeast community) must contain 3 
#   letters and then the numbers.  The exceptions are the systematic names 
#   such as YMR043W which will have 3 letters followed by numbers (I think 
#   standard # of numbers #=3 but not sure) followed either by W or C
#   However, The Systematic names are not used as identifiers by the yeast 
#   community after the gene has been given a (standard) name.
#
# Parameter
# $      Email Address for recipients
# 
sub orthologueNomenclatureValid ($) {

  my $routineName = "orthologueNomenclatureValid";

  my $sql = '
    select organism, ortho_abbrev, ortho_name, c_gene_id
      from orthologue 
      where ortho_abbrev like "% %" 
         or (    organism = "Human" 
             and ortho_abbrev <> upper(ortho_abbrev))
         or (    organism = "Mouse"
             and (   ortho_abbrev[1,1] <> upper(ortho_abbrev[1,1])
                  or substr(ortho_abbrev,2) <> lower(substr(ortho_abbrev,2))))
         or (    organism = "Yeast"
             and (   ortho_abbrev <> upper(ortho_abbrev)
	          or ortho_name <> upper(ortho_name)
                  or ortho_name <> ortho_abbrev
                  or length(ortho_abbrev) < 4))
      order by organism, ortho_abbrev;';
 
  my @colDesc = ("Organism    ",
		 "Ortho Abbrev",
		 "Ortho Name  ",
		 "Gene ID     " );

  my $nRecords = execSql ($sql, undef, @colDesc);

  if ( $nRecords > 0 ) {
    my $sendToAddress = $_[0];
    my $subject = "Orthologue(s) with suspect nomenclature";
    my $errMsg = "$nRecords orthologue(s) have suspect nomenclature.  ";
    logWarning ($errMsg); 
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql);
  }
  &recordResult($routineName, $nRecords);
}



#---------------------------------------------------------------
# orthologyHasEvidence
#
# This test identifies any orthology records without any
# evidence code.
# 
# 
#Parameter
# $      Email Address for recipients
# 

sub orthologyHasEvidence ($) {

  my $routineName = "orthologyHasEvidence";

  my $sql = "select zdb_id, c_gene_id, organism
             from   orthologue
             where  not exists (
                       select oev_ortho_zdb_id
                       from   orthologue_evidence
                       where  zdb_id = oev_ortho_zdb_id
                    )";

  my @colDesc = ("Orthology ZDB ID  ",
		 "Gene ZDB ID       ",
		 "Organism          ");
  
  my $nRecords = execSql ($sql, undef, @colDesc);
	
  if ( $nRecords > 0 ) {
    my $sendToAddress = $_[0];
    my $subject = "Orthology is missing evidence code.";
    my $errMsg = "$nRecords orthology records require an evidence code";

    logError ($errMsg);
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql);  
  }
  &recordResult($routineName, $nRecords); 
}



#---------------------------------------------------------------
# mouseOrthologyHasValidMGIAccession
#
# This test identifies any mouse orthology records without any
# 'valid' MGI accession ID. Due to historic reasons, in ZFIN,
# only the numerical string part of the MGI:####### is stored  
# as the accession ID. When the whole string of MGI:####### is 
# entered, that would generate a broken link to MGI site. Thus
# before we change the data in the database, we valide the id
# here.
# 
#Parameter
# $      Email Address for recipients
# 

sub mouseOrthologyHasValidMGIAccession ($) {

  my $routineName = "mouseOrthologyHasMGIAccession";

  my $sql = "select zdb_id, c_gene_id, organism
             from   orthologue
             where  organism = 'Mouse'
             and    not exists (
                       select dblink_linked_recid
                       from   db_link, foreign_db_contains
                       where  dblink_fdbcont_zdb_id = fdbcont_zdb_id
                       and    fdbcont_organism_common_name = 'Mouse'
                       and    fdbcont_fdb_db_name = 'MGI'
                       and    zdb_id = dblink_linked_recid
                       and    dblink_acc_num not like 'MGI:%'
                    )";

  my @colDesc = ("Orthology ZDB ID  ",
		 "Gene ZDB ID       ",
		 "Organism          ");
  
  my $nRecords = execSql ($sql, undef, @colDesc);
	
  if ( $nRecords > 0 ) {
    my $sendToAddress = $_[0];
    my $subject = "Mouse Orthology is missing MGI accession ID or the accession ID has the wrong format.";
    my $errMsg = "$nRecords mouse orthology records require a valid MGI accession ID";

    logError ($errMsg);
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql);  
  }
  &recordResult($routineName, $nRecords); 
}



#---------------------------------------------------------------
# mouseOrthologyHasEntrezAccession
#
# This test identifies any mouse orthology records without any
# Entrez accession ID.
# 
# 
#Parameter
# $      Email Address for recipients
# 

sub mouseOrthologyHasEntrezAccession ($) {

  my $routineName = "mouseOrthologyHasEntrezAccession";

  my $sql = "select zdb_id, c_gene_id, organism
             from   orthologue
             where  organism = 'Mouse'
             and    not exists (
                       select dblink_linked_recid
                       from   db_link, foreign_db_contains
                       where  dblink_fdbcont_zdb_id = fdbcont_zdb_id
                       and    fdbcont_organism_common_name = 'Mouse'
                       and    fdbcont_fdb_db_name = 'Entrez Gene'
                       and    zdb_id = dblink_linked_recid
                    )";

  my @colDesc = ("Orthology ZDB ID  ",
		 "Gene ZDB ID       ",
		 "Organism          ");
  
  my $nRecords = execSql ($sql, undef, @colDesc);
	
  if ( $nRecords > 0 ) {
    my $sendToAddress = $_[0];
    my $subject = "Mouse Orthology is missing Entrez Gene accession ID.";
    my $errMsg = "$nRecords mouse orthology records require a Entrez Gene accession ID";

    logError ($errMsg);
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql);  
  }
  &recordResult($routineName, $nRecords); 
}



#---------------------------------------------------------------
# humanOrthologyHasEntrezAccession
#
# This test identifies any human orthology records without any
# Entrez accession ID.
# 
# 
#Parameter
# $      Email Address for recipients
# 

sub humanOrthologyHasEntrezAccession ($) {

  my $routineName = "humanOrthologyHasEntrezAccession";

  my $sql = "select zdb_id, c_gene_id, organism
             from   orthologue o1
             where  organism = 'Human'
             and    not exists (
                       select dblink_linked_recid
                       from   db_link, foreign_db_contains
                       where  dblink_fdbcont_zdb_id = fdbcont_zdb_id
                       and    fdbcont_organism_common_name = 'Human'
                       and    fdbcont_fdb_db_name = 'Entrez Gene'
                       and    o1.zdb_id = dblink_linked_recid
                    )
             -- exclude organism type mismatch errors
             -- these errors reported in separate DBA validation below
             and    not exists (
                       select dblink_linked_recid 
                       from   db_link, foreign_db_contains, orthologue o2
                       where  organism <> fdbcont_organism_common_name
                       and    dblink_fdbcont_zdb_id = fdbcont_zdb_id
                       and    o2.zdb_id = dblink_linked_recid
                       and    o1.zdb_id = o2.zdb_id
                    )";

  my @colDesc = ("Orthology ZDB ID  ",
		 "Gene ZDB ID       ",
		 "Organism          ");
  
  my $nRecords = execSql ($sql, undef, @colDesc);
	
  if ( $nRecords > 0 ) {
    my $sendToAddress = $_[0];
    my $subject = "Human Orthology is missing Entrez Gene accession ID.";
    my $errMsg = "$nRecords human orthology records require a Entrez Gene accession ID.";

    logError ($errMsg);
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql);  
  }
  &recordResult($routineName, $nRecords); 
}



#---------------------------------------------------------------
# orthologyOrganismMatchesForeignDBContains
#
# This test identifies any human orthology records without any
# Entrez accession ID.
# 
# 
#Parameter
# $      Email Address for recipients
# 

sub orthologyOrganismMatchesForeignDBContains ($) {

  my $routineName = "orthologyOrganismMatchesForeignDBContains";

  my $sql = "select c_gene_id, zdb_id, organism, 
                    dblink_zdb_id, dblink_linked_recid, 
                    fdbcont_zdb_id, fdbcont_organism_common_name
             from   orthologue, db_link, foreign_db_contains
             where  dblink_linked_recid = zdb_id
             and    dblink_fdbcont_zdb_id = fdbcont_zdb_id
             and    organism <> fdbcont_organism_common_name";
             
  my @colDesc = ("ORTHO ID          ",
     		 "ORTHO Gene        ",
     		 "ORTHO Organism    ",
     		 "DBLINK ID         ",
     		 "DBLINK-ORTHO ID   ",
     		 "FDB ID            ",
       		 "FDB Organism      ");
               
  my $nRecords = execSql ($sql, undef, @colDesc);
             	
  if ( $nRecords > 0 ) {
    my $sendToAddress = $_[0];
    my $subject = "Orthologue organism is incorrectly matched with Foreign DB Organism.";
    my $errMsg = "$nRecords orthologue records need to be reconciled with"
               . " foreign_db_contains.  The organism types do not match.";

    logError ($errMsg);
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql);  
  }
  &recordResult($routineName, $nRecords); 
}

#======================= DB Link =======================================
#----------------------------------------------
# Parameter
# $      Email Address for recipients
#
# Note: The check could be more specific, such as if the db type is
# cDNA, then the RefSeq accession would be [NX]M_####, if the db type
# is Polypeptide, then would be [NX]P_####. We do this specific check 
# at the curator interface. 
#
sub refSeqAccessionInWrongFormat ($) {
    my $routineName = "refSeqAccessionInWrongFormat";
    my $sql = '
               select dblink_linked_recid, "RefSeq", dblink_acc_num
                 from db_link, foreign_db_contains
                where fdbcont_zdb_id = dblink_fdbcont_zdb_id
                  and fdbcont_fdb_db_name = "RefSeq"
                  and fdbcont_fdbdt_super_type = "sequence"
                  and dblink_acc_num[3] <> "_"
               UNION 
               select dblink_linked_recid, fdbcont_fdb_db_name, dblink_acc_num
                 from db_link, foreign_db_contains
                where fdbcont_zdb_id = dblink_fdbcont_zdb_id
                  and fdbcont_fdb_db_name <> "RefSeq"
                  and fdbcont_fdbdt_super_type = "sequence"
                  and dblink_acc_num[3] = "_"
                   ';

    my @colDesc =("Data zdb id",
		  "Db name    ",
                  "Acc number ");

    my $nRecords = execSql ($sql, undef, @colDesc);

    if ( $nRecords > 0 ) {

	my $sendToAddress = $_[0];
	my $subject = "RefSeq accession number in wrong format";
	my $errMsg = "In db_link, $nRecords RefSeq accession numbers are in wrong format";
	
	logError ($errMsg); 
	&sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql); 
    }
    &recordResult($routineName, $nRecords);
} 

#----------------------------------------------
# Parameter
# $      Email Address for recipients
#
# 
sub vegaAccessionInWrongFormat ($) {
    my $routineName = "vegaAccessionInWrongFormat";
    my $sql = '
               select dblink_linked_recid, "Vega_Trans", dblink_acc_num
                 from db_link, foreign_db_contains
                where fdbcont_zdb_id = dblink_fdbcont_zdb_id
                  and fdbcont_fdb_db_name = "Vega_Trans"
                  and fdbcont_fdbdt_super_type = "sequence"
                  and dblink_acc_num[1,3] <> "OTT"
               UNION 
               select dblink_linked_recid, fdbcont_fdb_db_name, dblink_acc_num
                 from db_link, foreign_db_contains
                where fdbcont_zdb_id = dblink_fdbcont_zdb_id
                  and fdbcont_fdb_db_name <> "Vega_Trans"
                  and fdbcont_fdbdt_super_type = "sequence"
                  and dblink_acc_num[1,3] = "OTT"
                   ';

    my @colDesc =("Data zdb id",
		  "Db name    ",
                  "Acc number ");

    my $nRecords = execSql ($sql, undef, @colDesc);

    if ( $nRecords > 0 ) {

	my $sendToAddress = $_[0];
	my $subject = "Vega Transcript accession number in wrong format";
	my $errMsg = "In db_link, $nRecords Vega accession numbers are in wrong format";
	
	logError ($errMsg); 
	&sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql); 
    }
    &recordResult($routineName, $nRecords);
}

#======================= ZDB Object Type ================================
#---------------------------------------------------------
#Parameter
# $      Email Address for recipients
#
sub zdbObjectHomeTableColumnExist ($) {

  my $routineName = "zdbObjectHomeTableColumnExist";

  my $sql = '
             select zobjtype_name,
                    zobjtype_home_table,
                    zobjtype_home_zdb_id_column

               from zdb_object_type
 
              where zobjtype_home_table not in 
                           (select tabname from systables)
                 or zobjtype_home_zdb_id_column not in
                           (select colname 
                              from zdb_object_type a, 
                                   systables b, 
                                   syscolumns c
                             where zobjtype_home_table = b.tabname
                               and b.tabid = c.tabid) ';
  
  my @colDesc = ("Zobjtype name",
		 "Zobjtype home table",
		 "Zobjtype home zdb id column" );

  my $nRecords = execSql ($sql, undef, @colDesc);

  if ( $nRecords > 0 ) {

    my $sendToAddress = $_[0];
    my $subject = "Home table or home column not available";
    my $errMsg = "In zdb_object_type, $nRecords records either has no home "
    	              ."table or has no home column";
      
    logError ($errMsg); 
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql); 
  }
  &recordResult($routineName, $nRecords);
} 

#----------------------------------------------------------
#Parameter
# $      Email Address for recipients
#

sub zdbObjectIsSourceDataCorrect($) {

  my $routineName = "zdbObjectIsSourceDataCorrect";

  my $sql = '
             select zobjtype_name,
                    zobjtype_is_data,
                    zobjtype_is_source

               from zdb_object_type

              where (zobjtype_is_data = "t" and
                     zobjtype_name not in (select get_obj_type(zactvd_zdb_id)
                                             from zdb_active_data) )
                 or (zobjtype_is_data = "f" and
                     zobjtype_name in (select get_obj_type(zactvd_zdb_id)
                                         from zdb_active_data) )
                 or (zobjtype_is_source = "t" and
                     zobjtype_name not in (select get_obj_type(zactvs_zdb_id)
                                             from zdb_active_source ) )
                 or (zobjtype_is_source = "f" and
                     zobjtype_name in (select get_obj_type(zactvs_zdb_id)
                                             from zdb_active_source ) )          
               ';
  
  my @colDesc = ("Zobjtype name     ",
		 "Zobjtype is data  ",
		 "Zobjtype is source" );

  my $nRecords = execSql ($sql, undef, @colDesc);

  if ( $nRecords > 0 ) {
    my $sendToAddress = $_[0];
    my $subject = " ZDB Source and Data misdefined";
    my $errMsg = "In zdb_object_type, $nRecords records are not consistent "
    	          ."with records in zdb_active_data and zdb_active_source";
      	
    logError ($errMsg); 
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql); 
  }
  &recordResult($routineName, $nRecords);
}

#------------------------------------------------------------
#zdbObjectHandledByGetObjName
#
# This ensures that the get_obj_name function is handling each object type
# defined in the zdb_object_type table.  get_obj_name returns null if it 
# does not recognize an object type.  It also flags an error (albeit a 
# misleading one) if there are no rows in existence for that object type.
#
#Parameter
# $      Email Address for recipients
# 
sub zdbObjectHandledByGetObjName ($) {

  my $routineName = "zdbObjectHandledByGetObjName";

  my $sql = "
             select zobjtype_name,
                    zobjtype_home_table,
                    zobjtype_home_zdb_id_column 
              from  zdb_object_type
              ";

  my @colDesc = ("Zobjtype name              ",
		 "Zobjtype home table        ",
		 "Zobjtype home ZDB ID column" );
  
  my $subSqlRef = \&subZdbObjectHandledByGetObjName;

  my $nRecords = execSql ($sql, $subSqlRef, @colDesc);

  if ( $nRecords > 0 ) {
    my $sendToAddress = $_[0];   	  
    my $subject = "Object name not available";
    my $errMsg = "In zdb_object_type, $nRecords records are not properly "
                  ."handled by the get_obj_name function.";
    logError ($errMsg); 
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql); 
  }
  &recordResult($routineName, $nRecords);
}

#----------------
#
sub subZdbObjectHandledByGetObjName {

  my @row = @_;
  my $sql = "select first 1 get_obj_name($row[2])
                 from $row[1]";
  
  my $sth = $dbh->prepare ($sql) or die "Prepare fails";
  $sth->execute();
  my $result =  $sth->fetchrow_array();
    
  return 1 if !$result || $result eq "NULL";
  return 0;
}


#-------------------------------------------------------------
# pubTitlesAreUnique
#
# Well, sort of.  Publication titles actually aren't unique.  We already have
# many distinct publications that have the same title.  This test is here to
# detect any NEW duplicate titles and report them.
# 
# Parameter
# $      Email Address for recipients
# 
sub pubTitlesAreUnique($) {
	
  my $routineName = "pubTitlesAreUnique";
	
  # Only need to exclude 1 record per pair.  That means if a duplicate
  # becomes a triplicate, we will detect it.
  # Exclude direct data submission pubs as we want many of these to have
  # the same title.

  my $sql1 = '
    select title 
      from publication, journal
      where zdb_id not in (
	"ZDB-PUB-021016-117", { "ZDB-PUB-961014-110", }
	"ZDB-PUB-961014-169", { "ZDB-PUB-961014-170", }
	"ZDB-PUB-021016-125", { "ZDB-PUB-961014-1217", }
	"ZDB-PUB-021016-60",  { "ZDB-PUB-991014-9", }
	"ZDB-PUB-000125-1",   { "ZDB-PUB-990525-2", }
	"ZDB-PUB-961014-288", { "ZDB-PUB-961014-289", }
	"ZDB-PUB-010718-37",  { "ZDB-PUB-010912-30", }
	"ZDB-PUB-981110-12",  { "ZDB-PUB-990218-4", }
	"ZDB-PUB-021016-112", "ZDB-PUB-961014-758", { "ZDB-PUB-961014-759", }
	"ZDB-PUB-000125-3",   { "ZDB-PUB-010131-19", }
	"ZDB-PUB-021015-13",  { "ZDB-PUB-030211-13", }
	"ZDB-PUB-961014-1233",{ "ZDB-PUB-961014-1234",}
	"ZDB-PUB-961014-106", { "ZDB-PUB-961014-107",}
	"ZDB-PUB-010417-9",   { "ZDB-PUB-990414-35",}
	"ZDB-PUB-010711-2",   { "ZDB-PUB-010814-8", }
	"ZDB-PUB-000824-10",  { "ZDB-PUB-990824-40",}
	"ZDB-PUB-010912-1",   { "ZDB-PUB-021017-13",}
        "ZDB-PUB-980420-9",   { "ZDB-PUB-030425-13",}
        "ZDB-PUB-010718-13",  { "ZDB-PUB-020913-1", }
        "ZDB-PUB-990414-54",  { "ZDB-PUB-021017-3", }
        "ZDB-PUB-010718-27",  { "ZDB-PUB-010821-1", }
        "ZDB-PUB-021017-74"   {, "ZDB-PUB-041012-5" }
        ,"ZDB-PUB-010918-3"   {, "ZDB-PUB-040216-6" }
        , "ZDB-PUB-050127-1" {, "ZDB-PUB-030408-12"}
        )
        and jrnl_abbrev <> "ZFIN Direct Data Submission"
        and jrnl_zdb_id = pub_jrnl_zdb_id
      group by title 
      having count(*) > 1 
     into temp dup_titles with no log;';
  
  # I tried running this through execSql, but if this query returned 0 rows
  # then the 2nd query would get a DBI error.
  my $sth = $dbh->prepare($sql1) or die "Prepare fails";
  $sth -> execute();

  my $sql2 = '
      select p.title, p.accession_no, p.zdb_id, p.authors, p.pub_date, jrnl_abbrev, p.pub_volume, p.pub_pages 
        from publication p, dup_titles d, journal
        where p.title = d.title 
        and jrnl_zdb_id = pub_jrnl_zdb_id
        order by p.title, p.zdb_id;';
  
  my @colDesc =  ("Title            ",
		  "Accession Number ",
		  "Pub ZDB ID       ",
		  "Authors          ",
		  "Pub Date         ",
		  "Jrnl_abbrev      ",
		  "Pub Volume       ",
		  "Pub Pages        ");

  my $nRecords = execSql ($sql2, undef, @colDesc);

  if ( $nRecords > 0 ) {

    my $sendToAddress = $_[0];
    my $subject = "duplicate pub titles detected";
    my $errMsg = "$nRecords publications have duplicate titles\n";
    logError ($errMsg);
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql1, $sql2);  
  }
  &recordResult($routineName, $nRecords);
} 

#-------------------------------------------------------
#Parameter
# $      Email Address for recipients
# 
sub addressStillNeedsUpdate ($) {
	
  my $routineName = "addressStillNeedsUpdate";
	
  my $sql = 'select pers_zdb_id
		from person_address
                where pers_old_address is null';

  my @colDesc = ("Person ZDB ID           ");

  my $nRecords = execSql ($sql, undef, @colDesc);

  if ( $nRecords > 0 ) {
    my $sendToAddress = $_[0];
    my $subject = "Autogen: People that need their addresses updated";
    my $errMsg ="$nRecords people need their addresses updated in new address table.";
      		        
    logError ($errMsg);
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql); 
  }
  &recordResult($routineName, $nRecords);
} 


#---------------------------------------------------------------
# Each entry in foreign_db should have 1 or more entries in
# foreign_db_contains.  foreign_db_contains describes what type(s) of
# data are available in the foreign DB.  If a foreign DB does not have
# 1 or more entries in foreign_db_contains then several joins in the
# web pages will fail, and the foreign DB will not show up.
#
# Parameter
#  $     Email Address for recipient
#
sub foreigndbNotInFdbcontains ($) {

  my $routineName = "foreigndbNotInFdbcontains";

  my $sql = " select fdb_db_name
                from foreign_db
               where fdb_db_name not in (
                        select fdbcont_fdb_db_name from foreign_db_contains) ";
  my @colDesc = ("Db Name    ");
  my $nRecords = execSql ($sql, undef, @colDesc);
  if ( $nRecords > 0 ) {
    my $sendToAddress = $_[0];
    my $subject = "Foreign db name not in fdbcontains";
    my $errMsg = "$nRecords foreign db records have no entry in foreign_db_contains.";
    logError($errMsg);
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql); 
  }
  &recordResult($routineName, $nRecords);
} 

#---------------------------------------------------------------
# An 'unique' annotation for go consists of a specific pub, evidence code,
# marker/feature, go_term, any inference data and any evidence_flags such as 
# 'not' or 'contributes to'.
# The marker_go_term_evidence table keeps track of pub, evidence code, go_term,
# and marker but does not enforce that the combination of these 4 is unique 
# (no AK on the four columns).  Instead, duplicate values of these 4 colums
# can exist in this table if and only if the 2 duplicate records also contain
# references to a unique combination of inference data (in the table
# inference_group_member) and go_evidence_flags (in the go_evidence_flag 
# table).  However, we can not enforce this relationship via a 
# table constraint as some marker, go_term, evidence_code, pub records will
# have no inference data or flags.  Therefore, on entering data into 
# the marker_go_term_evidence table, it is impossible to know if a user
# is trying to add a duplicate record, or if they are trying to add either
# and inference group or an evidence flag.  We need to check for duplicates
# at the end of the process.

# Parameter
#  $     Email Address for recipient
#
sub mrkrgoevDuplicatesFound ($) {

  my $routineName = "mrkrgoevDuplicatesFound";

  my $sql = 'select count(*),  
                    mrkrgoev_mrkr_zdb_id, 
                    mrkrgoev_go_term_zdb_id, 
                    mrkrgoev_source_zdb_id, 
                    mrkrgoev_evidence_code
               from marker_go_term_evidence
               where not exists (select * 
                                   from inference_group_member
                                   where mrkrgoev_zdb_id = 
                                            infgrmem_mrkrgoev_zdb_id)
               and not exists (select * 
                                   from go_evidence_flag
                                   where mrkrgoev_zdb_id = 
                                            goevflag_mrkrgoev_zdb_id)
               group by mrkrgoev_mrkr_zdb_id, 
                        mrkrgoev_go_term_zdb_id,
                        mrkrgoev_source_zdb_id,
                        mrkrgoev_evidence_code
               having count(*) > 1 ';

  my @colDesc = ("count", 
		 "mrkrgoev_mrkr_zdb_id" ,
		 "mrkrgoev_go_term_zdb_id",
		 "mrkrgoev_source_zdb_id", 
		 "mrkrgoev_evidence_code");

  my $nRecords = execSql ($sql, undef, @colDesc);
  if ( $nRecords > 0 ) {
    my $sendToAddress = $_[0];
    my $subject = "Possible duplicate records in marker_go_term_evidence";
    my $errMsg = "$nRecords are possible duplicates in marker_go_term_evidence";
    logError($errMsg);
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql); 
  }
  &recordResult($routineName, $nRecords);
} 
 
#----------------------------------------------------------------------------
# check for duplicate marker_goterm_Evidence, inference_groups.
#
# This statement checks for duplicate marker_Go_term_evidence
# records that have duplicate inference_group_members.
# 
# The first query uses 2 copies of the marker_go_term_evidence table
# each joining with inference_group_member table to find pairs of
# mrkrgoev records that have identical mrkr, go_term, source,
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
	        and a.mrkrgoev_go_Term_zdb_id = b.mrkrgoev_go_term_zdb_id
		and a.mrkrgoev_source_zdb_id = b.mrkrgoev_sourcE_zdb_id
		and a.mrkrgoev_evidence_code = b.mrkrgoev_evidence_code 
                and a.mrkrgoev_zdb_id = ia.infgrmem_mrkrgoev_zdb_id
                and b.mrkrgoev_zdb_id = ib.infgrmem_mrkrgoev_zdb_id
                and a.mrkrgoev_zdb_id > b.mrkrgoev_zdb_id
                and ia.infgrmem_inferred_from = ib.infgrmem_inferred_from
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



#--------------------------------------------------------------------------------------
# check for duplicate marker_goterm_Evidence, go_evidence_flag.

# Parameter
#  $     Email Address for recipient
#
sub mrkrgoevGoevflagDuplicatesFound ($) {

  my $routineName = "mrkrgoevGoevidenceFlagDuplicatesFound";

  my $sql = 'select count(*),  
                    mrkrgoev_mrkr_zdb_id, 
                    mrkrgoev_go_term_zdb_id, 
                    mrkrgoev_source_zdb_id, 
                    mrkrgoev_evidence_code, 
                    goevflag_gflag_name
               from marker_go_term_evidence, go_evidence_flag
               where not exists (select * 
                                   from inference_group_member
                                   where mrkrgoev_zdb_id = 
                                            infgrmem_mrkrgoev_zdb_id)
               and mrkrgoev_zdb_id = goevflag_mrkrgoev_zdb_id
               group by mrkrgoev_mrkr_zdb_id, 
                        mrkrgoev_go_term_zdb_id,
                        mrkrgoev_source_zdb_id,
                        mrkrgoev_evidence_code,
                        goevflag_gflag_name
               having count(*) > 1 ';

  my @colDesc = ("count", 
		 "mrkrgoev_mrkr_zdb_id" ,
		 "mrkrgoev_go_term_zdb_id",
		 "mrkrgoev_source_zdb_id",
		 "mrkrgoev_evidence_code",
                 "goevflag_gflag_name");

  my $nRecords = execSql ($sql, undef, @colDesc);
  if ( $nRecords > 0 ) {
    my $sendToAddress = $_[0];
    my $subject = "Possible duplicate records in marker_go_term_evidence, go_evidence_flag";
    my $errMsg = "$nRecords are possible duplicates in marker_go_term_evidence, go_evidence_flag";
    logError($errMsg);
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql); 
  }
  &recordResult($routineName, $nRecords);
}

#---------------------------------------------------------------
# check for obsolete annotations

# Parameter
#  $     Email Address for recipient
#
sub mrkrgoevObsoleteAnnotationsFound ($) {

  my $routineName = "mrkrgoevObsoleteAnnotationsFound";

  my $sql = 'select mrkrgoev_zdb_id
               from marker_go_term_evidence, go_term
               where mrkrgoev_go_term_zdb_id = goterm_zdb_id
               and goterm_is_obsolete = "t" '
              ;

  my @colDesc = ("mrkrgoev_zdb_id");

  my $nRecords = execSql ($sql, undef, @colDesc);
  if ( $nRecords > 0 ) {
    my $sendToAddress = $_[0];
    my $subject = "obsolete go terms exist with annotations";
    my $errMsg = "$nRecords annotations exist with obsolete go terms";
    logError($errMsg);
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql); 
  }
  &recordResult($routineName, $nRecords);
}


#---------------------------------------------------------------
# check for secondary annotations

# Parameter
#  $     Email Address for recipient
#
sub mrkrgoevSecondaryAnnotationsFound ($) {

  my $routineName = "mrkrgoevSecondaryAnnotationsFound";

  my $sql = 'select mrkrgoev_zdb_id
               from marker_go_term_evidence, go_term
               where mrkrgoev_go_term_zdb_id = goterm_zdb_id
               and goterm_is_secondary = "t"'
              ;

  my @colDesc = ("mrkrgoev_zdb_id");

  my $nRecords = execSql ($sql, undef, @colDesc);
  if ( $nRecords > 0 ) {
    my $sendToAddress = $_[0];
    my $subject = "secondary go terms exist with annotations";
    my $errMsg = "$nRecords annotations exist with secondary go terms";
    logError($errMsg);
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql); 
  }
  &recordResult($routineName, $nRecords);
}

#---------------------------------------------------------
#Parameter
# $      Email Address for recipients
# 

sub zdbReplacedDataIsReplaced ($) {

  my $routineName = "zdbReplacedDataIsReplaced";

  my $sql = '
             select zrepld_old_zdb_id,
                    zrepld_new_zdb_id,
                    zrepld_old_name  
               from zdb_replaced_data
              where zrepld_old_zdb_id in 
                       (select zactvd_zdb_id 
                          from zdb_active_data) ';
  
  my @colDesc = ("Zrepld old ZDB ID",
		 "Zrepld new ZDB ID",
                 "Zrepld old name  " );

  my $nRecords = execSql ($sql, undef, @colDesc);
  if ( $nRecords > 0 ) {
    my $sendToAddress = $_[0];
    my $subject = "Replaced data not replaced";
    my $errMsg = "In zdb_replaced_data, $nRecords replaced zdb ids are "
    	                    ."still in zdb_active_data\n";
    logError ($errMsg); 
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql);
  }
  &recordResult($routineName, $nRecords);
}

#========================== Active data & Source ========================
#-------------------
#
sub subZdbActiveDataSourceStillInUse {
 
  my @row = @_;
  my $sql = "select $row[2]
               from $row[1]                
              where $row[2] = '$row[0]'";
  
  my @result = $dbh->selectrow_array($sql);
  return @result? 0:1 ;
 
}

#-------------------------------------------------------------
#Parameter
# $      Email Address for recipients
# 

sub zdbActiveDataStillActive($) {
  
  my $routineName = "zdbActiveDataStillActive";
  &oldOrphanDataCheck($_[0]);
  my $sql = "
             select zactvd_zdb_id, 
                    zobjtype_home_table,
                    zobjtype_home_zdb_id_column
 
              from  zdb_active_data, 
                    zdb_object_type
              where get_obj_type(zactvd_zdb_id) = zobjtype_name";

  my @colDesc = ("Zactvd ZDB ID              ",
		 "Zobjtype home table        ",
		 "Zobjtype home zdb id column" );

  my $subSqlRef = \&subZdbActiveDataSourceStillInUse;
  my $nRecords = execSql ($sql, $subSqlRef, @colDesc);
  if ( $nRecords > 0 ) {
    
    &storeOrphan();
    my $sendToAddress = $_[0];
    my $subject = "orphan in zdb active data";
    my $errMsg = "In zdb_active_data, $nRecords ids are out of use, and stored in zdb_orphan_data table.";

    logWarning ($errMsg); 
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql); 
  } 
  &recordResult($routineName, $nRecords);
}           


#---------------------------------------------------------
#Parameter
# $      Email Address for recipients
#
sub zdbActiveSourceStillActive($) {
  
  my $routineName = "zdbActiveSourceStillActive";
  
  &oldOrphanSourceCheck($_[0]);
  my $sql = "
             select zactvs_zdb_id, 
                    zobjtype_home_table,
                    zobjtype_home_zdb_id_column
 
              from  zdb_active_source, 
                    zdb_object_type
              where get_obj_type(zactvs_zdb_id) = zobjtype_name";

  my @colDesc = ("Zactvs ZDB ID              ",
		 "Zobjtype home table        ",
		 "Zobjtype home zdb id column" );

  my $subSqlRef = \&subZdbActiveDataSourceStillInUse;

  my $nRecords = execSql ($sql, $subSqlRef, @colDesc);

  if ( $nRecords > 0 ) {

    &storeOrphan();
    my $sendToAddress = $_[0];
    my $subject = "orphan in zdb active source";
    my $errMsg = "In zdb_active_source, $nRecords ids are out of use, and stored in zdb_orphan_source table.";
             
    logWarning ($errMsg); 
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql);
  } 
  &recordResult($routineName, $nRecords);
}
   
#-------------------
#Parameter
# $      Email Address for recipients
#
sub oldOrphanDataCheck($) {

  open ORPH, ">$globalResultFile" or die "Cannot open the result file to write.";

  my $sql = "select * 
               from zdb_orphan_data ";
  my $sth = $dbh->prepare($sql) or die "Prepare fails";  
  $sth -> execute();

  my $fileNotEmpt;
  while (my @row = $sth ->fetchrow_array()) {

    $fileNotEmpt = 1;
    my $orphan = subZdbActiveDataSourceStillInUse(@row);

    if($orphan) {
      my $sql = "delete from zdb_active_data
                      where zactvd_zdb_id = '$row[0]'";
      $dbh -> do($sql);
      print ORPH "Delete $row[0] from zdb_active_data.\n";
    }else {
      my $sql = "delete from zdb_orphan_data
                      where zorphand_zdb_id = '$row[0]'";
      $dbh -> do($sql);
      print ORPH "$row[0] is restored.\n";
    }
  }
 
  close (ORPH);
  
  if($fileNotEmpt) {
    my $sendToAddress = $_[0];
    my $subject = "about previous orphans.";
    my $routineName = "oldOrphanDataCheck";
    my $msg = "Actions on the orphans detected last time.";
    &sendMail($sendToAddress, $subject,$routineName, $msg, );     

  }
}

#-------------------
#Parameter
# $      Email
# count the initial, final, deleted and mail all and the percentage delete from the previous day
sub scrubElsevierStatistics($){

    open RESULTFILE, ">$globalResultFile" or die "Cannot open the result file to write." ; 

    # START - this scrubs all of the static ips
    $sql = "delete from elsevier_statistics where es_incoming_ip in (select ei_ip from excluded_ip ) ; " ; 
    my $allsql = "" ; 
    $allsql = $allsql . $sql . "\n" ; 
    my $preparedStmt1 = $dbh->prepare($sql) or die "Prepare fails";  
    my $ipsscrubbed = $preparedStmt1-> execute();
    if($ipsscrubbed>0){
        print RESULTFILE "Deleted $ipsscrubbed rows from elsevier_statistics according to static ips from the excluded_ip table.\n" ; 
    }
    # END - this scrubs all of the static ips


    # START - this scrubs all of the dynamic ips
    $sql = 'select ei_ip from excluded_ip where ei_ip not like "%.%.%.%"  ; ' ; 
    $allsql = "" ; 
    $allsql = $allsql . $sql . "\n" ; 
    my $preparedStmt2 = $dbh->prepare($sql) or die "Prepare fails";  
    $preparedStmt2->execute()  ;  

    my $didScrubDynamicIPs = 0 ; 
    while (my @row = $preparedStmt2->fetchrow_array()) {
#        print "$row[0]\n" ; 
        my $subSQL = "delete from elsevier_statistics where es_incoming_ip like '$row[0]%'" ; 
        my $preparedStmt4 = $dbh->prepare($subSQL) or die "Prepare fails";  
        my $dynamicscrubbed =  $preparedStmt4->execute();

        if($dynamicscrubbed>0){
            print RESULTFILE "Deleted $dynamicscrubbed rows from elsevier_statistics according to subnet is $row[0].*.\n" ; 
            $didScrubDynamicIPs = 1 ; 
        }

    }
    # END - this scrubs all of the dynamic ips





    $sql = "delete from elsevier_statistics where es_http_user_agent like '%bot%' or es_http_user_agent like '%crawl%' or es_http_user_agent is null ; " ; 
    $allsql = $allsql . $sql . "\n" ; 
    my $preparedStmt3 = $dbh->prepare($sql) or die "Prepare fails";  
    my $agentsscrubbed = $preparedStmt3-> execute();
    if($agentsscrubbed >0){
        print RESULTFILE "Deleted $agentsscrubbed rows from elsevier_statistics according to user_agent.\n" ; 
    }
    $sql = "$allsql" ; 

    close(RESULTFILE) ; 
    if($ipsscrubbed>0 || $agentsscrubbed >0 || $didScrubDynamicIPs > 0){
        my $sendToAddress = $_[0];
        my $subject = "elsevier scrub statistics";
        my $routineName = "scrubElsevierStatistics";
        my $msg = "Scrubbed ips from elsevier_statistics table.";
        &sendMail($sendToAddress, $subject,$routineName, $msg, $sql);     
    }
}


#-------------------
#Parameter
# $      Email Address for recipients
# 
sub oldOrphanSourceCheck($) {

  open ORPH, ">$globalResultFile" or die "Cannot open the result file to write.";

  my $sql = "select * 
               from zdb_orphan_source ";
  my $sth = $dbh->prepare($sql) or die "Prepare fails";  
  $sth -> execute();

  my $fileNotEmpt;
  while (my @row = $sth ->fetchrow_array()) {
   
    $fileNotEmpt = 1;
    my $orphan = subZdbActiveDataSourceStillInUse(@row);
    
    if($orphan) {
      my $sql = "delete from zdb_active_source
                      where zactvs_zdb_id = '$row[0]'";
      $dbh -> do($sql);
      print ORPH "Delete $row[0] from zdb_active_source.\n";
    }else {
      my $sql = "delete from zdb_orphan_source
                      where zorphans_zdb_id = '$row[0]'";
      $dbh -> do($sql);
      print ORPH "$row[0] is restored.\n";
    }
  }
  close(ORPH);
  if($fileNotEmpt) {
    my $sendToAddress = $_[0];
    my $subject = "about previous orphans.";
    my $routineName = "oldOrphanSourceCheck";
    my $msg = "Actions on the orphans detected last time.";
    &sendMail($sendToAddress, $subject, $routineName, $msg, );
  }
}

#-------------------
# 
sub storeOrphan {
  
 
  my ($table, $zdbid, $hometable, $homecolumn);
  open F, "$globalResultFile" or die "Cannot open the $globalResultFile to read.\n";
  
  while (<F>) {
    
    if(/Zactvd ZDB ID\s+(\w.+)/) {
      $zdbid = $1;
      $table = "zdb_orphan_data";
    }
    if(/Zactvs ZDB ID\s+(\w.+)/) {
      $zdbid = $1;
      $table = "zdb_orphan_source";
    }
    if (/Zobjtype home table\s+(\w.+)/) {
      $hometable = $1;
    }
    if (/Zobjtype home zdb id column\s+(\w.+)/) {
      $homecolumn = $1;
      my $sql = "insert into $table values('$zdbid', '$hometable', '$homecolumn')";
      
      $dbh->do($sql);
    }
  }
  close (F);
}
#=======================================================================


#######################  Main ###########################################
#
# Define Usage 
# 

$usage = "Usage: validatedata.pl <dbname> [-d|-w|-m|-y|-o] ";

$document = <<ENDDOC;

$usage

Command line parameters:

  dbname   Name of database to validate. This must be a ZFIN database.
  -d       Excute the checks supposed to run daily.  
  -w       Excute the checks supposed to run weekly.
  -m       Excute the checks supposed to run monthly.
  -y       Excute the checks supposed to run yearly.
  -o       Excute the orphan checks.

ENDDOC

if (@ARGV < 2) {
  print $document and exit 1;
}

GetOptions (
	    "d"    => \$daily,
	    "w"    => \$weekly,
	    "m"    => \$monthly,
	    "y"    => \$yearly,
            "o"    => \$orphan 
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
my $linkageEmail = "<!--|VALIDATION_EMAIL_LINKAGE|-->";
my $otherEmail   = "<!--|VALIDATION_EMAIL_OTHER|-->";
my $estEmail     = "<!--|VALIDATION_EMAIL_EST|-->";
my $geneEmail    = "<!--|VALIDATION_EMAIL_GENE|-->";
my $mutantEmail  = "<!--|VALIDATION_EMAIL_MUTANT|-->";
my $dbaEmail     = "<!--|VALIDATION_EMAIL_DBA|-->";
my $goEmail      = "<!--|GO_EMAIL_CURATOR|-->";
my $adminEmail   = "<!--|ZFIN_ADMIN|-->";
my $morpholinoEmail = "<!--|VALIDATION_EMAIL_MORPHOLINO|-->";

if($daily) {
    scrubElsevierStatistics($xpatEmail) ; 
#    checkClosedElsevierFigureNoExpressions($xpatEmail); # Elsevier is allowing this for now
    expressionResultStageWindowOverlapsAnatomyItem($xpatEmail);
    xpatHasConsistentMarkerRelationship($xpatEmail);
    checkFigXpatexSourceConsistant($dbaEmail);
    checkFigApatoSourceConsistant($dbaEmail);

    featureAssociatedWithGenotype($mutantEmail);
    featureIsAlleleOfOrMrkrAbsent($mutantEmail);
    genotypesHaveNoNames($mutantEmail);
    linkageHasMembers($linkageEmail);
    linkagePairHas2Members($linkageEmail);

    foreigndbNotInFdbcontains($otherEmail);

    zdbObjectHomeTableColumnExist($dbaEmail);
    zdbObjectIsSourceDataCorrect($dbaEmail);
    zdbObjectHandledByGetObjName($dbaEmail);

    pubTitlesAreUnique($otherEmail);
    zdbReplacedDataIsReplaced($dbaEmail);

    mrkrgoevDuplicatesFound($goEmail);
    mrkrgoevGoevflagDuplicatesFound($goEmail);
    mrkrgoevObsoleteAnnotationsFound($goEmail);
    mrkrgoevSecondaryAnnotationsFound($goEmail);
}
if($orphan) {
  
  zdbActiveDataStillActive($dbaEmail);
  zdbActiveSourceStillActive($dbaEmail);
}
if($weekly) {
  # put these here until we get them down to 0 records.  Then move them to 
  # daily.
#  checkOpenElsevierFigureNoExpresWithPATO($xpatEmail); # elsevier is allowing this for now
	estsHave1Gene($estEmail);
	prefixedGenesHave1Est($estEmail);
	estsWithoutClonesHaveXxGenes($estEmail);
	xxGenesHaveNoClones($estEmail);
        xpatObjectNotGeneOrEFG ($xpatEmail);

	# these are curatorial errors (case219)
	# however, errors returned are difficult to
	# return to curators without dba
	associatedDataforPUB030905_1($dbaEmail);
	associatedDataforPUB030508_1($dbaEmail);
	associatedDataforPUB030905_2($dbaEmail);

	# put these here until we get them down to 0 records.  Then move them to 
	# daily.
	orthologyOrganismMatchesForeignDBContains($dbaEmail);

	refSeqAccessionInWrongFormat($geneEmail);
	vegaAccessionInWrongFormat($geneEmail);
	morpholinoAbbrevContainsGeneAbbrev($morpholinoEmail);

}
if($monthly) {
  orthologueHasDblink($geneEmail);
  orthologueNomenclatureValid($geneEmail);
  prefixedIbdGenesHave1Est($estEmail);
  genesWithCommonDblinks($geneEmail);
  orthologyHasEvidence($geneEmail);
  mouseOrthologyHasValidMGIAccession($geneEmail);
  mouseOrthologyHasEntrezAccession($geneEmail);
  humanOrthologyHasEntrezAccession($geneEmail);
  containedInRelationshipsInEST($geneEmail);
  encodesRelationshipsInBACorPAC($geneEmail);
  addressStillNeedsUpdate($adminEmail);
  mrkrgoevInfgrpDuplicatesFound($goEmail);
}
if($yearly) {

}

	   

#rmdir($globalWorkingDir);
