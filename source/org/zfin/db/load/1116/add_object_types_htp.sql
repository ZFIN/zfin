--liquibase formatted sql
--changeset sierra:add_object_types_htp.sql


insert into zdb_object_type (zobjtype_name, zobjtype_day, zobjtype_home_table,
			zobjtype_home_zdb_id_column, zobjtype_is_data, zobjtype_is_source,
			zobjtype_attribution_display_tier)
values ('HTPDSET', '2020-06-17', 'htp_dataset', 'hd_zdb_id', 't', 'f', 1);


