#! /private/bin/perl -wT
print "Content-type: text/html\n\n";
use CGI qw( :cgi );
param("key") =~ /(wz[\d]+)/;
my $key = $1;

$ENV{PATH} = ""; # for Taint

print "<script language='JavaScript1.2' src='/javascript/header.js'></script>";
print <<_END_
<P>
The Fasta record(s) for $key are:<P><P><pre>
_END_
;

system "/private/apps/wublast/xdget -n <!--|BLASTSERVER_BLAST_DATABASE_PATH|-->/wz_est $key";
print  "</pre>\n";
print "<script language='JavaScript1.2' src='/javascript/footer.js'></script>";

