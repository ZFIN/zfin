#!/private/bin/perl 

use XML::Twig;
use DBI;
use LWP::Simple;
use lib "<!--|ROOT_PATH|-->/server_apps/";
use ZFINPerlModules;

#set environment variables

$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";

$dbname = "<!--|DB_NAME|-->";

#print "$dbname\n\n";
# Download PubMed records that are indexed in MeSH for both asthma and 
# leukotrienes and were also published in 2009.

$db = 'pubmed';
$query = 'zebrafish[mesh]+OR+zebra fish[mesh]+OR+danio rerio';

#assemble the esearch URL
$base = 'http://eutils.ncbi.nlm.nih.gov/entrez/eutils/';
$url = $base . "esearch.fcgi?db=$db&term=$query&usehistory=y&reldate=10&datetype=edat&retmax=100";

#post the esearch URL
$output = get($url);

#parse WebEnv and QueryKey
$web = $1 if ($output =~ /<WebEnv>(\S+)<\/WebEnv>/);
$key = $1 if ($output =~ /<QueryKey>(\d+)<\/QueryKey>/);

if (-e "<!--|TARGETROOT|-->/server_apps/data_transfer/PUBMED/parsePubs.log"){
    system("/bin/rm -rf <!--|TARGETROOT|-->/server_apps/data_transfer/PUBMED/parsePubs.log") && die "can not remove old parsed files.";
}

if (-e "<!--|TARGETROOT|-->/server_apps/data_transfer/PUBMED/newPublicationsAdded.txt"){
    system("/bin/rm -rf <!--|TARGETROOT|-->/server_apps/data_transfer/PUBMED/newPublicationsAdded.txt") && die "can not remove old publicationsAdded file.";
}

if (-e "<!--|TARGETROOT|-->/server_apps/data_transfer/PUBMED/newJournalsAdded.txt"){
    system("/bin/rm -rf <!--|TARGETROOT|-->/server_apps/data_transfer/PUBMED/newJournalsAdded.txt") && die "can not remove old journalsAdded file.";
}

if (-e "<!--|TARGETROOT|-->/server_apps/data_transfer/PUBMED/newJournalsAdded.txt"){
    system("/bin/rm -rf <!--|TARGETROOT|-->/server_apps/data_transfer/PUBMED/loadSQLError.log") && die "can not remove old loadSQLError.log file.";
}

if (-e "<!--|TARGETROOT|-->/server_apps/data_transfer/PUBMED/newJournalsAdded.txt"){
    system("/bin/rm -rf <!--|TARGETROOT|-->/server_apps/data_transfer/PUBMED/loadSQLOutput.log") && die "can not remove old loadSQLOutput.log file.";
}

if (-e "<!--|TARGETROOT|-->/server_apps/data_transfer/PUBMED/newPubSummary.txt"){
    system("/bin/rm -rf <!--|TARGETROOT|-->/server_apps/data_transfer/PUBMED/newPubSummary.txt") && die "can not remove old newPubSummary.txt file.";
}

#print $web."\n";
#print $key."\n";
open (LOG, "><!--|TARGETROOT|-->/server_apps/data_transfer/PUBMED/parsePubs.log") || die "Cannot open <!--|TARGETROOT|-->/server_apps/data_transfer/PUBMED/parsePub.log : $!\n";

### include this code for ESearch-EFetch
#assemble the efetch URL
$url = $base . "efetch.fcgi?db=$db&query_key=$key&WebEnv=$web&retmax=100";
$url .= "&retmode=xml";

my $pubCount = 0;
$twig = XML::Twig->new( 
    twig_handlers => { 
	'PubmedArticle' => \&pubMedArticle
    } 
);
$twig->parseurl($url);

#print "Total Pubs Added: ".$pubCount."\n";
#print $web."\n";
#print $key."\n";

system("$ENV{'INFORMIXDIR'}/bin/dbaccess -a <!--|DB_NAME|--> loadNewPubs.sql >loadSQLOutput.log 2> loadSQLError.log") && die "loading the pubs failed.";

