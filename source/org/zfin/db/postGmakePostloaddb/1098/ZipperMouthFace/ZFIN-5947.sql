--liquibase formatted sql
--changeset pm:ZFIN-5947

create table pre_geno (
        pregeno_geno_id varchar(50) not null,
        pregeno_display_name varchar(255) not null,
        pregeno_handle varchar(255) not null,
        pregeno_nick_name varchar(255) not null,
        pregeno_wildtype boolean
);

-- load pre_geno table for those features newly added by this script and not having affected gene
insert into pre_geno (
        pregeno_geno_id,
        pregeno_display_name,
        pregeno_handle,
        pregeno_nick_name,pregeno_wildtype
)
values('x',
       'Cooch Behar' ,
       'CB'  ,
       'CB', 't');

create table pre_extnote (
        extnote_id varchar(50) not null,
        extnote_data_id varchar(255) not null,
        extnote_note varchar(255) not null,
        extnote_notetype varchar(255) not null,
        extnote_source_id varchar(50)
);

insert into pre_extnote (
       extnote_id ,
        extnote_data_id,
        extnote_note,
        extnote_notetype ,
        extnote_source_id
)
values('x',
       'y' ,
       ' This stock has ZZ (male) and ZW (female) sex chromosomes. No ZZ individual ever makes a female, but some ZW fish do become fertile phenotypic males. The mating of a ZW female to a sex-reversed phenotypic ZW male can produce WW fish, most of which are female, but some of which are male. Thus, the stock needs to be maintained in large enough numbers to ensure that the Z chromosome is maintained because the mating of a WW phenotypic female to a WW pheotypic male would result in offspring with no Z chromosome.

 From a fish dealer from India, who obtained the fish near Cooch Behar, India'  ,
       'genotype', 'ZDB-PUB-140923-28');

update pre_extnote set extnote_id=get_id('EXTNOTE');

update pre_geno set pregeno_geno_id = get_id('GENO');

alter table pre_geno add pregeno_fish_id varchar(50);

update pre_geno set pregeno_fish_id = get_id('FISH');
update pre_extnote set extnote_data_id=(select pregeno_geno_id from pre_geno);

insert into zdb_active_data select pregeno_geno_id from pre_geno;
insert into zdb_active_data select pregeno_fish_id from pre_geno;
insert into zdb_active_data select extnote_id from pre_extnote;
insert into genotype (
    geno_zdb_id,
    geno_display_name,
    geno_handle,
    geno_nickname,geno_is_wildtype
)
select  pregeno_geno_id,
        pregeno_display_name,
        pregeno_handle,
        pregeno_nick_name,pregeno_wildtype
 from pre_geno;

insert into external_note select * from pre_extnote;
insert into record_attribution (
    recattrib_data_zdb_id,
    recattrib_source_zdb_id
)
select  pregeno_geno_id,
        'ZDB-PUB-140923-28'
from pre_geno;

insert into fish (
    fish_zdb_id,
    fish_genotype_zdb_id,
    fish_name,
    fish_handle,fish_is_wildtype
)
select  pregeno_fish_id,
        pregeno_geno_id,
        pregeno_display_name,
        pregeno_handle,pregeno_wildtype
 from pre_geno;

insert into record_attribution (
    recattrib_data_zdb_id,
    recattrib_source_zdb_id
)
select  pregeno_fish_id,
        'ZDB-PUB-140923-28'
 from pre_geno;

insert into int_data_supplier(idsup_data_zdb_id,idsup_supplier_zdb_id,idsup_acc_num)  select pregeno_geno_id,'ZDB-LAB-991005-53',pregeno_geno_id from pre_geno;
drop table pre_geno;
drop table pre_extnote;

