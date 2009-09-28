#! /private/bin/perl -wT
print "Content-type: text/html\n\n";
use CGI qw( :cgi );
param("ottdarg") =~ /(OTTDAR[GT][\d]+)/;
my $ottdarg = $1;

$ENV{PATH} = ""; # for Taint

print "<script language='JavaScript1.2' src='http://<!--|DOMAIN_NAME|-->/header.js'></script>";
print <<_END_
<P>
The Novel Transcript Fasta record(s) for $ottdarg are:<P><P><pre>
_END_
;

system "/private/apps/wublast/xdget -n <!--|INTERNAL_BLAST_PATH|-->/vega_transcript $ottdarg";
print  "</pre>\n";
print "<script language='JavaScript1.2' src='http://<!--|DOMAIN_NAME|-->/footer.js'></script>";

