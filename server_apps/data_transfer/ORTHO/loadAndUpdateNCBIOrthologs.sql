begin work ;

create temp table tmp_orthos (taxonId int, 
       	    	  	      ncbiGeneId varchar(50),
			      chromosome varchar(10),
			      position varchar(50),
			      symbol varchar(255),
			      name varchar(255),
			      xrefDbname varchar(50),
			      xrefAccNum varchar(50),
			      xref varchar(50),
			      lastUpdated varchar(20))
with no log;



--have to tab delimit the file because pipes are used throughout 
--the chromosome and position fields

load from "<!--|ROOT_PATH|-->/server_apps/data_transfer/ORTHO/parsedOrthos.txt"
 DELIMITER '	'
insert into tmp_orthos;

delete from tmp_orthos
 where taxonId not in ('10090','7227','9606');

update tmp_orthos
  set xrefDbname= 'OMIM'
 where xrefdbname = 'MIM';

create temp table just_ncbi_info (taxonId int, 
       	    	  	      ncbiGeneId varchar(50),
			      chromosome varchar(10),
			      position varchar(50),
			      symbol varchar(255),
			      name varchar(255))
with no log;

create index tmp_chrom_index
 on just_ncbi_info (chromosome)
using btree in idxdbs1;

create index tmp_position_index
 on just_ncbi_info (position)
using btree in idxdbs2;

create index tmp_symbol_index
 on just_ncbi_info(symbol)
 using btree in idxdbs2;

create index tmp_name_index
 on just_ncbi_info(name)
 using btree in idxdbs1;

create index tmp_ncbigeneid_index
 on just_ncbi_info(ncbigeneid)
 using btree in idxdbs3;


insert into just_ncbi_info 
  select distinct taxonId, 
       	    	  	      ncbiGeneId,
			      chromosome,
			      position,
			      symbol,
			      name
  from tmp_orthos;

update statistics high for table just_ncbi_info;
update statistics high for table ncbi_ortholog;

unload to changedSymbols.txt
select ncbiGeneId, name, symbol
  from tmp_orthos, ncbi_ortholog
 where ncbiGeneId = noi_ncbi_gene_id
 and symbol != noi_symbol;

update ortholog
  set ortho_other_species_chromosome = (Select distinct chromosome
				from just_ncbi_info
				where ortho_other_species_ncbi_gene_id = ncbiGeneId)
where exists (Select 'x' from just_ncbi_info
      	     	     where ortho_other_species_ncbi_Gene_id = ncbiGeneId);


update ortholog
  set ortho_other_species_taxid = (Select distinct taxonid
				from just_ncbi_info
				where ortho_other_species_ncbi_gene_id = ncbiGeneId)
where exists (Select 'x' from just_ncbi_info
      	     	     where ortho_other_species_ncbi_Gene_id = ncbiGeneId);

update ortholog
  set ortho_other_species_name = (Select distinct name
				from just_ncbi_info
				where ortho_other_species_ncbi_gene_id = ncbiGeneId)
where exists (Select 'x' from just_ncbi_info
      	     	     where ortho_other_species_ncbi_Gene_id = ncbiGeneId);



update ortholog
  set ortho_other_species_symbol = (Select distinct symbol
				from just_ncbi_info
				where ortho_other_species_ncbi_gene_id = ncbiGeneId)
where exists (Select 'x' from just_ncbi_info
      	     	     where ortho_other_species_ncbi_Gene_id = ncbiGeneId);


update ncbi_ortholog
  set noi_chromosome = (Select distinct chromosome
				from just_ncbi_info
				where noi_ncbi_gene_id = ncbiGeneId)
where exists (Select 'x' from just_ncbi_info
      	     	     where noi_ncbi_Gene_id = ncbiGeneId);



update ncbi_ortholog
  set noi_symbol = (Select distinct symbol
				from just_ncbi_info
				where noi_ncbi_gene_id = ncbiGeneId)
where exists (Select 'x' from just_ncbi_info
      	     	     where noi_ncbi_Gene_id = ncbiGeneId);


