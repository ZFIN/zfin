#! /local/bin/perl5 -w 

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

#=================== Stage Window  =================================

#--------------------------------------------------------
#Parameter
# $      Email Address for recipients

sub stageWindowConsistent ($) {

  my $routineName = "stageWindowConsistent";

  my $sql = '
             select stg_zdb_id, 
                    stg_name
                    stg_hours_start, 
                    stg_hours_end
               from stage
              where stg_hours_start > stg_hours_end ';
  
  my @colDesc = ("Stg ZDB ID     ",
		 "Stg name       ",
		 "Stg hours start",
		 "Stg hours end  " );

  my $nRecords = execSql ($sql, undef, @colDesc);

  if ( $nRecords > 0 ) {
    my $sendToAddress = $_[0];
    my $subject = "stage window inconsistence in stage";
    my $errMsg = "In stage table, $nRecords records' stage start hours are "
                     ."greater than end hours. ";
    logError ($errMsg); 
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql);
  }
  &recordResult($routineName, $nRecords);
}
 
#-------------------------------------------------------
#Parameter
# $      Email Address for recipients

sub stageContainsStageWindowInContainerStageWindow ($) {

  my $routineName = "stageContainsStageWindowInContainerStageWindow";

  my $sql = '
              select stgcon_container_zdb_id,
                     s1.stg_name_long,
                     stgcon_contained_zdb_id,
                     s2.stg_name_long
  		from stage_contains, 
                     stage s1, stage s2
               where stgcon_container_zdb_id = s1.stg_zdb_id             
                 and stgcon_contained_zdb_id = s2.stg_zdb_id
                 and (s1.stg_hours_start > s2.stg_hours_start
                      or s1.stg_hours_end < s2.stg_hours_end) 
             ';

  my @colDesc = ("Stgcon container ZDB ID    ",
		 "Container stg name & period",
		 "Stgcon contained ZDB ID    ",
		 "Contained stg name & period" );

  my $nRecords = execSql ($sql, undef, @colDesc);
  
  if ( $nRecords > 0 ) {
    my $sendToAddress = $_[0];
    my $subject = "Stage window inconsistence in stage_contains";
    my $errMsg = "In stage_contains, $nRecords records' container's stage window" 
                   ." doesn't fully contain contained 's. ";

    logError ($errMsg);
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql);
  }
  &recordResult($routineName, $nRecords);
}

#=================== Anatomy Item  =================================
#-----------------------------------------------------------
#Parameter
# $      Email Address for recipients
 
sub anatomyItemStageWindowConsistent ($) {
  
  my $routineName = "anatomyItemStageWindowConsistent";
  
  my $sql = ' 
              select anatitem_zdb_id, 
                     anatitem_name,
                     anatitem_start_stg_zdb_id, 
                     s1.stg_name_long, 
                     anatitem_end_stg_zdb_id,
                     s2.stg_name_long

        	from anatomy_item,
                     stage s1, stage s2 
               where stg_window_consistent
                             (anatitem_start_stg_zdb_id,
                              anatitem_end_stg_zdb_id  ) = "f"
                 and anatitem_start_stg_zdb_id = s1.stg_zdb_id
                 and anatitem_end_stg_zdb_id = s2.stg_zdb_id
            ';

  my @colDesc = ("Anatitem ZDB ID          ",
		 "Anatitem name            ",
		 "Anatitem start stg ZDB ID",
		 "Start stg name and period",
		 "Anatitem end stg ZDB ID  ",
                 "End stg name and period  " );

  my $nRecords = execSql ($sql, undef, @colDesc);
  
  if ( $nRecords > 0 ) {
    my $sendToAddress = $_[0];
    my $subject = "Stage window inconsistence in anatomy_item";
    my $errMsg = "In anatomy_item, $nRecords records have inconsistent " 
                   ."stage window. ";

    logError ($errMsg);
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql);
  }
  &recordResult($routineName, $nRecords);
}
  
#-------------------------------------------------------------- 
#Parameter
# $      Email Address for recipients

