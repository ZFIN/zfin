#! /local/bin/perl5 -w 

##
# validatedata.pl
#
# Check the consistence and correctness of data in zfin database
##

use Getopt::Long qw(:config bundling);
use DBI;

#------------------------------------------------------------------------
# Log Header.  Each error check has its own header.
#
# Params 
#  @       List of lines that make up the header.
#
# Returns ()

sub logHeader(@) {

    print "\n===============================================================\n";

    my $line;

    foreach $line (@_) {
	print "$line\n";
    }
    return ();
}

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
    print "\nError: \n";
    foreach $line (@_) {
	print "$line";
    }
    print "\n";
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
    print "\nWarning: \n";
    foreach $line (@_) {
	print "$line";
    }
    print "\n";
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

     print MAIL "$msg";

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

  my $sql = "select vldcheck_count
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

  logHeader("Checking stage window consistence in stage table");

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
    my $routineName = "stageWindowConsistent";
    my $errMsg = "In stage table, $nRecords records' stage start hours are "
                     ."greater than end hours. ";
    logError ($errMsg); 
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql);
    &recordResult($routineName, $nRecords);

  }else {
    print "Passed!\n";
  }
}
 
#-------------------------------------------------------
#Parameter
# $      Email Address for recipients

sub stageContainsStageWindowInContainerStageWindow ($) {

  logHeader ("Checking contained's stage window is in container's" );

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
    my $routineName = "stageContainsStageWindowInContainerStageWindow";
    my $errMsg = "In stage_contains, $nRecords records' container's stage window" 
                   ." doesn't fully contain contained 's. ";

    logError ($errMsg);
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql);
    &recordResult($routineName, $nRecords);

  }else {
    print "Passed!\n";
  }
}

#=================== Anatomy Item  =================================
#-----------------------------------------------------------
#Parameter
# $      Email Address for recipients
 
sub anatomyItemStageWindowConsistent ($) {
  
  logHeader("Checking stage window consistence of each anatomy_item");
  
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
    my $routineName = "anatomyItemStageWindowConsistent";
    my $errMsg = "In anatomy_item, $nRecords records have inconsistent " 
                   ."stage window. ";

    logError ($errMsg);
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql);
    &recordResult($routineName, $nRecords);

  }else {
    print "Passed!\n";
  }
}
  
#-------------------------------------------------------------- 
#Parameter
# $      Email Address for recipients

sub anatomyContainsStageWindowConsistent ($) {
  
  logHeader("Checking stage windows consistence in anatomy_contains");
 
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
      my $routineName = "anatomyContainsStageWindowConsistent";
      my $errMsg = "In anatomy_contain, $nRecords records have inconsistent " 
                 ."stage window.";
                
      logError ($errMsg);
      sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql, $sqlDtl);
      &recordResult($routineName, $nRecords);

    }else {
      print "Two queries are not consistent.";
    }
  }

 else {
    print "Passed!\n";
  }
}

#------------------------------------------------------------
#Parameter
# $      Email Address for recipients
 

sub anatomyContainsStageWindowInContainerStageWindow($){

  logHeader("Checking stage window within container's stage window.");

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
    my $routineName = "anatomyContainsStageWindowInContainerStageWindow";
    my $errMsg = "In anatomy_contains, $nRecords records have stage window " 
                 ."out of the range of container's stage window. ";

    logError ($errMsg);
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql); 
    &recordResult($routineName, $nRecords);
 
  }else {
    print "Passed!\n";
  }
}

 	    	          	 
#-----------------------------------------------------------------
#Parameter
# $      Email Address for recipients
 

