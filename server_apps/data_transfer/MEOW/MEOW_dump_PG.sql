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
--   zfin_orthos.txt -- all known orthologs, indexed by gene_id
--   zfin_refs.txt -- all publications linked to genes, indexed by gene id
--   zfin_dblinks -- all links from genes to sequence DBs.
--   zfin_ortholinks -- similar to zfin_dblinks but is links from ortho to
--       their species DB files.
--   zfin_genes_relationships.txt - this file lists genes and 'related' markers
 
 
-- Create the main zfin_genes file

begin work;


create temp table meow_exp1_dup (
  zdb_id text,
  mname varchar(255),
  abbrev varchar(40),
  OR_lg varchar(2),
  source text
) ;

-- get panel mappings
insert into meow_exp1_dup 
  select distinct mrkr_zdb_id, mrkr_name, mrkr_abbrev, mm_chromosome, p.zdb_id
    from marker, marker_type_group_member, mapped_marker, panels p
     where mtgrpmem_mrkr_type=mrkr_type and mtgrpmem_mrkr_type_group='GENEDOM'
     and mrkr_zdb_id = marker_id
     and marker_type <> 'SNP'
     and refcross_id = p.zdb_id;

insert into meow_exp1_dup 
  select distinct a.mrkr_zdb_id, a.mrkr_name, a.mrkr_abbrev, mm_chromosome, p.zdb_id
    from marker a, marker b, marker_type_group_member, mapped_marker, marker_relationship, panels p
   where mtgrpmem_mrkr_type=a.mrkr_type and mtgrpmem_mrkr_type_group='GENEDOM'
     and b.mrkr_zdb_id = marker_id
     and a.mrkr_zdb_id = mrel_mrkr_1_zdb_id
     and b.mrkr_zdb_id = mrel_mrkr_2_zdb_id
     and refcross_id = p.zdb_id;

create temp table meow_exp1 (
  zdb_id text,
  mname varchar(255),
  abbrev varchar(40),
  OR_lg varchar(2),
  source_zdb_id text
) ;

insert into  meow_exp1 
  select distinct * 
    from meow_exp1_dup;

drop table meow_exp1_dup;

-- get independent linkages

-- mappings derived from markers

insert into meow_exp1 
  select distinct a.mrkr_zdb_id, a.mrkr_name, a.mrkr_abbrev, lnkg_chromosome, recattrib_source_zdb_id
    from marker a, marker b, marker_type_group_member, linkage_member, linkage, marker_relationship, record_attribution
   where b.mrkr_zdb_id = lnkgmem_member_zdb_id 
     and lnkgmem_linkage_zdb_id = lnkg_zdb_id 
     and mtgrpmem_mrkr_type=a.mrkr_type and mtgrpmem_mrkr_type_group='GENEDOM'
     and a.mrkr_zdb_id = mrel_mrkr_1_zdb_id
     and b.mrkr_zdb_id = mrel_mrkr_2_zdb_id
     and recattrib_data_zdb_id = lnkg_zdb_id;

-- mappings derived from clones- Sanger gene mapping data is derived from clone mapping data 

insert into meow_exp1 
  select distinct a.mrkr_zdb_id, a.mrkr_name, a.mrkr_abbrev, lnkg_chromosome, recattrib_source_zdb_id
    from marker a, marker b, marker_type_group_member, linkage_member, linkage,marker_relationship, record_attribution
   where b.mrkr_zdb_id = lnkgmem_member_zdb_id 
     and lnkgmem_linkage_zdb_id = lnkg_zdb_id 
     and mtgrpmem_mrkr_type=a.mrkr_type and mtgrpmem_mrkr_type_group='GENEDOM'
     and a.mrkr_zdb_id = mrel_mrkr_2_zdb_id
     and b.mrkr_zdb_id = mrel_mrkr_1_zdb_id
     and mrel_type = 'clone contains gene'
     and recattrib_data_zdb_id = lnkg_zdb_id;

insert into meow_exp1 
  select distinct mrkr_zdb_id, mrkr_name, mrkr_abbrev, lnkg_chromosome, recattrib_source_zdb_id
    from marker, marker_type_group_member, linkage_member, linkage, record_attribution
   where mrkr_zdb_id = lnkgmem_member_zdb_id 
     and lnkgmem_linkage_zdb_id = lnkg_zdb_id 
     and mtgrpmem_mrkr_type=mrkr_type and mtgrpmem_mrkr_type_group='GENEDOM'
     and recattrib_data_zdb_id = lnkg_zdb_id;

