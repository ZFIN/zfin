 -- update_vega_nomenclature.sql
 
begin work;

! echo "update Vega transcript names (ottdarT) to zfin transcript names"
update gff3 set gff_Name = (
	select mrkr_name from marker, transcript
	 where tscript_load_id = gff_ID
	   and tscript_mrkr_zdb_id = mrkr_zdb_id
) 
 where gff_source == 'vega'
   and gff_feature = 'transcript' 
   and exists (
	select 't' from transcript where gff_ID = tscript_load_id
);

! echo "update Vega gene names (ottdarG) to zfin names"
update gff3 set gff_Name = (
	select distinct mrkr_abbrev from marker, db_link
	 where dblink_acc_num = gff_ID
	   and dblink_linked_recid = mrkr_zdb_id
)
 where gff_source == 'vega'
   and gff_feature = 'gene' 
   and exists (
	select 't' from db_link where gff_ID = dblink_acc_num
);

update statistics high for table gff3;
commit work;
-- various unloads are expected to follow in their own files

