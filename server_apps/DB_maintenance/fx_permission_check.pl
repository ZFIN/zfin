#! /private/bin/perl -w 


##
# fx_permissions_check.pl
#
# Check the permissions of figures in ZFIN that have expression
# curation, reporting on those that are from closed pubs,
# should have full permission, but
# whose images have not been captured yet.
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

sub openReport()
  {
    system("/bin/rm -f reportPermissions");
    system("touch reportPermissions");
  }

sub sendReport($)
  {
    open(MAIL, "| $mailprog") 
	|| die "cannot open mailprog $mailprog, stopped";
 
   open(REPORT, "reportPermissions") 
	|| die "cannot open reportPermissons";

    print MAIL "To:". $_[0]."\n";

    print MAIL "Subject: AutoGen: FX lite-figures have permission to become full-figured\n";
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

chdir "<!--|ROOT_PATH|-->/<!--|CGI_BIN_DIR_NAME|-->/";

# set the mail program

$mailprog = '/usr/lib/sendmail -t -oi -oem';

# establish a list of possible curators to mail to

$get_curators_query = "select distinct cur_curator_zdb_id, email
                             from curation, 
                                   person 
                             where person.zdb_id = cur_curator_zdb_id";

# execute the get_curators query

my $cur = $dbh->prepare($get_curators_query);

$cur->execute;
my($cur_curator_zdb_id, $email);
$cur->bind_columns(\$cur_curator_zdb_id, \$email) ;

# for each curator, get a list of pubs that they've curated
# that have permissions to show images, but whose figures (one or more)
# do not have images 

while ($cur->fetch) {

    # open a new report each time, so each curator only gets their
    # personal records.

    openReport() ;
    
    open (REPORT, ">>reportPermissions") or die "can not open report" ;

    print REPORT "Images could be loaded for these figures, they have full permissions.\n\n" ;

    # prepare the query to get the lite figures that should
    # be full figures.
    #
    # 'image quality poor, permission exception' :
    #
    # in very few cases, curators have not loaded images b/c
    # they are poor.  we flag the comments field of a figure
    # record as 'image quality poor, permission exception'
    # if a curator explicitly asks us to.  Otherwise, we'll
    # send them the report until they fix it.

    $get_figs_query = "select cur_pub_zdb_id,
                  fig_label
                from curation, 
                     publication, 
                     figure, 
                     person 
                where fig_source_zdb_id = cur_pub_zdb_id
                and (fig_caption is null or fig_caption = '')
                and cur_topic = 'Expression' 
                and cur_closed_date is not null 
                and fig_comments != 'image quality poor, permission exception'
                and fig_source_zdb_id = publication.zdb_id 
                and cur_pub_zdb_id = publication.zdb_id 
                and pub_can_show_images = 't' 
                and not exists (select 'x' 
                                 from image 
                                 where img_fig_zdb_id = fig_zdb_id) 
                and person.zdb_id = cur_curator_zdb_id 
                and cur_curator_zdb_id = '$cur_curator_zdb_id'
                and fig_label like 'Fig.%' 
                order by cur_curator_zdb_id, cur_pub_zdb_id";

    # execute the sub-query

    my $sub_cur = $dbh->prepare($get_figs_query);
    
    $sub_cur->execute;
    my($cur_pub_zdb_id, $fig_label);
    $sub_cur->bind_columns(\$cur_pub_zdb_id, 
			   \$fig_label) ;
    
    # count the number of rows returned per curator

    $count = 0;
    
    # for each pub and figure, print out that figure/pub to a report

    while ($sub_cur->fetch)
    {
	print REPORT "$cur_pub_zdb_id : $fig_label" ;
	print REPORT "\n";
	
	# increment the counter

	$count++;
    }

    # send the report to the appropriate curator if the number of rows
    # returned is not zero.

    sendReport($email) if ($count != 0 );
}

# close the connection to the database.

$dbh->disconnect();
