-- load vega  gff3 files from Will Chow at Sanger
begin work;

create table gff3(
    seqname		varchar(9),
    source		varchar(25),
    feature		varchar(25),
    start		integer,
    end 		integer,
    score		varchar(5),
    strand		char(1),
    frame		char(1),
    -- group -- in gff3 9th column is now called 'attribute'
    ID    varchar(25) PRIMARY KEY,
    Name  varchar(44),
    Parent  varchar(25),
    biotype varchar(55)
) fragment by round robin in tbldbs1, tbldbs2, tbldbs3
;

-- this file is only regenerated with new vega loads
! echo "load_drerio_vega_id.sql <- drerio_vega_id.unl"

load from '/research/zprodmore/gff3/drerio_vega_id.unl' insert into gff3;

-- index transcripts
-- create index gff3_id_idx on gff3(id) in idxdbs3; -- is PK now
create index gff3_parent_idx on gff3(parent) in idxdbs3;
create index gff3_biotype_idx on gff3(biotype) in idxdbs3;

-- index chromosones, vega|ensembl , SO type
create index gff3_seqname_idx on gff3(seqname) in idxdbs2;
create index gff3_source_idx on  gff3(source) in idxdbs1;
create index gff3_feature_idx on  gff3(feature) in idxdbs3;

-- might need these if we get as far as "between" location location.
--create index gff3_start_idx on  gff3(start) in idxdbs1;
--create index gff3_end_idx on  gff3(end) in idxdbs3;

! echo "update transcript names (ottdarT) to zfin transcript names"
update gff3 set name = (
	select mrkr_name from marker, transcript
	 where tscript_load_id = ID
	   and tscript_mrkr_zdb_id = mrkr_zdb_id
)where feature = 'transcript' and exists (
	select 't' from transcript where ID = tscript_load_id
);

! echo "update Vega gene names (ottdarG) to zfin names"
update gff3 set name = (
	select distinct mrkr_abbrev from marker, db_link
	 where dblink_acc_num = ID
	   and dblink_linked_recid = mrkr_zdb_id
)
where feature = 'gene' and exists (
   select 't' from db_link where ID = dblink_acc_num
);

create index gff3_name_idx on gff3(name) in idxdbs3; -- after renaming
update statistics medium for table gff3;

! echo " In <!--|ROOT_PATH|-->/home/data_transfer/Downloads/"
! echo " load_drerio_vega_id.sql -> drerio_vega_transcript.gff3"

unload to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/drerio_vega_transcript.gff3'  DELIMITER "	"
select
   seqname,source,feature,start,end,score,strand,frame,
    case feature
    when 'transcript' then
    "ID="       || ID      ||
    ";Name="    || Name    ||
    ";Parent="  || Parent  ||
    ";biotype=" || biotype ||
    ";zdb_id="  || case tscript_mrkr_zdb_id when NULL then "" else tscript_mrkr_zdb_id end ||
    ";Alias="   || id
    when 'gene' then
    "ID="       || ID      ||
    ";Name="    || Name    ||
    ";biotype=" || biotype
    when 'exon' then
    "ID="       || ID      ||
    ";Name="    || Name    ||
    ";Parent="  || Parent
    when 'CDS' then
    "ID="       || ID      ||
    ";Name="    || Name    ||
    ";Parent="  || Parent
    end
from gff3, outer transcript
where tscript_load_id = id
order by 1::integer,4,3
;

-- In production, rollback/commit are supplied externally.
