#!/bin/sh

GLOBALSTORE="/research/zprodmore/gff3"
TARGETDIR="$TARGETROOT/server_apps/data_transfer/Downloads/GFF3"

cp $GLOBALSTORE/zmp.unl $TARGETDIR
cd $TARGETDIR && cat load_ZMP.sql commit.sql | dbaccess -a $DBNAME
