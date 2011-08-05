#!/bin/sh
cd /research/zunloads/projects/genePage ; dbaccess -a $DBNAME schema.sql ;
cd /research/zunloads/projects/GAF ; dbaccess -a $DBNAME schema.sql ;

