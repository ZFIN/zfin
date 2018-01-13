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

## -------  MAIN -------- ##
$dataDirectory = 'fx_permission';
system("rm -rf $dataDirectory");
mkdir "$dataDirectory";

# open a handle on the db

my $dbh = DBI->connect ("DBI:Pg:dbname=<!--|DB_NAME|-->;host=localhost", '','') or die "Cannot connect to database: $DBI::errstr\n";


# set the mail program

# establish a list of possible curators to mail to

$get_curators_query = "select distinct cur_curator_zdb_id, email, replace(name, ' ','')
                             from curation, 
                                   person 
                             where person.zdb_id = cur_curator_zdb_id";

# execute the get_curators query

my $cur = $dbh->prepare($get_curators_query);

$cur->execute;
my($cur_curator_zdb_id, $email);
$cur->bind_columns(\$cur_curator_zdb_id, \$email, \$curatorName) ;

# for each curator, get a list of pubs that they've curated
# that have permissions to show images, but whose figures (one or more)
# do not have images 

open (REP, '>fx_permission/email.list')|| die "cannot open email-list.txt";

while ($cur->fetch) {

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

    $get_figs_query = "select distinct cur_pub_zdb_id,
                  fig_label, fig_full_label
                from curation, 
                     publication, 
                     figure, 
                     person 
                where fig_source_zdb_id = cur_pub_zdb_id
                and (fig_caption is null or fig_caption = '')
                and (cur_topic = 'Expression' or cur_topic = 'Phenotype')
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
                order by cur_pub_zdb_id, fig_full_label";

    # execute the sub-query

    my $sub_cur = $dbh->prepare($get_figs_query);
    
    $sub_cur->execute;
    my($cur_pub_zdb_id, $fig_label);
    $sub_cur->bind_columns(\$cur_pub_zdb_id, 
			   \$fig_label,
			   \$fig_full_label) ;
    
    # count the number of rows returned per curator

    $count = 0;
    
    # for each pub and figure, print out that figure/pub to a report

    $FILENAME = $curatorName . "txt" ;
    open(dataFile, ">>fx_permission/$FILENAME");
    while ($sub_cur->fetch)
    {
	print dataFile "$cur_pub_zdb_id : $fig_label" if ($fig_full_label ne '');
	print dataFile "\n";

	# increment the counter
	$count++;
    }
    close(dataFile);

    my $filesize = -s "fx_permission/$FILENAME" || 0;
    if($filesize < 1){
	unlink "fx_permission/$FILENAME";
    }

}

close (REP);

# close the connection to the database.
$dbh->disconnect();

