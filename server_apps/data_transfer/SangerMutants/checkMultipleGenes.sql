-- loadSangerData.sql
-- input: allelezfin.unl
-- Some of the hard-coded data associated with this loading:
-- TL background :ZDB-GENO-990623-2
-- genotypes are on a [2,1,1] background
-- zygocity:
-- ZDB-LAB-050412-2  Stemple Lab or ZDB-LAB-070815-1 cuppen Lab
-- ZDB-PUB-120207-1


begin work;

---creating temp table to load all of the input data form sanger
create temp table sanger_pre_input_known (
     sanger_preinput_feature_abbrev varchar(255),
     sanger_preinput_gene_zdb_id varchar (50),
     sanger_preinput_background varchar(50),
     sanger_preinput_line_number varchar(70)
) with no log;

load from sangerKnown.unl insert into sanger_pre_input_known;

unload to 'duplicateGenes.unl' select sanger_preinput_feature_abbrev, sanger_preinput_gene_zdb_id,sanger_preinput_background from sanger_pre_input_known  where sanger_preinput_background like 'ZDB-GENE%';

create temp table duplicate_genes (
     dup_feature_abbrev varchar(255),
     dup_gene varchar (50),
     dup_bkgrd varchar(50)
) with no log;

load from duplicateGenes.unl insert into duplicate_genes;

delete from sanger_pre_input_known where sanger_preinput_feature_abbrev in (select dup_feature_abbrev from duplicate_genes);


unload to 'sangerInputWithoutDuplicates.unl'  select distinct * from sanger_pre_input_known;



commit work;


