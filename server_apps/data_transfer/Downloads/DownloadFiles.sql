-- Script to create data files for public download.
--
-- We extract several different kinds of information:
--
-- All genetic markers (includes genes, ests, sslps, etc.)
--	zfin id, name, symbol, type, OR_lg
--
-- Synonyms  (for any item in all genetic markers file) There may be multiple lines
--   per zfin id
--	zfin id, synonym
--
-- Orthology - separate files for: D
--   zebrafish - human
--	zfin id , zebrafish symbol, human symbol, OMIM id, Entrez Gene id
--   zebrafish - mouse
--	zfin id , zebrafish symbol, mouse symbol, MGI id, Entrez Gene id
--   zebrafish - fly
--	zfin id,  zebrafish symbol, fly symbol,  Flybase id
--   zebrafish - yeast
--	zfin id,  zebrafish symbol, yeast symbol,  SGD id
--
-- Gene Onotology-
--	A copy of the file we send to GO.
--
-- Gene Expression
--	gene zfin id , gene symbol, probe zfin id, probe name, expression type,
--      expression pattern zfin id, pub zfin id, genotype zfin id,
--      experiment zfin id
--
-- Mapping data
--	zfin id, symbol, panel symbol, LG, loc, metric
--
-- Sequence data - separate files for GenBank, RefSeq, Entrez Gene, Unigene,
-- UniProt, Interpro, GenPept and Vega (genes and transcripts) 1:1 Ensembl ID
-- as well as sequences indirectly associated with genes
--	zfin id, symbol, accession number
--
-- Genotypes
--	zfin id, allele/construct, type, gene symblol, corresponding zfin gene id
--
-- create genetic markers file
--
-- Morpholino data
--      zfin id of gene, gene symbol, zfin id of MO, MO symbol, public note
-- Marker Relationship data
--	marker1 id, marker1 symbol, marker 2 id, marker 2 symbol, relationship

UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/genetic_markers.txt'
  DELIMITER "	"
  select mrkr_zdb_id, mrkr_abbrev, mrkr_name, mrkr_type
    from marker order by 1;

-- create other names file

  select mrkr_zdb_id as current_id, mrkr_name as current_name,
		mrkr_abbrev as current_abbrev, dalias_alias as alias
    from marker, data_alias
    where dalias_data_zdb_id = mrkr_zdb_id
  union
   select feature_zdb_id, feature_name,feature_abbrev, dalias_alias
    from feature, data_alias
    where feature_zdb_id = dalias_data_zdb_id
  union
   select geno_zdb_id, geno_display_name, geno_handle, dalias_alias
    from genotype, data_alias
    where dalias_data_zdb_id = geno_Zdb_id
  into temp tmp_alias;


UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/aliases.txt'
  DELIMITER "	" select distinct current_id, current_name,
				  current_abbrev, alias
                    from tmp_alias
		    order by current_id, alias;


-- Create marker realtionship file

UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/gene_marker_relationship.txt'
 DELIMITER "	"  select gene.mrkr_zdb_id, gene.mrkr_abbrev,
			  seq.mrkr_zdb_id, seq.mrkr_abbrev,
			  mrel_type
		     from marker_relationship, marker gene, marker seq
		     where gene.mrkr_type[1,4] = 'GENE'
			and seq.mrkr_type[1,4] != 'GENE'
			and mrel_mrkr_1_zdb_id = gene.mrkr_zdb_id
			and mrel_mrkr_2_zdb_id = seq.mrkr_zdb_id
		   union
		     select gene.mrkr_zdb_id, gene.mrkr_abbrev,
			  seq.mrkr_zdb_id, seq.mrkr_abbrev,
			  mrel_type
		     from marker_relationship, marker gene, marker seq
		     where gene.mrkr_type[1,4] = 'GENE'
			and seq.mrkr_type[1,4] != 'GENE'
			and mrel_mrkr_2_zdb_id = gene.mrkr_zdb_id
			and mrel_mrkr_1_zdb_id = seq.mrkr_zdb_id;


-- Create the orthologues files - mouse, human, fly and yeast

create temp table ortho_exp (
  gene_id varchar(50),
  ortho_id varchar(50),
  zfish_name varchar(120),
  zfish_abbrev varchar(40),
  organism varchar(30),
  ortho_name varchar(120),
  ortho_abbrev varchar(15),
  flybase varchar(50),
  entrez varchar(50),
  mgi varchar(50),
  omim varchar(50),
  sgd varchar(50)
) with no log;