sub anatomyContainsStageWindowConsistent ($) {
  
  my $routineName = "anatomyContainsStageWindowConsistent";
 
  my $sql = '
             select anatcon_container_zdb_id, 
                    anatcon_contained_zdb_id,
                    anatcon_start_stg_zdb_id, 
                    anatcon_end_stg_zdb_id
               from anatomy_contains
              where stg_window_consistent (
                                   anatcon_start_stg_zdb_id,
                                   anatcon_end_stg_zdb_id
                                   )="f"  ';

  my $nRecords = execSql ($sql); 
  
  if ( $nRecords > 0 ) {
    
    my $sqlDtl = '
                  select anatcon_container_zdb_id,
                         i1.anatitem_name,
                         anatcon_contained_zdb_id,
                         i2.anatitem_name,
                         anatcon_start_stg_zdb_id,
                         s1.stg_name_long,
                         anatcon_end_stg_zdb_id,
                         s2.stg_name_long 
                    from anatomy_contains,
                         anatomy_item i1, anatomy_item i2,
                         stage s1, stage s2
                   where stg_window_consistent (
                                   anatcon_start_stg_zdb_id,
                                   anatcon_end_stg_zdb_id
                                   )="f"
                     and anatcon_container_zdb_id = i1.anatitem_zdb_id
                     and anatcon_contained_zdb_id = i2.anatitem_zdb_id
                     and anatcon_start_stg_zdb_id = s1.stg_zdb_id
                     and anatcon_end_stg_zdb_id = s2.stg_zdb_id
                   ';

    my @colDesc = ("Anatcon container ZDB ID ",
		   "Container name           ",
                   "Anatcon contained ZDB ID ",
		   "Contained name           ",
		   "Anatcon start stg ZDB ID ",
		   "Start stg name and period",
		   "Anatcon end stg ZDB ID   ",
		   "End stg name and period  " );

    my $nRecordsDtl = execSql($sqlDtl, undef, @colDesc);
 
    if ( $nRecords == $nRecordsDtl ) {
     
      my $sendToAddress = $_[0];
      my $subject = "Stage window inconsistence in anatomy_contains";
      my $errMsg = "In anatomy_contain, $nRecords records have inconsistent " 
                 ."stage window.";
                
      logError ($errMsg);
      sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql, $sqlDtl);
    }else {
      print "Two queries are not consistent.";
    }
  }
  &recordResult($routineName, $nRecords);
}

#------------------------------------------------------------
#Parameter
# $      Email Address for recipients
 

sub anatomyContainsStageWindowInContainerStageWindow($){

  my $routineName = "anatomyContainsStageWindowInContainerStageWindow";

  my $sql = '
              select anatcon_container_zdb_id,
                     anatitem_name,
  		     anatitem_start_stg_zdb_id,
                     s1.stg_name_long,
                     anatitem_end_stg_zdb_id,
                     s2.stg_name_long,
  		     anatcon_start_stg_zdb_id,
                     s3.stg_name_long,
                     anatcon_end_stg_zdb_id,
                     s4.stg_name_long
	        from anatomy_contains, 
                     anatomy_item, 
                     stage s1, stage s2, stage s3, stage s4
               where anatcon_container_zdb_id = anatitem_zdb_id
                and anatitem_start_stg_zdb_id = s1.stg_zdb_id             
                and anatitem_end_stg_zdb_id = s2.stg_zdb_id
                and anatcon_start_stg_zdb_id = s3.stg_zdb_id
                and anatcon_end_stg_zdb_id = s4.stg_zdb_id
                and (s1.stg_hours_start > s3.stg_hours_start
                    or s2.stg_hours_end < s4.stg_hours_end) 
                ';

  my @colDesc = ( "Anatcon container ZDB ID   ",
		  "Container name             ",
		  "Container start stg ZDB ID ",
		  "Start stg name and peroid  ",
		  "Container end stg ZDB ID   ",
		  "End stg name and period    ",
		  "Anatcon start stg ZDB ID   ",
		  "Start stg name and period  ",
		  "Anatcon end stg ZDB ID     ",
		  "End stg name and period    " );

  my $nRecords = execSql ($sql, undef, @colDesc);

  if ( $nRecords > 0 ) {

    my $sendToAddress = $_[0];
    my $subject = "Stage window out of range";
    my $errMsg = "In anatomy_contains, $nRecords records have stage window " 
                 ."out of the range of container's stage window. ";

    logError ($errMsg);
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql); 
  }
  &recordResult($routineName, $nRecords);
}



#=================== Expression Pattern ==========================              
#----------------------------------------------------------------
#Parameter
# $      Email Address for recipients
 

