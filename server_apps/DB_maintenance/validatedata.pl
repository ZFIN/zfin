#! /local/bin/perl5

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
    print  "\nError: \n";
    foreach $line (@_) {
	print "$line";
    }
    print "\n";
    $globalErrorCount++;
    return ();
}

#------------------------------------------------------------------------
# Execute SQL statement(s) and save its results in files.
#  
# Params 
#   $     SQL statement to be executed.
#   $     Reference of subroutine that does future query if any,
#         or as 'undef'.
#   @     Column description for query result.
#  
# Returns 
#   number of rows returned from the query.

sub execSql {

  my $sql = shift;
  my $subSqlRef = shift;
  my @colDesc = @_;

  my $nRecords = 0;
 
  my $sth = $dbh->prepare($sql) or die "Prepare fails";
  
  $sth -> execute();
  
  open RESULT, ">$globalResultFile" or die "Cannot open the file to write check result."; 
  
  while (my @row = $sth ->fetchrow_array()) {

    my $valid = 1;
    $valid = $subSqlRef->(@row) if $subSqlRef;
    
    if ($valid) {
      my $i = 0;
      $nRecords ++;

      while ($col= shift @row) {

	print RESULT "$colDesc[$i++]\t $col\n";
      }
      print RESULT "\n";    
    }  
  } 
  
  return ($nRecords);
}

#------------------------------------------------------------------------    
# Send the check result 
#
# Params
#   $       Recipient addresses
#   $       Email subject
#   $       Error Message 
#   @       Queries for the check
#
# Returns () 

