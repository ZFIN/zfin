--liquibase formatted sql
--changeset pm:DLOAD-557


create table pre_cne (
        precne_cne_name varchar(255),
        precne_cne_chr text,
        precne_cne_abbrev varchar(70)
     
);

create temp table pre_cne_chr (
cne_chr varchar(5),
cne_id varchar(50));


insert into pre_cne (
      precne_cne_name,
     precne_cne_chr,
      precne_cne_abbrev
        )
  select distinct
                  'nc.'||cneid,
                  zfishchr,
                  'conserved non-coding element '||cnename

    from cnedata ;


alter table pre_cne add precne_cne_zdb_id varchar(50);

update pre_cne set precne_cne_zdb_id = get_id('CNE');

insert into zdb_active_data select precne_cne_zdb_id from pre_cne;
insert into marker (
    mrkr_zdb_id,
    mrkr_name,
    mrkr_abbrev,
    mrkr_owner
)
select  precne_cne_zdb_id,
        precne_cne_name,
        precne_cne_abbrev,
        'ZDB-PERS-981201-7'

 from pre_cne;

 insert into pre_cne_chr(cne_chr,cne_id) select precne_cne_chr,precne_cne_zdb_id from pre_cne;
 alter table pre_cne_chr add precne_sfcl_zdb_id varchar(50);
 update pre_cne_chr set precne_sfcl_zdb_id=get_id('SFCL');
insert into zdb_active_data select precne_sfcl_zdb_id from pre_cne_chr;


insert into record_attribution (
    recattrib_data_zdb_id,
    recattrib_source_zdb_id
)
select  precne_cne_zdb_id,
        'ZDB-PUB-170214-158'
 from pre_cne;

insert into sequence_feature_chromosome_location (sfcl_Zdb_id, sfcl_feature_Zdb_id, sfcl_chromosome) select precne_sfcl_zdb_id,cne_id,cne_chr from  pre_cne_chr;


drop table pre_cne;
drop table pre_cne_chr;


