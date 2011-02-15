

-- should not *need* to be in a transaction
-- though if it does not cause problems, I would perfer it
-- note: the regen function called later does produce locks
--
begin work;

-- load mouse accessions, symbols, chromosomes, and locations
-- have current and withdrawn data sets
-- use the withdrawn set first to identify data that
-- might be stale and want to get looked at by someone.
-- wget -q ftp://ftp.informatics.jax.org/pub/reports/MRK_List1.rpt
-- cut -f 1-5,7 MRK_List1.rpt | grep "	Gene" | grep "	O" | cut -f1-4 | tr -d ' ' |sort -u >! mus_chr_loc_sym.tab
-- cut -f 1-5,7 MRK_List1.rpt | grep "	Gene" | grep -v "	O" | cut -f1-4 | tr -d ' ' |sort -u > ! mus_chr_loc_sym_W.tab

create table chr_loc_sym (
	cls_mgi varchar(20),
	cls_chr varchar(10),
	cls_loc varchar(10),
	cls_sym varchar(40))
fragment by round robin in tbldbs1,tbldbs2,tbldbs3
;

-- this set are both official and the so-called interm symbols
load from 'mus_chr_loc_sym.tab' delimiter '	' insert into chr_loc_sym;

create table chr_loc_symW (
	clsw_mgi varchar(10),
	clsw_chr varchar(10),
	clsw_loc varchar(10),
	clsw_sym varchar(40))
fragment by round robin in tbldbs1,tbldbs2,tbldbs3
;
-- this set have been withdrawn at MGI
load from 'mus_chr_loc_sym_W.tab' delimiter '	' insert into chr_loc_symw;

! echo "try to limit pointless symbol duplication via 'N/A' locations"

select distinct cls_sym tsym
 from chr_loc_sym
 where cls_loc != "N/A"
 into temp tmp_sym with no log
;
create unique index tmp_sym_tsym_idx on tmp_sym(tsym) in idxdbs1;
update statistics medium for table tmp_sym;

! echo "drop dup symbols with 'N/A' locations (was too slow)"
delete from chr_loc_sym
 where cls_loc == "N/A"
 and exists (select 't' from tmp_sym where cls_sym == tsym)
;
drop table tmp_sym;

! echo "try to limit pointless symbol duplication via 'UN' chromos"
select distinct cls_sym tsym
 from chr_loc_sym
 where cls_chr != "UN"
 into temp tmp_sym with no log
;
create unique index tmp_sym_tsym_idx on tmp_sym(tsym) in idxdbs1;
update statistics medium for table tmp_sym;

! echo "drop dup symbols with 'UN' locations (was slow)"
delete from chr_loc_sym
 where cls_chr == "UN"
 and exists (select 't' from tmp_sym where cls_sym == tsym)
;
drop table tmp_sym;

! echo "try to limit pointless symbol duplication via 'NULL' MGI accessions"
select distinct cls_sym tsym
 from chr_loc_sym
 where cls_mgi != "NULL"
 into temp tmp_sym with no log
;
create unique index tmp_sym_tsym_idx on tmp_sym(tsym) in idxdbs1;
update statistics medium for table tmp_sym;

! echo "drop dup symbols with NULL MGI accessions"
delete from chr_loc_sym
 where cls_mgi == "NULL"
 and exists (select 't' from tmp_sym where cls_sym == tsym)
;
drop table tmp_sym;

! echo "Drop rows with NULL symbols"
delete from chr_loc_sym where cls_sym IS NULL;

! echo "show symbols with more than one non-empty location"
select cls_sym tsym
 from chr_loc_sym
 group by 1 having count(*) > 1
 into temp tmp_sym with no log
;
select cls_sym, cls_chr, cls_loc
 from chr_loc_sym, tmp_sym
 where  cls_sym == tsym
 order by 1,2,3
;
drop table tmp_sym;

-- doubt we will allways get away with unique ...
create unique index chr_loc_sym_cls_sym_idx on chr_loc_sym(cls_sym) in idxdbs3;

