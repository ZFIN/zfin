#!/private/bin/perl -w
# GenerateZFINGFF3.pl
#-----------------------------------------------------------------------
# Runs script to create GFF3 files for public download.
# and internal gbrowse use
#
# We query ZFIN for:
# Vega Assembly clone lengths
# ZFIN Genes related to Vega transcripts & their Alias'
#
# Subset of genes with Vega transcripts that have:
#   Expression Pattern
#   Phenotype images
#   Antibodies
# We generate locations for Morpholinos
#

# Note: Once we fully switched to Postgres, modify the Jenkins job to call the groovy script directly.
# this perl script is just created so we can easily swithc between Informix and Postgres for Jenkins jobs
# by renaming files.
chdir "download-files";
my $cmd = "./generateGff3.groovy ";
system($cmd);
