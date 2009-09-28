#! /private/bin/perl 
use CGI qw( :cgi );
my $zprot = param("accNum");

print "Content-type: text/html\n\n";

print "<script language='JavaScript1.2' src='http://<!--|DOMAIN_NAME|-->/header.js'></script>";
print <<_END_
<P>
The Protein record for $zprot are:<P><P><pre>
_END_
;

system "/private/apps/wublast/xdget -p -d <!--|INTERNAL_BLAST_PATH|-->/ZFIN_PROT $zprot";
print  "</pre>\n";
print "<script language='JavaScript1.2' src='http://<!--|DOMAIN_NAME|-->/footer.js'></script>";