sub fishImageAnatomyStageWindowOverlapsAnatomyItem ($) {
	
  logHeader ("Checking fish image anatomy stage window overlaps with anatomy item's");
	
  my $sql = '
             select fimganat_anat_item_zdb_id, 
                    fimganat_fimg_start_stg_zdb_id,
		    fimganat_fimg_end_stg_zdb_id
	       from fish_image_anatomy 
              where anatitem_overlaps_stg_window (
                                  fimganat_anat_item_zdb_id,
                                  fimganat_fimg_start_stg_zdb_id,
                                  fimganat_fimg_end_stg_zdb_id   
                                  ) = "f" ';
  my $nRecords = execSql ($sql);

  if ( $nRecords > 0 ) {
   
    my $sqlDtl='
                select fimganat_anat_item_zdb_id, 
                       anatitem_name,
                       anatitem_start_stg_zdb_id, 
                       s1.stg_name_long, 
                       anatitem_end_stg_zdb_id,
                       s2.stg_name_long  
                       fimganat_fimg_start_stg_zdb_id,
                       s3.stg_name_long, 
		       fimganat_fimg_end_stg_zdb_id,
                       s4.stg_name_long

	          from fish_image_anatomy,
                       anatomy_item,
                       stage s1, stage s2, stage s3, stage s4
 
                 where anatitem_overlaps_stg_window (
                                  fimganat_anat_item_zdb_id,
                                  fimganat_fimg_start_stg_zdb_id,
                                  fimganat_fimg_end_stg_zdb_id   
                                  ) = "f" 
                   and fimganat_anat_item_zdb_id = anatitem_zdb_id
                   and anatitem_start_stg_zdb_id = s1.stg_zdb_id
                   and anatitem_end_stg_zdb_id = s2.stg_zdb_id
                   and fimganat_fimg_start_stg_zdb_id = s3.stg_zdb_id
                   and fimganat_fimg_end_stg_zdb_id = s4.stg_zdb_id
                 ';
    
    my @colDesc = ("Fimganat anat item ZDB ID   ",
		   "Anatitem name               ",
		   "Anatitem start stg ZDB ID   ",
		   "Start stg name & period     ",
		   "Anatitem end stg ZDB ID     ",
		   "End stg name & period       ",
		   "Fimganat fimg start stg ZDB ID",
		   "Start stg name & period     ",
		   "Fimganat fimg end stg ZDB ID",
		   "End stg name & period       " );

    my $nRecordsDtl = execSql($sqlDtl, undef, @colDesc);
 
    if ( $nRecords == $nRecordsDtl ) {
     
      my $sendToAddress =$_[0];
      my $subject = "Stage window not overlap";
      my $routineName = "fishImageAnatomyStageWindowOverlapsAnatomyItem";
      my $errMsg = "In fish_image_anatomy table, $nRecords records' stage"
    		     ." window overlap with anatomy items' stage window.";
      		     
      logError ($errMsg);
      &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql, $sqlDtl); 
      &recordResult($routineName, $nRecords);

    }else {
      print "Two queries are not consistent.\n";
    }
  }
  else {
    print "Passed!\n";
  }
}

#=================== Expression Pattern ==========================              
#----------------------------------------------------------------
#Parameter
# $      Email Address for recipients
 

sub expressionPatternStageWindowConsistent($) {
	
  logHeader ("Checking stage window consistence in expression_pattern_stage");
	
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
    my $routineName = "expressionPatternStageWindowConsistent";
    my $errMsg = "In expression_pattern_stage, $nRecords records' stage"
    		      ." windoware not consistent. ";
      		       
    logError ($errMsg);
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql);
    &recordResult($routineName, $nRecords);
 
  }else {
    print "Passed!\n";
  }
} 

#---------------------------------------------------------------
#Parameter
# $      Email Address for recipients
# 

sub expressionPatternAnatomyStageWindowOverlapsAnatomyItem ($) {
	
  logHeader("Checking expression_pattern_anatomy's stage window"
		 ." overlaps with the anatomy item's stage window. ");
	
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
      my $routineName = "expressionPatternAnatomyStageWindowOverlapsAnatomyItem";
      my $errMsg = "In expression_pattern_anatomy, $nRecords records' stage "
	           ."window don't overlap with the anatomy item's stage window.";
		      
      logError ($errMsg);	
      &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql, $sqlDtl);
      &recordResult($routineName, $nRecords);
 
    }else {
      print "Two queries are not consistent.\n";
    }
  }
  else {
    print "Passed!\n";
  }
}

#-----------------------------------------------------------
#Parameter
# $      Email Address for recipients
# 