--  Add in  unmapped genes
insert into meow_exp1 
  select mrkr_zdb_id,mrkr_name,mrkr_abbrev,'0','0'
    from marker,marker_type_group_member
   where mtgrpmem_mrkr_type=mrkr_type and mtgrpmem_mrkr_type_group='GENEDOM'
     and not exists (
                select 't'
                  from linkage_member
                 where lnkgmem_member_zdb_id = mrkr_zdb_id
                 )
     and not exists (
                select 't'
                 from mapped_marker where marker_id = mrkr_zdb_id
                 )
     and not exists (
                select 't'
                  from mapped_marker, marker_relationship
                 where mrel_mrkr_2_zdb_id = marker_id
                  and mrel_mrkr_1_zdb_id = mrkr_zdb_id
                )
      and not exists (
                select 't'
                  from linkage_member, marker_relationship
                 where mrel_mrkr_1_zdb_id = lnkgmem_member_zdb_id
                 and mrel_type = 'clone contains gene'
                 and mrel_mrkr_2_zdb_id = mrkr_zdb_id
                );

update meow_exp1
        set source_zdb_id = null
        where source_zdb_id = '0';

create index meow_exp1_zdb_id_idx on meow_exp1(zdb_id);


--  Okay, now write it to a file

\copy (select distinct * from meow_exp1 order by abbrev, source_zdb_id) to 'zfin_genes.txt' with DELIMITER '	'


-- Create the file of known correspondences

\copy (select distinct mrkr_zdb_id, mrkr_abbrev from feature_marker_relationship, marker where fmrel_mrkr_zdb_id = mrkr_zdb_id and fmrel_type = 'is allele of') to 'zfin_genes_mutants.txt' with DELIMITER '	';

-- NOW let's create the table of pubs associated with these genes.

\copy  (select recattrib_data_zdb_id, zdb_id, title, authors, pub_date, jrnl_abbrev||pub_volume||':'||pub_pages as source, accession_no from publication, record_attribution, journal where zdb_id = recattrib_source_zdb_id and jrnl_zdb_id= pub_jrnl_zdb_id and exists (select 't' from meow_exp1 where zdb_id =recattrib_data_zdb_id)) to 'zfin_pubs.txt' with DELIMITER '	';

-- Now the ortholog!
create temp table meow_exp3 (
  gene_id text,
  organism varchar(30),
  ortho_name varchar(255),
  ortho_abbrev varchar(15), 
  ortho_id text
) ;

insert into meow_exp3 
  select ortho_zebrafish_gene_zdb_id, organism_common_name, ortho_other_species_name, ortho_other_species_symbol, ortho_zdb_id
    from ortholog, organism
   where exists (select 't' from meow_exp1 where zdb_id = ortho_zebrafish_gene_zdb_id)
   and organism_taxid = ortho_other_species_taxid;

\copy (select * from meow_exp3) to 'zfin_orthos.txt' with DELIMITER '	';

\copy (select ortho_zebrafish_gene_zdb_id, fdb_db_name, oef_accession_number from ortholog, ortholog_external_reference, foreign_db_contains, foreign_db where exists (select 't' from meow_exp3 where ortho_id = ortho_zdb_id) and oef_fdbcont_zdb_id = fdbcont_zdb_id and fdbcont_fdb_db_id = fdb_db_pk_id and ortho_zdb_id = oef_ortho_zdb_id) to 'zfin_ortholinks.txt' with DELIMITER '	';

drop table meow_exp3;

-- And now the links to sequence DBs
create temp table tmp_out as
select dblink_linked_recid, fdb_db_name, dblink_acc_num 
from db_link, foreign_db_contains, foreign_db 
where exists (select 't' from meow_exp1 where zdb_id = dblink_linked_recid) 
and fdbcont_fdb_db_id = fdb_db_pk_id 
and dblink_fdbcont_zdb_id = fdbcont_zdb_id union 
  select  mrel_mrkr_1_zdb_id, fdb_db_name, dblink_acc_num 
    from db_link , marker_relationship, foreign_db_contains, foreign_db
   where mrel_mrkr_2_zdb_id = dblink_linked_recid
        and dblink_fdbcont_zdb_id = fdbcont_zdb_id
        and fdbcont_fdb_db_id = fdb_db_pk_id
     and fdb_db_name = 'GenBank'
     and mrel_type != 'gene produces transcript' -- supporting evidence
     and exists (select 't' from meow_exp1 where mrel_mrkr_1_zdb_id =zdb_id) ;

