--liquibase formatted sql
--changeset prita:ZFIN-6504b.sql

--this script backpopulates flanking sequence, and feature genomic muttaion detals ona whole slew of sanger alleles for which this information wasnt loaded.
--some of these alleles do not have affected gene
--all this infromation (mainly starnd info) based onw hich the calculations were done, came from sanger
--We have a table in the database sanger_flanking_sequence which stores allele, variation and if on positive or negative strand.


drop table sangerpointmut;
create   table sangerpointmut (featzdb text, strand text);
insert into sangerpointmut (featzdb,strand) select distinct feature_zdb_id,strand from feature, sanger_flanking_sequence where feature_abbrev=allele and feature_zdb_id not in (select fgmd_feature_zdb_id from feature_genomic_mutation_detail);
select count(*) from sangerpointmut;
create temp table featgenome (fgmdfeat text,fmmdid text,fgmdstrand text,fgmdseqref text,fgmdseqvar text,pub text);





insert into featgenome (fgmdfeat,fmmdid,fgmdstrand,fgmdseqref,fgmdseqvar,pub) select distinct featzdb,featzdb,strand,'G','T',recattrib_source_zdb_id from sangerpointmut,feature_dna_mutation_detail,record_attribution where  strand='1' and fdmd_dna_mutation_term_zdb_id ='ZDB-TERM-130401-1976' and featzdb=fdmd_feature_zdb_id and recattrib_data_zdb_id=fdmd_zdb_id;
insert into featgenome (fgmdfeat,fmmdid,fgmdstrand,fgmdseqref,fgmdseqvar,pub) select distinct featzdb,featzdb,strand,'C','T',recattrib_source_zdb_id from sangerpointmut,feature_dna_mutation_detail,record_attribution where  strand='1' and fdmd_dna_mutation_term_zdb_id ='ZDB-TERM-130401-1960' and featzdb=fdmd_feature_zdb_id and recattrib_data_zdb_id=fdmd_zdb_id;
insert into featgenome (fgmdfeat,fmmdid,fgmdstrand,fgmdseqref,fgmdseqvar,pub) select distinct featzdb,featzdb,strand,'T','G',recattrib_source_zdb_id from sangerpointmut,feature_dna_mutation_detail,record_attribution where  strand='1' and fdmd_dna_mutation_term_zdb_id ='ZDB-TERM-130401-1971' and featzdb=fdmd_feature_zdb_id and recattrib_data_zdb_id=fdmd_zdb_id;
insert into featgenome (fgmdfeat,fmmdid,fgmdstrand,fgmdseqref,fgmdseqvar,pub) select distinct featzdb,featzdb,strand,'T','A',recattrib_source_zdb_id from sangerpointmut,feature_dna_mutation_detail,record_attribution where  strand='1' and fdmd_dna_mutation_term_zdb_id ='ZDB-TERM-130401-1970' and featzdb=fdmd_feature_zdb_id and recattrib_data_zdb_id=fdmd_zdb_id;
insert into featgenome (fgmdfeat,fmmdid,fgmdstrand,fgmdseqref,fgmdseqvar,pub) select distinct featzdb,featzdb,strand,'C','A',recattrib_source_zdb_id from sangerpointmut,feature_dna_mutation_detail,record_attribution where  strand='1' and fdmd_dna_mutation_term_zdb_id ='ZDB-TERM-130401-1968' and featzdb=fdmd_feature_zdb_id and recattrib_data_zdb_id=fdmd_zdb_id;
insert into featgenome (fgmdfeat,fmmdid,fgmdstrand,fgmdseqref,fgmdseqvar,pub) select distinct featzdb,featzdb,strand,'G','A',recattrib_source_zdb_id from sangerpointmut,feature_dna_mutation_detail,record_attribution where  strand='1' and fdmd_dna_mutation_term_zdb_id ='ZDB-TERM-130401-1965' and featzdb=fdmd_feature_zdb_id and recattrib_data_zdb_id=fdmd_zdb_id;
insert into featgenome (fgmdfeat,fmmdid,fgmdstrand,fgmdseqref,fgmdseqvar,pub) select distinct featzdb,featzdb,strand,'A','T',recattrib_source_zdb_id from sangerpointmut,feature_dna_mutation_detail,record_attribution where  strand='1' and fdmd_dna_mutation_term_zdb_id ='ZDB-TERM-130401-1974' and featzdb=fdmd_feature_zdb_id and recattrib_data_zdb_id=fdmd_zdb_id;
insert into featgenome (fgmdfeat,fmmdid,fgmdstrand,fgmdseqref,fgmdseqvar,pub) select distinct featzdb,featzdb,strand,'T','C',recattrib_source_zdb_id from sangerpointmut,feature_dna_mutation_detail,record_attribution where  strand='1' and fdmd_dna_mutation_term_zdb_id ='ZDB-TERM-130401-1962' and featzdb=fdmd_feature_zdb_id and recattrib_data_zdb_id=fdmd_zdb_id;
insert into featgenome (fgmdfeat,fmmdid,fgmdstrand,fgmdseqref,fgmdseqvar,pub) select distinct featzdb,featzdb,strand,'A','C',recattrib_source_zdb_id from sangerpointmut,feature_dna_mutation_detail,record_attribution where  strand='1' and fdmd_dna_mutation_term_zdb_id ='ZDB-TERM-130401-1973' and featzdb=fdmd_feature_zdb_id and recattrib_data_zdb_id=fdmd_zdb_id;
insert into featgenome (fgmdfeat,fmmdid,fgmdstrand,fgmdseqref,fgmdseqvar,pub) select distinct featzdb,featzdb,strand,'A','G',recattrib_source_zdb_id from sangerpointmut,feature_dna_mutation_detail,record_attribution where  strand='1' and fdmd_dna_mutation_term_zdb_id ='ZDB-TERM-130401-1964' and featzdb=fdmd_feature_zdb_id and recattrib_data_zdb_id=fdmd_zdb_id;

