#!/bin/bash

rm -rf $SOURCEROOT/server_apps/data_transfer/DAF/*.unl
rm -rf $SOURCEROOT/server_apps/data_transfer/DAF/disease_association_file.zfin.txt

dbaccess -a $DBNAME $SOURCEROOT/server_apps/data_transfer/DAF/createDAF.sql

cat $SOURCEROOT/server_apps/data_transfer/DAF/dafHeader.txt $SOURCEROOT/server_apps/data_transfer/DAF/daf.unl > $SOURCEROOT/server_apps/data_transfer/DAF/disease_association_file.zfin.txt

