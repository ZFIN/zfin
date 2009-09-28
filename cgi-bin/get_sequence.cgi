#! /private/bin/perl 
use CGI qw( :cgi );
my $database = param("database") ;
my $type = param("type") ;
my $acc = param("acc")  ;
my $format = param("format")  ;


print "Content-Disposition: filename=\"$acc.fasta\"\n";
print "Content-type: text/$format\n\n";

system "/private/apps/wublast/xdget -$type <!--|INTERNAL_BLAST_PATH|-->/Current/$database $acc";

