#!/usr/bin/perl
use utf8;

use Encode;
use XML::Twig;
use LWP::Simple;
use lib "<!--|ROOT_PATH|-->/server_apps/";
$dbname = "<!--|DB_NAME|-->";

# clean up files from last run
@filesToRemove = ("parsePubs.log", "newPublicationsAdded.txt", "newJournalsAdded.txt",
                  "loadSQLError.log", "loadSQLOutput.log", "newPubSummary.txt", "parseMesh.log");
foreach my $file (@filesToRemove) {
    unlink "<!--|TARGETROOT|-->/server_apps/data_transfer/PUBMED/$file" or warn "Could not delete $file: $!";
}
unlink glob "<!--|TARGETROOT|-->/server_apps/data_transfer/PUBMED/*.clob";

open (LOG, "><!--|TARGETROOT|-->/server_apps/data_transfer/PUBMED/parsePubs.log") || die "Cannot open <!--|TARGETROOT|-->/server_apps/data_transfer/PUBMED/parsePub.log : $!\n";
open (MESH, "><!--|TARGETROOT|-->/server_apps/data_transfer/PUBMED/parseMesh.log") || die "Cannot open <!--|TARGETROOT|-->/server_apps/data_transfer/PUBMED/parseMesh.log : $!\n";

$base = 'https://eutils.ncbi.nlm.nih.gov/entrez/eutils/';
$db = 'pubmed';
my $pubCount = 0;
if (scalar @ARGV > 0) {
    # Got a list of PubMed IDs to fetch
    $ids = join(",", @ARGV);
    $url = $base . "efetch.fcgi?db=$db&id=$ids&retmode=xml";
    print "fetched url: ".$url."\n";
    $twig = XML::Twig->new(twig_handlers => { 'PubmedArticle' => \&pubMedArticle });
    $twig->parseurl($url);
} else {
    # Didn't get any command line arguments, so let's load new pubs
    # Download PubMed records that are indexed in MeSH for zebrafish and danio rerio
    # Use the TW (Text Words) field for zebrafish (to avoid picking up the journal Zebrafish.
    # http://www.ncbi.nlm.nih.gov/books/NBK3827/#_pubmedhelp_Search_Field_Descriptions_and_
    $query = 'zebrafish[TW]+OR+"zebra fish"[TW]+OR+"danio rerio"[ALL]';

    #assemble the esearch URL
    $retmax = '200';
    $url = $base . "esearch.fcgi?db=$db&term=$query&usehistory=y&reldate=80&datetype=edat&retmax=$retmax";

    #get the esearch URL
    #the usehistory key creates a url key that we can use to access the return: suggested for larger queries by NCBI
    $output = get($url);

    #parse WebEnv and QueryKey
    $web = $1 if ($output =~ /<WebEnv>(\S+)<\/WebEnv>/);
    $key = $1 if ($output =~ /<QueryKey>(\d+)<\/QueryKey>/);
    $count = $1 if ($output =~ /<Count>(\d+)<\/Count>/);

    print "webenv: ".$web."\n";
    print "query_key: ".$key."\n";
    print "total pub count: ".$count."\n";
    #assemble the efetch URL
    for ($retstart = 0; $retstart < $count; $retstart += $retmax) {
        $fetch_url = $base . "efetch.fcgi?db=$db&query_key=$key&WebEnv=$web";
        $fetch_url .= "&retmode=xml";
        $fetch_url .= "&retstart=$retstart";
        $fetch_url .= "&retmax=$retmax";
        print "fetched url: ".$fetch_url."\n";
        $twig = XML::Twig->new(
            twig_handlers =>
                { 'PubmedArticle' => \&pubMedArticle }
        );
        # The twig parser will go through an entire pubMedArticle, extracting fields as it goes.
        # consequently, the order it parses, is the order the load file will be generated.
        $twig->parseurl($fetch_url);
    }
}

system("$ENV{'INFORMIXDIR'}/bin/dbaccess -a <!--|DB_NAME|--> loadNewPubs.sql >loadSQLOutput.log 2> loadSQLError.log") && die "loading the pubs failed.";