update ncbi_ortholog
  set noi_name = (Select distinct name
				from just_ncbi_info
				where noi_ncbi_gene_id = ncbiGeneId)
where exists (Select 'x' from just_ncbi_info
      	     	     where noi_ncbi_Gene_id = ncbiGeneId);

delete from ncbi_ortholog
 where not exists (Select 'x' from just_ncbi_info
       	   	  	  where ncbiGeneId = noi_ncbi_gene_id);

unload to missingNcbiGeneIdsWithOrthos.txt
  select * from ortholog
 where not exists (Select 'x' from ncbi_ortholog
       	   	  	  where noi_ncbi_gene_id = ortho_other_species_ncbi_gene_id);

update ortholog
  set ortho_other_species_ncbi_gene_is_obsolete = 't'
  where not exists (Select 'x' from ncbi_ortholog
  	    	   	   where noi_ncbi_gene_id = ortho_other_species_ncbi_gene_id);

update ortholog
    set ortho_other_species_ncbi_gene_is_obsolete = 'f'
   where  exists (Select 'x' from ncbi_ortholog
  	    	   	   where noi_ncbi_gene_id = ortho_other_species_ncbi_gene_id)
   and ortho_other_species_ncbi_gene_is_obsolete = 't';


select distinct taxonid
  from just_ncbi_info
 where not exists (Select 'x' from organism
       	   	  	  where organism_taxid = taxonid);


insert into ncbi_ortholog (noi_ncbi_gene_id, noi_chromosome,noi_symbol, noi_name, noi_taxid)
 select distinct ncbiGeneId, chromosome, symbol, name, taxonid
   from just_ncbi_info
  where not exists (Select 'x' from ncbi_ortholog
  	    	   	   where ncbigeneid = noi_ncbi_gene_id);


---Now we do external references.

create temp table tmp_ortho_xref (ortho_zdb_id varchar(50), ncbigeneid varchar(50), fdbcont_id varchar(50), xrefDbname varchar(50), xrefaccnum varchar(50), taxonid varchar(50))
with no log;

insert into tmp_ortho_xref (ncbigeneid, xrefdbname, xrefaccnum, taxonid)
 select distinct ncbigeneid, xrefdbname, xrefaccnum, taxonid
   from tmp_orthos;

insert into tmp_ortho_xref (ncbigeneid, xrefdbname, xrefaccnum, taxonid)
 select distinct ncbigeneid, "Gene", ncbigeneid, taxonid
   from tmp_orthos;

create index tmp_ortho_index 
 on tmp_ortho_xref (ortho_Zdb_id)
 using btree in idxdbs1;

create index tmp_ncbigeneid_xref_index 
 on tmp_ortho_xref (ncbigeneid)
 using btree in idxdbs1;

create index tmp_fdbcontid_index 
 on tmp_ortho_xref (fdbcont_id)
 using btree in idxdbs2;

create index tmp_orthoref_index 
 on tmp_ortho_xref (ncbigeneid, xrefaccnum, fdbcont_id)
 using btree in idxdbs2;

update tmp_ortho_xref
  set taxonid = 'Mouse'
 where taxonid = '10090'
;

update tmp_ortho_xref
  set taxonid = 'Human'
 where taxonid = '9606'
;

update tmp_ortho_xref
  set taxonid = 'Fruit fly'
 where taxonid = '7227'
;


update tmp_ortho_xref
  set fdbcont_id = (select fdbcont_zdb_id	
      		     from foreign_db_contains, foreign_db, foreign_db_data_type
		     where fdbcont_fdb_db_id = fdb_db_pk_id
		     and fdbcont_Fdbdt_id = fdbdt_pk_id
		     and fdbdt_data_type = 'ortholog'
		     and fdb_db_name = xrefDbname
		     and fdbcont_organism_common_name = taxonid)
 where fdbcont_id is null;


delete from tmp_ortho_xref
 where fdbcont_id is null;