sub expressionPatternImageStageWindowOverlapsFishImage ($) {
	
  logHeader("Checking xpat image's stage window overlaps with that for fish image.");
	
  my $sql = '
             select xpatfimg_fimg_zdb_id, 
                    xpatfimg_xpat_start_stg_zdb_id,
		    xpatfimg_xpat_end_stg_zdb_id
	      from  expression_pattern_image 
              where fimg_overlaps_stg_window (
                                 xpatfimg_fimg_zdb_id,  
                                 xpatfimg_xpat_start_stg_zdb_id, 
                                 xpatfimg_xpat_end_stg_zdb_id
                                 ) = "f" ';
  my $nRecords = execSql ($sql); 
  
  if ( $nRecords > 0 ) {
		    			  
    my $sqlDtl='
                select xpatfimg_fimg_zdb_id,
                       fimgstg_start_stg_zdb_id,
                       s1.stg_name_long,
                       fimgstg_end_stg_zdb_id,
                       s2.stg_name_long, 
                       xpatfimg_xpat_start_stg_zdb_id,
                       s3.stg_name_long,    
                       xpatfimg_xpat_end_stg_zdb_id,
                       s4.stg_name_long
                  from expression_pattern_image,
                       fish_image_stage,
                       stage s1, stage s2, stage s3, stage s4
                 where fimg_overlaps_stg_window (
                                 xpatfimg_fimg_zdb_id,  
                                 xpatfimg_xpat_start_stg_zdb_id, 
                                 xpatfimg_xpat_end_stg_zdb_id
                                 ) = "f" 
                   and xpatfimg_fimg_zdb_id = fimgstg_fimg_zdb_id
                   and fimgstg_start_stg_zdb_id = s1.stg_zdb_id
                   and fimgstg_end_stg_zdb_id = s2.stg_zdb_id
                   and xpatfimg_xpat_start_stg_zdb_id = s3.stg_zdb_id
                   and xpatfimg_xpat_end_stg_zdb_id = s4.stg_zdb_id
                  ';
    
    my @colDesc = ("Xpatfimg fimg ZDB ID           ",
		   "Fimgstg start stg ZDB ID       ",
		   "Start stg name & period        ",
		   "Fimgstg end stg ZDB ID         ",
		   "End stg name & period          ",
		   "Xpatfimag xpat start stg ZDB ID",
		   "Start stg name & period        ",
		   "Xpatfimag xpat end stg ZDB ID  ",
		   "End stg name & period          " );

    my $nRecordsDtl = execSql ($sqlDtl, undef, @colDesc);

    if ( $nRecords == $nRecordsDtl ) {
     
      my $sendToAddress = $_[0];
      my $subject = "Stage window not overlap";
      my $routineName = "expressionPatternImageStageWindowOverlapsFishImage";
      my $errMsg = "In expression_pattern_image, $nRecords records' stage "
    		   ."window don't overlap with the fish image's stage window.";
      		       
      logError ($errMsg);
      &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql, $sqlDtl);
      &recordResult($routineName, $nRecords); 
    }else {
      print "Two queries are not consistent.\n";
    }
  }
  else {
    print "Passed!\n";
  }
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
# We can't enforce this with a foreign key because (as of 2003/01)
# locus.name is not unique (more grumbling).
# We can't enforce this with a check constraint beacuse the constraint
# crosses 2 tables.
# 
#Parameter
# $      Email Address for recipients
# 
sub fishNameEqualLocusName ($) {

  logHeader ("Checking fish.name = locus.name");
	
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
    my $routineName = "fishNameEqualLocusName";
    my $errMsg = "The name field in $nRecords fish record(s) does not equal locus name. ";
    
    logError ($errMsg);
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql);
    &recordResult($routineName, $nRecords); 
  }else {
    print "Passed!\n";
  }
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

  logHeader ("Checking fish.abbrev contains fish.allele");
	
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
    my $routineName = "fishAbbrevContainsFishAllele";
    my $errMsg = "The abbrev field in $nRecords fish record(s) does not contain fish allele. ";
    
    logError ($errMsg);
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql); 
    &recordResult($routineName, $nRecords);
  }else {
    print "Passed!\n";
  }
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

  logHeader ("Checking fish.abbrev starts with locus.abbbrev");

  my $sql = 'select fish.abbrev, locus.abbrev,
                    fish.zdb_id, get_fish_full_name(fish.zdb_id), fish.name, 
                    fish.allele, locus.zdb_id, locus_name
	       from fish, locus
	       where fish.locus = locus.zdb_id
		 and line_type = "mutant"
		 and fish.abbrev not like (locus.abbrev || "%")
		 and locus.abbrev <> ""
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
    my $routineName = "fishAbbrevStartsWithLocusAbbrev";
    my $errMsg = "The abbrev field in $nRecords fish record(s) does not start with locus abbrev. ";

    logError ($errMsg);
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql); 
    &recordResult($routineName, $nRecords); 
  }else {
    print "Passed!\n";
  }
} 



