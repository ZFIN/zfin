#!/private/bin/perl -w
#
# This script writes obo file header, calls
# anatitem_2_obo.pl and stg_2_obo.pl to generate
# obo format ZFIN anatomy file.
#
# Input:
#   None               
#
# Output:
#   OBO format AO file sent by email
# 

use strict;
use MIME::Lite;
use DBI;
require "err_report.pl";

#set environment variables
$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";

### open a handle on the db
my $dbh = DBI->connect('DBI:Informix:<!--|DB_NAME|-->',
                       '', 
                       '', 
		       {AutoCommit => 1,RaiseError => 1}
		      )
    || &reportError("Failed while connecting to <!--|DB_NAME|--> "); 

#------------------ Send OBO File----------------
# No parameter
#
sub sendResult {

    my $SUBJECT="Auto: OBO file ";
    my $MAILTO="<!--|AO_EMAIL_CURATOR|-->";   
    my $ATTFILE ="./zebrafish_anatomy.obo";
    
    # Create a new multipart message:
    my $msg = new MIME::Lite 
	From    => "$ENV{LOGNAME}",
	To      => "$MAILTO",
	Subject => "$SUBJECT",
	Type    => 'multipart/mixed';
    
    attach $msg 
	Type     => 'application/octet-stream',
	Encoding => 'base64',
	Path     => "./$ATTFILE",
	Filename => "$ATTFILE";
    
    # Output the message to sendmail
    
    open (SENDMAIL, "| /usr/lib/sendmail -t -oi");
    $msg->print(\*SENDMAIL);
    
    close(SENDMAIL);
}


sub loadOboToDB (){

    my $load_sql = "update obo_file
                     set (obofile_text,obofile_load_date,obofile_load_process) = (filetoblob('<!--|ROOT_PATH|-->/server_apps/data_transfer/Anatomy/zebrafish_anatomy.obo','server'),CURRENT YEAR TO SECOND,'AO Load') where obofile_name = 'zebrafish_anatomy.obo'";

    my $load_sth = $dbh->prepare($load_sql)
	    or  &reportError("Couldn't prepare the statement:$!\n");

    $load_sth->execute or &reportError("Couldn't execute the statement:$!\n");

}


#--------------- Main --------------------------------
#

chdir "<!--|ROOT_PATH|-->/server_apps/data_transfer/Anatomy/";

#remove old files
system("/bin/rm -f *.obo");

open (OUT, ">zebrafish_anatomy.obo") or &reportError("Cannot open file to write.");

#----  print header ------
my ($sec, $min, $hour, $mday, $month,$year) = localtime(time);
$year += 1900;
$month += 1;

my $typedef = <<END;

[Typedef]
id: part_of
name: part of
is_transitive: true

[Typedef]
id: develops_from
name: develops from
is_transitive: true

[Typedef]
id: start
name: start stage

[Typedef]
id: end
name: end stage

END

print OUT "format-version: 1.2\n";
print OUT "date: $mday:$month:$year $hour:$min\n";
print OUT "saved-by: ZFIN\n";
print OUT "default-namespace: zebrafish_anatomical_ontology\n";
print OUT "synonymtypedef: PLURAL \"PLURAL\" \n";
print OUT "$typedef";

#-- invoke scripts to generate anatomy items and stage items

print OUT `./anatitem_2_obo.pl`;

print OUT `./stg_2_obo.pl`;

close OUT;

if ( -e "<!--|ROOT_PATH|-->/j2ee/phenote/deploy/WEB-INF/data_transfer/zebrafish_anatomy_old.obo") {
    
    system("/bin/rm <!--|ROOT_PATH|-->/j2ee/phenote/deploy/WEB-INF/data_transfer/zebrafish_anatomy_old.obo") and die "can not rm zebrafish_anatomy_old.obo" ;
    
    print "rm'd zebrafish_anatomy_old.obo\n" ;
}

if ( -e "<!--|ROOT_PATH|-->/j2ee/phenote/deploy/WEB-INF/data_transfer/zebrafish_anatomy.obo") {
    
    system("/bin/mv <!--|ROOT_PATH|-->/j2ee/phenote/deploy/WEB-INF/data_transfer/zebrafish_anatomy.obo <!--|ROOT_PATH|-->/j2ee/phenote/deploy/WEB-INF/data_transfer/zebrafish_anatomy_old.obo") and die "can not mv zebrafish_anatomy_old.obo" ;
    
    print "mv'd zebrafish_anatomy.obo to zebrafish_anatomy_old.obo\n" ;
}

&loadOboToDB();

system ("$ENV{'INFORMIXDIR'}/bin/dbaccess <!--|DB_NAME|--> unloadAOFile.sql >out 2> report.txt") and die "unloadAOFile.sql failed";

system ("/bin/chmod 654 <!--|ROOT_PATH|-->/j2ee/phenote/deploy/WEB-INF/data_transfer/*") and die "could not chmod data_Transfer files";

system ("/bin/chgrp fishadmin <!--|ROOT_PATH|-->/j2ee/phenote/deploy/WEB-INF/data_transfer/*") and die "could not chmod data_Transfer files";


&sendResult();

exit;
