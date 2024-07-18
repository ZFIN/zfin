#!/bin/bash -e

# First make sure required environment variables are set
if [ -z "$ROOT_PATH" ]; then
  echo "Error: ROOT_PATH is not set."
  exit 1
fi
if [ -z "$DB_NAME" ]; then
  echo "Error: DB_NAME is not set."
  exit 1
fi

BASE_PATH="$ROOT_PATH/server_apps/data_transfer/Downloads/intermineData"

rm -rf $BASE_PATH/*/*.txt
rm -rf $BASE_PATH/*/*.obo
rm -rf $BASE_PATH/*/*.zfin

rm -rf "$ROOT_PATH/home/data_transfer/Downloads/intermineData/"

# Define a space-separated list of subdirectories
subdirs="disease dnaMutationDetail transcriptMutationDetail proteinMutationDetail allele eap phenoWarehouse dataDate featurePrefix mutagenMutagee cleanPhenotype ontologySubset images dataSourceSupplier featureCrossReferences people markerSequences omimPhenotype mutantAttributions chromosome lab company zfin_phenotypes zfin_orthos zfin_expression zfin_experiments zfin_markers zfin_stages zfin_genofeats zfin_genoenvs zfin_anatomy zfin_genotypes fish zfin_figures zfin_features zfin_pubs zfin_pato zfin_fmrels go-annotation go zfinSpatial zfin_kegg zfin_journals genotypePubs featurePubs feature_alias chromosome identifiers genotype_alias"

# Loop through the list and create directories if they do not exist
for subdir in $subdirs; do
  if [ ! -d "$BASE_PATH/$subdir" ]; then
    mkdir -p "$BASE_PATH/$subdir"
  fi
done

psql -v ON_ERROR_STOP=1 -d $DB_NAME -a -f $BASE_PATH/dump.sql

#Check if the file exists first
if [ -f /research/zprod/www_homes/zfin.org/server_apps/data_transfer/GO/gene_association.zfin.gz ]; then
  cp /research/zprod/www_homes/zfin.org/server_apps/data_transfer/GO/gene_association.zfin.gz $BASE_PATH/go-annotation
  gunzip $BASE_PATH/go-annotation/gene_association.zfin.gz
else
  echo "WARNING: GO file does not exist (in /research/zprod/www_homes/zfin.org/server_apps/data_transfer/GO/gene_association.zfin.gz)"
  echo "WARNING: Does /research/zprod/ exist?"
fi

cp -r $BASE_PATH/ $ROOT_PATH/home/data_transfer/Downloads/

exit
