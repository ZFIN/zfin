-- Command file to dump out GENE information for the MEOW server. Produces
-- 11 files in the FTP pub/transfer/MEOW directory.  This is currently (2001/02)
-- run as a cron job every week.  Don Gilbert at MEOW is then responsible for
-- picking up the files.  Lynn Schriml at NCBI also uses these files.  
-- MEOW *probably* picks up the files through FTP, but
-- they could also pick them up through HTTP via the home/transfer directory,
-- which is a symbolic link to the FTP directory.

-- Here are the files:
--   zfin_genes.txt -- the main file with all ZFIN genes.(mapped and unmapped)
--   zfin_genes_mutants.txt - this file list known correspondences between genes and mutants
--   zfin_orthos.txt -- all known orthologues, indexed by gene_id
--   zfin_refs.txt -- all publications linked to genes, indexed by gene id
--   zfin_dblinks -- all links from genes to sequence DBs.
--   zfin_ortholinks -- similar to zfin_dblinks but is links from ortho to
--       their species DB files.
--   zfin_genes_relationships.txt - this file lists genes and 'related' markers
 
 
-- Create the main zfin_genes file

create temp table meow_exp1_dup (
  zdb_id varchar(50),
  mname varchar(255),
  abbrev varchar(40),
  OR_lg varchar(2),
  source varchar(50)
) with no log;

-- get panel mappings
insert into meow_exp1_dup 
  select distinct mrkr_zdb_id, mrkr_name, mrkr_abbrev, or_lg, p.zdb_id
    from marker, mapped_marker, panels p
   where mrkr_type like 'GENE%'
     and mrkr_zdb_id = marker_id
     and marker_type <> 'SNP'
     and refcross_id = p.zdb_id;

insert into meow_exp1_dup 
  select distinct a.mrkr_zdb_id, a.mrkr_name, a.mrkr_abbrev, or_lg, p.zdb_id
    from marker a, marker b, mapped_marker, marker_relationship, panels p
   where a.mrkr_type like 'GENE%'
     and b.mrkr_zdb_id = marker_id
     and a.mrkr_zdb_id = mrel_mrkr_1_zdb_id
     and b.mrkr_zdb_id = mrel_mrkr_2_zdb_id
     and refcross_id = p.zdb_id;

create temp table meow_exp1 (
  zdb_id varchar(50),
  mname varchar(255),
  abbrev varchar(40),
  OR_lg varchar(2),
  source_zdb_id varchar(50)
) with no log;

insert into  meow_exp1 
  select distinct * 
    from meow_exp1_dup;

-- get independent linkages

-- mappings derived from markers

insert into meow_exp1 
  select distinct a.mrkr_zdb_id, a.mrkr_name, a.mrkr_abbrev, lnkg_or_lg, recattrib_source_zdb_id
    from marker a, marker b, linkage_member, linkage, marker_relationship, record_attribution
   where b.mrkr_zdb_id = lnkgmem_member_zdb_id 
     and lnkgmem_linkage_zdb_id = lnkg_zdb_id 
     and a.mrkr_type like 'GENE%'
     and a.mrkr_zdb_id = mrel_mrkr_1_zdb_id
     and b.mrkr_zdb_id = mrel_mrkr_2_zdb_id
     and recattrib_data_zdb_id = lnkg_zdb_id;

-- mappings derived from clones- Sanger gene mapping data is derived from clone mapping data 

insert into meow_exp1 
  select distinct a.mrkr_zdb_id, a.mrkr_name, a.mrkr_abbrev, lnkg_or_lg, recattrib_source_zdb_id
    from marker a, marker b, linkage_member, linkage,marker_relationship, record_attribution
   where b.mrkr_zdb_id = lnkgmem_member_zdb_id 
     and lnkgmem_linkage_zdb_id = lnkg_zdb_id 
     and a.mrkr_type like 'GENE%'
     and a.mrkr_zdb_id = mrel_mrkr_2_zdb_id
     and b.mrkr_zdb_id = mrel_mrkr_1_zdb_id
     and mrel_type = 'clone contains gene'
     and recattrib_data_zdb_id = lnkg_zdb_id;

insert into meow_exp1 
  select distinct mrkr_zdb_id, mrkr_name, mrkr_abbrev, lnkg_or_lg, recattrib_source_zdb_id
    from marker, linkage_member, linkage, record_attribution
   where mrkr_zdb_id = lnkgmem_member_zdb_id 
     and lnkgmem_linkage_zdb_id = lnkg_zdb_id 
     and mrkr_type like 'GENE%'
     and recattrib_data_zdb_id = lnkg_zdb_id;

--  Add in  unmapped genes
insert into meow_exp1 
  select mrkr_zdb_id,mrkr_name,mrkr_abbrev,'0','0'
    from marker
   where mrkr_type like 'GENE%'
     and mrkr_zdb_id not in (
		select lnkgmem_member_zdb_id
		  from linkage_member
		 )
     and mrkr_zdb_id not in (
		select marker_id
		  from mapped_marker
		 )
     and mrkr_zdb_id not in (
		select mrel_mrkr_1_zdb_id
                  from mapped_marker, marker_relationship	
		 where mrel_mrkr_2_zdb_id = marker_id
		)
      and mrkr_zdb_id not in (
		select mrel_mrkr_2_zdb_id
                  from linkage_member, marker_relationship	
		 where mrel_mrkr_1_zdb_id = lnkgmem_member_zdb_id
                 and mrel_type = 'clone contains gene'
		);

