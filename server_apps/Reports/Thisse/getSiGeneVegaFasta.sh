#!/bin/tcsh
# FILE: getSiGeneVegaFasta.sh
#
# Generate fasta file from vega acc# of 'si:' genes that do no have Thisse 
# expression, and no EST, cDNA, no GenBank cDNA and RefSeq cDNA 
# sequence. The report would be generated for Thisse twice a year on demand.
#
# INPUT: none
# 
# OUTPUT: email to Ceri a fasta file of si: gene vega sequence 
#

setenv INFORMIXDIR <!--|INFORMIX_DIR|-->
setenv INFORMIXSERVER <!--|INFORMIX_SERVER|-->
setenv ONCONFIG <!--|ONCONFIG_FILE|-->
setenv INFORMIXSQLHOSTS <!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->

chdir <!--|ROOT_PATH|-->/server_apps/Reports/Thisse

#-------------------------------------
# Generate the accession number list
#-------------------------------------

$INFORMIXDIR/bin/dbaccess <!--|DB_NAME|--> siGene_wo_xpat.sql > siGeneVegaAcc.unl

/bin/grep ^OTTDART siGeneVegaAcc.unl > siGeneVegaAcc.txt

#-------------------------------------
# Get the fasta file
#-------------------------------------

/private/apps/wublast/xdget -n -f /research/zblastdb/db/wu-db/vega_zfin siGeneVegaAcc.txt > siGeneVega.fa

#-------------------------------------
# Email the result to curator
#-------------------------------------

set SUBJECT = "Auto: si: gene w/o Thisse xpat fasta file"
set MAILTO = "van_slyke@uoneuro.uoregon.edu";

echo "From: $LOGNAME" > /tmp/Thisse_si_gene_mail
echo "To: $MAILTO" >> /tmp/Thisse_si_gene_mail
echo "Subject: $SUBJECT" >> /tmp/Thisse_si_gene_mail
echo "Mime-Version: 1.0" >> /tmp/Thisse_si_gene_mail
echo "Content-Type: text/plain" >> /tmp/Thisse_si_gene_mail

echo "si: genes w/o Thisse xpat:" >> /tmp/Thisse_si_gene_mail
cat ./siGeneVega.fa >>  /tmp/Thisse_si_gene_mail


/usr/lib/sendmail -t -oi < /tmp/Thisse_si_gene_mail

/bin/rm -f /tmp/Thisse_si_gene_mail

exit;