insert into featgenome (fgmdfeat,fmmdid,fgmdstrand,fgmdseqref,fgmdseqvar,pub) select distinct featzdb,featzdb,strand,'C','G',recattrib_source_zdb_id from sangerpointmut,feature_dna_mutation_detail,record_attribution where  strand='1' and fdmd_dna_mutation_term_zdb_id ='ZDB-TERM-130401-1969' and featzdb=fdmd_feature_zdb_id and recattrib_data_zdb_id=fdmd_zdb_id;
insert into featgenome (fgmdfeat,fmmdid,fgmdstrand,fgmdseqref,fgmdseqvar,pub) select distinct featzdb,featzdb,strand,'G','C',recattrib_source_zdb_id from sangerpointmut,feature_dna_mutation_detail,record_attribution where  strand='1' and fdmd_dna_mutation_term_zdb_id ='ZDB-TERM-130401-1975' and featzdb=fdmd_feature_zdb_id and recattrib_data_zdb_id=fdmd_zdb_id;


insert into featgenome (fgmdfeat,fmmdid,fgmdstrand,fgmdseqref,fgmdseqvar,pub) select distinct featzdb,featzdb,strand,'C','A',recattrib_source_zdb_id from sangerpointmut,feature_dna_mutation_detail,record_attribution where  strand='-1' and fdmd_dna_mutation_term_zdb_id ='ZDB-TERM-130401-1976' and featzdb=fdmd_feature_zdb_id and recattrib_data_zdb_id=fdmd_zdb_id;
insert into featgenome (fgmdfeat,fmmdid,fgmdstrand,fgmdseqref,fgmdseqvar,pub) select distinct featzdb,featzdb,strand,'G','A',recattrib_source_zdb_id from sangerpointmut,feature_dna_mutation_detail,record_attribution where  strand='-1' and fdmd_dna_mutation_term_zdb_id ='ZDB-TERM-130401-1960' and featzdb=fdmd_feature_zdb_id and recattrib_data_zdb_id=fdmd_zdb_id;
insert into featgenome (fgmdfeat,fmmdid,fgmdstrand,fgmdseqref,fgmdseqvar,pub) select distinct featzdb,featzdb,strand,'A','C',recattrib_source_zdb_id from sangerpointmut,feature_dna_mutation_detail,record_attribution where  strand='-1' and fdmd_dna_mutation_term_zdb_id ='ZDB-TERM-130401-1971' and featzdb=fdmd_feature_zdb_id and recattrib_data_zdb_id=fdmd_zdb_id;
insert into featgenome (fgmdfeat,fmmdid,fgmdstrand,fgmdseqref,fgmdseqvar,pub) select distinct featzdb,featzdb,strand,'A','T',recattrib_source_zdb_id from sangerpointmut,feature_dna_mutation_detail,record_attribution where  strand='-1' and fdmd_dna_mutation_term_zdb_id ='ZDB-TERM-130401-1970' and featzdb=fdmd_feature_zdb_id and recattrib_data_zdb_id=fdmd_zdb_id;
insert into featgenome (fgmdfeat,fmmdid,fgmdstrand,fgmdseqref,fgmdseqvar,pub) select distinct featzdb,featzdb,strand,'G','T',recattrib_source_zdb_id from sangerpointmut,feature_dna_mutation_detail,record_attribution where  strand='-1' and fdmd_dna_mutation_term_zdb_id ='ZDB-TERM-130401-1968' and featzdb=fdmd_feature_zdb_id and recattrib_data_zdb_id=fdmd_zdb_id;
insert into featgenome (fgmdfeat,fmmdid,fgmdstrand,fgmdseqref,fgmdseqvar,pub) select distinct featzdb,featzdb,strand,'C','T',recattrib_source_zdb_id from sangerpointmut,feature_dna_mutation_detail,record_attribution where  strand='-1' and fdmd_dna_mutation_term_zdb_id ='ZDB-TERM-130401-1965' and featzdb=fdmd_feature_zdb_id and recattrib_data_zdb_id=fdmd_zdb_id;
insert into featgenome (fgmdfeat,fmmdid,fgmdstrand,fgmdseqref,fgmdseqvar,pub) select distinct featzdb,featzdb,strand,'T','A',recattrib_source_zdb_id from sangerpointmut,feature_dna_mutation_detail,record_attribution where  strand='-1' and fdmd_dna_mutation_term_zdb_id ='ZDB-TERM-130401-1974' and featzdb=fdmd_feature_zdb_id and recattrib_data_zdb_id=fdmd_zdb_id;
insert into featgenome (fgmdfeat,fmmdid,fgmdstrand,fgmdseqref,fgmdseqvar,pub) select distinct featzdb,featzdb,strand,'A','G',recattrib_source_zdb_id from sangerpointmut,feature_dna_mutation_detail,record_attribution where  strand='-1' and fdmd_dna_mutation_term_zdb_id ='ZDB-TERM-130401-1962' and featzdb=fdmd_feature_zdb_id and recattrib_data_zdb_id=fdmd_zdb_id;
insert into featgenome (fgmdfeat,fmmdid,fgmdstrand,fgmdseqref,fgmdseqvar,pub) select distinct featzdb,featzdb,strand,'T','G',recattrib_source_zdb_id from sangerpointmut,feature_dna_mutation_detail,record_attribution where  strand='-1' and fdmd_dna_mutation_term_zdb_id ='ZDB-TERM-130401-1973' and featzdb=fdmd_feature_zdb_id and recattrib_data_zdb_id=fdmd_zdb_id;
insert into featgenome (fgmdfeat,fmmdid,fgmdstrand,fgmdseqref,fgmdseqvar,pub) select distinct featzdb,featzdb,strand,'T','C',recattrib_source_zdb_id from sangerpointmut,feature_dna_mutation_detail,record_attribution where  strand='-1' and fdmd_dna_mutation_term_zdb_id ='ZDB-TERM-130401-1964' and featzdb=fdmd_feature_zdb_id and recattrib_data_zdb_id=fdmd_zdb_id;
insert into featgenome (fgmdfeat,fmmdid,fgmdstrand,fgmdseqref,fgmdseqvar,pub) select distinct featzdb,featzdb,strand,'G','C',recattrib_source_zdb_id from sangerpointmut,feature_dna_mutation_detail,record_attribution where  strand='-1' and fdmd_dna_mutation_term_zdb_id ='ZDB-TERM-130401-1969' and featzdb=fdmd_feature_zdb_id and recattrib_data_zdb_id=fdmd_zdb_id;
insert into featgenome (fgmdfeat,fmmdid,fgmdstrand,fgmdseqref,fgmdseqvar,pub) select distinct featzdb,featzdb,strand,'C','G',recattrib_source_zdb_id from sangerpointmut,feature_dna_mutation_detail,record_attribution where  strand='-1' and fdmd_dna_mutation_term_zdb_id ='ZDB-TERM-130401-1975' and featzdb=fdmd_feature_zdb_id and recattrib_data_zdb_id=fdmd_zdb_id;