sub expressionPatternStageWindowConsistent($) {
	
  my $routineName = "expressionPatternStageWindowConsistent";
	
  my $sql = '
             select xpatstg_xpat_zdb_id, 
                    xpatstg_start_stg_zdb_id,
                    s1.stg_name_long,   
                    xpatstg_end_stg_zdb_id,
                    s2.stg_name_long

	       from expression_pattern_stage,
                    stage s1, stage s2 

              where stg_window_consistent (
                                     xpatstg_start_stg_zdb_id,
                                     xpatstg_end_stg_zdb_id
                                     ) = "f" 
                and xpatstg_start_stg_zdb_id = s1.stg_zdb_id
                and xpatstg_end_stg_zdb_id = s2.stg_zdb_id
              ';
  	
  my @colDesc = ("Xpatstg ZDB ID     ",
		 "Start Stage ZDB ID ",
		 "Start Stage Name   ",
		 "End Stage ZDB ID   ",
		 "End Stage Name     ");

  my $nRecords = execSql ($sql, undef, @colDesc);

  if ( $nRecords > 0 ) {

    my $sendToAddress = $_[0];
    my $subject = "Stage window inconsistence";
    my $errMsg = "In expression_pattern_stage, $nRecords records' stage"
    		      ." windoware not consistent. ";
      		       
    logError ($errMsg);
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql);
  }
  &recordResult($routineName, $nRecords);
} 

#---------------------------------------------------------------
#Parameter
# $      Email Address for recipients
# 

sub expressionPatternAnatomyStageWindowOverlapsAnatomyItem ($) {
	
  my $routineName = "expressionPatternAnatomyStageWindowOverlapsAnatomyItem";
	
  my $sql = '
            select xpatanat_anat_item_zdb_id, 
                   xpatanat_xpat_start_stg_zdb_id,
       	           xpatanat_xpat_end_stg_zdb_id
	      from expression_pattern_anatomy
             where anatitem_overlaps_stg_window(
                                     xpatanat_anat_item_zdb_id,
                                     xpatanat_xpat_start_stg_zdb_id,
                                     xpatanat_xpat_end_stg_zdb_id
                                     ) = "f"';

  my $nRecords = execSql ($sql);
  
  if ( $nRecords > 0 ) {

    my $sqlDtl = ' 
                   select xpatanat_anat_item_zdb_id,
			  anatitem_name,
			  anatitem_start_stg_zdb_id,
			  s1.stg_name_long,
			  anatitem_end_stg_zdb_id,
			  s2.stg_name_long,
                          xpatanat_xpat_start_stg_zdb_id,
			  s3.stg_name_long,
                          xpatanat_xpat_end_stg_zdb_id,
			  s4.stg_name_long,
			  xpatanat_xpat_zdb_id
                     from expression_pattern_anatomy, 
                          anatomy_item,
			  stage s1, stage s2, stage s3, stage s4
                    where anatitem_overlaps_stg_window(
                                     xpatanat_anat_item_zdb_id,
                                     xpatanat_xpat_start_stg_zdb_id,
                                     xpatanat_xpat_end_stg_zdb_id
                                     ) = "f"
                      and xpatanat_anat_item_zdb_id      = anatitem_zdb_id
		      and anatitem_start_stg_zdb_id      = s1.stg_zdb_id
		      and anatitem_end_stg_zdb_id        = s2.stg_zdb_id
		      and xpatanat_xpat_start_stg_zdb_id = s3.stg_zdb_id
		      and xpatanat_xpat_end_stg_zdb_id   = s4.stg_zdb_id
	         ';

    my @colDesc = ("Xpatanat anat item ZDB ID",
		   "Anatitem name            ",
		   "Anatitem start stg ZDB ID",
		   "Start stg name and period",
		   "Anatitem end stg ZDB ID  ",
		   "End stg name and period  ",
		   "Xpatanat xpat start stg ZDB ID",
		   "Start stg name and period",
		   "Xpatanat xpat end stg ZDB ID",
		   "End stg name and period     ",
		   "Xpatanat xpat ZDB ID     " );

    my $nRecordsDtl = execSql($sqlDtl, undef, @colDesc);
   
    if ( $nRecords == $nRecordsDtl ) {
     
      my $sendToAddress = $_[0];
      my $subject = "Xpat's stage window not overlaps with anatomy item's";
      my $errMsg = "In expression_pattern_anatomy, $nRecords records' stage "
	           ."window don't overlap with the anatomy item's stage window.";
		      
      logError ($errMsg);	
      &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql, $sqlDtl);
    }else {
      print "Two queries are not consistent.\n";
    }
  }
  &recordResult($routineName, $nRecords);
}


#======================== Fish Names, Abbrevs, Alleles =====================
#
# These checks are an attmept to slightly constrain the chaos that is the
# mutant tables.  These checks exist only because the tables are poorly
# designed and have redundant data in them.  They are an attempt to keep
# the redundancy consistent.  Grumble, grumble.