insert into ortho_exp
  select distinct c_gene_id, zdb_id, mrkr_name, mrkr_abbrev, organism, ortho_name,
         ortho_abbrev,
         NULL::varchar(50),
         NULL::varchar(50),
         NULL::varchar(50),
         NULL::varchar(50),
         NULL::varchar(50)
    from orthologue,marker
	where c_gene_id = mrkr_zdb_id;

update ortho_exp
	set flybase = (select distinct dblink_acc_num from db_link, orthologue o, foreign_db_contains
	   where dblink_fdbcont_zdb_id = fdbcont_zdb_id
	        and fdbcont_fdb_db_name = 'FLYBASE'
	        and fdbcont_organism_common_name = o.organism
		and o.zdb_id = dblink_linked_recid
		and ortho_id = o.zdb_id);

update ortho_exp
	set Entrez = (select dblink_acc_num from db_link, orthologue o, foreign_db_contains
	   where dblink_fdbcont_zdb_id = fdbcont_zdb_id
	        and fdbcont_fdb_db_name = 'Entrez Gene'
	        and fdbcont_organism_common_name = o.organism
		and o.zdb_id = dblink_linked_recid
		and ortho_id = o.zdb_id);


update ortho_exp
	set mgi = (select 'MGI:' || dblink_acc_num from db_link , orthologue o, foreign_db_contains
	   where dblink_fdbcont_zdb_id = fdbcont_zdb_id
	        and fdbcont_fdb_db_name = 'MGI'
	        and fdbcont_organism_common_name = o.organism
		and o.zdb_id = dblink_linked_recid
		and ortho_id = o.zdb_id);


update ortho_exp
	set omim = (select distinct dblink_acc_num from db_link, orthologue o, foreign_db_contains
	   where dblink_fdbcont_zdb_id = fdbcont_zdb_id
	        and fdbcont_fdb_db_name = 'OMIM'
	        and fdbcont_organism_common_name = o.organism
		and o.zdb_id = dblink_linked_recid
		and ortho_id = o.zdb_id);


update ortho_exp
	set sgd = (select dblink_acc_num from db_link, orthologue o, foreign_db_contains
	   where dblink_fdbcont_zdb_id = fdbcont_zdb_id
	        and fdbcont_fdb_db_name = 'SGD'
	        and fdbcont_organism_common_name = o.organism
		and o.zdb_id = dblink_linked_recid
		and ortho_id = o.zdb_id);


UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/fly_orthos.txt'
  DELIMITER "	"
  select gene_id, zfish_abbrev, zfish_name, ortho_abbrev, ortho_name, flybase
    from ortho_exp where organism = 'Fly' order by 1;


UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/human_orthos.txt'
  DELIMITER "	"
  select gene_id, zfish_abbrev, zfish_name, ortho_abbrev, ortho_name, omim, entrez
    from ortho_exp where organism = 'Human' order by 1;

UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/mouse_orthos.txt'
  DELIMITER "	"
  select gene_id, zfish_abbrev, zfish_name, ortho_abbrev, ortho_name, mgi, entrez
    from ortho_exp where organism = 'Mouse' order by 1;

UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/yeast_orthos.txt'
  DELIMITER "	"
  select gene_id, zfish_abbrev, zfish_name, ortho_abbrev, ortho_name, sgd
    from ortho_exp where organism = 'Yeast' order by 1;

-- generate a file with genes and associated expression experiment

UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/xpat.txt'
 DELIMITER "	"
 select gene.mrkr_zdb_id gene_zdb, gene.mrkr_abbrev,
        probe.mrkr_zdb_id probe_zdb, probe.mrkr_abbrev,
        xpatex_assay_name, xpatex_zdb_id xpat_zdb,
        xpatex_source_zdb_id,
        genox_geno_zdb_id, genox_exp_zdb_id
 from expression_experiment
      join genotype_experiment
	  on genox_zdb_id = xpatex_genox_zdb_id
      join marker gene
	  on gene.mrkr_zdb_id = xpatex_gene_zdb_id
      left join marker probe
	  on probe.mrkr_zdb_id = xpatex_probe_feature_zdb_id
 order by gene_zdb, xpat_zdb, probe_zdb;

