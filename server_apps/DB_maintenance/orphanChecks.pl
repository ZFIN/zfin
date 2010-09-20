#! /private/bin/perl -w 

##
# orphanChecks.pl
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
     print MAIL "Subject: validatedata: $subject\n";

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

$usage = "Usage: orphanChecks.pl <dbname>  ";

$document = <<ENDDOC;

$usage

Command line parameters:

  dbname   Name of database to validate. This must be a ZFIN database.

ENDDOC

if (@ARGV < 1) {
  print $document and exit 1;
}

#
# Define GLOBALS
#	
			
$globalDbName = $ARGV[0]; #"<!--|DB_NAME|-->";
$globalUsername = "";
$globalPassword = "";

$globalResultFile = "/tmp/<!--|DB_NAME|-->"."orphancheckresult.txt";

#set environment variables
$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";

$dbh = DBI->connect("DBI:Informix:$globalDbName", 
			      $globalUsername, 
			      $globalPassword) 
       or die "Cannot connect to Informix database $globalDbName:$DBI::errstr\n";

my $dbaEmail     = "<!--|VALIDATION_EMAIL_DBA|-->";
  
  zdbActiveDataStillActive($dbaEmail);
  zdbActiveSourceStillActive($dbaEmail);
	   

