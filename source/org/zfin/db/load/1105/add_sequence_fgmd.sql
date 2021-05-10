--liquibase formatted sql
--changeset sierra:add_sequence_fgmd.sql

create sequence fgmd_seq start 1 ;


insert into zdb_object_type (zobjtype_name,
       zobjtype_day,
       zobjtype_home_table,
       zobjtype_home_zdb_id_column,
       zobjtype_is_data,
       zobjtype_is_source,
       zobjtype_attribution_display_tier)
values ('FGMD',
        current_date,
        'feature_genomic_mutation_detail',
        'fgmd_zdb_id',
        't',
        'f',
        2
        );

