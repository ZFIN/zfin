#!/bin/tcsh

rm -rf run.sql;

cat loadTerms.sql handleSecondaryTerms.sql handleRelationships.sql handleSynonyms.sql fixAnnotationsUponOntologyLoad.sql loadSubsets.sql dropTempTables.sql > run.sql;

/private/apps/Informix/informix/bin/dbaccess swrdb run.sql;