\copy (select * from tmp_out) to 'zfin_dblinks.txt' with DELIMITER '	';

create temp table tmp_sc_out as 
select distinct mrkr_zdb_id, mrkr_abbrev, dblink_acc_num
    from marker
    left outer join (db_link left outer join foreign_db_contains on fdbcont_zdb_id = dblink_fdbcont_zdb_id left outer join foreign_db on fdb_db_pk_id = fdbcont_fdb_db_id and fdb_db_name = 'GenBank') on dblink_linked_recid = mrkr_zdb_id
    where mrkr_type = 'EST';

\copy (select * from tmp_sc_out order by mrkr_zdb_id) to 'SC.txt' with DELIMITER '	';

create temp table tmp_sc_sts as select distinct mrkr_zdb_id, mrkr_abbrev, dblink_acc_num
    from marker, db_link, foreign_db_contains, foreign_db
    where mrkr_type in ('STS', 'SSLP','RAPD')
      and dblink_linked_recid = mrkr_zdb_id
      and dblink_fdbcont_zdb_id = fdbcont_zdb_id
      and fdb_db_name = 'GenBank'
      and fdbcont_fdb_db_id= fdb_db_pk_id
    ;

\copy (select * from tmp_sc_sts order by mrkr_zdb_id) to 'SC_sts.txt' with DELIMITER '	';

\copy (select zrepld_old_zdb_id, zrepld_new_zdb_id from zdb_replaced_data) to 'zdb_history.txt' with DELIMITER '	';

-- generate a file with genes that have expression data in ZFIN
-- NCBI will use the gene symbol to link to xpatselect page.

\copy (select xpatex_gene_zdb_id, mrkr_abbrev from expression_experiment2 join marker on mrkr_zdb_id = xpatex_gene_zdb_id) to 'xpat.txt' with DELIMITER '	';


\copy ( select zdb_id, abbrev, metric from panels) to 'panels.txt' with DELIMITER '	';

create temp table tmp_sanger_mappings as select distinct pm.target_id, pm.zdb_id, pm.abbrev, pm.OR_lg, pm.lg_location,
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
        from paneled_markers pm
        left outer join mapped_marker mm on pm.zdb_id = mm.marker_id and pm.target_id = mm.refcross_id and pm.lg_location = mm.mm_chrom_location     and mm.map_name = pm.map_name and mm.metric = pm.metric
        where substring(pm.zdb_id,1,8) <> 'ZDB-ALT-'
        order by 1;

\copy (select * from tmp_sanger_mappings) to 'sanger_mappings.txt' with DELIMITER '	' ;

\copy (select distinct target_id, zdb_id, abbrev, OR_lg, lg_location from paneled_markers where substring(zdb_id,1,7) <> 'ZDB-ALT' order by 1) to 'mappings.txt' with DELIMITER '	'

--- generate file with zmap mapping data

\copy (select zdb_id, abbrev, abbrevp, panel_id, zmap_chromosome, lg_location from zmap_pub_pan_mark) to 'zmap_mappings.txt' with DELIMITER '	';

\copy (select distinct zdb_id, abbrev from paneled_markers) to 'markers.txt' with DELIMITER '	';

\copy (select distinct mrkr_zdb_id, dalias_alias from marker, data_alias where mrkr_zdb_id = dalias_data_zdb_id order by 1) to 'marker_alias.txt' with DELIMITER '	';

\copy (select substring(dblink_acc_num,1,20) as ottdarg,substring(mrkr_zdb_id,1,25) as zdb_id, mrkr_abbrev symbol, dalias_alias alias from db_link join marker on mrkr_zdb_id = dblink_linked_recid left outer join data_alias on mrkr_zdb_id = dalias_data_zdb_id where dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-14' order by 1) to 'sanger_alias.txt' with DELIMITER '	';

\copy (select distinct mrel_mrkr_1_zdb_id, mrel_mrkr_2_zdb_id from marker_relationship, meow_exp1 where substring(mrel_type,1,4) = 'gene' and mrel_mrkr_1_zdb_id = meow_exp1.zdb_id order by 1) to 'gene_relationships.txt' with DELIMITER '	';
 
drop table meow_exp1;

commit work;

