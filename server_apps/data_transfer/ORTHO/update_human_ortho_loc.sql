-- should not *need* to be in a transaction
-- though if it does not cause problems, I would perfer it
-- note: the regen function called later does produce locks
--
begin work;

create table ent_chr_loc_sym_mim (
	eclsm_ent varchar(20),
	eclsm_chr varchar(30),
	eclsm_loc varchar(30),
	eclsm_sym varchar(40),
	eclsm_mim varchar(20))
fragment by round robin in tbldbs1,tbldbs2,tbldbs3
;

load from 'hum_chr_loc_sym_mim.tab' delimiter '	' insert into ent_chr_loc_sym_mim;
update statistics high for table  ent_chr_loc_sym_mim;

create table omim_gene(og_mim integer primary key, og_gene integer)
 fragment by round robin in tbldbs1,tbldbs2,tbldbs3;

load from 'mim2gene.tab' delimiter "	" insert into omim_gene;
create index omim_gene_og_gene_idx on omim_gene(og_gene) in idxdbs3;
update statistics high for table omim_gene;

! echo "remove non-gene OMIM ids"
update ent_chr_loc_sym_mim set eclsm_mim = NULL
 where eclsm_mim is not NULL
  and not exists (
  select 't' from omim_gene where eclsm_mim == og_mim
);

! echo "add in any new omim gene links we find"
update ent_chr_loc_sym_mim set eclsm_mim = (
  select og_mim from omim_gene
   where eclsm_ent == og_gene
 )
 where eclsm_mim  is NULL
   and exists (
  select 't' from omim_gene
   where eclsm_ent == og_gene
     and  og_mim is not NULL
);

! echo "check for descrepencies between incomming OMIM & Entrez Gene"
select count(*) omim_different
 from ent_chr_loc_sym_mim
  join omim_gene on eclsm_ent == og_gene
 where eclsm_mim != og_mim
   and eclsm_mim is not NULL
;


! echo "many OMMIN gene links REMAIN?"
select count(distinct eclsm_mim) from ent_chr_loc_sym_mim where eclsm_mim is not NULL;

! echo "many OMMIN gene links are NULL?"
select count (*) from ent_chr_loc_sym_mim where eclsm_mim is  NULL;


! echo "Are there any OMIM in ZFIN that are not for (current) genes?"
select c_gene_id[1,25] gene, dblink_acc_num[1,15] nongene_omim
 from db_link join orthologue on dblink_linked_recid == zdb_id
 where dblink_fdbcont_zdb_id == 'ZDB-FDBCONT-040412-25'  -- OMIM
   and not exists (
	select 't' from omim_gene where (og_mim::varchar(20)) = dblink_acc_num
);

select c_gene_id[1,25] gene, omim.dblink_acc_num[1,15] old_omim, eclsm_mim[1,15] new_omim, eclsm_ent[1,15] entrez_id
 from db_link entrez, db_link omim, orthologue, ent_chr_loc_sym_mim
 where entrez.dblink_linked_recid ==  zdb_id
   and entrez.dblink_linked_recid == omim.dblink_linked_recid
   and entrez.dblink_fdbcont_zdb_id == 'ZDB-FDBCONT-040412-27' -- Entrez
   and omim.dblink_fdbcont_zdb_id == 'ZDB-FDBCONT-040412-25'  -- OMIM
   and organism = "Human"
   and entrez.dblink_acc_num == eclsm_ent
   and omim.dblink_acc_num != eclsm_mim
;

! echo "update existing OMIM to agree with incomming OMIM based on NCBI Gene"
select --c_gene_id[1,25] gene, omim.dblink_acc_num[1,15] old_omim, eclsm_mim[1,15] new_omim, eclsm_ent[1,15] entrez_id
  omim.dblink_zdb_id zad, eclsm_mim newmim
 from db_link entrez, db_link omim, orthologue, ent_chr_loc_sym_mim
 where entrez.dblink_linked_recid ==  zdb_id
   and entrez.dblink_linked_recid == omim.dblink_linked_recid
   and entrez.dblink_fdbcont_zdb_id == 'ZDB-FDBCONT-040412-27' -- Entrez
   and omim.dblink_fdbcont_zdb_id == 'ZDB-FDBCONT-040412-25'  -- OMIM
   and organism = "Human"
   and entrez.dblink_acc_num == eclsm_ent
   and omim.dblink_acc_num != eclsm_mim
   into temp tmp_dbl with no log
