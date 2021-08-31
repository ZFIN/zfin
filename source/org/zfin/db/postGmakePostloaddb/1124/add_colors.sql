--liquibase formatted sql
--changeset cmpich:add_colors


-- define marker - fluorescent-protein association table
create table fluorescent_efg
(
    fe_mrkr_zdb_id   VARCHAR(100) NOT NULL REFERENCES marker (mrkr_zdb_id),
    fe_fl_protein_id bigint       NOT NULL REFERENCES fluorescent_protein (fp_pk_id)
);

insert into fluorescent_efg
select distinct mrkr_zdb_id, fp_pk_id
from marker,
     fluorescent_protein
where mrkr_type = 'EFG'
  AND lower(mrkr_name) = lower(fluorescent_protein.fp_name)