#======= FISH - ALLELE - INT_FISH_CHROMO - CHROMOSOME - ALLELE - FISH ======
#
# Before recorded history there was an unsuccessful attempt to support 
# double mutants and ZFIN.  While the attempt failed, it failed only
# after the tables had been modified in certain ways.  The tables still
# only support single mutants, however due to the way they are designed
# they can easilyt support single mutants incorrectly.  These tests
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

  logHeader ("Checking each alteration has 1 fish");

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
    my $routineName = "alterationHas1Fish";
    my $errMsg = "$nRecords alterations had 0 or more than 1 associated fish. ";

    logError ($errMsg);
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql); 
    &recordResult($routineName, $nRecords);
  }else {
    print "Passed!\n";
  }
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

  logHeader ("Checking each mutant has 1 record in all 4 mutant tables");

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
    my $routineName = "mutantHas4TableBox";
    my $errMsg = "$nRecords mutant(s) had 0 or more than 1 records in associated tables. ";

    logError ($errMsg);
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql); 
    &recordResult($routineName, $nRecords); 
  }else {
    print "Passed!\n";
  }
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

  logHeader ("Checking locus abbrev is unique");
	
  my $sql = 'select abbrev, locus_name, zdb_id
               from locus loc1
	       where (   abbrev <> "xxx"
		      or abbrev is NULL)
		 and exists
		     ( select count(*), abbrev
			 from locus loc2
			 where loc1.abbrev = loc2.abbrev
			   and locus_name not like "Df%"
			   and locus_name not like "T%"
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
    my $routineName = "locusAbbrevUnique";
    my $errMsg = "$nRecords locus records have non-unique abbrevs. ";

    logWarning ($errMsg);
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql); 
    &recordResult($routineName, $nRecords); 
  }else {
    print "Passed!\n";
  }
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

  logHeader ("Checking locus name is unique");
	
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
    my $routineName = "locusNameUnique";
    my $errMsg = "$nRecords locus records have non-unique names. ";

    logError ($errMsg);
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql); 
    &recordResult($routineName, $nRecords);
  }else {
    print "Passed!\n";
  }
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

  logHeader ("Checking locus abbrev is set");
	
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
    my $routineName = "locusAbbrevIsSet";
    my $errMsg = "Locus abbrev not set in $nRecords locus record(s). ";

    logError ($errMsg);
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql); 
    &recordResult($routineName, $nRecords);
  }else {
    print "Passed!\n";
  }
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

  logHeader ("Checking each EST has 1 gene");

  my $sql = 'select mrkr_zdb_id, mrkr_name, mrkr_abbrev
               from marker m2
               where mrkr_type = "EST"
                 and 1 <> 
                     ( select count(*) 
                         from marker m1, marker_relationship
                         where mrel_mrkr_1_zdb_id = m1.mrkr_zdb_id
                           and mrel_mrkr_2_zdb_id = m2.mrkr_zdb_id
                           and m1.mrkr_type = "GENE" )
               order by mrkr_name';

  my @colDesc = ("EST ZDB ID        ",
		 "EST Name          ",
		 "EST Abbrev        ");
  
  my $nRecords = execSql ($sql, undef, @colDesc);
	
  if ( $nRecords > 0 ) {
    my $sendToAddress = $_[0];
    my $subject = "ESTs have 0 or > 1 associated genes";
    my $routineName = "estsHave1Gene";
    my $errMsg = "$nRecords ESTs had 0 or more than 1 associated genes. ";

    logError ($errMsg);
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql);   
    &recordResult($routineName, $nRecords);
  }else {
    print "Passed!\n";
  }
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

  logHeader ("Checking prefixed genes have 1 EST");

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
    my $routineName = "prefixedGenesHave1Est";
    my $errMsg = "$nRecords prefixed genes had 0 or more than 1 associated ESTs. ";

    logWarning ($errMsg);
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql);  
    &recordResult($routineName, $nRecords); 
  }else {
    print "Passed!\n";
  }
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

  logHeader ("Reporting prefixed id: genes that do not have an EST");

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
    my $routineName = "prefixedIbdGenesHave1Est";
    my $errMsg = "$nRecords 'id:' genes had 0 or more than 1 associated ESTs. ";

    logError ($errMsg);
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql);  
    &recordResult($routineName, $nRecords);
  }else {
    print "Passed!\n";
  }
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

  logHeader ("Checking ESTs without clones have an XX gene (excluding 6)");

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
    my $routineName = "estsWithoutClonesHaveXxGenes";
    my $errMsg = "$nRecords ESTs without clone records do not have corresponding xx: genes. ";

    logError ($errMsg);
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql); 
    &recordResult($routineName, $nRecords);
  }else {
    print "Passed!\n";
  }
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

  logHeader ("Checking xx: genes have no clones");

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
    my $routineName = "xxGenesHaveNoClones";
    my $errMsg = "$nRecords xx: genes had a corresponding clone record\n";

    logError ($errMsg);
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql); 
    &recordResult($routineName, $nRecords); 
  }else {
    print "Passed!\n";
  }
} 


