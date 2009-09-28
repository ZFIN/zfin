#! /private/bin/perl 
use CGI qw( :cgi );
my $database = param("database") ;
my $type = param("type") ;
my $acc = param("acc")  ;


print "Content-type: text/html\n\n";

print "<script language='JavaScript1.2' src='http://<!--|DOMAIN_NAME|-->/javascript/header.js'></script>";
print <<_END_
<P>
The record for $database is:<P><P><pre>
_END_
;

system "/private/apps/wublast/xdget -$type <!--|INTERNAL_BLAST_PATH|-->/$database $acc";

print  "</pre>\n";
print "<script language='JavaScript1.2' src='http://<!--|DOMAIN_NAME|-->/javascript/footer.js'></script>";