#---------------------------------------------------------------
# fishNameEqualLocusName
#
# The fish.name column should equal a locus.name column.
# We cant enforce this with a foreign key because (as of 2003/01)
# locus.name is not unique (more grumbling).
# We can't enforce this with a check constraint beacuse the constraint
# crosses 2 tables.
# 
#Parameter
# $      Email Address for recipients
# 
sub fishNameEqualLocusName ($) {

  my $routineName = "fishNameEqualLocusName";
	
  my $sql = 'select fish.name, locus_name, fish.zdb_id, locus.zdb_id, 
                    get_fish_full_name(fish.zdb_id), 
                    fish.abbrev, fish.allele, locus.abbrev
               from fish, locus
              where fish.locus = locus.zdb_id
                and line_type = "mutant"
                and fish.name <> locus_name';

  my @colDesc = ("Fish name         ",
		 "Locus name        ",
		 "Fish ZDB ID       ",
		 "Locus ZDB ID      ",
		 "Full fish name    ",
		 "Fish abbrev       ",
		 "Fish allele       ",
		 "Locus abbrev      " );
  
  my $nRecords = execSql ($sql, undef, @colDesc);
	
  if ( $nRecords > 0 ) {
    my $sendToAddress = $_[0];
    my $subject = "Fish name and locus name disagree";
    my $errMsg = "The name field in $nRecords fish record(s) does not equal locus name. ";
    
    logError ($errMsg);
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql);
  }
  &recordResult($routineName, $nRecords); 
} 


#---------------------------------------------------------------
# fishAbbrevContainsFishAllele
#
# The fish.abbrev column in mutants should contain the allele specified
# in the fish's allele column.  
#
# This constraint could be enforced with a check constraint, but I don't 
# feel like verifying that the web pages populate the fields in an order
# that would satisfy the constraint.
# 
# Of course, the allele field doesn't belong in the fish record in the first
# place.
# 
#Parameter
# $      Email Address for recipients
# 
sub fishAbbrevContainsFishAllele ($) {

  my $routineName = "fishAbbrevContainsFishAllele";
	
  my $sql = 'select abbrev, allele, zdb_id
		 from fish
		 where line_type = "mutant"
		   and fish.abbrev not like "%" || fish.allele || "%"';

  my @colDesc = ("Fish abbrev       ",
		 "Fish allele       ",
		 "Fish ZDB ID       ");
  
  my $nRecords = execSql ($sql, undef, @colDesc);
	
  if ( $nRecords > 0 ) {
    my $sendToAddress = $_[0];
    my $subject = "Fish abbrev does not contain fish allele";
    my $errMsg = "The abbrev field in $nRecords fish record(s) does not contain fish allele. ";
    
    logError ($errMsg);
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql); 
  }
  &recordResult($routineName, $nRecords);
} 


#---------------------------------------------------------------
# fishAbbrevStartsWithLocusAbbrev
#
# The fish.abbrev column should start with the locus abbrev.
#
# We can't enforce this with a check constraint beacuse the constraint
# crosses 2 tables.
# 
#Parameter
# $      Email Address for recipients
# 
sub fishAbbrevStartsWithLocusAbbrev ($) {

  my $routineName = "fishAbbrevStartsWithLocusAbbrev";

  my $sql = 'select fish.abbrev, locus.abbrev,
                    fish.zdb_id, get_fish_full_name(fish.zdb_id), fish.name, 
                    fish.allele, locus.zdb_id, locus_name
	       from fish, locus
	       where fish.locus = locus.zdb_id
		 and line_type = "mutant"
		 and fish.abbrev not like (locus.abbrev || "%")
		 and locus.abbrev <> ""
                 and locus.abbrev <> "NULL"
                 and locus.abbrev is not null
                 and (   substr(locus.abbrev,1,4) <> "unm "
                      or substr(locus.abbrev,1,3) <> substr(fish.abbrev,1,3))';

  my @colDesc = ("Fish abbrev       ",
		 "Locus abbrev      ",
		 "Fish ZDB ID       ",
		 "Full fish name    ",
		 "Fish name         ",
		 "Fish allele       ",
		 "Locus ZDB ID      ",
		 "Locus name        ");
  
  my $nRecords = execSql ($sql, undef, @colDesc);
	
  if ( $nRecords > 0 ) {
    my $sendToAddress = $_[0];
    my $subject = "Fish abbrev does not start with locus abbrev";
    my $errMsg = "The abbrev field in $nRecords fish record(s) does not start with locus abbrev. ";

    logError ($errMsg);
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql); 
  }
  &recordResult($routineName, $nRecords); 
} 



