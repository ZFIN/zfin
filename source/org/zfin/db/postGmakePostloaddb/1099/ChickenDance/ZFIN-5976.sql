--liquibase formatted sql
--changeset xshao:ZFIN-5976

delete from record_attribution where recattrib_pk_id = '725138';

update expression_experiment2 
                                set xpatex_atb_zdb_id = 'ZDB-ATB-090413-1'
                              where xpatex_atb_zdb_id = 'ZDB-ATB-100430-8';

delete from data_alias where dalias_zdb_id = 'ZDB-DALIAS-100430-34';

delete from data_alias where dalias_zdb_id = 'ZDB-DALIAS-100430-36';

delete from int_data_supplier where idsup_data_zdb_id = 'ZDB-ATB-100430-8' and idsup_supplier_zdb_id = 'ZDB-COMPANY-080225-1';

update data_alias 
                                set dalias_data_zdb_id = 'ZDB-ATB-090413-1'
                              where dalias_data_zdb_id = 'ZDB-ATB-100430-8';

update int_data_supplier 
                                set idsup_data_zdb_id = 'ZDB-ATB-090413-1'
                              where idsup_data_zdb_id = 'ZDB-ATB-100430-8';


update record_attribution set recattrib_data_zdb_id = 'ZDB-ATB-090413-1' where recattrib_data_zdb_id = 'ZDB-ATB-100430-8';

delete from zdb_replaced_data where zrepld_old_zdb_id = 'ZDB-ATB-100430-8';

update zdb_replaced_data set zrepld_new_zdb_id = 'ZDB-ATB-090413-1' where zrepld_new_zdb_id = 'ZDB-ATB-100430-8';

delete from zdb_active_data where zactvd_zdb_id = 'ZDB-ATB-100430-8';

insert into zdb_replaced_data (zrepld_old_zdb_id, zrepld_new_zdb_id) values ('ZDB-ATB-100430-8', 'ZDB-ATB-090413-1');

create temp table temp_alias_id (
dalias_id varchar(50)
);

insert into temp_alias_id
select get_id('DALIAS') from single;


insert into zdb_active_data select dalias_id from temp_alias_id;

insert into data_alias (dalias_zdb_id, dalias_data_zdb_id, dalias_alias, dalias_group_id)
                            select dalias_id, 'ZDB-ATB-090413-1', 'Ab2-tpm', '1'
                              from temp_alias_id;

