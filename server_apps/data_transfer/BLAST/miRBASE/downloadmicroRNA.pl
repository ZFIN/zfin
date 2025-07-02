#!/usr/bin/env perl

use DBI;

my $dbname = $ENV{'DB_NAME'};
my $username = "";
my $password = "";
my $dbhost = $ENV{'PGHOST'};
my $dbport = "5432";
my $BLASTSERVER_FASTA_FILE_PATH = "/tmp/fasta_file_path";

cd $BLASTSERVER_FASTA_FILE_PATH/fasta/microRNA ;

mv *.txt $BLASTSERVER_FASTA_FILE_PATH/fasta/Backup;

my $dbh = DBI->connect ("DBI:Informix:$dbname:$dbhost:$dbport", 
			$username, 
			$password,
			{AutoCommit => 1,RaiseError => 1}
    ) 
    or die "Cannot connect to Informix database: $DBI::errstr\n";

my $unloadMicroRNAMirBASEStemLoop = "unload to miRBASEStemLoopAccession.txt
                        select dblink_Acc_num 
                          from db_link
                          where exists (select 'x' from marker
                                          where mrkr_zdb_id = dblink_linked_recid
                                          and mrkr_abbrev like 'mir%')
                          and dblink_acc_num like 'MI0%'";

my $unloadMicroRNAMiRBASEMature = "unload to miRBASEMatureAccession.txt
                        select dblink_Acc_num 
                          from db_link
                          where exists (select 'x' from marker, transcript
                                          where mrkr_zdb_id = dblink_linked_recid
                                          and mrkr_abbrev like 'mir%'
                                          and mrkr_zdb_id = tscript_mrkr_zdb_id)
                          and dblink_acc_num like 'MIMA%'";

my $unloadMicroRNAZFINStemLoop = "unload to ZFINStemLoopAccession.txt
                        select dblink_Acc_num 
                          from db_link
                          where exists (select 'x' from marker
                                          where mrkr_zdb_id = dblink_linked_recid
                                          and mrkr_abbrev like 'mir%')
                          and dblink_acc_num like 'ZFINNUCL%'";

my $unloadMicroRNAZFINMature = "unload to ZFINMatureAccession.txt
                        select dblink_Acc_num 
                          from db_link
                          where exists (select 'x' from marker, transcript
                                          where mrkr_zdb_id = dblink_linked_recid
                                          and mrkr_abbrev like 'mir%'
                                          and mrkr_zdb_id = tscript_mrkr_zdb_id)
                          and dblink_acc_num like 'ZFINNUCL%'";






$dbh->disconnect();
