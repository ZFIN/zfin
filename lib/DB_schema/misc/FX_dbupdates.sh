#!/bin/sh

# Run this script after loading and postloading for all the freshest FX goodnes

echo `/bin/date` '--- get_max_labels.sql ---';
dbaccess $DBNAME get_max_labels.sql;

echo `/bin/date` '--- FX_version_7.sql ---';
dbaccess $DBNAME FX_version_7.sql;

echo `/bin/date` '--- run xpat_blowout.sql just yet.. --- ';
dbaccess $DBNAME xpat_blowout.sql ;