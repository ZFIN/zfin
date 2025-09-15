UNLOAD NOTES
===

These notes provide steps you can take after restoring a DB unload to trim down the DB to just the bare minimum tables and functions
needed to run the ncbi load.


Capture Current DB State to Dump Files
---

### Make sure DB is Run and reachable by hostname 'db'
### You may also want to set up a private network between db and temporary compile container like:
docker network create --internal zfin_isolated
docker network connect zfin_isolated zfin_org-db-1
cd $SOURCEROOT/server_apps
docker run -u root --rm -it --network zfin_isolated -v `pwd`:/opt/server_apps   --add-host db:172.19.0.2  zfin_org-compile bash

### Run command to capture current state of DB
### Only includes the tables that are necessary for ncbi load
pg_dump -h db -Fc zfindb -t accession_bank -t accession_bank_accbk_pk_id_seq -t foreign_db_data_type -t marker -t expression_experiment2 -t foreign_db_contains -t foreign_db -t db_link -t marker_relationship -t record_attribution -t reference_protein -t zdb_active_data -t zdb_object_type -t dblink_seq -t marker_types -t marker_type_group_member -t alias_group -t foreign_db_contains_display_group -t foreign_db_contains_display_group_member -t marker_relationship_type -t marker_annotation_status -t assembly -t marker_assembly -t data_alias -t antibody -t clone -t marker_sequence -t snp_sequence -t transcript -t publication -t marker_history_audit -f ./input/slimdb.bak

### These tables aren't directly needed (the perl load didn't use them). However, java code loads them via hibernate relationships
###    -t data_alias -t antibody -t clone -t marker_sequence -t snp_sequence -t transcript -t publication -t marker_history_audit 

### Run command to capture functions
### This command will dump a set of required functions
psql -U postgres -h db zfindb -o ./input/functions.sql -A -t -c "SELECT pg_get_functiondef(f.oid) || ';' FROM pg_catalog.pg_proc f WHERE proname in ('get_id', 'get_id_and_insert_active_data','db_link', 'expression_experiment2', 'marker_abbrev_after_update', 'marker_abbrev_insert', 'marker_abbrev_update', 'marker_audit_insert', 'marker_audit_update', 'marker_name_order', 'marker_name', 'marker_relationship', 'marker', 'record_attribution_sync_modified_at', 'record_attribution', 'zdb_object_type', 'p_check_zdb_object_table', 'scrub_char', 'get_dblink_acc_num_display', 'p_dblink_has_parent', 'p_check_caps_acc_num', 'checkDblinkTranscriptWithdrawn', 'checkdblinktranscriptwithdrawn', 'get_genbank_dblink_length_type', 'get_obj_type', 'mhist_event');"


Restore Captured DB to New Database
---

### Run command to create the before state of the database
psql -h db -U postgres -c "drop database ncbi WITH (FORCE)"
psql -h db -U postgres -c "create database ncbi"

### These commands will produce errors due to being a partial DB load. We will ignore for now.
psql -h db -U postgres -d ncbi -f ./input/functions.sql
pg_restore -U postgres -h db -d ncbi -c ./input/slimdb.bak

### Run them again due to chicken/egg issues:
psql -h db -U postgres -d ncbi -f ./input/functions.sql
pg_restore -U postgres -h db -d ncbi -c ./input/slimdb.bak


(Optional) Capture a before state in its own schema
---

### Populating temp DB
psql -h db -d ncbi <<EOF
create schema before;

create table before.marker (LIKE public.marker INCLUDING ALL);
create table before.expression_experiment2 (LIKE public.expression_experiment2 INCLUDING ALL);
create table before.foreign_db_contains (LIKE public.foreign_db_contains INCLUDING ALL);
create table before.foreign_db (LIKE public.foreign_db INCLUDING ALL);
create table before.db_link1 (LIKE public.db_link INCLUDING ALL);
create table before.marker_relationship (LIKE public.marker_relationship INCLUDING ALL);
create table before.record_attribution (LIKE public.record_attribution INCLUDING ALL);
create table before.reference_protein (LIKE public.reference_protein INCLUDING ALL);
create table before.zdb_active_data (LIKE public.zdb_active_data INCLUDING ALL);
create table before.zdb_object_type (LIKE public.zdb_object_type INCLUDING ALL);

insert into before.marker select * from  public.marker;
insert into before.expression_experiment2 select * from  public.expression_experiment2;
insert into before.foreign_db_contains select * from  public.foreign_db_contains;
insert into before.foreign_db select * from  public.foreign_db;
insert into before.db_link1 select * from  public.db_link;
insert into before.marker_relationship select * from  public.marker_relationship;
insert into before.record_attribution select * from  public.record_attribution;
insert into before.reference_protein select * from  public.reference_protein;
insert into before.zdb_active_data select * from  public.zdb_active_data;
insert into before.zdb_object_type select * from  public.zdb_object_type;
EOF


Capture the Newly Created Minimal DB to File
---

pg_dump -Fc ncbi -f minimal-db.bak


Last steps
---
Now you have a db snapshot that can be loaded as the base state of the DB to run the ncbi load against