create index chr_loc_sym_cls_mgi_idx on chr_loc_sym(cls_mgi) in idxdbs2;

update statistics medium for table chr_loc_sym;

! echo "drop withdrawn that also exist in current"
delete from chr_loc_symW where exists (select 't' from chr_loc_sym where cls_sym == clsw_sym);

! echo "try to limit pointless W-symbol duplication via 'N/A' locations"

select distinct clsw_sym tsym
 from chr_loc_symw
 where clsw_loc != "N/A"
 into temp tmp_sym with no log
;
! echo "drop dup W-symbols without locations"
delete from chr_loc_symw
 where clsw_loc == "N/A"
 and exists (select 't' from tmp_sym where clsw_sym == tsym)
;
drop table tmp_sym;

! echo "try to limit pointless W-symbol duplication via 'UN' chromos"
select distinct clsw_sym tsym
 from chr_loc_symw
 where clsw_chr != "UN"
 into temp tmp_sym with no log
;
create unique index tmp_sym_tsym_idx on tmp_sym(tsym) in idxdbs1;
update statistics medium for table tmp_sym;

! echo "drop dup W-symbols with 'UN' chromo (was too slow)"
delete from chr_loc_symw
 where clsw_chr == "UN"
 and exists (select 't' from tmp_sym where clsw_sym == tsym)
;
drop table tmp_sym;

! echo "try to limit pointless W-symbol duplication via 'NULL' MGI accessions"
select distinct clsw_sym tsym
 from chr_loc_symw
 where clsw_mgi != "NULL"
 into temp tmp_sym with no log
;
create unique index tmp_sym_tsym_idx on tmp_sym(tsym) in idxdbs1;
update statistics medium for table tmp_sym;
! echo "drop dup symbols with NULL MGI accessions (was too slow)"
delete from chr_loc_symw
 where clsw_mgi == "NULL"
 and exists (select 't' from tmp_sym where clsw_sym == tsym)
;
drop table tmp_sym;


! echo "find W-symbls with more than one non-empty location"
select clsw_sym tsym
 from chr_loc_symw
 group by 1 having count(*) > 1
 into temp tmp_sym with no log
;
-- just looking
--select clsw_sym, clsw_chr, clsw_loc
-- from chr_loc_symw, tmp_sym
-- where  clsw_sym == tsym
-- order by 1,2,3
--;

! echo "Do any of the duplicate withdrawn mouse symbols exist in ZFIN?"
select ortho_abbrev,ortho_chromosome,ortho_position
 from orthologue, tmp_sym
 where organism == "Mouse"
   and ortho_abbrev == tsym
;

! echo "drop duplicate Withdrawn symbols"
delete from  chr_loc_symw where exists(
	select 't' from  tmp_sym where clsw_sym == tsym
);
drop table tmp_sym;

create unique index chr_loc_symw_clsw_sym_idx on chr_loc_symw(clsw_sym) in idxdbs2;

create index chr_loc_symw_clsw_mgi_idx on chr_loc_symw(clsw_mgi) in idxdbs3;
update statistics medium for table chr_loc_symw;

! echo "add 'cM' to numeric locations"
update chr_loc_sym set cls_loc = cls_loc || " cM"
 where cls_loc not in ('N/A','syntenic');

update chr_loc_symw set clsw_loc = clsw_loc || " cM"
 where clsw_loc not in ('N/A','syntenic');

! echo "done prepping the input"
! echo "find existing mouse orthologs with withdrawn symbols. show curators?"
select c_gene_id[1,25] gene, ortho_abbrev symbol
 from orthologue, chr_loc_symw
 where organism == "Mouse"
   and ortho_abbrev == clsw_sym
;

! echo "Do incomming withdrawn accessions have different gene symbols"
select ortho_abbrev old_sym,  clsw_sym new_sym
 from chr_loc_symw, db_link, orthologue
 where dblink_linked_recid ==  zdb_id
   and organism == "Mouse"
   and clsw_mgi == dblink_acc_num
   and ortho_abbrev != clsw_sym
