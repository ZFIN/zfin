#!/bin/bash

cd /tmp/abstracts/
for abstractFile in /tmp/abstracts/* ; do 
  fulltxt=${abstractFile%.*}
  filename=$(basename $fulltxt)
  prefix="update publication set pub_abstract= (select bytea_import('"
  fullpath="/tmp/abstracts/";
  suffix="')) where zdb_id = "
  id="'$filename';";
  echo $prefix$abstractFile$suffix$id >> $SOURCEROOT/server_apps/DB_maintenance/postgres/clobLoad.sql;
done

cd /tmp/nonzf_pubs/
for nonzfPubFile in /tmp/nonzf_pubs/* ; do 
  fulltxt=${nonzfPubFile%%.*}
  filename=$(basename $fulltxt)
  prefix="update person set nonzf_pubs= (select bytea_import('"
  fullpath="/tmp/nonzf_pubs/";
  suffix="')) where zdb_id = "
  id="'$filename';";
  echo $prefix$nonzfPubFile$suffix$id >> $SOURCEROOT/server_apps/DB_maintenance/postgres/clobLoad.sql;
done