--unload to orthologExternalReferencesGoingAway.txt
-- select ortho_zdb_id, noi_symbol, oef_accession_number 
--   from ortholog, ortholog_external_reference, ncbi_ortholog
--   where ortho_zdb_id = oef_ortho_Zdb_id
--   and ortho_other_species_ncbi_gene_id = noi_ncbi_gene_id
--   and not exists (Select 'x' from tmp_ortho_xref
--       	   	  	  where oef_accession_number = xrefaccnum
--			  and oef_fdbcont_zdb_id = fdbcont_id);


delete from ncbi_ortholog_external_reference;

delete from ortholog_external_reference
 where exists (Select 'x' from tmp_ortho_xref, ortholog
       	      	      where ncbigeneid = ortho_other_species_ncbi_gene_id
		      and ortholog.ortho_zdb_id = tmp_ortho_xref.ortho_zdb_id);

update statistics high for table tmp_ortho_xref;

insert into ncbi_ortholog_external_reference (noer_other_species_ncbi_gene_id, noer_other_species_accession_number, noer_fdbcont_zdb_id)
  select distinct ncbigeneid, xrefaccnum, fdbcont_id
   from  tmp_ortho_xref
   ;

insert into ortholog_external_reference (oef_ortho_zdb_id, oef_accession_number, oef_fdbcont_zdb_id)
  select distinct ortholog.ortho_zdb_id, xrefaccnum, fdbcont_id
   from ortholog, tmp_ortho_xref
   where ncbigeneid = ortho_other_species_ncbi_gene_id
   and not exists (select 'x' from ortholog_external_reference
       	   	  	  where oef_ortho_zdb_id = ortholog.ortho_zdb_id
			  and oef_accession_number = xrefaccnum
			  and oef_fdbcont_zdb_id = fdbcont_id);


insert into ortholog_load_tracking (olt_load_name,
       	     			    	olt_last_run,
					olt_number_of_mgi_links,
					olt_number_of_hgnc_links,
					olt_number_of_omim_links,
					olt_number_of_gene_links,
					olt_number_of_flybase_links)
select "ncbi ortho load" as namer, current year to second as dater,
( select count(*) as mgi_count
    from ortholog_external_reference, foreign_db_contains, foreign_db
   where oef_fdbcont_zdb_id = fdbcont_zdb_id
   and fdbcont_fdb_db_id = fdb_db_pk_id
   and fdb_db_name = "MGI"),
( select count(*) as hgnc_count
   from ortholog_external_reference, foreign_db_contains, foreign_db
   where oef_fdbcont_zdb_id = fdbcont_zdb_id
   and fdbcont_fdb_db_id = fdb_db_pk_id
   and fdb_db_name = "HGNC"),
 (select count(*) as omim_count
    from ortholog_external_reference, foreign_db_contains, foreign_db
   where oef_fdbcont_zdb_id = fdbcont_zdb_id
   and fdbcont_fdb_db_id = fdb_db_pk_id
   and fdb_db_name = "OMIM"),
 (select count(*) as gene_count
    from ortholog_external_reference, foreign_db_contains, foreign_db
   where oef_fdbcont_zdb_id = fdbcont_zdb_id
   and fdbcont_fdb_db_id = fdb_db_pk_id
   and fdb_db_name = "Gene"),
 (select count(*) as flybase_count
    from ortholog_external_reference, foreign_db_contains, foreign_db
   where oef_fdbcont_zdb_id = fdbcont_zdb_id
   and fdbcont_fdb_db_id = fdb_db_pk_id
   and fdb_db_name = "FLYBASE")
 from single;

unload to ortho_statistics.txt
 delimiter "	"
 select "load name",current year to second,"number of MGI links",
 	"number of HGNC links", "number of OMIM links",
	"number of GENE links", "number of FLYBASE links"
 from single
union
 select olt_load_name,
       	     			    	olt_last_run,
					olt_number_of_mgi_links,
					olt_number_of_hgnc_links,
					olt_number_of_omim_links,
					olt_number_of_gene_links,
					olt_number_of_flybase_links from ortholog_load_tracking
   where olt_last_run > current year to second - 30 units day;

commit work;

--rollback work;