;

! echo "on the theory stale is better than none update withdrawn chrmo"
update orthologue set ortho_chromosome = (
	select clsw_chr from chr_loc_symw
	 where ortho_abbrev == clsw_sym
)
 where organism == "Mouse"
   and exists (
	select 't' from chr_loc_symw
	 where ortho_abbrev == clsw_sym
	   and (ortho_chromosome is NULL OR ortho_chromosome !=  clsw_chr)
);

--! echo "any withdrawn locations not like N/A ?"
-- select * from chr_loc_symw where clsw_loc != "N/A";
-- yes, about 75

! echo "on the off chance stale is better than none update withdrawn location"
update orthologue set ortho_position = (
	select clsw_loc from chr_loc_symw
	 where ortho_abbrev == clsw_sym
) where organism == "Mouse"
 and exists (
	select 't' from chr_loc_symw
	 where ortho_abbrev == clsw_sym
	   and (ortho_position is NULL OR ortho_position !=  clsw_loc)
);

-- not quick either
! echo "do any accessions exist that no longer correspond with the symbol they were assigned in ZFIN"
select ortho_abbrev old_sym,  cls_sym new_sym
 from chr_loc_sym, db_link,orthologue
 where dblink_linked_recid ==  zdb_id
   and organism == "Mouse"
   and cls_mgi == dblink_acc_num
   and ortho_abbrev != cls_sym
;


! echo "update mouse chr"

update orthologue set ortho_chromosome = (
	select cls_chr from chr_loc_sym
	 where ortho_abbrev == cls_sym
)
 where organism == "Mouse"
   and ortho_chromosome is NULL
   and exists (
	select 1 from chr_loc_sym
	 where ortho_abbrev == cls_sym
);

update orthologue set ortho_chromosome = (
	select cls_chr from chr_loc_sym
	 where ortho_abbrev == cls_sym
)
 where organism == "Mouse"
   and ortho_chromosome is not NULL
   and exists (
	select 1 from chr_loc_sym
	 where ortho_abbrev == cls_sym
	   and ortho_chromosome !=  cls_chr
);

! echo "update mouse location"
update orthologue set ortho_position = (
	select cls_loc from chr_loc_sym
	 where ortho_abbrev == cls_sym
)
 where organism == "Mouse"
   and ortho_position is NULL
   and exists (
	select 't' from chr_loc_sym
	 where ortho_abbrev == cls_sym
);

update orthologue set ortho_position = (
	select cls_loc from chr_loc_sym
	 where ortho_abbrev == cls_sym
)
 where organism == "Mouse"
   and ortho_position is not NULL
   and exists (
	select 't' from chr_loc_sym
	 where ortho_abbrev == cls_sym
	   and ortho_position != cls_loc
);

-- previously 2,300
! echo "Mouse non NULL Chromos"
select count(*) mus_chromo_not_null
 from orthologue
 where organism == 'Mouse'
  and ortho_chromosome is not NULL
;
-- previously 2,300
! echo "Mouse non NULL Locations"
select count(*) mus_loc_not_null
 from orthologue
 where organism == 'Mouse'
  and ortho_position is not NULL
;

! echo "what zfin mouse symbols are not in the current MGI set?"
select c_gene_id gene, ortho_abbrev tsym from orthologue
 where organism == 'Mouse'
 and not exists (
	select 't' from chr_loc_sym
	  where ortho_abbrev == cls_sym
)  into temp tmp_sym with no log
;

select * from tmp_sym order by 1;

! echo "which are not in the withdrawn set either?"
! echo "symbols ending in '-ps' are pseudo genes not in this update"
! echo "but someone might want to check one them once and a while"
select * from tmp_sym where not exists (
	select 1 from chr_loc_symw
	 where tsym == clsw_sym
)order by 1;

drop table chr_loc_symw;
drop table tmp_sym;
drop table chr_loc_sym;


-- rollback work;

--
commit work;


