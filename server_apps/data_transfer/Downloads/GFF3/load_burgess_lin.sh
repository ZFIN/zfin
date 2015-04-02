#!/bin/sh

GLOBALSTORE="/research/zprodmore/gff3"
TARGETDIR="$TARGETROOT/server_apps/data_transfer/Downloads/GFF3"

cp $GLOBALSTORE/Burgess_Lin.unl $TARGETDIR
cd $TARGETDIR && cat load_BL_gff.sql commit.sql | dbaccess -a $DBNAME
