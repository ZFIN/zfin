#!/bin/sh
dbaccess -a $DBNAME /research/zunloads/projects/genePage/schema.sql

dbaccess -a $DBNAME schema.sql

echo "running GEO job about 15 minutes.  Without 10 tests will fail. "
curl http://$DOMAIN_NAME/webapp/quartz/run/org.zfin.datatransfer.microarray.MicroarrayWebserviceJob.class
echo "Don running GEO job exiting." 