update featgenome set fmmdid=get_id('FGMD');

insert into zdb_active_data (zactvd_zdb_id) select fmmdid from featgenome;

insert into feature_genomic_mutation_detail(fgmd_zdb_id,fgmd_feature_zdb_id,fgmd_sequence_of_reference,fgmd_sequence_of_variation,fgmd_variation_strand) select fmmdid,fgmdfeat,fgmdseqref,fgmdseqvar,fgmdstrand from featgenome;
insert into record_Attribution(recattrib_Data_Zdb_id,recattrib_source_zdb_id) select fmmdid,pub from featgenome;

update variant_flanking_sequence set vfseq_variation=
   (select distinct fgmd_sequence_of_reference|| '/' ||fgmd_sequence_of_variation  from feature_genomic_mutation_detail where fgmd_feature_zdb_id=vfseq_data_Zdb_id and vfseq_variation is null) from feature_genomic_mutation_detail where fgmd_feature_zdb_id=vfseq_data_Zdb_id and vfseq_variation is null ;

--update variant_flanking_sequence set vfseq_sequence=
--   (select distinct vfseq_five_prime_flanking_sequence||'['||fgmd_sequence_of_reference|| '/' ||fgmd_sequence_of_variation||']'||vfseq_three_prime_flanking_sequence from feature_genomic_mutation_detail where fgmd_feature_zdb_id=vfseq_data_Zdb_id and vfseq_sequence is null) from feature_genomic_mutation_detail where fgmd_feature_zdb_id=vfseq_data_Zdb_id and vfseq_sequence is null ;


