--liquibase formatted sql
--changeset christian:ZFIN-7866

-- setup
create temp table tmp_cmrel
(
    relid   varchar(50),
    consid  varchar(50),
    mrkrid  varchar(50),
    reltype varchar(50)
);

create temp table tmp_mrel
(
    relid   varchar(50),
    consid  varchar(50),
    mrkrid  varchar(50),
    reltype varchar(50)
);



-- First construct edit
update construct
set construct_name='Tg(actb2:APC,cryaa:mCherry)'
where construct_zdb_id = 'ZDB-TGCONSTRCT-160222-5';
update marker
set mrkr_name='Tg(actb2:APC,cryaa:mCherry)'
where mrkr_zdb_id = 'ZDB-TGCONSTRCT-160222-5';
update marker
set mrkr_abbrev='Tg(actb2:APC,cryaa:mCherry)'
where mrkr_zdb_id = 'ZDB-TGCONSTRCT-160222-5';

-- remove last parenthesis
delete
from construct_component
where cc_pk_id in
      (select cc_pk_id
       from construct_component
       where cc_construct_Zdb_id = 'ZDB-TGCONSTRCT-160222-5'
       order by cc_order desc
       limit 1);

insert into construct_component (cc_construct_Zdb_id, cc_Component_type, cc_component_category, cc_component_zdb_id,
                                 cc_component, cc_cassette_number, cc_order)
values ('ZDB-TGCONSTRCT-160222-5', 'controlled vocab component', 'promoter component', 'ZDB-CV-150506-11', ',', 2, 6);

insert into construct_component (cc_construct_Zdb_id, cc_Component_type, cc_component_category, cc_component_zdb_id,
                                 cc_component, cc_cassette_number, cc_order)
values ('ZDB-TGCONSTRCT-160222-5', 'promoter of', 'promoter component', 'ZDB-GENE-020508-1', 'cryaa', 2, 7);
insert into construct_component (cc_construct_Zdb_id, cc_Component_type, cc_component_category, cc_component_zdb_id,
                                 cc_component, cc_cassette_number, cc_order)
values ('ZDB-TGCONSTRCT-160222-5', 'controlled vocab component', 'promoter component', 'ZDB-CV-150506-10', ':', 2, 8);
insert into construct_component (cc_construct_Zdb_id, cc_Component_type, cc_component_category, cc_component_zdb_id,
                                 cc_component, cc_cassette_number, cc_order)
values ('ZDB-TGCONSTRCT-160222-5', 'coding sequence of', 'coding component', 'ZDB-EFG-080214-1', 'mCherry', 2, 9);
-- add last parenthesis
insert into construct_component (cc_construct_Zdb_id, cc_Component_type, cc_component_category, cc_component_zdb_id,
                                 cc_component, cc_cassette_number, cc_order)
values ('ZDB-TGCONSTRCT-160222-5', 'controlled vocab component', 'construct wrapper component', 'ZDB-CV-150506-8', ')',
        2, 10);


insert into tmp_cmrel(relid, consid, mrkrid, reltype)
values (get_id('CMREL'), 'ZDB-TGCONSTRCT-160222-5', 'ZDB-GENE-020508-1', 'promoter of');
insert into tmp_cmrel(relid, consid, mrkrid, reltype)
values (get_id('CMREL'), 'ZDB-TGCONSTRCT-160222-5', 'ZDB-EFG-080214-1', 'coding sequence of');

insert into zdb_active_data (zactvd_zdb_id)
select relid
from tmp_cmrel;

insert into construct_marker_relationship(conmrkrrel_zdb_id, conmrkrrel_construct_zdb_id, conmrkrrel_mrkr_zdb_id,
                                          conmrkrrel_relationship_type)
select relid, consid, mrkrid, reltype
from tmp_cmrel;

insert into tmp_mrel(relid, consid, mrkrid, reltype)
values (get_id('MREL'), 'ZDB-TGCONSTRCT-160222-5', 'ZDB-GENE-020508-1', 'promoter of');
insert into tmp_mrel(relid, consid, mrkrid, reltype)
values (get_id('MREL'), 'ZDB-TGCONSTRCT-160222-5', 'ZDB-EFG-080214-1', 'coding sequence of');

insert into zdb_active_data (zactvd_zdb_id)
select relid
from tmp_mrel;

insert into marker_relationship(mrel_zdb_id, mrel_mrkr_1_zdb_id, mrel_mrkr_2_zdb_id, mrel_type)
select relid, consid, mrkrid, reltype
from tmp_mrel;

