-- insert_marker_relationship.sql

! echo "$HOST $DBNAME `date +%Y%m%d`  `whoami`"

-- to create the input file for this script ...
--# currently all the clone accessions are 8 chars but I would not want to count on it
--awk '{split($NF,acc,",");for(a in acc)printf("%s|%s|\n",substr($1,2),substr(acc[a],1,index(acc[a],".")-1));}' \transcripts_for_tom.defline >! ottdarT_clnacc.unl



begin work;

create table ott_acc ( oa_tscript varchar(50), oa_clone varchar(50) )
 fragment by round robin in tbldbs1, tbldbs2, tbldbs3
;
! echo "load file 'ottdarT_clnacc.unl' into a table"
load from "ottdarT_clnacc.unl" insert into ott_acc;

create index ott_acc_oa_tscript_idx on ott_acc(oa_tscript) in idxdbs3;
create index ott_acc_oa_clone_idx on ott_acc(oa_clone) in idxdbs3;

update statistics high for table ott_acc;

! echo "find transcript zdb_ids"
update ott_acc set oa_tscript = (
	select tscript_mrkr_zdb_id from transcript
	 where oa_tscript = tscript_load_id
)where exists (
	select 't' from transcript where oa_tscript = tscript_load_id
);


! echo "Find problematic Clones in ZFIN ... potential merge candidates"
select distinct oa_clone from ott_acc into temp tmp_clone with no log;

select dblink_acc_num, count (*)
 from db_link, tmp_clone
 where dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-36' -- GenBank-Genomic
   and dblink_linked_recid[1,8]  !=  'ZDB-GENE'
   and dblink_acc_num = oa_clone
 group by 1 having count(*) > 1
;
drop table tmp_clone;

! echo "find DNA Clone zdb_ids"
update ott_acc set oa_clone = (
	select distinct dblink_linked_recid from db_link
	 where oa_clone = dblink_acc_num
	 and dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-36' -- GenBank-Genomic
	 and dblink_linked_recid[1,8]  !=  'ZDB-GENE'        -- curators direct assignment
	 and dblink_linked_recid[1,8]  !=  'ZDB-TGCO'
) where exists (
	select 't' from db_link
	 where oa_clone = dblink_acc_num
	 and dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-36' -- GenBank-Genomic
	 and dblink_linked_recid[1,8]  !=  'ZDB-GENE'       -- curators direct assignment
	 and dblink_linked_recid[1,8]  !=  'ZDB-TGCO'
);

! echo "Find any marked as withdrawn but here none the less (zombie transcripts?)"
select * from transcript , ott_acc
 where oa_tscript = tscript_mrkr_zdb_id
  and tscript_status_id = 1 -- withdrawn
;

! echo "dig them up"
update transcript set tscript_status_id = NULL where exists (
	select 't' from ott_acc
	 where oa_tscript = tscript_mrkr_zdb_id
	   and tscript_status_id = 1 -- withdrawn
);


! echo "Find any not converted"

select * from ott_acc
 where oa_tscript[1,4] != 'ZDB-' OR oa_clone[1,4] != 'ZDB-'
;

! echo "Drop em"
delete from ott_acc where oa_tscript[1,4] != 'ZDB-';
delete from ott_acc where oa_clone[1,4] != 'ZDB-';


! echo " drop incomming 'clone contains transcript' relationshipts that already exist"
delete from  ott_acc where exists (
	select 't' from marker_relationship
	 where mrel_type = 'clone contains transcript'
	   and mrel_mrkr_1_zdb_id = oa_clone
	   and mrel_mrkr_2_zdb_id = oa_tscript
);


! echo "Find any existing mrel that are no longer being ed and drop attribution"
-- I am not quite willing to drop the relationship without being forced to
-- since the transcript is still found in the clone and if the transcript is deleted
-- it takes care of it anyway
delete from record_attribution
 where recattrib_source_zdb_id = 'ZDB-PUB-030703-1'
   and recattrib_data_zdb_id[1,8] = 'ZDB-MREL-'
   and not exists (
	select 't' from marker_relationship, ott_acc
	 where mrel_type = 'clone contains transcript'
	   and mrel_mrkr_1_zdb_id = oa_clone
	   and mrel_mrkr_2_zdb_id = oa_tscript
	   and mrel_zdb_id = recattrib_data_zdb_id
);


! echo "prepare to load the remaining new 'clone contains transcript' marker relationships"
alter table ott_acc add  zad varchar(50);
update ott_acc set zad = get_id('MREL');
insert into zdb_active_data select zad from ott_acc;

insert into marker_relationship(mrel_zdb_id, mrel_mrkr_1_zdb_id, mrel_mrkr_2_zdb_id, mrel_type, mrel_comments)
 select zad, oa_clone, oa_tscript, 'clone contains transcript','Indicated by the Sanger sequencing project'
  from ott_acc;

! echo "and attributed"
insert into record_attribution(recattrib_data_zdb_id,recattrib_source_zdb_id)
 select zad,'ZDB-PUB-030703-1'
 from ott_acc;

drop table ott_acc;

-- transaction cloaed externally
--rollback work;
--commit work;
