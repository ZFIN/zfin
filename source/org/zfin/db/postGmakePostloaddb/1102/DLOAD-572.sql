--liquibase formatted sql
--changeset pm:DLOAD-572


update dupcrispr set zdbid=(select mrkr_zdb_id from marker where mrkr_abbrev=substring(cname from 9 for char_length(cname))) where zdbid is null;
update dupcrispr set cname=(select mrkr_zdb_id from marker where cname=mrkr_name);
update dupcrispr set pmid =(select zdb_id from publication where accession_no=cast(pmid as integer));
update dupcrispr set cseq= get_id('DALIAS');
update dupcrispr set gene= get_id('DBLINK');
update dupcrispr set zdbid= (select mrel_zdb_id from marker_relationship where mrel_mrkr_2_zdb_id=zdbid and mrel_mrkr_1_zdb_id =cname and mrel_type='knockdown reagent targets gene');

insert into zdb_active_Data (zactvd_zdb_id) select cseq from dupcrispr;

insert into zdb_active_Data (zactvd_zdb_id) select gene from dupcrispr;
insert into data_alias (dalias_zdb_id,dalias_alias,dalias_data_zdb_id,dalias_group_id) select cseq,calias,cname,1 from dupcrispr;
insert into record_attribution (recattrib_Data_zdb_id,recattrib_source_zdb_id) select cseq,'ZDB-PUB-151209-4' from dupcrispr;
insert into record_attribution (recattrib_Data_zdb_id,recattrib_source_zdb_id) select cname,'ZDB-PUB-151209-4' from dupcrispr where cname not in (Select recattrib_Data_zdb_id from record_attribution where recattrib_source_zdb_id='ZDB-PUB-151209-4');
--insert into record_attribution (recattrib_Data_zdb_id,recattrib_source_zdb_id) select cname,pmid from dupcrispr;
insert into record_attribution (recattrib_Data_zdb_id,recattrib_source_zdb_id) select zdbid,'ZDB-PUB-151209-4' from dupcrispr where zdbid not in (Select recattrib_Data_zdb_id from record_attribution where recattrib_source_zdb_id='ZDB-PUB-151209-4');
--insert into record_attribution (recattrib_Data_zdb_id,recattrib_source_zdb_id) select zdbid,pmid from dupcrispr;
insert into db_link(dblink_linked_recid,dblink_Zdb_id,dblink_acc_num,dblink_fdbcont_zdb_id) select cname,gene,calias,'ZDB-FDBCONT-160128-1' from dupcrispr;
insert into record_attribution (recattrib_Data_zdb_id,recattrib_source_zdb_id) select gene,'ZDB-PUB-151209-4' from dupcrispr where gene not in (select recattrib_Data_zdb_id from record_attribution where recattrib_source_zdb_id='ZDB-PUB-151209-4');