sub pubMedArticle {
    $pubCount++;
    my ($twig, $pubMedArticle) = @_;

    my %row = {};
    if (defined $pubMedArticle->first_child('MedlineCitation')) {
        my $medlineCitation = $pubMedArticle->first_child('MedlineCitation');

        $row{'pmid'} = $medlineCitation->first_child_text('PMID');

        if (defined $medlineCitation->first_child('KeywordList')) {
            my $keywordList = $medlineCitation->first_child('KeywordList');
            my @keywords = $keywordList->children_text('Keyword');
            $row{'keywords'} = join(", ", @keywords);
        }

        if (defined $medlineCitation->first_child('Article')) {
            my $article = $medlineCitation->first_child('Article');

            my $articleTitle = $article->first_child_text('ArticleTitle');
            $articleTitle =~ s/\|/\\|/g;
            $articleTitle =~ s/\.+$//;
            $row{'title'} = $articleTitle;

            $row{'pages'} = $article->first_child_text('Pagination');

            if (defined $article->first_child('Abstract')) {
                my $abstract = $article->first_child('Abstract');
                if (defined $abstract->children('AbstractText')) {
                    my @paragraphs = ();
                    for my $abstractText ($abstract->children('AbstractText')) {
                        my $nlmCategory = $abstractText->att('NlmCategory');
                        my $label  = $abstractText->att('Label');
                        my $text = $abstractText->text;
                        if ((defined $label && $label ne 'UNLABELLED' && $label ne 'UNASSIGNED') ||
                            (defined $nlmCategory && $nlmCategory ne 'UNLABELLED' && $nlmCategory ne 'UNASSIGNED')) {
                            # part of a structured abstract
                            my $displayedLabel = ucfirst(lc($label || $nlmCategory));
                            push(@paragraphs, "<div class='pub-abstract-section'><span class='pub-abstract-section-label'>$displayedLabel</span> $text</div>");
                        } else {
                            push(@paragraphs, $text);
                        }
                    }
                    $joinedParagraphs = join("", @paragraphs);
                    $joinedParagraphs =~ s/\|/\\|/g;

                    my $abstractFileName = sprintf("<!--|TARGETROOT|-->/server_apps/data_transfer/PUBMED/abstract%08d.clob", $pubCount);
                    open(ABS, ">", $abstractFileName);
                    print ABS escape_utf8($joinedParagraphs);
                    close ABS;
                    $row{'abstract'} = $abstractFileName;
                }
            }

            my $authors = 'none';
            my $numAuthors = '0';
            if (defined $article->first_child('AuthorList')) {
                my $authorList = $article->first_child('AuthorList');
                if (defined $authorList->children('Author')) {
                    my @authorNames = ();
                    for my $author ($authorList->children('Author')) {
                        if (defined $author->first_child('CollectiveName')) {
                            push(@authorNames, $author->first_child_text('CollectiveName'))
                        } else {
                            # we are intentionally not handling the Suffix element at this time
                            # because it would complicate author list parsing. it would ideally
                            # be worked out with some databse schema changes
                            my $AuthorLastName = $author->first_child_trimmed_text('LastName');
                            my $AuthorForeName = $author->first_child_trimmed_text('ForeName');
                            my $AuthorInitials = $author->first_child_trimmed_text('Initials');
                            $AuthorInitials = join('.', split(//, $AuthorInitials)) . '.';
                            push(@authorNames, "$AuthorLastName, $AuthorInitials");
                        }
                    }
                    $authors = join(", ", @authorNames);
                    $numAuthors = @authorNames;
                }
            }
            $row{'authors'} = $authors;
            $row{'numAuthors'} = $numAuthors;

            if (defined $article->first_child('ArticleDate')) {
                my $articleDate = $article->first_child('ArticleDate');
                $row{'year'} = $articleDate->first_child_text('Year');
                $row{'month'} = $articleDate->first_child_text('Month');
                $row{'day'} = $articleDate->first_child_text('Day');
            }

            if (defined $article->first_child('Journal')) {
                my $journal = $article->first_child('Journal');
                $row{'issn'} = $journal->first_child_text('ISSN');

                if (defined $journal->first_child('JournalIssue')) {
                    my $journalIssue = $journal->first_child('JournalIssue');
                    $row{'volume'} = $journalIssue->first_child_text('Volume');
                    $row{'issue'} = $journalIssue->first_child_text('Issue');
                }

                $row{'journaltitle'} = $journal->first_child_text('Title');
                $row{'iso'} = $journal->first_child_text('ISOAbbreviation');
            }
        }

        if (defined $medlineCitation->first_child('MeshHeadingList')) {
            my $meshList = $medlineCitation->first_child('MeshHeadingList');
            if (defined $meshList->children('MeshHeading')) {
                for my $meshHeading ($meshList->children('MeshHeading')) {
                    if (defined $meshHeading->first_child('DescriptorName')) {
                        my $descriptor = $meshHeading->first_child('DescriptorName');
                        my $descId = $descriptor->att('UI');
                        my $descIsMajor = $descriptor->att('MajorTopicYN');
                        $descIsMajor =~ tr/YN/tf/;
                        print MESH "$row{pmid}|$descId||$descIsMajor\n";
                        if (defined $meshHeading->children('QualifierName')) {
                            my @qualifiers = $meshHeading->children('QualifierName');
                            if (@qualifiers) {
                                for my $qualifier (@qualifiers) {
                                    my $qualId = $qualifier->att('UI');
                                    my $qualIsMajor = $qualifier->att('MajorTopicYN');
                                    $qualIsMajor =~ tr/YN/tf/;
                                    print MESH "$row{pmid}|$descId|$qualId|$qualIsMajor\n";
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    if (defined $pubMedArticle->first_child('PubmedData')) {
        my $pubmedData = $pubMedArticle->first_child('PubmedData');
        $row{'status'} = $pubmedData->first_child_text('PublicationStatus');
    }

    my @fields = ('pmid', 'keywords', 'title', 'pages', 'abstract', 'authors',
                  'numAuthors', 'year', 'month', 'day', 'issn', 'volume',
                  'issue', 'journaltitle', 'iso', 'status');
    print LOG join('|', map { escape_utf8($row{$_} =~ s/\|/\\\|/rg) } @fields), "\n";
}

sub escape_utf8 {
    return encode("iso-8859-1", $_[0], Encode::FB_HTMLCREF);
}

close LOG;
close MESH;


exit;
