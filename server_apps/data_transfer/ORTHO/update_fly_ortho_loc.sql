-- should not *need* to be in a transaction
-- though if it does not cause problems, I would perfer it
-- note: the regen function called later does produce locks
--
begin work;

create table chr_fb_sym_eg(
	cfse_chr varchar(10),
	cfse_fb varchar(25) PRIMARY KEY,
	cfse_sym varchar(25),
	cfse_eg varchar(15)
) fragment by round robin in tbldbs1,tbldbs2,tbldbs3
;

load from  'fly_chr_id_sym_eg.tab'  delimiter ' '  insert into  chr_fb_sym_eg;

update statistics medium for table chr_fb_sym_eg;
-- use the fb_id to update the symbol and location

! echo "any FlyBase_id in zfin not also in the load?"
select c_gene_id[1,25] gene, dblink_acc_num bad_id, ortho_abbrev
 from orthologue, db_link
 where dblink_linked_recid == zdb_id
   and organism == "Fruit fly"
   and dblink_fdbcont_zdb_id == "ZDB-FDBCONT-040412-21"
   and not exists (
   	select 't' from chr_fb_sym_eg where dblink_acc_num == cfse_fb
);

! echo "any fly orthos in zfin without a FlyBase_id?"
select c_gene_id[1,25] gene, ortho_abbrev
 from orthologue
 where organism == "Fruit fly"
   and not exists (
   	select 't' from db_link
   	 where dblink_linked_recid == zdb_id
   	 and dblink_fdbcont_zdb_id == "ZDB-FDBCONT-040412-21"
);

! echo "any fly symbols in ZFIN need updating bases on FlyBase_id?"
select c_gene_id[1,25] gene, ortho_abbrev old_name, cfse_sym new_name
 from orthologue, db_link, chr_fb_sym_eg
 where dblink_linked_recid == zdb_id
   and organism == "Fruit fly"
   and dblink_fdbcont_zdb_id == "ZDB-FDBCONT-040412-21" -- flybase
   and dblink_acc_num == cfse_fb
   and ortho_abbrev != cfse_sym
;

! echo "any Entrez_id in ZFIN disagree based on FlyBase_id?"
select c_gene_id[1,25] gene, ortho_abbrev fly_sym, dblink_acc_num old_entrez,cfse_eg new_entrez
 from orthologue, db_link eg, chr_fb_sym_eg
 where eg.dblink_linked_recid == zdb_id
   and organism == "Fruit fly"
   and eg.dblink_fdbcont_zdb_id == "ZDB-FDBCONT-040412-23" -- entrez fly
   and eg.dblink_acc_num != cfse_eg
   and ortho_abbrev == cfse_sym
;

! echo "any Fruit fly ortho in ZFIN missing a entrez id?"

select c_gene_id[1,25] gene, ortho_abbrev fly_symbol, cfse_eg missing_entrez
 from orthologue, chr_fb_sym_eg
 where organism == "Fruit fly"
   and cfse_eg is not null
   and ortho_abbrev == cfse_sym
   and not exists (
   	select 't' from db_link
   	 where dblink_linked_recid == zdb_id
   	   and dblink_fdbcont_zdb_id == "ZDB-FDBCONT-040412-23"
   	   and dblink_acc_num == cfse_eg
);

-----------------------------------------------------------------
-- what we actually came here to do
! echo "update Fruit fly chromo where was NULL"

update orthologue set ortho_chromosome = (
	select cfse_chr from chr_fb_sym_eg
	 where ortho_abbrev == cfse_sym
)
 where organism == 'Fruit fly'
   and ortho_chromosome is NULL
   and exists (
	select 1 from chr_fb_sym_eg
	 where ortho_abbrev == cfse_sym
);

! echo "update Fruit fly chromo where changed"
update orthologue set ortho_chromosome = (
	select cfse_chr from chr_fb_sym_eg
	 where ortho_abbrev == cfse_sym
)
 where organism == 'Fruit fly'
   and ortho_chromosome is not NULL
   and exists (
	select 1 from chr_fb_sym_eg
	 where ortho_abbrev == cfse_sym
	   and ortho_chromosome !=  cfse_chr
);

---------------------------------------
--  location are ommited delibertly  --
---------------------------------------

drop table chr_fb_sym_eg;

--rollback work;
--
commit work;