#============================ Linkage ===================================
#---------------------------------------------------------------
#Parameter
# $      Email Address for recipients
# 
sub linkageHasMembers ($) {

  logHeader ("Checking linkage has member(s) in linkage_member");
	
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
    my $routineName = "linkageHasMembers";
    my $errMsg = "In linkage table, $nRecords records have no members in linkage_member. ";
    
    logError ($errMsg);
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql); 
    &recordResult($routineName, $nRecords); 
  }else {
    print "Passed!\n";
  }
} 

#---------------------------------------------------------------
#Parameter
# $      Email Address for recipients
# 
sub linkagePairHas2Members ($) {
	
  logHeader ("Checking records in linkage_pair have 2 members in linkage_pair_member");
	
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
    my $routineName = "linkagePairHas2Members";
    my $errMsg = "In linkage_pair table, $nRecords records have less than or"
                   ." more than two members in linkage_pair_member. ";
    logError ($errMsg); 
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql);  
    &recordResult($routineName, $nRecords); 
  }else {
    print "Passed!\n";
  }
} 

#=========================== Dblink =====================================   	
#--------------------------------------------------------------
#Parameter
# $      Email Address for recipients
# 
sub dblinkRecidIsOrthoOrMarker ($) {
	
  logHeader("Checking each linked_recid in db_link exists in the orthologue, " 
             . "or the marker table.");

  my $sql = ' 
              select linked_recid,
                     db_name, 
                     acc_num
   		from db_link
   	       where linked_recid not in 
   			 (select zdb_id from orthologue)
        	 and linked_recid not in 	
                         (select mrkr_zdb_id from marker) ';

  my @colDesc = ("Linked recid",
		 "Db name     ",
		 "Acc num     " );

  my $nRecords = execSql ($sql, undef, @colDesc);
  
  if ( $nRecords > 0 ) {
    my $sendToAddress = $_[0];
    my $subject = "Linked_recid in neither orthologue nor marker";
    my $routineName = "dblinkRecidIsOrthoOrMarker";
    my $errMsg = "In db_link, $nRecords items' linked_recid doesn't exist in "
                 . "either the orthologue table or the marker table.";
                
    logWarning ($errMsg);
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql);
    &recordResult($routineName, $nRecords);
  }else {
    print "Passed!\n";
  }
}

