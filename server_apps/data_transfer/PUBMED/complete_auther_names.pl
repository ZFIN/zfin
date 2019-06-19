#! /opt/zfin/bin/perl -w
# complete_auther_names.pl
# load pubmed_publication_author with auther data from pubmed

use DBI;
use XML::Twig;
use utf8;

$dbname = "<!--|DB_NAME|-->";
$username = "";
$password = "";

system("/bin/date");
system("/bin/rm -f authors");

### open a handle on the db
$dbh = DBI->connect ("DBI:Pg:dbname=$dbname;host=localhost", $username, $password) or die "Cannot connect to database: $DBI::errstr\n";

$sql = "select distinct zdb_id, accession_no, title
          from publication 
         where accession_no is not null 
           and title is not null
           and not exists (select 1 from pubmed_publication_author
                            where ppa_pubmed_id = accession_no::text 
                              and ppa_publication_zdb_id = zdb_id);";

my $cur = $dbh->prepare($sql);
$cur ->execute();

my ($pubZdbId, $accession, $pubTitle);

$cur->bind_columns(\$pubZdbId,\$accession,\$pubTitle);

%pmids = ();
      
while ($cur->fetch()) {
   $pmids{$accession} = $pubZdbId;
}

$cur->finish(); 


$ctTotal = 0;

open(my $AUTHOR, ">:encoding(utf-8)", "authors") || die "Cannot open authors : $!\n";

%uniqueNames = ();

foreach $pmid (sort keys %pmids) {
  
  $pubZdbId = $pmids{$pmid};
  
  $url = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=pubmed&id=".$pmid."&retmode=xml";
  $twig = XML::Twig->nparse($url);
  if ($twig) {
    $root = $twig->root;
    $authListElmt = $root->first_descendant('AuthorList');
  
    if ($authListElmt) {
      @authors = $authListElmt->children;
      foreach $author (@authors) {
        $lastNameElm = $author->first_child('LastName');
        if ($lastNameElm) {
          $lastName = $lastNameElm->text;
          $firstNameElm = $author->first_child('ForeName');
          $firstName = $firstNameElm ? $firstNameElm->text : "";
          $middleNameElm = $author->first_child('Initials');
          $middleName = $middleNameElm ? $middleNameElm->text : "";
        
          if (!exists($uniqueNames{$pmid.$lastName.$middleName.$firstName})) {      
            print $AUTHOR "$pmid|$pubZdbId|$lastName|$middleName|$firstName\n";
            $uniqueNames{$pmid.$lastName.$middleName.$firstName} = $pubZdbId;
            $ctTotal++;
          }
        }   
      }
    } 
  }
}

$dbh->disconnect();


close $AUTHOR;

system("psql -d <!--|DB_NAME|--> -a -f load_complete_author_names.sql");

system("/bin/date");

print "\n$ctTotal author names added.\n";

exit;