;

update db_link set dblink_acc_num = (
	select newmim from tmp_dbl where dblink_zdb_id == zad
)
where exists (
	select 't' from tmp_dbl where dblink_zdb_id == zad
);

drop table tmp_dbl;


--! echo "Find OMIM accessions not in ZFIN (but could be)"
--select  c_gene_id[1,25] gene, eclsm_mim[1,15] new_omim, eclsm_ent[1,15] entrez_id
-- from db_link entrez, orthologue, ent_chr_loc_sym_mim
-- where entrez.dblink_linked_recid ==  zdb_id
--   and entrez.dblink_fdbcont_zdb_id == 'ZDB-FDBCONT-040412-27' -- Entrez
--   and entrez.dblink_acc_num == eclsm_ent
--   and eclsm_mim IS NOT NULL
--   and not exists(
--   		select 't' from db_link omim
--   		 where omim.dblink_fdbcont_zdb_id == 'ZDB-FDBCONT-040412-25'
--   		 and entrez.dblink_linked_recid == omim.dblink_linked_recid
--   		 and omim.dblink_acc_num == eclsm_mim
--  )
-- order by entrez_id
--;

! echo "Add OMIM accessions into ZFIN where possible"
select get_id('DBLINK') zad, entrez.dblink_linked_recid ortho, eclsm_mim newmim
 from db_link entrez, orthologue, ent_chr_loc_sym_mim
 where entrez.dblink_linked_recid ==  zdb_id
   and entrez.dblink_fdbcont_zdb_id == 'ZDB-FDBCONT-040412-27' -- Entrez
   and entrez.dblink_acc_num == eclsm_ent
   and eclsm_mim IS NOT NULL
   and not exists(
   		select 't' from db_link omim
   		 where omim.dblink_fdbcont_zdb_id == 'ZDB-FDBCONT-040412-25'
   		 and entrez.dblink_linked_recid == omim.dblink_linked_recid
   		 and omim.dblink_acc_num == eclsm_mim
  )
 into temp tmp_dbl with no log
;

insert into zdb_active_data select zad from tmp_dbl;
insert into db_link (
	dblink_linked_recid,
	dblink_acc_num,
	dblink_info,
	dblink_zdb_id,
	--dblink_acc_num_display,
	--dblink_length,
	dblink_fdbcont_zdb_id
)
select ortho, newmim, "not curated " || TODAY, zad, 'ZDB-FDBCONT-040412-25'
 from tmp_dbl
;
-- we may not have a pub ... ZDB-PUB-030905-1  is manualy curated zfin citation
-- insert into record_attribution

drop table tmp_dbl;
-------------------------------------------------------------------------------

! echo "try to limit pointless symbol duplication via '-' locations"
select distinct eclsm_sym tsym
 from ent_chr_loc_sym_mim
 where eclsm_loc != '-'
 into temp tmp_sym with no log
;
create unique index tmp_sym_tsym_idx on tmp_sym(tsym) in idxdbs1;
update statistics medium for table tmp_sym;

! echo "drop dup symbols with '-' locations (was slow)"
delete from ent_chr_loc_sym_mim
 where eclsm_loc == '-'
 and exists (select 't' from tmp_sym where eclsm_sym == tsym)
;
drop table tmp_sym;

! echo "try to limit pointless symbol duplication via 'Un' chromos"
select distinct eclsm_sym tsym
 from ent_chr_loc_sym_mim
 where eclsm_chr != "Un"
 into temp tmp_sym with no log
;
create unique index tmp_sym_tsym_idx on tmp_sym(tsym) in idxdbs1;
update statistics medium for table tmp_sym;

! echo "drop dup symbols with 'Un' chromos (was slow)"
delete from ent_chr_loc_sym_mim
 where eclsm_chr == "Un"
 and exists (select 't' from tmp_sym where eclsm_sym == tsym)
;
drop table tmp_sym;

