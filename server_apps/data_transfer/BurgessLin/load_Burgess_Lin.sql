-- load Burgess-Lin sample data in tsv format into  the database

begin work;

create table burgess_lin1(
	bl1_la_acc varchar(10),
	bl1_vec_acc varchar(10),   -- all are JN244738
	bl1_gss_acc varchar(10),
	bl1_gene_sym varchar(40),  -- 2/3 a3/4 are blank  --	bl_gene_sub varchar(20),  !!! need to parse out the parens
	bl1_intron varchar(20),
	bl1_gene_zdbid varchar(50),         -- all blank
	bl1_chr int,
	bl1_start int,
	bl1_end int
) fragment by round robin in tbldbs1, tbldbs2,tbldbs3
-- extents first  extents next
;

create table burgess_lin2(
	bl2_la_acc varchar(10),
	bl2_plate varchar(20),
	bl2_parent varchar(10)
) fragment by round robin in tbldbs1, tbldbs2,tbldbs3
-- extents first  extents next
;

! echo "read la_gb_chr_loc.tab into table burgess_lin1"
load from 'la_gb_chr_loc.tab' delimiter '	' insert into burgess_lin1 ;


! echo "read la_fish_parent.tab into table burgess_lin2"
load from 'la_fish_parent.tab' delimiter '	' insert into burgess_lin2 ;






--drop table burgess_lin1;
--drop table burgess_lin2;

--
commit work;

--rollback work;