#======= FISH - ALLELE - INT_FISH_CHROMO - CHROMOSOME - ALLELE - FISH ======
#
# Before recorded history there was an unsuccessful attempt to support 
# double mutants at ZFIN.  While the attempt failed, it failed only
# after the tables had been modified in certain ways.  The tables still
# only support single mutants, however due to the way they are designed
# they can easily support single mutants incorrectly.  These tests
# attempt to verify that single mutants are done correctly.
#
# Mutant data is spread across 4 tables (5 if you count locus, but those
# tests are elsewhere).  These 4 tables form a box, with 1:1 relationships
# between each of them.  These tests make sure that relationships are in
# fact 1:1.
#
# The mutant table box is:
#
#   ALTERATION -- 1 ------------------------- 1 -- FISH
#     |                                             |
#     1                                             1
#     |                                             |
#     |                                             |
#     |                                             |
#     |                                             |
#     1                                             1
#     |                                             |
#   CHROMOSOME -- 1 -------------- 1 -- INT_FISH_CHROMO


#--------------------------------------------------------------
# alterationHas1Fish(
#
# Confirm that an alteration record has a single fish record
# Can't make the allele column of fish be unique because it is null
# for wildtype fish.
#
# Parameter
#  $        Email address of recipients
#

sub alterationHas1Fish($) {

  my $routineName = "alterationHas1Fish";

  my $sql = 'select allele, zdb_id 
               from alteration a
               where 1 <>
                     ( select count(*) 
                         from fish f
                         where f.allele = a. allele )
               order by allele';

  my @colDesc = ("Alteration Name   ",
		 "Alteration ZDB ID ");
  
  my $nRecords = execSql ($sql, undef, @colDesc);
	
  if ( $nRecords > 0 ) {
    my $sendToAddress = $_[0];
    my $subject = "Alterations have 0 or > 1 associated fish";
    my $errMsg = "$nRecords alterations had 0 or more than 1 associated fish. ";

    logError ($errMsg);
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql); 
  }
  &recordResult($routineName, $nRecords);
} 



#--------------------------------------------------------------
# mutantHas4TableBox
#
# Every mutant fish must have an alteration record.  The previous test
# verified that every alteration had a mutant.  This test asks the 
# mirror of that question, but it does so by going all the way around 
# the box, thus also confirming that the int_fish_chromo and chromosome
# records exist as well.
#
# Parameter
#  $        Email address of recipients
#

sub mutantHas4TableBox($) {

  my $routineName = "mutantHas4TableBox";

  # I originally tried this a 4 way outer join, but I couldn't get it to
  # work.

  my $sql = 'select abbrev, name, allele, zdb_id
               from fish f
               where line_type = "mutant"
                 and 1 <> 
                     ( select count(*)
                         from int_fish_chromo ifc, chromosome c, alteration a
                         where f.zdb_id = ifc.source_id
                           and ifc.target_id = c.zdb_id
                           and c.zdb_id = a.chrom_id
                           and a.allele = f.allele )
               order by abbrev, name, allele';

  my @colDesc = ("Fish Abbrev       ",
		 "Fish Name         ",
		 "Fish Allele       ",
		 "Fish ZDB ID       ");
  
  my $nRecords = execSql ($sql, undef, @colDesc);
	
  if ( $nRecords > 0 ) {
    my $sendToAddress = $_[0];
    my $subject = "Mutant FISH records missing records in other mutant tables ";
    my $errMsg = "$nRecords mutant(s) had 0 or more than 1 records in associated tables. ";

    logError ($errMsg);
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql); 
  }
  &recordResult($routineName, $nRecords); 
} 


#======================== Locus Names, Abbrevs =====================
#
# Strictly speaking, locus names and abbrevs are not unique, and therefore
# we can't enforce their uniqueness with a database constraint.  However,
# the number of cases where they can be non-unique is a small set, and
# therefore we can check for non-unique names and abbrevs that are 
# outside that set.  
#
# A request has been passed on to Erik to think about nomenclature changes
# which would get rid of the duplicate names and abbrevs altogether.

