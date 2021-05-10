#!/bin/tcsh

source /private/ZfinLinks/Commons/env/${INSTANCE}.env
dbaccess ${DBNAME} ${TARGETROOT}/server_apps/Reports/BetterFish/betterFish.sql ;
