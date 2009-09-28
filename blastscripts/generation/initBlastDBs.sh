#!env bash
export BLAST_PATH=/research/zusers/ndunn/yoyoblastfiles ;
export BLAST_BINARY=/private/apps/wublast

# This script regenerates blast databases.
# All blast dbs are loads unless otherwise noted.  
#
# have to handle prerequisites first

downloadGenbank.sh ; 
convertGenbankSequenceToFasta.sh ; 

# assemble
assembleGenbank.sh ; 

# mv from working to target
createGenbankBlastDBsFromFasta.sh ; 


# ---------------------------
# NUCLEOTIDE BLAST DATABASES:
# ---------------------------
# vega_zfin (not done here) 

# 	(from embryonix/ZfinGbAcc and genomix weeklyZfinSeq_cDNASeq and blastdbupdate.pl)
# zfin_cdna
# requies gbk_zf_all.fa
createzfin_cdna.sh ;
# ZFINGenesWithExpression
createZFINGenesWithExpression.sh  ;

# should be inluded above?
#
# (NEEDS ACCESSIONS)
# GenomicDNA 

#
#
# NO LOCAL PROCESSING
# LoadedMicroRNAMature - insertions based on script
# LoadedMicroRNAStemLoop - insertions based on script
# zfin_mrph - MarkerSequence
# gbk_zf_dna_new 
#
# THESE START OUT EMPTY or with current accessions, so nothing to do
	# unreleasedRNA () - curated 
	# CuratedMicroRNAMature - curated
	# CuratedMicroRNAStemLoop - curated

# FILE ONLY LOADS
# ensembl_zf
# tigr_zf
# gbk_est_zf
# refseq_zf_rna
# gbk_htg_zf
# gbk_gss_zf
# wgs_zf

# Non-generated loads:
# repbase_zf
# gbk_hs_mrna
# gbk_est_hs
# gbk_hs_dna
# gbk_ms_mrna
# gbk_est_ms
# gbk_ms_dna


# ------------------------
# PROTEIN BLAST DATABASES:
# ------------------------
# publishedProtein - curated  (from tom intially)
# unreleasedProtein - curated (starts out empty)

# FILE ONLY LOADS
# refseq_zf_aa
# sptr_zf

# Non-generated loads:
# sptr_hs
# sptr_ms

