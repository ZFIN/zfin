#!/bin/bash -e

rm -rf <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/*/*.txt
rm -rf <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/*/*.obo
rm -rf <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/*/*.zfin

rm -rf <!--|ROOT_PATH|-->/home/data_transfer/Downloads/intermineData/

if [ ! -d <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/disease ]; then
  mkdir <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/disease/
fi;

if [ ! -d <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/dataDate ]; then
  mkdir <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/dataDate/
fi;

if [ ! -d <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/featurePrefix ]; then
  mkdir <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/featurePrefix/
fi;

if [ ! -d <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/mutagenMutagee ]; then
  mkdir <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/mutagenMutagee/
fi;

if [ ! -d <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/cleanPhenotype ]; then
  mkdir <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/cleanPhenotype/
fi;

if [ ! -d <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/ontologySubset ]; then
  mkdir <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/ontologySubset/
fi;

if [ ! -d <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/images ]; then
  mkdir <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/images/
fi;

if [ ! -d <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/dataSourceSupplier ]; then
  mkdir <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/dataSourceSupplier/
fi;

if [ ! -d <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/featureCrossReferences ]; then
  mkdir <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/featureCrossReferences/
fi;

if [ ! -d <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/people ]; then
  mkdir <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/people/
fi;

if [ ! -d <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/markerSequences ]; then
  mkdir <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/markerSequences
fi;


if [ ! -d <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/omimPhenotype ]; then
  mkdir <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/omimPhenotype
fi;

if [ ! -d <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/mutantAttributions ]; then
  mkdir <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/mutantAttributions
fi;

if [ ! -d <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/chromosome ]; then
  mkdir <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/chromosome
fi;

if [ ! -d <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/lab ]; then
  mkdir <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/lab
fi;

if [ ! -d <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/company ]; then
  mkdir <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/company
fi;

if [ ! -d <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfin_phenotypes ]; then
  mkdir <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfin_phenotypes
fi;

if [ ! -d <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfin_orthos ]; then
  mkdir <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfin_orthos
fi;

if [ ! -d <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfin_expression ]; then
  mkdir <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfin_expression
fi;

if [ ! -d <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfin_experiments ]; then
  mkdir <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfin_experiments
fi;

if [ ! -d <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfin_markers ]; then
  mkdir <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfin_markers
fi;

if [ ! -d <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfin_stages ]; then
  mkdir <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfin_stages
fi;

if [ ! -d <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfin_genofeats ]; then
  mkdir <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfin_genofeats
fi;

if [ ! -d <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfin_genoenvs ]; then
  mkdir <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfin_genoenvs
fi;

if [ ! -d <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfin_anatomy ]; then
  mkdir <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfin_anatomy
fi;

if [ ! -d <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfin_genotypes ]; then
  mkdir <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfin_genotypes
fi;

if [ ! -d <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/fish ]; then
  mkdir <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/fish
fi;

if [ ! -d <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfin_figures ]; then
  mkdir <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfin_figures
fi;

if [ ! -d <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfin_features ]; then
  mkdir <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfin_features
fi;

if [ ! -d <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfin_pubs ]; then
  mkdir <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfin_pubs
fi;

if [ ! -d <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfin_pato ]; then
  mkdir <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfin_pato
fi;

if [ ! -d <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfin_fmrels ]; then
  mkdir <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfin_fmrels
fi;

if [ ! -d <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/go-annotation ]; then
  mkdir <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/go-annotation
fi;

if [ ! -d <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/go ]; then
  mkdir <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/go
fi;

if [ ! -d <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfinSpatial ]; then
  mkdir <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfinSpatial
fi;

if [ ! -d <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfin_kegg ]; then
  mkdir <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfin_kegg
fi;

if [ ! -d <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfin_journals ]; then
  mkdir <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfin_journals
fi;

if [ ! -d <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/genotypePubs ]; then
  mkdir <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/genotypePubs
fi

if [ ! -d <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/featurePubs ]; then
  mkdir <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/featurePubs
fi;

if [ ! -d <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/feature_alias ]; then
  mkdir <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/feature_alias
fi;

if [ ! -d <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/chromosome ]; then
  mkdir <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/chromosome
fi;

if [ ! -d <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/identifiers ]; then
  mkdir <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/identifiers
fi;

if [ ! -d <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/genotype_alias ]; then
  mkdir <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/genotype_alias
fi;

export INFORMIXSQLHOSTS=<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->
export INFORMIX_SERVER=<!--|INFORMIX_SERVER|-->
export INFORMIXDIR=<!--|INFORMIX_DIR|-->
export INFORMIXSERVER=<!--|INFORMIX_SERVER|-->
export LD_LIBRARY_PATH=/private/apps/Informix/informix/lib:/private/apps/Informix/informix/lib/esql
export PATH=/private/apps/Informix/informix/bin:/private/ZfinLinks/Commons/bin:/private/apps/wublast:/local/apps/netpbm/bin:/local/bin:/home/zusers/staylor/bin:/local/apps/Lang/SUNWspro/bin:/local/apps/java/bin:/bin:/usr/bin:/cs/bin:/local/apps/X11/bin:/usr/bin/X11:/local/apps/mh/bin:/local/apps/tex/bin:/etc:/usr/etc:/usr/dt/bin:/usr/openwin/bin:/usr/ccs/bin:/sbin:/usr/sbin:/usr/sfw/bin:.

$INFORMIXDIR/bin/dbaccess -a <!--|DB_NAME|--> <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/dump.sql 

#cp /research/zprod/catalina_bases/zfin.org/temp/quality.obo <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfin_pato/

#cp /research/zprod/catalina_bases/zfin.org/temp/anatomy.obo <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfin_anatomy/

#mv <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfin_anatomy/anatomy.obo <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfin_anatomy/zebrafish_anatomy.obo 

#cp /research/zprod/catalina_bases/zfin.org/temp/gene-ontology.obo <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/go/

#mv <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/go/gene-ontology.obo <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/go/gene_ontology.obo

#cp /research/zprod/catalina_bases/zfin.org/temp/spatial.obo <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/zfinSpatial/

cp /research/zprod/www_homes/zfin.org/server_apps/data_transfer/GO/gene_association.zfin.gz <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/go-annotation
gunzip <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/go-annotation/gene_association.zfin.gz

cp -r <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/ <!--|ROOT_PATH|-->/home/data_transfer/Downloads/

exit
