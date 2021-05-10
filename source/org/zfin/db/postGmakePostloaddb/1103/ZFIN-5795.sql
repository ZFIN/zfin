--liquibase formatted sql
--changeset pm:ZFIN-5795

--first attribute fish that dont have attributions, but their corresponding genotypes do


insert into record_attribution (recattrib_Data_zdb_id, recattrib_source_zdb_id) select fish_zdb_id, recattrib_source_zdb_id
from fish ,record_attribution
where fish_zdb_id not in (select recattrib_data_zdb_id from record_attribution)
and fish_genotype_zdb_id in (select recattrib_Data_zdb_id from record_attribution)
and fish_genotype_zdb_id=recattrib_data_zdb_id;

--then create fish records for the fish that have corresponding genotypes.

create table pre_geno (
        pregeno_geno_id varchar(50) not null,
        pregeno_display_name varchar(255) not null,
        pregeno_handle varchar(255) not null,
        pregeno_nick_name varchar(255) not null,
        pregeno_wildtype boolean, pregeno_complexity_order bigint
);

-- load pre_geno table for those features newly added by this script and not having affected gene
insert into pre_geno (
        pregeno_geno_id,
        pregeno_display_name,
        pregeno_handle,
        pregeno_nick_name,pregeno_wildtype,pregeno_complexity_order
)
select geno_zdb_id, geno_display_name, geno_handle, geno_nickname,geno_is_wildtype,geno_complexity_order from genotype
where geno_zdb_id not in (select fish_genotype_zdb_id from fish);



alter table pre_geno add pregeno_fish_id varchar(50);

update pre_geno set pregeno_fish_id = get_id('FISH');



insert into zdb_active_data select pregeno_fish_id from pre_geno;


insert into fish (
    fish_zdb_id,
    fish_genotype_zdb_id,
    fish_name,
    fish_handle,fish_is_wildtype,fish_order,fish_name_order,fish_functional_affected_gene_count
)
select  pregeno_fish_id,
        pregeno_geno_id,
        pregeno_display_name,
        pregeno_handle,pregeno_wildtype,pregeno_complexity_order,pregeno_display_name,0
 from pre_geno;

insert into record_attribution (
    recattrib_data_zdb_id,
    recattrib_source_zdb_id
)
select  pregeno_fish_id,
        recattrib_source_zdb_id
 from pre_geno,record_Attribution where pregeno_geno_id =recattrib_data_zdb_id;


drop table pre_geno;