#-------------------------------------------------------------
#Parameter
# $      Email Address for recipients
# 
sub orthologueHasDblink ($) {

  logHeader ("Checking orthologue records have corresponding db_link record");

  my $sql = '
             select zdb_id,
                    c_gene_id,
                    ortho_name,
                    entry_time
               from orthologue
              where c_gene_id not in (
                                select linked_recid 
                                  from db_link) 
             ';
 
  my @colDesc = ("Ortho ZDB ID",
		 "C gene ID   ",
		 "Ortho name  ",
		 "Entry time  " );

  my $nRecords = execSql ($sql, undef, @colDesc);

  if ( $nRecords > 0 ) {
    my $sendToAddress = $_[0];
    my $subject = "Orthologues not match any db link";
    my $routineName = "orthologueHasDblink";
    my $errMsg = "In orthologue table, $nRecords records have no corresponding "
    	                    ."gene record in db_link. ";
    logError ($errMsg); 
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql);
    &recordResult($routineName, $nRecords);
  }else {
    print "Passed!\n";
  }
} 
   			
#======================= ZDB Object Type ================================
#---------------------------------------------------------
#Parameter
# $      Email Address for recipients
#
sub zdbObjectHomeTableColumnExist ($) {

  logHeader("Checking home table and home column exist for zdb object type");

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
    my $routineName = "zdbObjectHomeTableColumnExist";
    my $errMsg = "In zdb_object_type, $nRecords records either has no home "
    	              ."table or has no home column\n";
      
    logError ($errMsg); 
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql); 
    &recordResult($routineName, $nRecords);
  }else {
    print "Passed!\n";
  }
} 

#----------------------------------------------------------
#Parameter
# $      Email Address for recipients
#

sub zdbObjectIsSourceDataCorrect($) {

  logHeader("Checking zdb_object_type is consistent with zdb_active_data "
            ."and zdb_active_source");

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
    my $routineName = "zdbObjectIsSourceDataCorrect";
    my $errMsg = "In zdb_object_type, $nRecords records are not consistent "
    	          ."with records in zdb_active_data and zdb_active_source\n";
      	
    logError ($errMsg); 
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql); 
    &recordResult($routineName, $nRecords);
  }else {
    print "Passed!\n";
  }
}

#------------------------------------------------------------
#Parameter
# $      Email Address for recipients
# 
sub zdbObjectHandledByGetObjName ($) {

  logHeader("Checking each object type has a name");

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
    my $routineName = "zdbObjectHandledByGetObjName";
    my $errMsg = "In zdb_object_type, $nRecords records are not properly "
                  ."handled by the get_obj_name function.";
    logError ($errMsg); 
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql); 
    &recordResult($routineName, $nRecords);
  }else {
    print "Passed!\n";
  }
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
  
  logHeader ("Reporting locus and its allele attributed to the same source");

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
     my $routineName = "locusAlleleHaveDupPub";
     my $errMsg = "$nRecords pairs of locus and allele have the same attribution.";

     logError ($errMsg);
     &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql);
     &recordResult($routineName, $nRecords);
   }else {
     print "Passed!\n";
   }
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
	
  logHeader ("Checking for duplicate publication titles (ignoring known valid dups)");
	
  # Only need to exclude 1 record per pair.  That means if a duplicate
  # becomes a triplicate, we will detect it.

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
	"ZDB-PUB-961014-1233",{ "ZDB-PUB-961014-1234", }
	"ZDB-PUB-961014-106", { "ZDB-PUB-961014-107", }
	"ZDB-PUB-010417-9",   { "ZDB-PUB-990414-35", }
	"ZDB-PUB-010711-2",   { "ZDB-PUB-010814-8", }
	"ZDB-PUB-000824-10",  { "ZDB-PUB-990824-40", }
	"ZDB-PUB-010912-1",   { "ZDB-PUB-021017-13" }
        "ZDB-PUB-980420-9"    { "ZDB-PUB-030425-13" }
        "ZDB-PUB-010718-13"   { "ZDB-PUB-020913-1"  }      

)
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
    my $routineName = "pubTitlesAreUnique";
    my $errMsg = "$nRecords publications have duplicate titles\n";
    logError ($errMsg);
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql1, $sql2);  
    &recordResult($routineName, $nRecords);
  }
  else {
    print "Passed!\n";
  }
} 


