#!/local/bin/perl 
#  This script creates a file that ZFIN sends to Stanford. The file is tab 
#  delimitted with 14 columns, each GO term/gene association on a seperate  
#  line. 
#  We must send the file via email to GO after running the script. A reminder
#  email, containing the path to the file, is sent to a member of ZFIN. 


#set environment variables

$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";
print 'generating SQL';
system ("$ENV{'INFORMIXDIR'}/bin/dbaccess <!--|DB_NAME|--> gofile.sql >out 2> report.txt");
system ("goparser.pl");
system ("perl5 check-gene-association.pl -d gene_association.zfin");
