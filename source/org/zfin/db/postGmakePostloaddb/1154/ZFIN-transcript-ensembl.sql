--liquibase formatted sql
--changeset cmpich:ZFIN-transcript-ensembl.sql

delete
from transcript
where tscript_mrkr_zdb_id = 'ZDB-TSCRIPT-240307-1';
delete
from marker
where mrkr_zdb_id = 'ZDB-TSCRIPT-240307-1';
delete
from marker_relationship
where mrel_mrkr_2_zdb_id = 'ZDB-TSCRIPT-240307-1';
delete
from db_link
where dblink_linked_recid = 'ZDB-TSCRIPT-240307-1';
delete
from foreign_db_contains_display_group_member
where fdbcdgm_fdbcont_zdb_id = 'ZDB-FDBCONT-240304-1';
delete
from foreign_db_contains
where fdbcont_zdb_id = 'ZDB-FDBCONT-240304-1';


-- add ensemb_trans to Nucleotide_sequence display group
insert into foreign_db_contains (fdbcont_organism_common_name, fdbcont_zdb_id, fdbcont_fdbdt_id, fdbcont_fdb_db_id, fdbcont_primary_blastdb_zdb_id)
values ('Zebrafish', 'ZDB-FDBCONT-240304-1', 3, 61, 'ZDB-BLASTDB-130708-1');

alter table transcript
    add column tscript_genotype_zdb_id text;

alter table transcript
    add constraint transcript_genotype_foreign_key
        Foreign key (tscript_genotype_zdb_id)
            references genotype (geno_zdb_id);


insert into foreign_db_contains_display_group_member (fdbcdgm_fdbcont_zdb_id, fdbcdgm_group_id)
VALUES ('ZDB-FDBCONT-240304-1', 9),
       ('ZDB-FDBCONT-240304-1', 12);

insert into int_fdbcont_analysis_tool (ifat_fdbcont_zdb_id, ifat_blastdb_zdb_id)
VALUES ('ZDB-FDBCONT-240304-1', 'ZDB-BLASTDB-090929-27');

create table vocabulary
(
    v_id           serial not null,
    v_name         text   not null,
    v_description  text   not null,
    v_date_created DATE   NOT NULL DEFAULT CURRENT_DATE
);

create table vocabulary_term
(
    vt_id           serial not null,
    vt_name         text   not null,
    vt_v_id         integer,
    vt_date_created DATE   NOT NULL DEFAULT CURRENT_DATE
);

insert into vocabulary (v_name, v_description)
VALUES ('transcript annotation method', 'All allowed values for transcript annotation method');

insert into vocabulary_term (vt_name, vt_v_id)
VALUES ('Havana', (select v_id from vocabulary));
insert into vocabulary_term (vt_name, vt_v_id)
VALUES ('Ensembl', (select v_id from vocabulary));

alter table transcript
    add column tscript_vocab_id integer;

ALTER TABLE vocabulary_term
    ADD CONSTRAINT vocabulary_term_pk PRIMARY KEY (vt_id);

alter table transcript
    add constraint transcript_vocab_foreign_key
        Foreign key (tscript_vocab_id)
            references vocabulary_term (vt_id);

update transcript
set tscript_vocab_id = 1
where tscript_load_id not like 'ZDB-TSCRIPT%';



drop table if exists transcript_strain_raw;
select geno_zdb_Id, geno_display_name, tscript_mrkr_zdb_id, clone_mrkr_zdb_id
into temp table transcript_strain_raw
from genotype as g,
     probe_library,
     clone,
     marker_relationship,
     transcript
where probelib_strain_zdb_id = g.geno_zdb_id
  AND clone_probelib_zdb_id = probelib_zdb_id
  AND mrel_mrkr_1_zdb_id = clone_mrkr_zdb_id
  AND mrel_mrkr_2_zdb_id = tscript_mrkr_zdb_id
  and mrel_type = 'clone contains transcript';

drop table if exists transcript_strain;
select tscript_mrkr_zdb_id, geno_zdb_Id, geno_display_name, count(*) as number_of_clones
into temp table transcript_strain
from transcript_strain_raw
group by tscript_mrkr_zdb_id, geno_zdb_Id, geno_display_name
order by number_of_clones desc;

update transcript as t
set tscript_genotype_zdb_id = subquery.geno_zdb_id
from (select geno_zdb_id, tscript_mrkr_zdb_id from transcript_strain) as subquery
where t.tscript_mrkr_zdb_id = subquery.tscript_mrkr_zdb_id;