-----------------------
! echo "try to limit pointless symbol duplication via NULL OMIM accessions"
select distinct eclsm_sym tsym
 from ent_chr_loc_sym_mim
 where eclsm_mim IS NOT NULL
 into temp tmp_sym with no log
;
create unique index tmp_sym_tsym_idx on tmp_sym(tsym) in idxdbs1;
update statistics medium for table tmp_sym;

! echo "drop dup symbols with NULL OMIM accessions"
delete from ent_chr_loc_sym_mim
 where eclsm_mim IS NULL
   and eclsm_ent IS NULL -- maybe
   and exists (select 't' from tmp_sym where eclsm_sym == tsym)
;
drop table tmp_sym;


--! echo "try to limit pointless symbol duplication via  OMIM accessions"
--select distinct eclsm_sym tsym
-- from ent_chr_loc_sym_mim, omim_gene
-- where eclsm_mim == omimg_id
-- into temp tmp_sym with no log
--;
--create unique index tmp_sym_tsym_idx on tmp_sym(tsym) in idxdbs1;
--update statistics medium for table tmp_sym;
--
--! echo "drop dup symbols with OMIM accessions"
--delete from ent_chr_loc_sym_mim
-- where eclsm_mim IS NOT NULL
--   and exists (select 't' from tmp_sym where eclsm_sym == tsym)
--   and not exists (select 't' from omim_gene where eclsm_mim == omimg_id)
--;
--drop table tmp_sym;



-----------------

! echo "try to limit pointless symbol duplication via NULL Entrez accessions?"
select distinct eclsm_sym tsym
 from ent_chr_loc_sym_mim
 where eclsm_ent IS NOT NULL
 into temp tmp_sym with no log
;
--create unique index tmp_sym_tsym_idx on tmp_sym(tsym) in idxdbs1;
--update statistics medium for table tmp_sym;

! echo "drop dup symbols with NULL Entrez accessions"
delete from ent_chr_loc_sym_mim
 where eclsm_mim IS NULL
   and eclsm_ent IS NULL
   and exists (select 't' from tmp_sym where eclsm_sym == tsym)
;
drop table tmp_sym;


! echo "find symbols with more than one non-empty location"
select eclsm_sym tsym
 from ent_chr_loc_sym_mim
 group by 1 having count(*) > 1
 into temp tmp_dup with no log
;

--select ent_chr_loc_sym_mim.*--eclsm_sym, eclsm_chr, eclsm_loc
-- from ent_chr_loc_sym_mim, tmp_dup
-- where  eclsm_sym == tsym
-- order by eclsm_sym, eclsm_chr, eclsm_loc
--;

! echo "Do any of the duplicate Human symbols or accessions exist in ZFIN?"
select ortho_abbrev,ortho_chromosome,ortho_position
 from orthologue, tmp_dup
 where organism == "Human"
   and ortho_abbrev == tsym
union
select ortho_abbrev,ortho_chromosome,ortho_position
 from orthologue, tmp_dup, db_link,ent_chr_loc_sym_mim
 where organism == "Human"
   and ortho_abbrev == tsym
   and dblink_linked_recid ==  zdb_id
   and (eclsm_mim == dblink_acc_num or eclsm_ent == dblink_acc_num)
   order by 1,2,3
;

! echo "drop duplicate Withdrawn symbols"
delete from  ent_chr_loc_sym_mim where exists(
	select 't' from  tmp_dup where eclsm_sym == tsym
);
-- don't drop tmp_dup till later


-- doubt we will allways get away with unique ...
create unique index ent_chr_loc_sym_mim_eclsm_sym_idx on ent_chr_loc_sym_mim(eclsm_sym) in idxdbs3;
create unique index ent_chr_loc_sym_mim_eclsm_ent_idx on ent_chr_loc_sym_mim(eclsm_ent) in idxdbs1;
-- see if the OMIM are PKs... nope
create index ent_chr_loc_sym_mim_eclsm_mim_idx on ent_chr_loc_sym_mim(eclsm_mim) in idxdbs2;
update statistics medium for table ent_chr_loc_sym_mim;

! echo "done prepping the input"

! echo "update Human chromo where NULL"