update meow_exp1
	set source_zdb_id = null
	where source_zdb_id = '0';

--  Okay, now write it to a file
UNLOAD to '<!--|FTP_ROOT|-->/pub/transfer/MEOW/zfin_genes.txt' 
  DELIMITER "	" 
  select distinct *
    from meow_exp1 order by abbrev, source_zdb_id;


-- Create the file of known correspondences
create temp table meow_mutant (
  gene_id varchar(50),
  gene_abbrev varchar(40)
) with no log;  


insert into meow_mutant (gene_id,gene_abbrev)
   select distinct mrkr_zdb_id, mrkr_abbrev
     from feature_marker_relationship, marker
    where fmrel_mrkr_zdb_id = mrkr_zdb_id
      and fmrel_type = "is allele of";

UNLOAD to '<!--|FTP_ROOT|-->/pub/transfer/MEOW/zfin_genes_mutants.txt' 
  DELIMITER "	" 
  select * 
    from meow_mutant;


-- NOW let's create the table of pubs associated with these genes.
create temp table meow_exp2 (
  gene_id varchar(50),
  zdb_id varchar(50),
  title lvarchar,
  authors lvarchar ,
  pub_date date ,
  source_zdb_id lvarchar ,
  accession_no varchar(80)
) with no log;

insert into meow_exp2 
  select recattrib_data_zdb_id, zdb_id, title, authors, pub_date, jrnl_abbrev||pub_volume||':'||pub_pages as source, 
	accession_no 
    from publication, record_attribution, journal
   where zdb_id = recattrib_source_zdb_id
     and jrnl_zdb_id= pub_jrnl_zdb_id
     and recattrib_data_zdb_id in
       ( select zdb_id 
           from meow_exp1 ) ;

UNLOAD to '<!--|FTP_ROOT|-->/pub/transfer/MEOW/zfin_pubs.txt' 
  DELIMITER "	" 
  select * 
    from meow_exp2;

-- Now the orthologues!
create temp table meow_exp3 (
  gene_id varchar(50),
  organism varchar(30),
  ortho_name varchar(255),
  ortho_abbrev varchar(15), 
  ortho_id varchar(50)
) with no log;

insert into meow_exp3 
  select c_gene_id, organism, ortho_name, ortho_abbrev, zdb_id
    from orthologue 
   where c_gene_id in 
       ( select zdb_id 
           from meow_exp1 );

UNLOAD to '<!--|FTP_ROOT|-->/pub/transfer/MEOW/zfin_orthos.txt' 
  DELIMITER "	" 
  select * 
    from meow_exp3;

-- And now the links to sequence DBs
create temp table meow_exp4 (
  gene_id varchar(50),
  DB_name varchar(50),
  acc_num varchar(50)
) with no log;

insert into meow_exp4 
  select dblink_linked_recid, fdbcont_fdb_DB_name, dblink_acc_num 
    from db_link, foreign_db_contains
   where dblink_linked_recid in 
       ( select zdb_id 
           from meow_exp1 )
     and dblink_fdbcont_zdb_id = fdbcont_zdb_id;

insert into meow_exp4 
  select  mrel_mrkr_1_zdb_id, fdbcont_fdb_DB_name, dblink_acc_num 
    from db_link , marker_relationship, foreign_db_contains
   where   mrel_mrkr_2_zdb_id = dblink_linked_recid
        and dblink_fdbcont_zdb_id = fdbcont_zdb_id
        and fdbcont_fdb_db_name = 'GenBank'
	and mrel_mrkr_1_zdb_id in 
       ( select zdb_id 
           from meow_exp1 );

UNLOAD to '<!--|FTP_ROOT|-->/pub/transfer/MEOW/zfin_dblinks.txt' 
  DELIMITER "	" 
  select * 
    from meow_exp4;

--  generate the ortho_links file with DB_links to other species DBs
create temp table meow_exp5 (
  linked_recid varchar(50), 
  DB_name varchar(50), 
  acc_num varchar(50)
) with no log;

insert into meow_exp5
  select dblink_linked_recid, fdbcont_fdb_DB_name, dblink_acc_num
    from db_link, foreign_db_contains
   where dblink_linked_recid in 
       ( select ortho_id 
         from meow_exp3 )
     and dblink_fdbcont_zdb_id = fdbcont_zdb_id;

UNLOAD to '<!--|FTP_ROOT|-->/pub/transfer/MEOW/zfin_ortholinks.txt' 
  DELIMITER "	" 
  select * 
    from meow_exp5;


-- generate a file of cDNAs and assoc GenBank accession numbers

