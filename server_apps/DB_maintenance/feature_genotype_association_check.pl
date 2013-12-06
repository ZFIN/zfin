#! /private/bin/perl -w 

# After curation is done, each feature record should be associated
# with a genotype as an entry in genotype_feature table. Since a 
# feature is created first and then associated with a genotype, we
# couldn't make this a database constraint.  We are excluding Burges Lin features from this requirement.
##

use DBI;
use MIME::Lite;

# set environment variables

$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";

## -------  MAIN -------- ##
$dataDirectory = 'feature_genotype';
system("rm -rf $dataDirectory");
my $exitVal = 0;
mkdir "$dataDirectory";


# open a handle on the db

my $dbh = DBI->connect('DBI:Informix:<!--|DB_NAME|-->',
                       '', 
                       '', 
                       {AutoCommit => 1,RaiseError => 1}
                      )
  || emailError("Failed while connecting to <!--|DB_NAME|--> "); 


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
print "hello";
# for each curator, get a list of features that they've curated
# that do not have a genotype

open (REP, '>feature_genotype/email.list')|| die "cannot open email-list.txt";

while ($cur->fetch) {

    $get_features_query = "select distinct feature_name, feature_zdb_id, recattrib_source_zdb_id
               from feature, updates,record_attribution
              where not exists
                    (select 't'
                       from genotype_feature
                      where genofeat_feature_zdb_id = feature_zdb_id)
              and rec_id=feature_zdb_id
              and feature_zdb_id=recattrib_data_zdb_id
              and recattrib_source_zdb_id!='ZDB-PUB-121121-1'
              and feature_lab_prefix_id!=85
              and submitter_id='$cur_curator_zdb_id'";

    # execute the sub-query

    my $sub_cur = $dbh->prepare($get_features_query);
    
    $sub_cur->execute;
#    my($feature_name, $feature_zdb_id);
    $sub_cur->bind_columns(\$feature_name, 
			   \$feature_zdb_id,
			   \$recattrib_source_zdb_id) ;
    
    # count the number of rows returned per curator
    $count = 0;
    
    $FILENAME = $curatorName . "txt" ;
    open(dataFile, ">>feature_genotype/$FILENAME");
    while ($sub_cur->fetch)
    {
	print dataFile "$feature_name , $feature_zdb_id , $recattrib_source_zdb_id";
	print dataFile "\n";

	# increment the counter
	$count++;
    }
    close(dataFile);

    my $filesize = -s "feature_genotype/$FILENAME" || 0;
    if($filesize < 1){
	unlink "feature_genotype/$FILENAME";
        $exitVal = 1;
    }

    print REP $email . " "  if($count > 0);

}

close (REP);

# close the connection to the database.
$dbh->disconnect();

exit $exitVal;