update orthologue set ortho_chromosome = (
	select eclsm_chr from ent_chr_loc_sym_mim
	 where ortho_abbrev == eclsm_sym
)
 where organism == 'Human'
   and ortho_chromosome is NULL
   and exists (
	select 1 from ent_chr_loc_sym_mim
	 where ortho_abbrev == eclsm_sym
);

! echo "update Human chromo where different"
update orthologue set ortho_chromosome = (
	select eclsm_chr from ent_chr_loc_sym_mim
	 where ortho_abbrev == eclsm_sym
)
 where organism == 'Human'
   and ortho_chromosome is not NULL
   and exists (
	select 1 from ent_chr_loc_sym_mim
	 where ortho_abbrev == eclsm_sym
	   and ortho_chromosome !=  eclsm_chr
);

! echo "update Human location where NULL"
update orthologue set ortho_position = (
	select eclsm_loc from ent_chr_loc_sym_mim
	 where ortho_abbrev == eclsm_sym
)
 where organism == 'Human'
   and ortho_position is NULL
   and exists (
	select 't' from ent_chr_loc_sym_mim
	 where ortho_abbrev == eclsm_sym
);

! echo "update Human location where different"
update orthologue set ortho_position = (
	select eclsm_loc from ent_chr_loc_sym_mim
	 where ortho_abbrev == eclsm_sym
)
 where organism == 'Human'
   and ortho_position is not NULL
   and exists (
	select 't' from ent_chr_loc_sym_mim
	 where ortho_abbrev == eclsm_sym
	   and ortho_position != eclsm_loc
);

--original load yeilded 9,674
! echo "non null Chromos"
select count(*) hum_chr_not_null
 from orthologue
 where organism == 'Human'
  and ortho_chromosome is not NULL
;
-- original load yeilded 9,671
! echo "non null Locations"
select count(*) hum_loc_not_null
 from orthologue
 where organism == 'Human'
  and ortho_position is not NULL
;

! echo "what zfin Human symbols are not in the current Entrez Gene set?"
select c_gene_id[1,25] gene,ortho_abbrev tsym from orthologue
 where organism == 'Human'
 and not exists (
	select 't' from ent_chr_loc_sym_mim
	  where ortho_abbrev == eclsm_sym
) and not exists (
	select 't' from tmp_dup where ortho_abbrev == tsym
);


! echo "check if the Entrez gene accessions agree with Human symbol"
select c_gene_id[1,25] gene, ortho_abbrev[1,15] old_sym, eclsm_sym[1,20] new_sym, eclsm_ent[1,10] entrez_id
 from db_link, orthologue, ent_chr_loc_sym_mim
 where dblink_linked_recid ==  zdb_id
   and organism = "Human"
   and dblink_fdbcont_zdb_id == 'ZDB-FDBCONT-040412-27'
   and dblink_acc_num == eclsm_ent
   and ortho_abbrev != eclsm_sym
;



! echo "find othhologs in ZFIN that are missing a link to Entrez gene"
select  c_gene_id[1,25] gene, eclsm_ent[1,15] entrez_id, eclsm_sym new_sym
 from orthologue, ent_chr_loc_sym_mim
 where organism = "Human"
   and not exists (
	select 't' from db_link entrez
	 where entrez.dblink_linked_recid ==  zdb_id
	   and entrez.dblink_fdbcont_zdb_id == 'ZDB-FDBCONT-040412-27' -- Entrez
) and ortho_abbrev == eclsm_sym
;


-- gives 13 false positives,  defer it to another bugz
-- most likely b/c deleted while isolating for chr/loc
--select  c_gene_id[1,25] gene, eclsm_ent[1,15] entrez_id, eclsm_sym new_sym
-- from orthologue, ent_chr_loc_sym_mim
-- where organism = "Human"
--   and ortho_abbrev == eclsm_sym
--   and not exists (
--	select 't' from db_link entrez
--	 where entrez.dblink_linked_recid ==  zdb_id
--	   and entrez.dblink_fdbcont_zdb_id == 'ZDB-FDBCONT-040412-27' -- Entrez
       -- and entrez.dblink_acc_num == eclsm_ent
--)
--  order by entrez_id
--;


drop table omim_gene;
drop table tmp_dup;
drop table ent_chr_loc_sym_mim;

-- rollback work;
--
commit work;