#-------------------------------------------------------------
#Parameter
# $      Email Address for recipients
# 
sub externNoteAssociationWithData($) {
	
  logHeader ("Checking each record in external_note has matching in data_external_note");
	
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
      my $routineName = "externNoteAssociationWithData";
      my $errMsg = "In external_note, $nRecords records do not have "
	."matching in data_external_note. ";
    
      logError ($errMsg);
      &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql, $sqlDtl);
      &recordResult($routineName, $nRecords);
    }else {
      print "Two queries are not consistent.\n";
    }
  }
  else {
    print "Passed!\n";
  }
} 


#-------------------------------------------------------
#Parameter
# $      Email Address for recipients
# 
sub extinctFishHaveNoSuppliers ($) {
	
  logHeader ("Checking extinct fish does not have supplier");
	
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
    my $routineName = "extinctFishHaveNoSuppliers";
    my $errMsg ="In fish table, $nRecords records have fish_extinct as"
                    ." true, but have records in int_data_supplier table.";
      		        
    logError ($errMsg);
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql); 
    &recordResult($routineName, $nRecords);
  }else {
    print "Passed!\n";
  }
} 


#---------------------------------------------------------
#Parameter
# $      Email Address for recipients
# 
sub putativeNonZfinGeneNotInZfin ($) {
 
  logHeader ("Checking putative gene name not exist in ZFIN as a gene name");
  
  my $sql = '
             select putgene_mrkr_zdb_id, 
                    putgene_putative_gene_name
               from putative_non_zfin_gene, 
                    marker  
              where putgene_putative_gene_name = mrkr_name
                and mrkr_type = "GENE"
            ';
  
  my @colDesc = ("Putgene mrkr ZDB ID       ",
		 "Putgene putative gene name" );

  my $nRecords = execSql ($sql, undef, @colDesc);
  if ( $nRecords > 0 ) {
    my $sendToAddress = $_[0];
    my $subject = "Putative gene name in ZFIN";
    my $routineName = "putativeNonZfinGeneNotInZfin";
    my $errMsg = "In putative_non_zfin_gene table, $nRecords records' "
    	              ."putative gene name is a gene name in ZFIN .";
      	
    logError ($errMsg); 
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql); 
    &recordResult($routineName, $nRecords);
  }else {
    print "Passed!\n";
  }
} 

#---------------------------------------------------------------
# Each entry in foreign_db should have 1 or more entryies in
# foreign_db_contains.  foreign_db_contains describes what type(s) of
# data are available in the foreign DB.  If a foreign DB does not have
# 1 or more entries in foreign_db_contains then several joins in the
# web pages will fail, and the foreign DB will not show up.
#
# Parameter
#  $     Email Address for recipient
#
sub foreigndbNotInFdbcontains ($) {

  logHeader ("Checking each foreign db record has at least one record in foreign_db_contains");

  my $sql = " select db_name
                from foreign_db
               where db_name not in (
                        select fdbcont_db_name from foreign_db_contains) ";
  my @colDesc = ("Db Name    " );
  my $nRecords = execSql ($sql, undef, @colDesc);
  if ( $nRecords > 0 ) {
    my $sendToAddress = $_[0];
    my $subject = "Foreign db name not in fdbcontains";
    my $routineName = "foreigndbNotInFdbcontains";
    my $errMsg = "$nRecords foreign db records have no entry in foreign_db_contains.";
    logError($errMsg);
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql); 
    &recordResult($routineName, $nRecords);
  }else {
    print "Passed!\n";
  }
} 


#---------------------------------------------------------
#Parameter
# $      Email Address for recipients
# 

