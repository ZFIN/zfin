 -- update_vega_nomenclature.sql

! echo "update_vega_nomenclature.sql"

begin work;

! echo "update Vega transcript names (ottdarT) to zfin transcript names"
update gff3 set gff_Name = (
	select mrkr_abbrev
	 from transcript
	  join marker on tscript_mrkr_zdb_id = mrkr_zdb_id
	 where tscript_load_id = gff_ID
	   and gff_Name != mrkr_abbrev
)
 where gff_source == 'vega'
   and gff_feature = 'transcript'
   and exists (
	select 't' from transcript where gff_ID = tscript_load_id
);

! echo "update Vega gene names (ottdarG) to zfin names"
update gff3 set gff_Name = (
	select distinct mrkr_abbrev
	 from  db_link
	  join marker_relationship on dblink_linked_recid = mrel_mrkr_2_zdb_id
	  join marker on mrkr_zdb_id = mrel_mrkr_1_zdb_id
	 where dblink_acc_num = gff_ID
	   and mrel_type == "gene produces transcript"
	   and mrkr_type[1,4] == "GENE"
	   and gff_Name != mrkr_abbrev
)
 where gff_source == 'vega'
   and gff_feature = 'gene'
   and exists (
	select 't' from db_link where gff_ID = dblink_acc_num
);

update statistics high for table gff3;
--
commit work;
-- various unloads are expected to follow in their own files

