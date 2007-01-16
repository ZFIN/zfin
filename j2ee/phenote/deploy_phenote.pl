#!/private/bin/perl 

$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";

$dir = "<!--|ROOT_PATH|-->/j2ee/phenote";
chdir "$dir";
print "$dir"."\n" ;
   
system("/bin/rm -rf <!--|ROOT_PATH|-->/j2ee/phenote/deploy/*") and die "can not remove all phenote files";

system("/local/bin/unzip -d <!--|ROOT_PATH|-->/j2ee/phenote/deploy/ <!--|ROOT_PATH|-->/j2ee/phenote/phenote.war ") and die "can not unzip phenote.war";

system("/bin/rm -rf <!--|ROOT_PATH|-->/j2ee/phenote/deploy/WEB-INF/data_transfer/*") and die "can not remove all phenote files";


system ("$ENV{'INFORMIXDIR'}/bin/dbaccess <!--|DB_NAME|--> deploy_phenote.sql >out 2> deploy_report.txt") and die "deploy_phenote.sql did not complete successfully";

system ("/bin/chmod 654 <!--|ROOT_PATH|-->/j2ee/phenote/deploy/WEB-INF/data_transfer/*") and die "could not chmod data_Transfer files";

system ("/bin/chgrp fishadmin <!--|ROOT_PATH|-->/j2ee/phenote/deploy/WEB-INF/data_transfer/*") and die "could not chmod data_Transfer files";

exit;
