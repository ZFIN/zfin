--liquibase formatted sql
--changeset cmpich:ZFIN-9444.sql

insert into foreign_db_contains_display_group_member
    (fdbcdgm_fdbcont_zdb_id, fdbcdgm_group_id, fdbcdgm_can_view, fdbcdgm_can_add, fdbcdgm_can_edit, fdbcdgm_can_delete)
values ('ZDB-FDBCONT-040412-31', 12, true, true, true, true);

create temp table id
(
    id text
);

insert into id
select get_id('BLASTDB');

insert into blast_database
(blastdb_zdb_id, blastdb_name, blastdb_abbrev, blastdb_description, blastdb_public,
 blastdb_type, blastdb_tool_display_name,
 blastdb_is_being_processed, blastdb_origination_id)
select id,
       'Published RNA Washington',
       'wz_est',
       'EST sequences curated by ZFIN staff from publications.',
       false,
       'nucleotide',
       'Published RNA',
       false,
       2
from id;

update foreign_db_contains
set fdbcont_primary_blastdb_zdb_id = (select id from id)
where fdbcont_zdb_id = 'ZDB-FDBCONT-040412-31';

update foreign_db
set fdb_db_query = '/action/blast/display-sequence?accession='
where fdb_db_name = 'WashUZ';

update db_link
set dblink_acc_num = db_link.dblink_acc_num_display
where dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-31';

update db_link
set dblink_acc_num = db_link.dblink_acc_num_display,
    dblink_info = dblink_info || ', added prefix wz to accession '||CURRENT_DATE
where dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-31';