UNLOAD to '<!--|FTP_ROOT|-->/pub/transfer/MEOW/SC.txt'
  DELIMITER "	"
  select distinct mrkr_zdb_id, mrkr_abbrev, dblink_acc_num
    from marker, OUTER (db_link, foreign_db_contains)
    where (mrkr_type = 'EST')
      and dblink_linked_recid = mrkr_zdb_id
      and dblink_fdbcont_zdb_id = fdbcont_zdb_id
      and fdbcont_fdb_db_name = 'GenBank'
    order by mrkr_zdB_id; 

-- generate a file of anonymous markers  and assoc GenBank accession numbers
UNLOAD to '<!--|FTP_ROOT|-->/pub/transfer/MEOW/SC_sts.txt'
  DELIMITER "	"
  select distinct mrkr_zdb_id, mrkr_abbrev, dblink_acc_num
    from marker, db_link, foreign_db_contains
    where mrkr_type in ('STS', 'SSLP','RAPD')
      and dblink_linked_recid = mrkr_zdb_id
      and dblink_fdbcont_zdb_id = fdbcont_zdb_id
      and fdbcont_fdb_db_name = 'GenBank'
    order by mrkr_zdb_id; 

-- generate a file with zdb history data

UNLOAD to '<!--|FTP_ROOT|-->/pub/transfer/MEOW/zdb_history.txt'
 DELIMITER "	" select zrepld_old_zdb_id, zrepld_new_zdb_id from zdb_replaced_data;

-- generate a file with genes that have expression data in ZFIN
-- NCBI will use the gene symbol to link to xpatselect page.

UNLOAD to '<!--|FTP_ROOT|-->/pub/transfer/MEOW/xpat.txt'
 DELIMITER "	"  select xpatex_gene_zdb_id, mrkr_abbrev
		     from expression_experiment
			  join marker on mrkr_zdb_id = xpatex_gene_zdb_id;

--- generate mapping data for NCBI
 
UNLOAD to '<!--|FTP_ROOT|-->/pub/transfer/MEOW/panels.txt' 
  DELIMITER "	" select zdb_id, abbrev, metric from panels; 

UNLOAD to '<!--|FTP_ROOT|-->/pub/transfer/MEOW/sanger_mappings.txt' 
  DELIMITER "	" select distinct pm.target_id, pm.zdb_id, pm.abbrev, pm.OR_lg, pm.lg_location,
        case
            when target_id in('ZDB-REFCROSS-980521-11','ZDB-REFCROSS-000320-1')
                then 1
            when target_id = 'ZDB-REFCROSS-990426-6'
                then 2
            when target_id = 'ZDB-REFCROSS-990707-1' 
                and owner in ('ZDB-PERS-971016-22','ZDB-PERS-971205-2')
                then 2
            else 3
        end
        from paneled_markers pm, outer mapped_marker mm  
        where pm.zdb_id[1,8] <> ('ZDB-ALT-') 
        and mm.marker_id   == pm.zdb_id
        and mm.refcross_id == pm.target_id
        and mm.or_lg       == pm.or_lg
        and mm.lg_location == pm.lg_location
        and mm.map_name    == pm.map_name
        and mm.metric      == pm.metric
        order by 1;

UNLOAD to '<!--|FTP_ROOT|-->/pub/transfer/MEOW/mappings.txt' 
  DELIMITER "	" select distinct target_id, zdb_id, abbrev, OR_lg, lg_location from paneled_markers where zdb_id not like 'ZDB-ALT%' order by 1;


--- generate file with zmap mapping data
UNLOAD to '<!--|FTP_ROOT|-->/pub/transfer/MEOW/zmap_mappings.txt' 
 DELIMITER "	"  select zdb_id, abbrev, abbrevp, panel_id, or_lg, lg_location from zmap_pub_pan_mark;

UNLOAD to '<!--|FTP_ROOT|-->/pub/transfer/MEOW/markers.txt' 
  DELIMITER "	" select distinct zdb_id, abbrev from paneled_markers;

UNLOAD to '<!--|FTP_ROOT|-->/pub/transfer/MEOW/marker_alias.txt' 

  DELIMITER "	" select distinct mrkr_zdb_id, dalias_alias from marker , data_alias where mrkr_zdb_id = dalias_data_zdb_id order by 1;

--- generate alias file for Sanger

UNLOAD to '<!--|FTP_ROOT|-->/pub/transfer/MEOW/sanger_alias.txt' 
DELIMITER "	" select dblink_acc_num[1,20]ottdarg,mrkr_zdb_id[1,25] zdbid ,mrkr_abbrev symbol,
dalias_alias alias
 from db_link,marker, outer data_alias
 where dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-14'
 and dblink_linked_recid = mrkr_zdb_id
 and dblink_linked_recid = dalias_data_zdb_id
order by 1
;

--- generate marker relationship file

UNLOAD to '<!--|FTP_ROOT|-->/pub/transfer/MEOW/gene_relationships.txt'
 
  DELIMITER "	" select distinct mrel_mrkr_1_zdb_id, mrel_mrkr_2_zdb_id from marker_relationship, meow_exp1 where mrel_type like 'gene%' and mrel_mrkr_1_zdb_id = meow_exp1.zdb_id order by 1;



