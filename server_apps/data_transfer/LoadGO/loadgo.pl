#!/local/bin/perl 

#
# loadsp.pl
#

#------------------- Download -----------

sub downloadGOtermFiles () {

   system("/local/bin/wget http://www.geneontology.org/ontology/function.ontology -O function.ontology");
   system("/local/bin/wget http://www.geneontology.org/ontology/process.ontology -O process.ontology");
   system("/local/bin/wget http://www.geneontology.org/ontology/component.ontology -O component.ontology");
   system("/local/bin/wget http://www.geneontology.org/ontology/GO.defs -O go.defs");
 
 }
#   Main
#


#set environment variables
$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";

chdir "<!--|ROOT_PATH|-->/server_apps/data_transfer/LoadGO/";


#remove old files
 
system("/bin/rm -f *.ontology");
system("/bin/rm -f *.unl");
system("/bin/rm -f *.txt");


&downloadGOtermFiles();
print 'Download done';

print "\nontology.pl function.ontology process.ontology component.ontology\n";
system ("test.pl function.ontology process.ontology component.ontology");
$count = 0;
$retry = 1;
# wait till parsing is finished
while( !( -e "ontology.unl")) {

  $count++;
  if ($count > 10)
  {
    if ($retry) 
    {
      $count = 0;
      $retry = 0;
      print "retry ontology.pl\n";
      system("test.pl function.ontology process.ontology component.ontology");
    }
    else
    {
         &sendErrorReport("Failed to run ontology.pl"); 
      exit;
    }
  }  
}


# ------------ Loading ---------------------
print "\nloading...\n";
system ("$ENV{'INFORMIXDIR'}/bin/dbaccess <!--|DB_NAME|--> loadgoterms.sql >out 2> report.txt");

exit;