sub zdbReplacedDataIsReplaced ($) {

  logHeader("Checking the replaced zdb id is no longer in zdb_active_data");

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
    my $routineName = "zdbReplacedDataIsReplaced";
    my $errMsg = "In zdb_replaced_data, $nRecords replaced zdb ids are "
    	                    ."still in zdb_active_data\n";
    logError ($errMsg); 
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql);
    &recordResult($routineName, $nRecords);
  }else {
    print "Passed!\n";
  }
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
  
  logHeader ("Checking zdb_id in zdb_active_data still in use");
  
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
    my $routineName = "zdbActiveDataStillActive";
    my $errMsg = "In zdb_active_data, $nRecords ids are out of use, and stored in zdb_orphan_data table.";

    logWarning ($errMsg); 
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql); 
    &recordResult($routineName, $nRecords);
  }else {
    print "Passed!\n";
  } 
}           
#---------------------------------------------------------
#Parameter
# $      Email Address for recipients
#
sub zdbActiveSourceStillActive($) {
  
  logHeader ("Checking zdb_id in zdb_active_source still in use");
  
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
    my $routineName = "zdbActiveSourceStillActive";
    my $errMsg = "In zdb_active_source, $nRecords ids are out of use, and stored in zdb_orphan_source table.";
             
    logWarning ($errMsg); 
    &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql);
    &recordResult($routineName, $nRecords);
  }else {
    print "Passed!\n";
  } 
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
#  if results are as expected
#    print simple 'test passed' message
#  else
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

if($daily) {
  print "run daily check. \n";

  stageWindowConsistent ($adEmail);
  stageContainsStageWindowInContainerStageWindow($adEmail);

  anatomyItemStageWindowConsistent($adEmail);
  anatomyContainsStageWindowConsistent($adEmail);
  anatomyContainsStageWindowInContainerStageWindow($adEmail);
  fishImageAnatomyStageWindowOverlapsAnatomyItem ($adEmail);

  expressionPatternStageWindowConsistent($xpatEmail);
  expressionPatternAnatomyStageWindowOverlapsAnatomyItem($xpatEmail);
  expressionPatternImageStageWindowOverlapsFishImage($xpatEmail);

  fishNameEqualLocusName($mutantEmail);
  fishAbbrevContainsFishAllele($mutantEmail);
  fishAbbrevStartsWithLocusAbbrev($mutantEmail);

  alterationHas1Fish($mutantEmail);
  mutantHas4TableBox($mutantEmail);

  locusAbbrevUnique($mutantEmail);
  locusNameUnique($mutantEmail);
  locusAlleleHaveDupPub($mutantEmail);

  linkageHasMembers($linkageEmail);
  linkagePairHas2Members($linkageEmail);

  dblinkRecidIsOrthoOrMarker($geneEmail);
  foreigndbNotInFdbcontains($otherEmail);

  zdbObjectHomeTableColumnExist($dbaEmail);
  zdbObjectIsSourceDataCorrect($dbaEmail);
  zdbObjectHandledByGetObjName($dbaEmail);


  pubTitlesAreUnique($otherEmail);
  externNoteAssociationWithData($otherEmail);
  extinctFishHaveNoSuppliers($otherEmail);
  putativeNonZfinGeneNotInZfin($geneEmail);
  zdbReplacedDataIsReplaced($dbaEmail);
}
if($orphan) {
  
  zdbActiveDataStillActive($dbaEmail);
  zdbActiveSourceStillActive($dbaEmail);
}
if($weekly) {
  print "run weekly check. \n";

  # put these here until we get them down to 0 records.  Then move them to 
  # daily.
  estsHave1Gene($estEmail);
  prefixedGenesHave1Est($estEmail);
  estsWithoutClonesHaveXxGenes($estEmail);
  xxGenesHaveNoClones($estEmail);
}
if($monthly) {
  print "run monthly check. \n";
  orthologueHasDblink($geneEmail);
  locusAbbrevIsSet($mutantEmail);
  prefixedIbdGenesHave1Est($estEmail);
}
if($yearly) {
  print "run yearly check. \n";
  foreigndbNotInFdbcontains($otherEmail);
}

	   

#rmdir($globalWorkingDir);
