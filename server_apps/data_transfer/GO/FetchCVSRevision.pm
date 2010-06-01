package FetchCVSRevision;

# fetch previous revision
use LWP::Simple;
use LWP::UserAgent;
use HTTP::Request;
use HTTP::Response;

# fetching most recent revisions from CVS WEB
# finds the first "Revision <b>1.234</b>"
# in the url given as an argument
# incase of internet failure returns 0

sub fcvsr_get() {
    shift;
	my $url = shift;
    #print $url . "\n";
	my $ua = LWP::UserAgent->new();
	$ua->timeout(30);   # why we bother to do it this way
	my $request = HTTP::Request->new(GET => $url);
	my $page = $ua->request($request);

	#print $page->content;

	my $revision = 0;
	if ( ! $page->is_error() ) {
    	$page->content =~ m/Revision \<b\>([0123456789.]*)/;
    	$revision = $1;
	}
	return $revision;
}
1;
