-- E_load_unload_BL_gff.sql
! echo "E_load_unload_BL_gff.sql -> zfin_BL.gff3"

begin work;

create table bed (
	bed_chr	integer not null,
	bed_beg	integer not null,
	bed_end	integer not null,
	bed_acc	char(8) not null primary key,
	bed_strand char(1) not null
)
fragment by round robin in tbldbs1,tbldbs2,tbldbs3
;

-- BL_chr_beg_end_la_strnd.tab should be pulled from
-- /research/zprodmore/gff3/


load from 'BL_chr_beg_end_la_strnd.tab' delimiter "	"
	insert into bed;

update statistics high for table bed;

! echo "E_load_unload_BL_gff.sql -> zfin_tginsertion.gff3"
UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/zfin_tginsertion.gff3' DELIMITER "	"
select bed_chr,
           "ZFIN" gff_source,
           "Transgenic_insertion" feature,
           bed_beg gstart,
           bed_end   gend,
           "." gff_score,
           bed_strand,
           "." gff_frame,
       'ID=' || feature_zdb_id    --- FEATURE
       ||';Name=' || feature_abbrev[1,8]
       ||';Alias='|| feature_zdb_id || ','
                  || feature_abbrev || ','
                  || feature_name || ';'  attribute
 from  bed join feature on feature_abbrev[1,8] == bed_acc
 order by 1,4,5,9
 ;

drop table bed;

! echo "this rollback is expected"
rollback work;
