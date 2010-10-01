#! /private/bin/perl -w 

##
# updateunFeatures.pl
#
# update unspecified and unrecovered feature names and abbrevs to match gene abbrevs.
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
     print MAIL "Subject: updateunFeatures: $subject\n";

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








#========================  Features  ================================
#
#---------------------------------------------------------------
# unFeatureNameAbbrevUpdate
#
# features with names like unspecified_* related to genes, must be kept up to 
# date.  feature_names/abbrevs should equal current gene abbrev plus unspecified_* prefix
#
# Parameter
# $ Email Address for recipients

sub unFeatureNameAbbrevUpdate($) {
  my $routineName = "unFeatureNameAbbrevUpdate";
  
 my $sql = "select feature_name, mrkr_abbrev
               from feature, marker, feature_marker_relationship
               where feature_zdb_id = fmrel_ftr_zdb_id
               and mrkr_zdb_id = fmrel_mrkr_zdb_id
               and feature_name like 'unspecified\_%'
               and feature_type = 'UNSPECIFIED'
               and feature_name != 'unspecified\_'||mrkr_abbrev;";

  my @colDesc = ("Feature name         ",
		 "Feature mrkr_abbrev      ");

  my $nRecords = execSql ($sql, undef, @colDesc);
  
  my $sth = $dbh->do("update feature
                        set feature_name = (select 'un\_'||mrkr_abbrev
                                                  from marker, 
		       	    feature_marker_relationship
                       where mrkr_zdb_id = fmrel_mrkr_zdb_id
                       and feature_zdb_id = fmrel_ftr_zdb_id
                       and feature_name like 'unspecified\_%')
                 where feature_name like 'unspecified\_%' 
               and feature_type = 'UNSPECIFIED'
                 and not exists (Select 'x'
     	 		           from feature_marker_relationship,
			                marker
			           where mrkr_Zdb_id = fmrel_mrkr_Zdb_id
			           and feature_zdb_id = fmrel_ftr_zdb_id
			           and feature_name = 'unspecified\_'||mrkr_abbrev);" );

  $sth = $dbh->do("update feature
                set feature_abbrev = 
		    (select 'unspecified\_'||mrkr_abbrev
                       from marker, 
		       	    feature_marker_relationship
                       where mrkr_zdb_id = fmrel_mrkr_zdb_id
                       and feature_zdb_id = fmrel_ftr_zdb_id
                       and feature_abbrev like 'unspecified\_%')
                 where feature_name like 'unspecified\_%' 
               and feature_type = 'UNSPECIFIED'
                 and not exists (Select 'x'
     	 		           from feature_marker_relationship,
			                marker
			           where mrkr_Zdb_id = fmrel_mrkr_Zdb_id
			           and feature_zdb_id = fmrel_ftr_zdb_id
			           and feature_abbrev = 'unspecified\_'||mrkr_abbrev);" );
  
 if ( $nRecords > 0 ) {
  my $sendToAddress = $_[0];
  my $subject = "unAlleles have been updates";
  my $errMsg = "There are $nRecords unAllele record(s) whose names have been updated to reflect gene name changes. ";
    
  logError ($errMsg);
  &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql);
  &recordResult($routineName, $nRecords);
 }
}
#---------------------------------------------------------------
# unrecoveredFeatureNameAbbrevUpdate
#
# features with names like unrec_* related to genes, must be kept up to 
# date.  feature_names/abbrevs should equal current gene abbrev plus unrec_* prefix
#
# Parameter
# $ Email Address for recipients

sub unrecoveredFeatureNameAbbrevUpdate($) {
  my $routineName = "unrecoveredFeatureNameAbbrevUpdate";
  
 my $sql = "select feature_name, mrkr_abbrev
               from feature, marker, feature_marker_relationship
               where feature_zdb_id = fmrel_ftr_zdb_id
               and mrkr_zdb_id = fmrel_mrkr_zdb_id
               and feature_name like 'unrec\_%'
               and feature_name != 'unrec\_'||mrkr_abbrev;";

  my @colDesc = ("Feature name         ",
		 "Feature mrkr_abbrev      ");

  my $nRecords = execSql ($sql, undef, @colDesc);
  
  my $sth = $dbh->do("update feature
                        set feature_name = (select 'unrec\_'||mrkr_abbrev
                                                  from marker, 
		       	    feature_marker_relationship
                       where mrkr_zdb_id = fmrel_mrkr_zdb_id
                       and feature_zdb_id = fmrel_ftr_zdb_id
                       and feature_name like 'unrec\_%')
                 where feature_name like 'unrec\_%' 
                 and not exists (Select 'x'
     	 		           from feature_marker_relationship,
			                marker
			           where mrkr_Zdb_id = fmrel_mrkr_Zdb_id
			           and feature_zdb_id = fmrel_ftr_zdb_id
			           and feature_name = 'unrec\_'||mrkr_abbrev);" );

  $sth = $dbh->do("update feature
                set feature_abbrev = 
		    (select 'unrec\_'||mrkr_abbrev
                       from marker, 
		       	    feature_marker_relationship
                       where mrkr_zdb_id = fmrel_mrkr_zdb_id
                       and feature_zdb_id = fmrel_ftr_zdb_id
                       and feature_abbrev like 'unrec\_%')
                 where feature_name like 'unrec\_%' 
                 and not exists (Select 'x'
     	 		           from feature_marker_relationship,
			                marker
			           where mrkr_Zdb_id = fmrel_mrkr_Zdb_id
			           and feature_zdb_id = fmrel_ftr_zdb_id
			           and feature_abbrev = 'unrec\_'||mrkr_abbrev);" );
  
 if ( $nRecords > 0 ) {
  my $sendToAddress = $_[0];
  my $subject = "unrecAlleles have been updates";
  my $errMsg = "There are $nRecords unrecAllele record(s) whose names have been updated to reflect gene name changes. ";
    
  logError ($errMsg);
  &sendMail($sendToAddress, $subject, $routineName, $errMsg, $sql);
  &recordResult($routineName, $nRecords);
 }
}

#######################  Main ###########################################
#
# Define Usage
#

$usage = "Usage: updateunFeatures.pl <dbname>";

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


#$globalWorkingDir = "/tmp/updateunFeatures";  # Created & deleted by this script.
#my $dirPerms = oct(770);
#mkdir($globalWorkingDir, $dirPerms);

# Each routine below:
#  runs some SQL
#  checks results -- usually the number of row
#  if results are NOT as expected
#    print error message
#    send error message and query results to sb.
#  endif

my $dbaEmail     = "<!--|VALIDATION_EMAIL_DBA|-->";


    unFeatureNameAbbrevUpdate($dbaEmail);
    unrecoveredFeatureNameAbbrevUpdate($dbaEmail);


	   

#rmdir($globalWorkingDir);