#---------------------------------------------------------------
# locusAbbrevUnique
#
# Check that all locus abbrevs outside the special cases are unique.
# 
#Parameter
# $      Email Address for recipients
# 
sub locusAbbrevUnique ($) {

  my $routineName = "locusAbbrevUnique";
	
  my $sql = 'select abbrev, locus_name, zdb_id
               from locus loc1
	       where abbrev <> "xxx"
		 and exists
		     ( select count(*), abbrev
			 from locus loc2
			 where loc1.abbrev = loc2.abbrev
			   and loc2.locus_name not like "Df%"
			   and loc2.locus_name not like "T%"
		       group by abbrev
		       having count(*) > 1 )
               order by abbrev, locus_name, zdb_id';

  my @colDesc = ("Locus abbrev      ",
		 "Locus name        ",
		 "Locus ZDB ID      ");
  
  my $nRecords = execSql ($sql, undef, @colDesc);
	
  if ( $nRecords > 0 ) {
    my $sendToAddress = $_[0];
    my $subject = "Locus record(s) with non-unique abbrevs";
    my $errMsg = "$nRecords locus records have non-unique abbrevs. ";

    logWarning ($errMsg);
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql); 
  }
  &recordResult($routineName, $nRecords); 
} 



#---------------------------------------------------------------
# locusNameUnique
#
# Check that all locus name outside the special cases are unique.
# 
#Parameter
# $      Email Address for recipients
# 
sub locusNameUnique ($) {

  my $routineName = "locusNameUnique";
	
  my $sql = 'select locus_name, abbrev, zdb_id
	       from locus 
	       where exists
		     ( select count(*), locus_name
			 from locus 
			 where locus_name not like "Df%"
			   and locus_name not like "T%"
			 group by locus_name 
			 having count(*) > 1 )
	       order by locus_name, abbrev, zdb_id';

  my @colDesc = ("Locus name        ",
		 "Locus abbrev      ",
		 "Locus ZDB ID      ");
  
  my $nRecords = execSql ($sql, undef, @colDesc);
	
  if ( $nRecords > 0 ) {
    my $sendToAddress = $_[0];
    my $subject = "locus record(s) with non-unique names";
    my $errMsg = "$nRecords locus records have non-unique names. ";
    logError ($errMsg);
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql); 
  }
  &recordResult($routineName, $nRecords);
} 



#---------------------------------------------------------------
# locusAbbrevIsSet
#
# Reports all locus abbrevs (outside the special cases) that are not set.
# 
#Parameter
# $      Email Address for recipients
# 
sub locusAbbrevIsSet ($) {

  my $routineName = "locusAbbrevIsSet";
	
  my $sql = 'select abbrev, locus_name, zdb_id
	       from locus 
	       where (   abbrev = "xxx" 
                      or abbrev is NULL)
                 and locus_name not like "Df%"
	         and locus_name not like "T%"
	       order by abbrev, locus_name, zdb_id';

  my @colDesc = ("Locus abbrev      ",
		 "Locus name        ",
		 "Locus ZDB ID      ");
  
  my $nRecords = execSql ($sql, undef, @colDesc);
	
  if ( $nRecords > 0 ) {
    my $sendToAddress = $_[0];
    my $subject = "Locus abbrev not set";
    my $errMsg = "Locus abbrev not set in $nRecords locus record(s). ";

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
                 and mrkr_name like "__:%"
                 and mrkr_name not like "id:%"
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

#======================== Others ====================================
#locusAlleleHaveDupPub 
#
# if a source is to one allele, it should only be an attribution to the 
# allele not also to the locus in the database. But on the website, the 
# sources of all the alleles of the locus are all collected in the 
# publication section of the locus.

sub locusAlleleHaveDupPub ($) {
  
  my $routineName = "locusAlleleHaveDupPub";

  my $sql = '
             select f.zdb_id, l.zdb_id, r1.recattrib_source_zdb_id 
               from record_attribution r1, record_attribution r2, 
                    fish f, locus l 
              where f.locus = l.zdb_id 
                and f.zdb_id = r1.recattrib_data_zdb_id 
                and l.zdb_id = r2.recattrib_data_zdb_id 
                and r1.recattrib_source_zdb_id = r2.recattrib_source_zdb_id';

  my @colDesc = ("Allele ZDB ID       ",
                 "Locus  ZDB ID       ",
                 "Source ZDB ID       ");

  my $nRecords = execSql ($sql, undef, @colDesc);

  if ($nRecords > 0) {
     my $sendToAddress = $_[0];
     my $subject = "locus&allele attribute to the same source";
     my $errMsg = "$nRecords pairs of locus and allele have the same attribution.";

     logError ($errMsg);
     &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql);
  }
  &recordResult($routineName, $nRecords);
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
      from publication 
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
        "ZDB-PUB-010918-3"    { "ZDB-PUB-040216-6"  } )
        and source <> "ZFIN Direct Data Submission"
      group by title 
      having count(*) > 1 
     into temp dup_titles with no log;';
  
  # I tried running this through execSql, but if this query returned 0 rows
  # then the 2nd query would get a DBI error.
  my $sth = $dbh->prepare($sql1) or die "Prepare fails";
  $sth -> execute();

  my $sql2 = '
      select p.title, p.accession_no, p.zdb_id, p.authors, p.pub_date, p.source 
        from publication p, dup_titles d 
        where p.title = d.title 
        order by p.title, p.zdb_id;';
  
  my @colDesc =  ("Title            ",
		  "Accession Number ",
		  "Pub ZDB ID       ",
		  "Authors          ",
		  "Pub Date         ",
		  "Source           " );

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


#-------------------------------------------------------------
#Parameter
# $      Email Address for recipients
# 
sub externNoteAssociationWithData($) {
	
  my $routineName = "externNoteAssociationWithData";
	
  my $sql = '
             select extnote_zdb_id 
	       from external_note
	      where extnote_zdb_id not in (
		     	      select dextnote_extnote_zdb_id 
		        	from data_external_note ) 
                  ';
  my $nRecords = execSql ($sql);
	
  if ($nRecords > 0) {
  
    my $sqlDtl = '
                  select extnote_zdb_id,  
                         recattrib_source_zdb_id
                    from external_note,
                         record_attribution
                   where extnote_zdb_id not in (
		     	          select dextnote_extnote_zdb_id 
		        	    from data_external_note )
                     and extnote_zdb_id = recattrib_data_zdb_id
                ';
  
    my @colDesc =  ("Extnote ZDB ID         ",
                    "Recattrib source ZDB ID" );
 
    my $nRecordsDtl = execSql ($sqlDtl, undef, @colDesc);
  
    if ( $nRecords == $nRecordsDtl ) {
      
      my $sendToAddress = $_[0];
      my $subject = "external note inconsistenct";
      my $errMsg = "In external_note, $nRecords records do not have "
	."matching in data_external_note. ";
    
      logError ($errMsg);
      &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql, $sqlDtl);
    }else {
      print "Two queries are not consistent.\n";
    }
  }
  &recordResult($routineName, $nRecords);
} 