! echo "Inserted data into file xpat.txt"


-- generate a file to map experiment id to environment condition description
UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/xpat_environment.txt'
 DELIMITER "	"
 select exp_zdb_id, cdt_group,
        case when expcond_mrkr_zdb_id is not null
             then expcond_mrkr_zdb_id
	     else cdt_name
 	end, expcond_value, expunit_name, expcond_comments
   from experiment, experiment_condition, condition_data_type, experiment_unit
  where exp_zdb_id = expcond_exp_zdb_id
    and expcond_cdt_zdb_id = cdt_zdb_id
    and expcond_expunit_zdb_id = expunit_zdb_id
    and exists (select 't'
                  from genotype_experiment, expression_experiment
                 where exp_zdb_id = genox_exp_zdb_id
                   and genox_zdb_id = xpatex_genox_zdb_id)
order by exp_zdb_id, cdt_group;


-- generate a file with genes and associated expression experiment

UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/phenotype.txt'
 DELIMITER "	"
 select geno_zdb_id, geno_display_name,
			apato_Start_stg_zdb_id,
			(select stg_name
				from stage
				where stg_zdb_id = apato_start_Stg_zdb_id),
			apato_end_Stg_zdb_id,
			(select stg_name
				from stage
				where stg_zdb_id = apato_end_stg_zdb_id),
			apato_entity_a_zdb_id,
				case
				  when
				  get_obj_type(apato_Entity_a_zdb_id) = 'ANAT'
				  then
					(select anatitem_name
					  from anatomy_item
					  where anatitem_zdb_id =
						apato_entity_a_zdb_id)
				  when
				  get_obj_type(apato_entity_a_zdb_id)='GOTERM'
				  then
					(select goterm_name
					   from go_term
					   where goterm_zdb_id = apato_entity_a_zdb_id)
				  end,
			apato_quality_zdb_id,
				(select term_name
					from term
					where term_Zdb_id = apato_quality_zdb_id),
			apato_entity_b_zdb_id,
				case
				  when
				  get_obj_type(apato_Entity_b_zdb_id) = 'ANAT'
				  then
					(select anatitem_name
					  from anatomy_item
					  where anatitem_zdb_id =
						apato_entity_b_zdb_id)
				  when
				  get_obj_type(apato_entity_b_zdb_id)='GOTERM'
				  then
					(select goterm_name
					   from go_term
					   where goterm_zdb_id = apato_entity_b_zdb_id)
				  end,
			apato_tag,
			apato_pub_zdb_id,
			genox_exp_zdb_id
 from atomic_phenotype, genotype, genotype_experiment
      where apato_genox_zdb_id = genox_zdb_id
	and genox_geno_zdb_id = geno_zdb_id
 order by geno_zdb_id, apato_pub_zdb_id;

! echo "Inserted data into file phenotype.txt"


UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/pheno_obo.txt'
 DELIMITER "	"
select "ZDB:"||geno_zdb_id, geno_display_name,
			(select stg_obo_id from stage 
			   where stg_zdb_id = apato_Start_stg_zdb_id),
			(select stg_obo_id from stage
                           where stg_zdb_id = apato_end_Stg_zdb_id),
				case
				  when
				  get_obj_type(apato_Entity_a_zdb_id) = 'ANAT'
				  then
					(select anatitem_obo_id
					  from anatomy_item
					  where anatitem_zdb_id =
						apato_entity_a_zdb_id)
				  when
				  get_obj_type(apato_entity_a_zdb_id)='GOTERM'
				  then
					(select "GO:"||goterm_go_id
					   from go_term
					   where goterm_zdb_id = apato_entity_a_zdb_id)
				  end,
				(select term_ont_id
					from term
					where term_Zdb_id = apato_quality_zdb_id),
				case
				  when
				  get_obj_type(apato_Entity_b_zdb_id) = 'ANAT'
				  then
					(select anatitem_obo_id
					  from anatomy_item
					  where anatitem_zdb_id =
						apato_entity_b_zdb_id)
				  when
				  get_obj_type(apato_entity_b_zdb_id)='GOTERM'
				  then
					(select goterm_go_id
					   from go_term
					   where goterm_zdb_id = apato_entity_b_zdb_id)
				  end,
			apato_tag,
			"ZDB:"||apato_pub_zdb_id,
			"ZDB:"||genox_exp_zdb_id
 from atomic_phenotype, genotype, genotype_experiment
      where apato_genox_zdb_id = genox_zdb_id
	and genox_geno_zdb_id = geno_zdb_id
 order by geno_zdb_id, apato_pub_zdb_id ;

