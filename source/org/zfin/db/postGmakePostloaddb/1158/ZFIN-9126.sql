--liquibase formatted sql
--changeset rtaylor:ZFIN-9126.sql


create temp table tmp_dalias (alias_id varchar(50), construct_id varchar(50),construct_name text);

insert into tmp_dalias (construct_name, construct_id)
VALUES
    ('Tg(and1-Hsa.HBB:EGFP)', 'ZDB-TGCONSTRCT-161115-2'),
    ('Tg(and1:EGFP)', 'ZDB-TGCONSTRCT-161115-3'),
    ('Tg2(rr.2pand1:EGFP)', 'ZDB-TGCONSTRCT-161115-4'),
    ('Tg(pth2:EGFP)', 'ZDB-TGCONSTRCT-220422-1'),
    ('Tg(pth2:TagRFP)', 'ZDB-TGCONSTRCT-220422-2'),
    ('TgBAC(cdh1:cdh1-TagBFP,cryaa:Cerulean)', 'ZDB-TGCONSTRCT-190812-12'),
    ('Tg(mylpfa:hsa.tpm3,myl7-:egfp)', 'ZDB-TGCONSTRCT-220928-1'),
    ('Tg(rho:GAP-YFP-2A-NTR2.0)', 'ZDB-TGCONSTRCT-230502-2'),
    ('Tg(uts2d:GFP-CAAX)', 'ZDB-TGCONSTRCT-211019-1');

update tmp_dalias set alias_id=get_id('DALIAS');

insert into zdb_active_data (zactvd_zdb_id) select alias_id from tmp_dalias;
insert into data_alias(dalias_zdb_id,dalias_data_zdb_id,dalias_alias,dalias_group_id) select alias_id,construct_id,construct_name,1 from tmp_dalias;
