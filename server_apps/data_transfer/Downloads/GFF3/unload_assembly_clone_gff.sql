-- unload_assembly_clone_gff.sql

--drop table assembly;
--drop table clonelist;

--! echo `pwd`
! echo "generate full length clone and vega trimmed clone GFF3 tracks"
create table assembly (
        asmb_lg varchar(3),
        asmb_five integer,
        asmb_three integer,
        asmb_name varchar(40),
        asmb_accession varchar(50) PRIMARY KEY,
        asmb_acc_vers char(2),
        asmb_int_start integer,
        asmb_int_end integer,
        asmb_strand integer
) fragment by round robin in tbldbs1, tbldbs2, tbldbs3
extent size 625001 next size 625001
;

! echo "load file 'assembly_for_tom.tab' into a table"
-- assembly_for_tom.tab is expected to be copied to "here" first
load from 'assembly_for_tom.tab' delimiter '	' insert into assembly;

update assembly set asmb_name = upper(asmb_name);
create index asmb_strand_idx on assembly(asmb_strand);
update statistics medium for table assembly ;

create table clonelist(
	cl_acc  varchar(25) primary key,
	cl_status varchar(25) default "unannotated"
) fragment by round robin in tbldbs1, tbldbs2, tbldbs3
;

! echo "load file 'clone_acc_status.unl' into a table"
-- clonelist_for_tom.tab is expected to be copied to "here" first
-- -- delimiter '	'
load from 'clone_acc_status.unl' insert into clonelist;

update statistics medium for table assembly;
update statistics medium for table clonelist;

! echo "Vega_Clone not matching incomming"
select *--  count(*) extra_dblink
 from db_link
 where dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040826-2'
   and not exists(
     select 't' from assembly where asmb_accession == dblink_acc_num
);

! echo "incomming missing Vega_Clone"
select * -- count(*) missing_dblink
 from assembly
 where not exists(
	select 't' from db_link where dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040826-2'
	 and asmb_accession == dblink_acc_num
);

! echo "assembly_for_tom.tab -> unload_assembly_clone_gff.sql -> full_length_clones.gff3"
UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/full_length_clones.gff3' DELIMITER "	"
select asmb_lg gff_ID,
    'vega.fulllength' gff_source,
    'clone' gff_feature,
    case when asmb_strand == 1 then
    	(asmb_five - (asmb_int_start - 1))::integer
    	else
    	(asmb_three + (asmb_int_start - 1) - dblink_length)::integer
    	end full_start,
    case when asmb_strand == 1 then
    	 (asmb_five - (asmb_int_start - 1) + dblink_length)::integer
    	else
    	(asmb_three + (asmb_int_start - 1))::integer
    	end full_end,
    '.' gff_score,
    case when  asmb_strand == 1 then '+' else '-' end,
    '.' gff_frame_phase,
    'ID=' || dblink_acc_num ||';Name='|| asmb_name ||
    ';Alias='|| dblink_linked_recid ||
    ';status='|| case when cl_status is null then "unannotated" else cl_status end attribute
 from db_link, assembly, outer clonelist
 where dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040826-2'
   and asmb_accession == dblink_acc_num
   and asmb_accession == cl_acc
;
-- to be valid the gff3 requires a header
! /usr/bin/awk '{a[NR]=$0}END{a[0]=h;for(i=0;i<=NR;i++)print a[i]>FILENAME}' h="##gff-version 3" <!--|ROOT_PATH|-->/home/data_transfer/Downloads/full_length_clones.gff3

! echo "assembly_for_tom.tab -> unload_assembly_clone_gff.sql -> vega_clone.gff3"
UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/vega_clone.gff3' DELIMITER "	"
select asmb_lg gff_ID,
    'vega.trimmed' gff_source,
    'clone' gff_feature,
    asmb_five trim_start,
    asmb_three trim_end,
    '.' score,
    case when asmb_strand == 1 then '+' else '-' end,
    '.' gff_frame_phase,
    'ID=' || dblink_acc_num ||';Name=' || asmb_name ||
    ';Alias='|| dblink_linked_recid ||
    ';status='|| case when cl_status is null then "unannotated" else cl_status end attribute
 from db_link, assembly, outer clonelist
 where dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040826-2'
   and asmb_accession == dblink_acc_num
   and asmb_accession == cl_acc
;

-- to be valid the gff3 requires a header
! /usr/bin/awk '{a[NR]=$0}END{a[0]=h;for(i=0;i<=NR;i++)print a[i]>FILENAME}' h="##gff-version 3" <!--|ROOT_PATH|-->/home/data_transfer/Downloads/vega_clone.gff3

drop table assembly;
drop table clonelist;