-- generate a file to map experiment id to environment condition description
UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/pheno_environment.txt'
 DELIMITER "	"
 select exp_zdb_id, cdt_group,
        case when expcond_mrkr_zdb_id is not null
             then expcond_mrkr_zdb_id
	     else cdt_name
 	end, expcond_value, expunit_name, expcond_comments
   from experiment, experiment_condition, condition_data_type, experiment_unit
  where exp_zdb_id = expcond_exp_zdb_id
    and expcond_cdt_zdb_id = cdt_zdb_id
    and expcond_expunit_zdb_id = expunit_zdb_id
    and exists (select 't'
                  from genotype_experiment, atomic_phenotype
                 where exp_zdb_id = genox_exp_zdb_id
                   and genox_zdb_id = apato_genox_zdb_id)
order by exp_zdb_id, cdt_group;


UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/gene_ontology_translation.txt'
 DELIMITER "	"
 select goterm_zdb_id, "GO:"||goterm_go_id
   from go_term
   where goterm_is_obsolete = 'f'
   and goterm_is_secondary = 'f';

UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/anatomy_ontology_translation.txt'
 DELIMITER "	"
 select anatitem_zdb_id, anatitem_obo_id
   from anatomy_item
   where anatitem_is_obsolete = 'f';

UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/phenotype_quality_ontology_translation.txt'
 DELIMITER "	"
 select term_zdb_id, term_ont_id, term_name
   from term
   where term_is_obsolete = 'f'
   and term_is_secondary = 'f';

UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/pub_to_pubmed_id_translation.txt'
 DELIMITER "	"
 select zdb_id, accession_no
   from publication ;


-- Create mapping data file
UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/mappings.txt'
 DELIMITER "	" select marker_id, mrkr_abbrev, p.abbrev,or_lg, lg_location, p.metric
 from mapped_marker, panels p, marker m
 where refcross_id = p.zdb_id and marker_id = mrkr_zdb_id
 order by 1;

-- Generate sequence data files for GenBank, RefSeq, Entrez, UniGene, UniProt, Interpro and GenPept

-- the last condition is added to filter out mis-placed acc
UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/genbank.txt'
 DELIMITER "	" select mrkr_zdb_id, mrkr_abbrev, dblink_acc_num from marker, db_link, foreign_db_contains
	where mrkr_zdb_id = dblink_linked_recid
	  and dblink_fdbcont_zdb_id = fdbcont_zdb_id
	  and fdbcont_fdb_db_name = 'GenBank'
          and dblink_acc_num[3] <> "_" order by 1;

-- the last condition is added to filter out mis-placed acc
UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/refseq.txt'
 DELIMITER "	" select mrkr_zdb_id, mrkr_abbrev,dblink_acc_num from marker, db_link, foreign_db_contains
	where mrkr_zdb_id = dblink_linked_recid
	  and dblink_fdbcont_zdb_id = fdbcont_zdb_id
	  and fdbcont_fdb_db_name = 'RefSeq'
          and dblink_acc_num[3] = "_" order by 1;

UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/entrezgene.txt'
 DELIMITER "	" select mrkr_zdb_id, mrkr_abbrev,dblink_acc_num from marker, db_link, foreign_db_contains
	where mrkr_zdb_id = dblink_linked_recid
	  and dblink_fdbcont_zdb_id = fdbcont_zdb_id
	  and fdbcont_fdb_db_name = 'Entrez Gene' order by 1;

UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/unigene.txt'
 DELIMITER "	" select mrkr_zdb_id, mrkr_abbrev,dblink_acc_num from marker, db_link, foreign_db_contains
	where mrkr_zdb_id = dblink_linked_recid
	  and dblink_fdbcont_zdb_id = fdbcont_zdb_id
	  and fdbcont_fdb_db_name = 'UniGene' order by 1;

UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/uniprot.txt'
 DELIMITER "	" select mrkr_zdb_id, mrkr_abbrev,dblink_acc_num from marker, db_link, foreign_db_contains
	where mrkr_zdb_id = dblink_linked_recid
	  and dblink_fdbcont_zdb_id = fdbcont_zdb_id
	  and fdbcont_fdb_db_name = 'UniProt' order by 1;

UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/interpro.txt'
 DELIMITER "	" select mrkr_zdb_id, mrkr_abbrev,dblink_acc_num from marker, db_link, foreign_db_contains
	where mrkr_zdb_id = dblink_linked_recid
	  and dblink_fdbcont_zdb_id = fdbcont_zdb_id
	  and fdbcont_fdb_db_name = 'InterPro' order by 1;

UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/pfam.txt'
 DELIMITER "	" select mrkr_zdb_id, mrkr_abbrev,dblink_acc_num from marker, db_link, foreign_db_contains
	where mrkr_zdb_id = dblink_linked_recid
	  and dblink_fdbcont_zdb_id = fdbcont_zdb_id
	  and fdbcont_fdb_db_name = 'Pfam' order by 1;

UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/genpept.txt'
 DELIMITER "	" select mrkr_zdb_id, mrkr_abbrev,dblink_acc_num from marker, db_link, foreign_db_contains
	where mrkr_zdb_id = dblink_linked_recid
	  and fdbcont_zdb_id = dblink_fdbcont_zdb_id
	  and fdbcont_fdb_db_name = 'GenPept' order by 1;

UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/vega.txt'
 DELIMITER "	" select mrkr_zdb_id, mrkr_abbrev,dblink_acc_num from marker, db_link, foreign_db_contains
	where mrkr_zdb_id = dblink_linked_recid
	  and fdbcont_zdb_id = dblink_fdbcont_zdb_id
	  and fdbcont_fdb_db_name in ('VEGA','INTVEGA') order by 1;

UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/vega_transcript.txt'
 DELIMITER "	" select mrkr_zdb_id, mrkr_abbrev,dblink_acc_num from marker, db_link, foreign_db_contains
        where mrkr_zdb_id = dblink_linked_recid
          and fdbcont_zdb_id = dblink_fdbcont_zdb_id
          and fdbcont_fdb_db_name in ('Vega_Trans','INTVEGA') order by 1;

-- the changing assembly version number in db_name
-- is apt to come back to bite us so I am opting for the zdb_id
UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/ensembl_1_to_1.txt'
 DELIMITER "	" 
select  "#    ZDBID          ","SYMBOL",fdbcont_fdb_db_name
 from foreign_db_contains
 where fdbcont_zdb_id = 'ZDB-FDBCONT-061018-1'
union
select mrkr_zdb_id, mrkr_abbrev,dblink_acc_num from marker, db_link
	where mrkr_zdb_id = dblink_linked_recid
	  and dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-061018-1' order by 1;


-- Generate genotype_feature file

create temp table geno_data (
  genotype_id varchar(50),
  geno_display_name varchar(255),
  geno_handle varchar(255),
  feature_name varchar(255),
  feature_abbrev varchar(30),
  feature_type varchar(30),
  gene_abbrev varchar(40),
  gene_id varchar(50),
  feature_zdb_id varchar(50)
) with no log ;

insert into geno_data (genotype_id, geno_display_name, geno_handle,
		feature_name, feature_abbrev, feature_type, feature_zdb_id)
  select genofeat_geno_zdb_id,
	        geno_display_name,
		geno_handle,
		feature_name,
		feature_abbrev,
		lower(feature_type),
		feature_zdb_id
    from genotype_feature, feature, genotype
   where genofeat_feature_zdb_id = feature_zdb_id
    and geno_zdb_id = genofeat_geno_zdb_id;

update geno_data set (gene_id, gene_abbrev) =
	    (( select mrkr_zdb_id, mrkr_abbrev
		     from marker, feature_marker_relationship, feature
		    where geno_data.feature_name = feature_name
		      and fmrel_ftr_zdb_id = feature_zdb_id
		      and fmrel_mrkr_zdb_id = mrkr_zdb_id
		      and fmrel_type = "is allele of"));

UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/genotype_features.txt'
 DELIMITER "	" select distinct genotype_id,
				geno_display_name,
				geno_handle,
				feature_zdb_id,
				feature_name,
				feature_abbrev,
				feature_type,
				gene_abbrev,
				gene_id
			from geno_data order by genotype_id, geno_display_name;


UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/genotype_backgrounds.txt'
 DELIMITER "	" select distinct geno_zdb_id,
			geno_display_name,
			genoback_background_zdb_id
                    from genotype, genotype_background
                    where geno_Zdb_id = genoback_geno_Zdb_id ;

UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/wildtypes.txt'
 DELIMITER "	" select distinct geno_zdb_id, geno_display_name, geno_handle
                    from genotype
                    where geno_is_wildtype = 't' ;


-- generate a file with zdb history data

UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/zdb_history.txt'
 DELIMITER "	" select zrepld_old_zdb_id, zrepld_new_zdb_id from zdb_replaced_data;

-- clean up
--drop table ortho_exp;
--drop table geno_data;


-- indirect sequence links for genes

select distinct gene.mrkr_zdb_id gene_zdb,
       gene.mrkr_abbrev gene_sym,
       dblink_acc_num genbank_acc
from marker gene, marker est, db_link, marker_relationship, foreign_db_contains
where gene.mrkr_zdb_id = mrel_mrkr_1_zdb_id
and   est.mrkr_zdb_id  = mrel_mrkr_2_zdb_id
and  mrel_type = 'gene encodes small segment'
and est.mrkr_zdb_id = dblink_linked_recid
and est.mrkr_type  in ('EST','CDNA')
and gene.mrkr_type = 'GENE'
and dblink_fdbcont_zdb_id = fdbcont_zdb_id
and fdbcont_fdb_db_name = 'GenBank'
into temp tmp_veg with no log;

UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/gene_seq.txt'
 DELIMITER "	"
select * from tmp_veg
order by 1,3;

drop table tmp_veg;


-- Anatomical Ontologies

unload to  '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/anatomy_ontology.txt'
DELIMITER "	"
select anatrel_anatitem_1_zdb_id, anatrel_anatitem_2_zdb_id
  from anatomy_relationship;

unload to  '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/stage_ontology.txt'
 DELIMITER "	"
  select stg_zdb_id,
           stg_name,
           stg_hours_start,
           stg_hours_end
  from stage
  order by stg_hours_start, stg_hours_end desc
;

unload to  '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/anatomy_item.txt'
 DELIMITER "	"
select
    anatitem_zdb_id,
    anatitem_name,
    anatitem_start_stg_zdb_id,
    anatitem_end_stg_zdb_id
from anatomy_item
;

unload to  '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/anatomy_relationship.txt'
DELIMITER "	"
select anatrel_anatitem_1_zdb_id, anatrel_anatitem_2_zdb_id, anatrel_dagedit_id
  from anatomy_relationship;


unload to  '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/xpat_stage_anatomy.txt'
 DELIMITER "	"
select xpatres_xpatex_zdb_id,
       xpatres_start_stg_zdb_id,
       xpatres_end_stg_zdb_id,
       xpatres_anat_item_zdb_id,
       xpatres_expression_found
  from expression_result;


unload to  '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/anatomy_synonyms.txt'
 DELIMITER "	"
 select dalias_data_zdb_id, 
 	anatitem_name,
 	dalias_alias 
  from data_alias, anatomy_item
  where dalias_data_zdb_id = anatitem_zdb_id 
    and dalias_data_zdb_id like 'ZDB-ANAT%'  
    and dalias_alias not like 'ZFA:%' 
    and dalias_group != 'plural'
    and dalias_group != 'secondary id'
  order by anatitem_name;


-- Morpholino data
-- unloaded Morpholino data would have HTML tags in public note column,
-- which will be removed by Perl script
unload to  '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/Morpholinos2.txt'
 DELIMITER "	"
select gn.mrkr_zdb_id, gn.mrkr_abbrev, mo.mrkr_zdb_id, mo.mrkr_abbrev, mrkrseq_sequence, mo.mrkr_comments
  from marker gn, marker mo, marker_sequence, marker_relationship
  where gn.mrkr_zdb_id = mrel_mrkr_2_zdb_id
    and mo.mrkr_zdb_id = mrel_mrkr_1_zdb_id
    and mrel_mrkr_2_zdb_id like "ZDB-GENE-%"
    and mrel_mrkr_1_zdb_id like "ZDB-MRPHLNO-%"
    and mrel_type = "knockdown reagent targets gene"
    and mo.mrkr_zdb_id = mrkrseq_mrkr_zdb_id
    order by gn.mrkr_abbrev;



