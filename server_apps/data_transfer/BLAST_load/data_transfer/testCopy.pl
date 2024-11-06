#!/private/bin/perl -w
#
# The script checks several ftp sites for new release,
# and invokes corresponding scripts to transfer and
# process data. It executes at /zdevblast/dev_BLAST_files/fasta,
# uses timestamped *.ftp file to probe new release,
# then calls scripts under /Users/informix/BLAST_load/data_transfer to execute.
# Outputs are saved in *.report file. This script
# runs weekly and sends out summary via email.
#
#   For Trace site, it first check for incremental updates,
# then check for the next release file.
#   For Ensembl, it probes the content of the directory,
# and fetch back the file name of the new release.
#

use strict;
use Net::FTP;


# MAIN

&cpToHelixAndRsyncGenomixDev;

# sub routines

sub cpToHelixAndRsyncGenomixDev() {
    chdir "@BLASTSERVER_BLAST_DATABASE_PATH@/Current" ;
    
    # WEBHOST_BLAST_DATABASE_PATH is always /research/zprodmore/blastdb.  
    # we do these one by one because we don't want to overwrite any load files on helix
    # from ZFIN (especially curated ones)

    system("rm -f @WEBHOST_BLAST_DATABASE_PATH@/Backup/*") && die "@WEBHOST_BLAST_DATABASE_PATH@/Backup delete failed for blastdbupdate.pl";

    # check if files exist; if they don't we don't want to put the current files to backup and then 
    # have nothing to move.

    my $ckFile = "@BLASTSERVER_FASTA_FILE_PATH@/fasta/GenBank/gbk_zf_cdna.xnd";
    if  (-e $ckFile) {
	# rm the current files for blastdbupdate members.
	system("mv -f @WEBHOST_BLAST_DATABASE_PATH@/Current/gbk*.x* @WEBHOST_BLAST_DATABASE_PATH@/Backup/" ) && die "@WEBHOST_BLAST_DATABASE_PATH@/Current/gbk* delete failed for blastdbupdate.pl";
	system("mv -f @BLASTSERVER_FASTA_FILE_PATH@/fasta/GenBank/gbk*.x* @WEBHOST_BLAST_DATABASE_PATH@/Current/") && die "@WEBHOST_BLAST_DATABASE_PATH@/Current mv failed from @BLASTSERVER_BLAST_DATABASE_PATH@/Current/gbk";
	}

    $ckFile ="@BLASTSERVER_FASTA_FILE_PATH@/fasta/Ensembl/ensembl_zf.xnd";
    if  (-e $ckFile) {

	system("mv -f @WEBHOST_BLAST_DATABASE_PATH@/Current/ensembl*.x* @WEBHOST_BLAST_DATABASE_PATH@/Backup/") && die "@WEBHOST_BLAST_DATABASE_PATH@/Current/ensembl* delete failed for blastdbupdate.pl";
        system("mv -f @BLASTSERVER_FASTA_FILE_PATH@/fasta/Emsembl/ensembl_zf*.x* @WEBHOST_BLAST_DATABASE_PATH@/Current/") && die "@WEBHOST_BLAST_DATABASE_PATH@/Current mv failed from @BLASTSERVER_FASTA_FILE_PATH@/fasta/Current/ensembl";

    }
    $ckFile ="@BLASTSERVER_FASTA_FILE_PATH@/fasta/SPTrEMBL/sptr_zf.xpd";
    if  (-e $ckFile) {	
	
	system("mv -f @WEBHOST_BLAST_DATABASE_PATH@/Current/sptr*.x* @WEBHOST_BLAST_DATABASE_PATH@/Backup/") && die "@WEBHOST_BLAST_DATABASE_PATH@/Current/sptr* delete failed for blastdbupdate.pl";
	system("mv -f @BLASTSERVER_FASTA_FILE_PATH@/fasta/SPTrEMBL/sptr*.x* @WEBHOST_BLAST_DATABASE_PATH@/Current/") && die "@WEBHOST_BLAST_DATABASE_PATH@/Current mv failed from @BLASTSERVER_FASTA_FILE_PATH@/fasta/Current/sptr";

    }

    $ckFile ="@BLASTSERVER_FASTA_FILE_PATH@/fasta/TIGR/tigr_zf.xnd";
    if  (-e $ckFile) {	
      
	system("mv -f @WEBHOST_BLAST_DATABASE_PATH@/Current/tigr_zf*.x* @WEBHOST_BLAST_DATABASE_PATH@/Backup/") && die "@WEBHOST_BLAST_DATABASE_PATH@/Current/tigr* delete failed for blastdbupdate.pl";
	system("mv -f @BLASTSERVER_FASTA_FILE_PATH@/fasta/TIGR/tigr_zf*.x* @WEBHOST_BLAST_DATABASE_PATH@/Current/") && die "@WEBHOST_BLAST_DATABASE_PATH@/Current mv failed from @BLASTSERVER_FASTA_FILE_PATH@/fasta/Current/tigr";
    }

    $ckFile ="@BLASTSERVER_FASTA_FILE_PATH@/fasta/Trace/repbase_zf.xnd";
    if  (-e $ckFile) {	
      	system("mv -f @WEBHOST_BLAST_DATABASE_PATH@/Current/repbase_zf*.x* @WEBHOST_BLAST_DATABASE_PATH@/Backup/") && die "@WEBHOST_BLAST_DATABASE_PATH@/Current/repbase* delete failed for blastdbupdate.pl";
	system("mv -f @BLASTSERVER_FASTA_FILE_PATH@/fasta/Trace/repbase_zf*.x* @WEBHOST_BLAST_DATABASE_PATH@/Current/ @WEBHOST_BLAST_DATABASE_PATH@/Backup/") && die "@WEBHOST_BLAST_DATABASE_PATH@/Current/repbase* delete failed for blastdbupdate.pl";
    }

    $ckFile ="@BLASTSERVER_FASTA_FILE_PATH@/fasta/RefSeq/refseq_zf.xnd";
    if  (-e $ckFile) {	
      	system("mv -f @WEBHOST_BLAST_DATABASE_PATH@/Current/refseq_zf*.x* @WEBHOST_BLAST_DATABASE_PATH@/Backup/") && die "@WEBHOST_BLAST_DATABASE_PATH@/Current/refseq* delete failed for blastdbupdate.pl";
	system("mv -f @BLASTSERVER_FASTA_FILE_PATH@/fasta/RefSeq/refseq_zf*.x* @WEBHOST_BLAST_DATABASE_PATH@/Current/ @WEBHOST_BLAST_DATABASE_PATH@/Backup/") && die "@WEBHOST_BLAST_DATABASE_PATH@/Current/refseq* delete failed for blastdbupdate.pl";
    }

    # change group to zfishweb for informix files.
    system("/usr/bin/chgrp -R -L zfishweb @WEBHOST_BLAST_DATABASE_PATH@/") ;
    system("/bin/chmod -R -L g+w @WEBHOST_BLAST_DATABASE_PATH@/") ;

    # this rsync will update the default environment on genomix for developers.
    system("/usr/bin/rsync -rcvuK @BLASTSERVER_BLAST_DATABASE_PATH@/Current/*.x* /zdevblast/dev_blastdb/Current/") ;

    # this rsync will update the almdb environment on genomix for almost.
    system("/usr/bin/rsync -rcvuK @BLASTSERVER_BLAST_DATABASE_PATH@/Current/*.x* /zdevblast/almdb/Current/") ;

}
