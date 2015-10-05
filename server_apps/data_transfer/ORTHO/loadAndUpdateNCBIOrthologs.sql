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

create index ncbi_gene_id_tmp_index
 on tmp_orthos(ncbiGeneId)
 using btree in idxdbs2;

create index ncbi_symbol_tmp_index
  on tmp_orthos (symbol)
 using btree in idxdbs1;

unload to changedSymbols.txt
select ncbiGeneId, name, symbol
  from tmp_orthos, ncbi_ortholog
 where ncbiGeneId = noi_ncbi_gene_id
 and symbol != noi_symbol;

update ncbi_ortholog
  set noi_chromosome = (Select distinct chromosome
				from tmp_orthos
				where noi_ncbi_gene_id = ncbiGeneId)
where exists (Select 'x' from tmp_orthos
      	     	     where noi_ncbi_Gene_id = ncbiGeneId);

update ncbi_ortholog
  set noi_position = (Select distinct position
				from tmp_orthos
				where noi_ncbi_gene_id = ncbiGeneId)
where exists (Select 'x' from tmp_orthos
      	     	     where noi_ncbi_Gene_id = ncbiGeneId);



update ncbi_ortholog
  set noi_symbol = (Select distinct symbol
				from tmp_orthos
				where noi_ncbi_gene_id = ncbiGeneId)
where exists (Select 'x' from tmp_orthos
      	     	     where noi_ncbi_Gene_id = ncbiGeneId);


update ncbi_ortholog
  set noi_name = (Select distinct name
				from tmp_orthos
				where noi_ncbi_gene_id = ncbiGeneId)
where exists (Select 'x' from tmp_orthos
      	     	     where noi_ncbi_Gene_id = ncbiGeneId);


unload to missingNCBIIds.txt
 select noi_ncbi_gene_id, noi_symbol
   from ncbi_ortholog
 where not exists (Select 'x' from tmp_orthos
       	   	  	  where ncbiGeneId = noi_ncbi_gene_id);

delete from ncbi_ortholog
 where not exists (Select 'x' from tmp_orthos
       	   	  	  where ncbiGeneId = noi_ncbi_gene_id);


insert into ncbi_ortholog (noi_ncbi_gene_id, noi_chromosome, noi_position, noi_symbol, noi_name, noi_taxid)
 select distinct ncbiGeneId, chromosome, position, symbol, name, taxonid
   from tmp_orthos
  where not exists (Select 'x' from ncbi_ortholog
  	    	   	   where ncbigeneid = noi_ncbi_gene_id);


---Now we do external references.

create temp table tmp_ortho_xref (ortho_zdb_id varchar(50), ncbigeneid varchar(50), fdbcont_id varchar(50), xrefDbname varchar(50), xrefaccnum varchar(50))
with no log;

insert into tmp_ortho_xref (ncbigeneid, xrefdbname, xrefaccnum)
 select distinct ncbigeneid, xrefdbname, xrefaccnum
   from tmp_orthos;

update tmp_ortho_xref
  set fdbcont_id = (select fdbcont_zdb_id	
      		     from foreign_db_contains, foreign_db, foreign_db_data_type
		     where fdbcont_fdb_db_id = fdb_db_pk_id
		     and fdbcont_Fdbdt_id = fdbdt_pk_id
		     and fdbdt_data_type = 'orthologue'
		     and fdb_db_name = xrefDbname);


delete from tmp_ortho_xref
 where fdbcont_id is null;

unload to orthologExternalReferencesGoingAway.txt
 select ortho_zdb_id, ortho_symbol, oef_accession_number 
   from ortholog, ortholog_external_reference, ncbi_ortholog
   where ortho_zdb_id = oef_ortho_Zdb_id
   and ortho_other_species_ncbi_gene_id = noi_ncbi_gene_id
   and not exists (Select 'x' from tmp_ortho_xref
       	   	  	  where oef_accession_number = xrefaccnum
			  and oef_fdbcont_zdb_id = fdbcont_id);


delete from ortholog_external_reference;
  

insert into ortholog_external_reference (oef_ortho_zdb_id, oef_accession_number, oef_fdbcont_zdb_id)
  select ortho_zdb_id, xrefaccnum, fdbcont_id
   from ortholog, tmp_ortho_xref
   where ncbigeneid = ortho_other_species_ncbi_gene_id
   ;






commit work;

--rollback work;