sub sendMail(@) {

     my $sendToAddress = shift;
     my $subject = shift;
     my $msg = shift;
     my @sql = @_; 

     open MAIL, "|/usr/lib/sendmail -t";

     print MAIL "To: $sendToAddress\n";
     print MAIL "Subject: $subject\n";

     print MAIL "$msg\n";
     
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
    my $errMsg = "In stage table, $nRecords records' stage start hours are "
                     ."greater than end hours. \n";
    logError ($errMsg); 
    &sendMail($sendToAddress, $subject, $errMsg, $sql); 
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
    my $errMsg = "In stage_contains, $nRecords records' container's stage window" 
                   ."doesn't fully contain contained 's.\n ";

    logError ($errMsg);
    &sendMail($sendToAddress, $subject, $errMsg, $sql); 
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
    my $errMsg = "In anatomy_item, $nRecords records have inconsistent" 
                   ."stage window.\n ";

    logError ($errMsg);
    &sendMail($sendToAddress, $subject, $errMsg, $sql); 
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
      my $errMsg = "In anatomy_contain, $nRecords records have inconsistent" 
                 ."stage window.\n";
                
      logError ($errMsg);
      sendMail($sendToAddress, $subject, $errMsg, $sql, $sqlDtl); 
    }else {
      print "Two queries are not consistent.\n";
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
    my $errMsg = "In anatomy_contains, $nRecords records have stage window" 
                 ."out of the range of container's stage window.\n ";

    logError ($errMsg);
    &sendMail($sendToAddress, $subject, $errMsg, $sql); 
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
      my $errMsg = "In fish_image_anatomy table, $nRecords records' stage"
    		     ." window overlap with anatomy items' stage window.\n";
      		     
      logError ($errMsg);
      &sendMail($sendToAddress, $subject, $errMsg, $sql, $sqlDtl); 
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
  	
  my $nRecords = execSql ($sql, undef, @colDesc);

  if ( $nRecords > 0 ) {

    my $sendToAddress = $_[0];
    my $subject = "Stage window inconsistence";
    my $errMsg = "In expression_pattern_stage, $nRecords records' stage"
    		      ." windoware not consistent. \n";
      		       
    logError ($errMsg);
    &sendMail($sendToAddress, $subject, $errMsg, $sql); 
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
      my $errMsg = "In expression_pattern_anatomy, $nRecords records' stage "
	           ."window don't overlap with the anatomy item's stage window.\n";
		      
      logError ($errMsg);	
      &sendMail($sendToAddress, $subject, $errMsg, $sql, $sqlDtl); 
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
      my $errMsg = "In expression_pattern_image, $nRecords records' stage "
    		   ."window don't overlap with the fish image's stage window.\n";
      		       
      logError ($errMsg);
      &sendMail($sendToAddress, $subject, $errMsg, $sql, $sqlDtl); 
    }else {
      print "Two queries are not consistent.\n";
    }
  }
  else {
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
                    lnkg_source_zdb_id 	
               from linkage
	      where lnkg_zdb_id not in (
	            	      select lnkgmem_linkage_zdb_id 
                                from linkage_member)';

  my @colDesc = ("Lnkg ZDB ID       ",
		 "Lnkg or lg        ",
		 "Lnkg source ZDB ID" );
  
  my $nRecords = execSql ($sql, undef, @colDesc);
	
  if ( $nRecords > 0 ) {
    my $sendToAddress = $_[0];
    my $subject = "linkage has no member(s)";
    my $errMsg = "In linkage table, $nRecords records have no members in linkage_member.\n ";
    
    logError ($errMsg);
    &sendMail($sendToAddress, $subject, $errMsg, $sql); 
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
  
  my $nRecords = execSql ($sql, undef, @colDes);

  if ( $nRecords > 0 ) {

    my $sendToAddress = $_[0];
    my $subject = "linkage pair has one or more than 2 members";
    my $errMsg = "In linkage_pair table, $nRecords records have less than or"
                   ." more than two members in linkage_pair_member. \n";
    logError ($errMsg); 
    &sendMail($sendToAddress, $subject, $errMsg, $sql); 
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
	
  logHeader("Checking each linked_recid in db_link exists in the orthologue," 
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
    my $errMsg = "In db_link, $nRecords items' linked_recid doesn't exist in "
                 . "either the orthologue table or the marker table.\n";
                
    logError ($errMsg);
    &sendMail($sendToAddress, $subject, $errMsg, $sql); 
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
    my $errMsg = "In orthologue table, $nRecords records have no corresponding"
    	                    ."gene record in db_link. \n";
    logError ($errMsg); 
    &sendMail($sendToAddress, $subject, $errMsg, $sql); 
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
    my $errMsg = "In zdb_object_type, $nRecords records either has no home  "
    	              ."table or has no home column\n";
      
    logError ($errMsg); 
    &sendMail($sendToAddress, $subject, $errMsg, $sql); 
  }else {
    print "Passed!\n";
  }
} 

#----------------------------------------------------------
#Parameter
# $      Email Address for recipients
#

sub zdbObjectIsSourceDataCorrect($) {

  logHeader("Checking zdb_object_type is consistent with zdb_active_data"
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
    my $errMsg = "In zdb_object_type, $nRecords records are not consistent  "
    	          ."with records in zdb_active_data and zdb_active_source\n";
      	
    logError ($errMsg); 
    &sendMail($sendToAddress, $subject, $errMsg, $sql); 
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
    my $errMsg = "In zdb_object_type, $nRecords records do not have properly"
                  ."defined get_obj_name function for it.\n";
    logError ($errMsg); 
    &sendMail($sendToAddress, $subject, $errMsg, $sql); 
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
      my $errMsg = "In external_note, $nRecords records do not have"
	."matching in data_external_note. \n";
    
      logError ($errMsg);
      &sendMail($sendToAddress, $subject, $errMsg, $sql, $sqlDtl); 
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
    my $errMsg ="In fish table, $nRecords records have fish_extinct as"
                    ." true, but have records in int_data_supplier table.\n";
      		        
    logError ($errMsg);
    &sendMail($sendToAddress, $subject, $errMsg, $sql); 
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
    my $errMsg = "In putative_non_zfin_gene table, $nRecords records' "
    	              ."putative gene name is a gene name in ZFIN . \n";
      	
    logError ($errMsg); 
    &sendMail($sendToAddress, $subject, $errMsg, $sql); 
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
    my $errMsg = "In zdb_replaced_data, $nRecords replaced zdb ids are "
    	                    ."still in zdb_active_data\n";
    logError ($errMsg); 
    &sendMail($sendToAddress, $subject, $errMsg, $sql); 
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
    my $sendToAddress = $_[0];
    my $subject = "orphan in zdb active data";
    my $errMsg = "In zdb_active_data, $nRecords ids are out of use.\n";

    logError ($errMsg); 
    &sendMail($sendToAddress, $subject, $errMsg, $sql); 
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
    my $sendToAddress = $_[0];
    my $subject = "orphan in zdb active source";
    my $errMsg = "In zdb_active_source, $nRecords ids are out of use.\n";
             
    logError ($errMsg); 
    &sendMail($sendToAddress, $subject, $errMsg, $sql); 
  }else {
    print "Passed!\n";
  } 
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

$globalErrorCount = 0;
$globalResultFile = "checkresult.txt";

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
my $curatorEmail = "<!--|VALIDATION_EMAIL_CURATOR|-->";
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

  linkageHasMembers($linkageEmail);
  linkagePairHas2Members($linkageEmail);

  dblinkRecidIsOrthoOrMarker($otherEmail);

  zdbObjectHomeTableColumnExist($dbaEmail);
  zdbObjectIsSourceDataCorrect($dbaEmail);
  zdbObjectHandledByGetObjName($dbaEmail);

  externNoteAssociationWithData($otherEmail);
  extinctFishHaveNoSuppliers($otherEmail);
  putativeNonZfinGeneNotInZfin($otherEmail);
  zdbReplacedDataIsReplaced($dbaEmail);
}
if($orphan) {
  zdbActiveSourceStillActive($dbaEmail);
  zdbActiveDataStillActive($dbaEmail);
}
if($weekly) {
  print "run weekly check. \n";
}
if($monthly) {
  print "run monthly check. \n";
  orthologueHasDblink($curatorEmail);
}
if($yearly) {
  print "run yearly check. \n";
}

	   

#rmdir($globalWorkingDir);