sub pubMedArticle {
    $pubCount++;
    my ($twig, $pubMedArticle) = @_; 
    if (defined $pubMedArticle->first_child('MedlineCitation')) {
	if (defined $pubMedArticle->first_child('MedlineCitation')->first_child('PMID')){
	    my $pmid = $pubMedArticle->first_child('MedlineCitation')->first_child('PMID')->text;
	    print LOG $pmid."|";
	} 
	else{
	    print LOG "|";
	}
	if (defined $pubMedArticle->first_child('MedlineCitation')->first_child('KeywordList')){
	    if (defined $pubMedArticle->first_child('MedlineCitation')->first_child('KeywordList')->children('Keyword')){
		my $word;
		my $wordSet;
		for my $keyword ($pubMedArticle->first_child('MedlineCitation')->first_child('KeywordList')->children('Keyword')){
		    $word = $keyword->text;
		    if (length $wordSet eq 0){
			$wordSet = $word;
		    }
		    else {
			$wordSet = $wordSet.", ".$word;
		    }
		}
		print LOG $wordSet."|";
		$wordSet = '';
	    }
	}
	else{
	    print  LOG "|";
	}
	if (defined $pubMedArticle->first_child('MedlineCitation')->first_child('Article')){
	    if (defined $pubMedArticle->first_child('MedlineCitation')->first_child('Article')->first_child('ArticleTitle')){
		my $ArticleTitle = $pubMedArticle->first_child('MedlineCitation')->first_child('Article')->first_child('ArticleTitle')->text;
		print LOG $ArticleTitle."|";
	    }
	    else{
		print LOG "|";
	    }
	    if (defined $pubMedArticle->first_child('MedlineCitation')->first_child('Article')->first_child('Pagination')){
		my $Pages = $pubMedArticle->first_child('MedlineCitation')->first_child('Article')->first_child('Pagination')->text;
		print LOG $Pages."|";
	    }
	    else{
		print LOG "|";
	    }
	    if (defined $pubMedArticle->first_child('MedlineCitation')->first_child('Article')->first_child('Abstract')){
		if (defined $pubMedArticle->first_child('MedlineCitation')->first_child('Article')->first_child('Abstract')->first_child('AbstractText')){
		    my $AbstractText = $pubMedArticle->first_child('MedlineCitation')->first_child('Article')->first_child('Abstract')->first_child('AbstractText')->text;
		    print LOG $AbstractText."|";
		}
	    }
	    else {
		print LOG "|";
	    }

	    if (defined $pubMedArticle->first_child('MedlineCitation')->first_child('Article')->first_child('AuthorList')){
		my $counter=0;
		if (defined $pubMedArticle->first_child('MedlineCitation')->first_child('Article')->first_child('AuthorList')->children('Author')){
		    for my $author ($pubMedArticle->first_child('MedlineCitation')->first_child('Article')->first_child('AuthorList')->children('Author')){
			my $AuthorLastName;
			my $AuthorForeName;
			my $AuthorInitials;
			$counter++;
			for (qw / LastName ForeName Initials /){
			    if ($_ eq "LastName"){
				$AuthorLastName = $author->first_child($_)->text;
				#print $AuthorLastName."\n";
			    }
			    if ($_ eq "ForeName") {
				$AuthorForeName = $author->first_child($_)->text;
			       
			    }
			    if ($_ eq "Initials") {
				$AuthorInitials = $author->first_child($_)->text;
				my $length = length $AuthorInitials;
				#print $AuthorInitials."\n";
				if ($length > 1) {
				    my $newInitials;
				    my @characters = split //, $AuthorInitials;
				    foreach $char (@characters) {
					if (length $newInitials eq 0){
					    $newInitials = $char;
					}
					else {
					    $newInitials = $newInitials.".".$char;
					}
				    }
				    $AuthorInitials = $newInitials;
				    #print "AuthorInitials: ".$AuthorInitials."\n";
				}
				
			    }
			}
			$AuthorAbbrev = $AuthorLastName.", ".$AuthorInitials.".";
			#print "AuthorAbbrev: ".$AuthorAbbrev."\n";
		    
			if (length $Authors eq 0){
			    $Authors = $AuthorAbbrev;
			}
			else {
			    $Authors = $Authors.", ".$AuthorAbbrev
			}
			
		    }
		    print LOG $Authors."|";
		    print LOG $counter."|";
		}
		else{
		    print LOG "||";
		}
		$Authors = '';
		$counter = 0;
	    }
	    if (defined $pubMedArticle->first_child('MedlineCitation')->first_child('Article')->first_child('ArticleDate')){
		if (defined $pubMedArticle->first_child('MedlineCitation')->first_child('Article')->first_child('ArticleDate')->first_child('Year')){
		    my $ArticleYear = $pubMedArticle->first_child('MedlineCitation')->first_child('Article')->first_child('ArticleDate')->first_child('Year')->text;
		    print LOG $ArticleYear."|";  
		}
		else{
		    print LOG "|";
		}
		if (defined $pubMedArticle->first_child('MedlineCitation')->first_child('Article')->first_child('ArticleDate')->first_child('Month')){
		    my $ArticleMonth = $pubMedArticle->first_child('MedlineCitation')->first_child('Article')->first_child('ArticleDate')->first_child('Month')->text;
		    print LOG $ArticleMonth."|";  
		}
		else{
		    print LOG "|";
		}
		if (defined $pubMedArticle->first_child('MedlineCitation')->first_child('Article')->first_child('ArticleDate')->first_child('Day')){
		    my $ArticleDay = $pubMedArticle->first_child('MedlineCitation')->first_child('Article')->first_child('ArticleDate')->first_child('Day')->text;
		    print LOG $ArticleDay."|";  
		}
		else{
		    print LOG "|";
		}
	    }
	    else{
		    print LOG "|||";
		}
	    if (defined $pubMedArticle->first_child('MedlineCitation')->first_child('Article')->first_child('Journal')){
		if (defined $pubMedArticle->first_child('MedlineCitation')->first_child('Article')->first_child('Journal')->first_child('ISSN')){
		    my $ISSN = $pubMedArticle->first_child('MedlineCitation')->first_child('Article')->first_child('Journal')->first_child('ISSN')->text;
		    print LOG $ISSN."|";
		}
		else{
		    print LOG "|";
		}
		if (defined $pubMedArticle->first_child('MedlineCitation')->first_child('Article')->first_child('Journal')->first_child('JournalIssue')){
		    if (defined $pubMedArticle->first_child('MedlineCitation')->first_child('Article')->first_child('Journal')->first_child('JournalIssue')->first_child('Volume')){
			my $Volume = $pubMedArticle->first_child('MedlineCitation')->first_child('Article')->first_child('Journal')->first_child('JournalIssue')->first_child('Volume')->text;
			print LOG $Volume."|";
		    }
		    else{
			print LOG "|";
		    }
		    if (defined $pubMedArticle->first_child('MedlineCitation')->first_child('Article')->first_child('Journal')->first_child('JournalIssue')->first_child('Issue')){
			my $Issue = $pubMedArticle->first_child('MedlineCitation')->first_child('Article')->first_child('Journal')->first_child('JournalIssue')->first_child('Issue')->text;
			print LOG $Issue."|";
		    }
		    else{
			print LOG "|";
		    }
		}
		if (defined $pubMedArticle->first_child('MedlineCitation')->first_child('Article')->first_child('Journal')->first_child('Title')){
		    my $JournalTitle = $pubMedArticle->first_child('MedlineCitation')->first_child('Article')->first_child('Journal')->first_child('Title')->text;
		    print LOG $JournalTitle."|";
	
		}
		else{
		    print LOG "|";
		}
		if (defined $pubMedArticle->first_child('MedlineCitation')->first_child('Article')->first_child('Journal')->first_child('ISOAbbreviation')){
		    my $ISO = $pubMedArticle->first_child('MedlineCitation')->first_child('Article')->first_child('Journal')->first_child('ISOAbbreviation')->text;
		    print LOG $ISO."|";
		  
		}
		else{
		    print LOG "|";
		}
	    }
	}
    }
    if (defined $pubMedArticle->first_child('PubmedData')) {
	if (defined $pubMedArticle->first_child('PubmedData')->first_child('PublicationStatus')){
	    if (defined $pubMedArticle->first_child('PubmedData')->first_child('PublicationStatus')){
		my $PublicationStatus = $pubMedArticle->first_child('PubmedData')->first_child('PublicationStatus')->text;
		print LOG  $PublicationStatus."|";
	    }
	    else {
		print LOG "|";
	    }
	}
    }
    print LOG "\n";
}