#-------------------------------------------------------
#Parameter
# $      Email Address for recipients
# 
sub extinctFishHaveNoSuppliers ($) {
	
  my $routineName = "extinctFishHaveNoSuppliers";
	
  my $sql = ' 
              select f.zdb_id, 
                     f.fish_extinct,
                     idsup_supplier_zdb_id
		from fish f, 
                     int_data_supplier
	       where f.fish_extinct = "t" 
                 and f.zdb_id in (
		      		select idsup_data_zdb_id 
                                  from int_data_supplier)
              ';

  my @colDesc = ("Fish ZDB ID          ",
		 "Fish extinct         ",
		 "Idsup supplier ZDB Id" );

  my $nRecords = execSql ($sql, undef, @colDesc);

  if ( $nRecords > 0 ) {
    my $sendToAddress = $_[0];
    my $subject = "Extinct fish has supplier";
    my $errMsg ="In fish table, $nRecords records have fish_extinct as"
                    ." true, but have records in int_data_supplier table.";
      		        
    logError ($errMsg);
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql); 
  }
  &recordResult($routineName, $nRecords);
} 


#---------------------------------------------------------
#Parameter
# $      Email Address for recipients
# 
sub putativeNonZfinGeneNotInZfin ($) {
 
  my $routineName = "putativeNonZfinGeneNotInZfin";
  
  my $sql = '
             select putgene_mrkr_zdb_id, 
                    putgene_putative_gene_name
               from putative_non_zfin_gene, 
                    marker  
              where putgene_putative_gene_name = mrkr_name
                and mrkr_type in (select mtgrpmem_mrkr_type
                                    from marker_type_group_member
                                   where mtgrpmem_mrkr_type_group="GENEDOM")
            ';
  
  my @colDesc = ("Putgene mrkr ZDB ID       ",
		 "Putgene putative gene name" );

  my $nRecords = execSql ($sql, undef, @colDesc);
  if ( $nRecords > 0 ) {
    my $sendToAddress = $_[0];
    my $subject = "Putative gene name in ZFIN";
    my $errMsg = "In putative_non_zfin_gene table, $nRecords records' "
    	              ."putative gene name is a gene name in ZFIN .";
      	
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
 
#---------------------------------------------------------------
# check for duplicate marker_goterm_Evidence, inference_groups.

# Parameter
#  $     Email Address for recipient
#
sub mrkrgoevInfgrpDuplicatesFound ($) {

  my $routineName = "mrkrgoevInfgrpDuplicatesFound";

  my $sql = 'select count(*),  
                    mrkrgoev_mrkr_zdb_id, 
                    mrkrgoev_go_term_zdb_id, 
                    mrkrgoev_source_zdb_id, 
                    mrkrgoev_evidence_code, 
                    infgrmem_inferred_from
               from marker_go_term_evidence, inference_group_member
               where not exists (select * 
                                   from go_evidence_flag
                                   where mrkrgoev_zdb_id = 
                                            goevflag_mrkrgoev_zdb_id)
               and mrkrgoev_zdb_id = infgrmem_mrkrgoev_zdb_id
               group by mrkrgoev_mrkr_zdb_id, 
                        mrkrgoev_go_term_zdb_id,
                        mrkrgoev_source_zdb_id,
                        mrkrgoev_evidence_code,
                        infgrmem_inferred_from
               having count(*) > 1 ';

  my @colDesc = ("count", 
		 "mrkrgoev_mrkr_zdb_id" ,
		 "mrkrgoev_go_Term_zdb_id",
		 "mrkrgoev_source_zdb_id",
		 "mrkrgoev_evidence_code",
		 "infgrmem_inferred_from");

  my $nRecords = execSql ($sql, undef, @colDesc);
  if ( $nRecords > 0 ) {
    my $sendToAddress = $_[0];
    my $subject = "Possible duplicate records in marker_go_term_evidence, inference_group_member";
    my $errMsg = "$nRecords are possible duplicates in marker_go_term_evidence, inference_group_member";
    logError($errMsg);
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql); 
  }
  &recordResult($routineName, $nRecords);
} 

#---------------------------------------------------------------
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
  my $sql = "select *
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

$usage = "Usage: validatetest.pl <dbname> [-d|-w|-m|-y|-o] ";

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
my $goEmail      = "<!--|GO_EMAIL_ERR|-->";

if($daily) {
  stageWindowConsistent ($adEmail);
  stageContainsStageWindowInContainerStageWindow($adEmail);

  anatomyItemStageWindowConsistent($adEmail);
  anatomyContainsStageWindowConsistent($adEmail);
  anatomyContainsStageWindowInContainerStageWindow($adEmail);

  expressionPatternStageWindowConsistent($xpatEmail);
  expressionPatternAnatomyStageWindowOverlapsAnatomyItem($xpatEmail);

  fishNameEqualLocusName($mutantEmail);
  fishAbbrevContainsFishAllele($mutantEmail);

  alterationHas1Fish($mutantEmail);
  mutantHas4TableBox($mutantEmail);

  locusNameUnique($mutantEmail);
  locusAlleleHaveDupPub($mutantEmail);

  linkageHasMembers($linkageEmail);
  linkagePairHas2Members($linkageEmail);

  foreigndbNotInFdbcontains($otherEmail);

  zdbObjectHomeTableColumnExist($dbaEmail);
  zdbObjectIsSourceDataCorrect($dbaEmail);
  zdbObjectHandledByGetObjName($dbaEmail);


  pubTitlesAreUnique($otherEmail);
  externNoteAssociationWithData($otherEmail);
  extinctFishHaveNoSuppliers($otherEmail);
  putativeNonZfinGeneNotInZfin($geneEmail);
  zdbReplacedDataIsReplaced($dbaEmail);

  mrkrgoevDuplicatesFound($dbaEmail);
  mrkrgoevInfgrpDuplicatesFound($dbaEmail);
  mrkrgoevGoevflagDuplicatesFound($dbaEmail);
  mrkrgoevObsoleteAnnotationsFound($dbaEmail);
  mrkrgoevSecondaryAnnotationsFound($dbaEmail);
}
if($orphan) {
  
  zdbActiveDataStillActive($dbaEmail);
  zdbActiveSourceStillActive($dbaEmail);
}
if($weekly) {
  # put these here until we get them down to 0 records.  Then move them to 
  # daily.
  estsHave1Gene($estEmail);
  prefixedGenesHave1Est($estEmail);
  estsWithoutClonesHaveXxGenes($estEmail);
  xxGenesHaveNoClones($estEmail);
  fishAbbrevStartsWithLocusAbbrev($mutantEmail);
}
if($monthly) {
  orthologueHasDblink($geneEmail);
  orthologueNomenclatureValid($geneEmail);
  locusAbbrevIsSet($mutantEmail);
  locusAbbrevUnique($mutantEmail);
  prefixedIbdGenesHave1Est($estEmail);
  genesWithCommonDblinks($geneEmail);
}
if($yearly) {
  print "run yearly check. \n\n";
}

	   

#rmdir($globalWorkingDir);