insert into record_attribution (recattrib_Data_zdb_id, recattrib_source_zdb_id, recattrib_source_type)
select relid, 'ZDB-PUB-150410-5', 'standard'
from tmp_mrel;

delete from tmp_cmrel;
delete from tmp_mrel;

-- second construct edit
update construct
set construct_name='Tg(hsp70l:APX,cryaa:mCherry)'
where construct_zdb_id = 'ZDB-TGCONSTRCT-160222-6';
update marker
set mrkr_name='Tg(hsp70l:APX,cryaa:mCherry)'
where mrkr_zdb_id = 'ZDB-TGCONSTRCT-160222-6';
update marker
set mrkr_abbrev='Tg(hsp70l:APX,cryaa:mCherry)'
where mrkr_zdb_id = 'ZDB-TGCONSTRCT-160222-6';

-- remove last parenthesis
delete
from construct_component
where cc_pk_id in
      (select cc_pk_id
       from construct_component
       where cc_construct_Zdb_id = 'ZDB-TGCONSTRCT-160222-6'
       order by cc_order desc
       limit 1);

insert into construct_component (cc_construct_Zdb_id, cc_Component_type, cc_component_category, cc_component_zdb_id,
                                 cc_component, cc_cassette_number, cc_order)
values ('ZDB-TGCONSTRCT-160222-6', 'controlled vocab component', 'promoter component', 'ZDB-CV-150506-11', ',', 2, 6);

insert into construct_component (cc_construct_Zdb_id, cc_Component_type, cc_component_category, cc_component_zdb_id,
                                 cc_component, cc_cassette_number, cc_order)
values ('ZDB-TGCONSTRCT-160222-6', 'promoter of', 'promoter component', 'ZDB-GENE-020508-1', 'cryaa', 2, 7);
insert into construct_component (cc_construct_Zdb_id, cc_Component_type, cc_component_category, cc_component_zdb_id,
                                 cc_component, cc_cassette_number, cc_order)
values ('ZDB-TGCONSTRCT-160222-6', 'controlled vocab component', 'promoter component', 'ZDB-CV-150506-10', ':', 2, 8);
insert into construct_component (cc_construct_Zdb_id, cc_Component_type, cc_component_category, cc_component_zdb_id,
                                 cc_component, cc_cassette_number, cc_order)
values ('ZDB-TGCONSTRCT-160222-6', 'coding sequence of', 'coding component', 'ZDB-EFG-080214-1', 'mCherry', 2, 9);
-- add last parenthesis
insert into construct_component (cc_construct_Zdb_id, cc_Component_type, cc_component_category, cc_component_zdb_id,
                                 cc_component, cc_cassette_number, cc_order)
values ('ZDB-TGCONSTRCT-160222-6', 'controlled vocab component', 'construct wrapper component', 'ZDB-CV-150506-8', ')',
        2, 10);


insert into tmp_cmrel(relid, consid, mrkrid, reltype)
values (get_id('CMREL'), 'ZDB-TGCONSTRCT-160222-6', 'ZDB-GENE-020508-1', 'promoter of');
insert into tmp_cmrel(relid, consid, mrkrid, reltype)
values (get_id('CMREL'), 'ZDB-TGCONSTRCT-160222-6', 'ZDB-EFG-080214-1', 'coding sequence of');


insert into zdb_active_data (zactvd_zdb_id)
select relid
from tmp_cmrel;

insert into construct_marker_relationship(conmrkrrel_zdb_id, conmrkrrel_construct_zdb_id, conmrkrrel_mrkr_zdb_id,
                                          conmrkrrel_relationship_type)
select relid, consid, mrkrid, reltype
from tmp_cmrel;

insert into tmp_mrel(relid, consid, mrkrid, reltype)
values (get_id('MREL'), 'ZDB-TGCONSTRCT-160222-6', 'ZDB-GENE-020508-1', 'promoter of');
insert into tmp_mrel(relid, consid, mrkrid, reltype)
values (get_id('MREL'), 'ZDB-TGCONSTRCT-160222-6', 'ZDB-EFG-080214-1', 'coding sequence of');

insert into zdb_active_data (zactvd_zdb_id)
select relid
from tmp_mrel;

insert into marker_relationship(mrel_zdb_id, mrel_mrkr_1_zdb_id, mrel_mrkr_2_zdb_id, mrel_type)
select relid, consid, mrkrid, reltype
from tmp_mrel;

insert into record_attribution (recattrib_Data_zdb_id, recattrib_source_zdb_id, recattrib_source_type)
select relid, 'ZDB-PUB-150410-5', 'standard'
from tmp_mrel;

-- drop temp tables
drop table tmp_cmrel;
drop table tmp_mrel;