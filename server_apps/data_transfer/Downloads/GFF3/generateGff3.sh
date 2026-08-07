#!/bin/sh

psql -v ON_ERROR_STOP=1 -d $DBNAME -f E_zfin_ensembl_gene.sql -f E_expression_gff3.sql -f E_phenotype_gff3.sql -f E_antibody_gff3.sql -f unload_mutants.sql -f zfin_zmp_gff3.sql

./generateGff3.groovy

cd $SOURCEROOT
gradle createGff3Files

gzip -f zfin_genes.grcz12.gff3
gzip -f zfin_refseq.grcz12.gff3
# mv, not cp: createGff3Files is a JavaExec task, so it writes both gff3 files into
# $SOURCEROOT (we cd'd there above and never came back). Copying left the gzipped
# results behind as untracked files in the git checkout after every run.
mv zfin_genes*.gff3.* /opt/zfin/www_homes/zfin.org/home/data_transfer/Downloads/
mv zfin_ref*.gff3.* /opt/zfin/www_homes/zfin.org/home/data_transfer/Downloads/