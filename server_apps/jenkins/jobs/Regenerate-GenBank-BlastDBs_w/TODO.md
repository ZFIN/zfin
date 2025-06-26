### Placeholders:
These placeholders are found in the code for this change:

```
BLASTSERVER_BLAST_DATABASE_PATH
BLASTSERVER_FASTA_FILE_PATH
HOSTNAME
SCRIPT_PATH
TARGET_PATH
WEBHOST_BLAST_DATABASE_PATH
WEBHOST_FASTA_FILE_PATH
```

### Values:
These are the values on playground:

```
BLASTSERVER_BLAST_DATABASE_PATH=/research/zblastfiles/zmore/testdb
BLASTSERVER_FASTA_FILE_PATH=/tmp/fasta_file_path
HOSTNAME=testbed.zfin.org
WEBHOST_BLAST_DATABASE_PATH=/research/zblastfiles/zmore/testdb
WEBHOST_FASTA_FILE_PATH=/research/zblastfiles/dev_files
SCRIPT_PATH=/research/zusers/blast/BLAST_load/data_transfer
TARGET_PATH=/research/zusers/blast/BLAST_load/target
```

### Replacements for migration:
We can replace those values with:

#### SCRIPT_PATH
This is only used in:
```
blastdbupdate.pl:$reptfiles{"genbank"} = "@SCRIPT_PATH@/genbankupdate.report";
blastdbupdate.pl:$stampfiles{"genbank"} = "@SCRIPT_PATH@/GenBank/genbank.ftp";
blastdbupdate.pl:    system ("@TARGET_PATH@/GenBank/weeklyGB/weeklyGbUpdate.sh > @SCRIPT_PATH@/GenBank/weeklyGB/weeklyGbupdate.report 2>&1 ") &&  print MAIL "\t Update Failed! \n" ;
blastdbupdate.pl:    print MAIL "\t please check "."@SCRIPT_PATH@/GenBank/weeklyGB/"."weeklyGbupdate.report. \n";
GenBank/convertGenBank.sh:touch @SCRIPT_PATH@/GenBank/genbank.ftp;
```

So it's only used to write a couple reports and keep track of a timestamp
We can set it to $TARGETROOT/server_apps/data_transfer/BLAST/

```
SCRIPT_PATH=$TARGETROOT/server_apps/data_transfer/BLAST/
```

#### TARGET_PATH
Used in the following files:
```
./GenBank/pushGenBank.sh
./GenBank/convertGenBank.sh
./GenBank/weeklyGB/weeklyPushGenBank.sh
./GenBank/weeklyGB/weeklyGbUpdate.sh
./GenBank/revertGenBank.sh
./GenBank/assembleGenBank.sh
./GenBank/processGB.sh
./GenBank/downloadGenBank.sh
./blastdbupdate.pl
```
We can set it to $TARGETROOT/server_apps/data_transfer/BLAST/

#### BLASTSERVER_BLAST_DATABASE_PATH
This is now set to $BLAST_PATH which is /opt/zfin/blastdb
```
BLAST/GenBank/devoGenBank.sh
BLAST/GenBank/distributeToNodesGenBank.sh
BLAST/GenBank/postGbRelease.sh
BLAST/GenBank/revertGenBank.sh
BLAST/GenBank/sync.sh
BLAST/GenBank/weeklyGB/weeklyPushGenBank.sh
BLAST/GenBank/weeklyGB/weeklyWudbFormatGenBank.sh
BLAST/blastdbupdate.pl
```

#### BLASTSERVER_FASTA_FILE_PATH
This is set to /tmp/fasta_file_path on playground. That should be fine.
We just need to make sure the directory exists whenever referenced.

#### WEBHOST_BLAST_DATABASE_PATH
This seems like it should be the same as BLASTSERVER_BLAST_DATABASE_PATH (/opt/zfin/blastdb)



### Perl and executable paths:
- `#!/private/bin/perl -w` becomes `#!/usr/bin/env perl` followed by `use warnings`
- 

