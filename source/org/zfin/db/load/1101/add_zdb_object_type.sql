--liquibase formatted sql 
--changeset sierra:add_zdb_object_type.sql

insert into zdb_object_type (zobjtype_name,
       zobjtype_day,
       zobjtype_home_table,
       zobjtype_home_zdb_id_column,
       zobjtype_is_data,
       zobjtype_is_source,
       zobjtype_attribution_display_tier)
values ('VFSEQ',
        current_date,
        'variant_flanking_sequence',
        'vfseq_zdb_id',
        't',
        'f',
        2
        );
