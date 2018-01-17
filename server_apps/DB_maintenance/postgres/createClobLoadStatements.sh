#!/bin/bash

cd /tmp/abstracts/
for abstractFile in /tmp/abstracts/* ; do 
  fulltxt=${abstractFile%%.*}
  prefix="update publication set pub_abstract= (select bytea_import('"
  fullpath="/tmp/abstracts/";
  suffix="')) where zdb_id = "
  id="'$fulltxt';";
  echo $prefixabstractFile$suffix$id >> $SOURCEROOT/server_apps/DB_maintenance/postgres/clobLoad.sql;
done

cd /tmp/nonzf_pubs/
for nonzfPubFile in /tmp/nonzf_pubs/* ; do 
  fulltxt=${nonzfPubFile%%.*}
  prefix="update person set nonzf_pubs= (select bytea_import('"
  fullpath="/tmp/nonzf_pubs/";
  suffix="')) where zdb_id = "
  id="'$fulltxt';";
  echo $prefix$nonzfPubFile$suffix$id >> $SOURCEROOT/server_apps/DB_maintenance/postgres/clobLoad.sql;
done
