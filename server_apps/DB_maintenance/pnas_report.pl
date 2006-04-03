#! /private/bin/perl -w 


##
# pnas_report.pl
# script runs once a month to generate a list of pubs
# from Proc. Nat'l Academ. of Sciences for Dave F. (or the current
# figure-permission curator. 
# Curator requested that the list is non-duplicative, so this 
# script also calls updatePNAS.sql which records the date this 
# script was executed and which figures it pulled out as needing
# permission.
##

use DBI;
use MIME::Lite;

# set environment variables

$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";

$mailprog = '/usr/lib/sendmail -t -oi -oem';

# subroutines to send email reports to curators.

sub openUpdateList()
  {
    system("/bin/rm -f updateList.unl");
    system("/bin/touch updateList.unl");

  }

sub openReport()
  {
    system("/bin/rm -f reportPNAS.unl");
    system("/bin/touch reportPNAS.unl");
  }

sub sendReport()
  {
    open(MAIL, "| $mailprog") 
	|| die "cannot open mailprog $mailprog, stopped";
 
   open(REPORT, "reportPNAS.unl") 
	|| die "cannot open reportPermissons";

    open(UPDATE, "updateList.unl")
	|| die "cannot open updateList" ;

    print MAIL "To:dfashena\@uoneuro.uoregon.edu,staylor\@cs.uoregon.edu\n";

    print MAIL "Subject: AutoGen: PNAs lite figure report\n";

    while($line = <REPORT>)
    {
	print MAIL $line;
    }
    close (REPORT);

    close (MAIL);
  }


## -------  MAIN -------- ##

# open a handle on the db

my $dbh = DBI->connect('DBI:Informix:<!--|DB_NAME|-->',
                       '', 
                       '', 
                       {AutoCommit => 1,RaiseError => 1}
                      )
  || emailError("Failed while connecting to <!--|DB_NAME|--> "); 


# move into the appropriate directory

chdir "<!--|ROOT_PATH|-->/server_apps/DB_maintenance/";

# set the mail program

$mailprog = '/usr/lib/sendmail -t -oi -oem';

# generate the PNAs permissions report for Dave F.

openReport();
    
openUpdateList();

open (UPDATE, ">> updateList.unl") or die "can not open update list";

open (REPORT, ">> reportPNAS.unl") or die "can not open report" ;

$pnas_query = "select zdb_id,
                  title,
                  authors,
                  year(pub_date) as pyear,
                  jrnl_abbrev, 
                  pub_volume, 
                  pub_pages
                  from publication, journal
                  where jrnl_zdb_id = pub_jrnl_zdb_id
                  and jrnl_name like 'Proc. Natl. Acad. Sci.%'";


# execute the pnas query

my $pnas_cur = $dbh->prepare($pnas_query);

$pnas_cur->execute;

my($zdb_id,
   $title,
   $authors,
   $pyear, 
   $jrnl_abbrev, 
   $pub_volume, 
   $pub_pages);

$pnas_cur->bind_columns(\$zdb_id,
			\$title,
			\$authors,
			\$pyear, 
			\$jrnl_abbrev, 
			\$pub_volume, 
			\$pub_pages) ;

my $counter = 0;

while ($pnas_cur->fetch) {

    $pnas_sub_query = "select fig_label, fig_zdb_id
                             from publication, figure, journal
                             where fig_source_zdb_id = '$zdb_id'
                             and jrnl_zdb_id = pub_jrnl_zdb_id
                             and jrnl_name like 'Proc. Natl. Acad. Sci.%'
                             and fig_source_zdb_id = publication.zdb_id 
                             and pub_can_show_images = 'f' 
                             and not exists (select 'x' 
                                                from fish_image 
                                                where fimg_fig_zdb_id = 
                                                           fig_zdb_id)
                             and fig_label like 'Fig.%'
			     and (fig_comments is null or fig_comments = '')";

    my $pnas_sub_cur = $dbh->prepare($pnas_sub_query);
    
    $pnas_sub_cur->execute;
	
    my($fig_label, $fig_zdb_id);
    $pnas_sub_cur->bind_columns(\$fig_label, \$fig_zdb_id) ;

    my $figure_label = "";

    while ($pnas_sub_cur->fetch)
    {
	$figure_label = $figure_label.", ".$fig_label ;
	$counter++ ;

	if ($counter ne 0) {
	    print UPDATE "$fig_zdb_id|\n";
	}
    }

    $figure_label =~ s/^\, //;

    if ($counter ne 0) {
	print REPORT "$zdb_id\n";
	print REPORT "$title\n" ;
	print REPORT "$authors\n";
	print REPORT "DATE:";
	print REPORT $pyear;
	print REPORT " SOURCE: ";
	print REPORT $jrnl_abbrev;
	print REPORT " ";
	print REPORT $pub_volume;
	print REPORT ":";
	print REPORT $pub_pages;
	print REPORT "\n";
	print REPORT "$figure_label\n\n\n" 
	}
}


# update the pnas figures in the database so we don't select them again

system ("$ENV{'INFORMIXDIR'}/bin/dbaccess <!--|DB_NAME|--> <!--|ROOT_PATH|-->/server_apps/DB_maintenance/updatePNAS.sql");

close(UPDATE);
close(REPORT);

if ($counter ne 0){
    sendReport();
}
# close the connection to the database.

$dbh->disconnect();
