--liquibase formatted sql
--changeset prita:ZFIN-6153.sql


create   table featmkr (featzdb text, genezdb text, strand text,feattype text);
insert into featmkr (featzdb,genezdb,strand,feattype) select distinct fmrel_ftr_zdb_id,fmrel_mrkr_zdb_id,zeg_strand,feature_type from feature_marker_relationship, zfin_ensembl_gene,feature where fmrel_mrkr_zdb_id=zeg_gene_zdb_id and fmrel_ftr_zdb_id=feature_zdb_id;

create temp table featgenomemd (fgmdfeat text,fmmdid text,fgmdstrand text,fgmdseqref text,fgmdseqvar text,pub text);

insert into featgenomemd (fgmdfeat,fmmdid,fgmdstrand,fgmdseqref,fgmdseqvar,pub) select distinct featzdb,featzdb,strand,fdmd_deleted_sequence,'',recattrib_source_zdb_id from featmkr,feature_dna_mutation_detail,record_attribution where feattype='DELETION' and strand='+' and featzdb=fdmd_feature_zdb_id and fdmd_deleted_sequence is not null and fdmd_zdb_id=recattrib_data_zdb_id;
insert into featgenomemd (fgmdfeat,fmmdid,fgmdstrand,fgmdseqref,fgmdseqvar,pub) select distinct featzdb,featzdb,strand,'',fdmd_inserted_sequence,recattrib_source_zdb_id from featmkr,feature_dna_mutation_detail,record_attribution where feattype='INSERTION' and strand='+'and featzdb=fdmd_feature_zdb_id and fdmd_inserted_sequence is not null and fdmd_zdb_id=recattrib_data_zdb_id ;

insert into featgenomemd (fgmdfeat,fmmdid,fgmdstrand,fgmdseqref,fgmdseqvar,pub)
select distinct featzdb,featzdb,strand,fdmd_deleted_sequence,fdmd_inserted_sequence,recattrib_source_zdb_id
from featmkr,feature_dna_mutation_detail,record_attribution
where feattype='INDEL' and strand='+' and featzdb=fdmd_feature_zdb_id
and (fdmd_deleted_sequence is not null or fdmd_inserted_sequence is not null)  and fdmd_zdb_id=recattrib_data_zdb_id ;


insert into featgenomemd (fgmdfeat,fmmdid,fgmdstrand,fgmdseqref,fgmdseqvar,pub) select distinct featzdb,featzdb,strand,reverse(fdmd_deleted_sequence),'',recattrib_source_zdb_id  from featmkr,feature_dna_mutation_detail,record_attribution where feattype='DELETION' and strand='-' and featzdb=fdmd_feature_zdb_id and fdmd_deleted_sequence is not null and fdmd_zdb_id=recattrib_data_zdb_id;
insert into featgenomemd (fgmdfeat,fmmdid,fgmdstrand,fgmdseqref,fgmdseqvar,pub) select distinct featzdb,featzdb,strand,'',reverse(fdmd_inserted_sequence),recattrib_source_zdb_id from featmkr,feature_dna_mutation_detail,record_attribution where feattype='INSERTION' and strand='-' and featzdb=fdmd_feature_zdb_id and fdmd_inserted_sequence is not null and fdmd_zdb_id=recattrib_data_zdb_id ;
insert into featgenomemd (fgmdfeat,fmmdid,fgmdstrand,fgmdseqref,fgmdseqvar,pub)
select distinct featzdb,featzdb,strand,reverse(fdmd_deleted_sequence),reverse(fdmd_inserted_sequence),recattrib_source_zdb_id
from featmkr,feature_dna_mutation_detail,record_attribution
 where feattype='INDEL' and strand='-' and featzdb=fdmd_feature_zdb_id
and (fdmd_deleted_sequence is not null or fdmd_inserted_sequence is not null) and fdmd_zdb_id=recattrib_data_zdb_id  ;

update featgenomemd set fgmdseqref=upper(fgmdseqref);
update featgenomemd set fgmdseqvar=upper(fgmdseqvar);

update featgenomemd set fgmdseqref=replace(fgmdseqref,'A','t');
update featgenomemd set fgmdseqref=replace(fgmdseqref,'T','A');
update featgenomemd set fgmdseqref=replace(fgmdseqref,'G','c');
update featgenomemd set fgmdseqref=replace(fgmdseqref,'C','G');

update featgenomemd set fgmdseqvar=replace(fgmdseqvar,'A','t');
update featgenomemd set fgmdseqvar=replace(fgmdseqvar,'T','A');
update featgenomemd set fgmdseqvar=replace(fgmdseqvar,'G','c');
update featgenomemd set fgmdseqvar=replace(fgmdseqvar,'C','G');

update featgenomemd set fgmdseqvar=replace(fgmdseqvar,'t','T');
update featgenomemd set fgmdseqvar=replace(fgmdseqvar,'c','C');
update featgenomemd set fgmdseqref=replace(fgmdseqref,'t','T');
update featgenomemd set fgmdseqref=replace(fgmdseqref,'c','C');

update featgenomemd set fgmdseqref='N/A' where fgmdseqref is null;
update featgenomemd set fgmdseqvar='N/A' where fgmdseqvar is null;



insert into featgenomemd (fgmdfeat,fmmdid,fgmdstrand,fgmdseqref,fgmdseqvar,pub) select distinct featzdb,featzdb,strand,'G','T',recattrib_source_zdb_id from featmkr,feature_dna_mutation_detail,record_attribution where feattype='POINT_MUTATION' and strand='+' and fdmd_dna_mutation_term_zdb_id ='ZDB-TERM-130401-1976' and featzdb=fdmd_feature_zdb_id and recattrib_data_zdb_id=fdmd_zdb_id;
insert into featgenomemd (fgmdfeat,fmmdid,fgmdstrand,fgmdseqref,fgmdseqvar,pub) select distinct featzdb,featzdb,strand,'C','T',recattrib_source_zdb_id from featmkr,feature_dna_mutation_detail,record_attribution where feattype='POINT_MUTATION' and strand='+' and fdmd_dna_mutation_term_zdb_id ='ZDB-TERM-130401-1960' and featzdb=fdmd_feature_zdb_id and recattrib_data_zdb_id=fdmd_zdb_id;
insert into featgenomemd (fgmdfeat,fmmdid,fgmdstrand,fgmdseqref,fgmdseqvar,pub) select distinct featzdb,featzdb,strand,'T','G',recattrib_source_zdb_id from featmkr,feature_dna_mutation_detail,record_attribution where feattype='POINT_MUTATION' and strand='+' and fdmd_dna_mutation_term_zdb_id ='ZDB-TERM-130401-1971' and featzdb=fdmd_feature_zdb_id and recattrib_data_zdb_id=fdmd_zdb_id;
insert into featgenomemd (fgmdfeat,fmmdid,fgmdstrand,fgmdseqref,fgmdseqvar,pub) select distinct featzdb,featzdb,strand,'T','A',recattrib_source_zdb_id from featmkr,feature_dna_mutation_detail,record_attribution where feattype='POINT_MUTATION' and strand='+' and fdmd_dna_mutation_term_zdb_id ='ZDB-TERM-130401-1970' and featzdb=fdmd_feature_zdb_id and recattrib_data_zdb_id=fdmd_zdb_id;
insert into featgenomemd (fgmdfeat,fmmdid,fgmdstrand,fgmdseqref,fgmdseqvar,pub) select distinct featzdb,featzdb,strand,'C','A',recattrib_source_zdb_id from featmkr,feature_dna_mutation_detail,record_attribution where feattype='POINT_MUTATION' and strand='+' and fdmd_dna_mutation_term_zdb_id ='ZDB-TERM-130401-1968' and featzdb=fdmd_feature_zdb_id and recattrib_data_zdb_id=fdmd_zdb_id;
insert into featgenomemd (fgmdfeat,fmmdid,fgmdstrand,fgmdseqref,fgmdseqvar,pub) select distinct featzdb,featzdb,strand,'G','A',recattrib_source_zdb_id from featmkr,feature_dna_mutation_detail,record_attribution where feattype='POINT_MUTATION' and strand='+' and fdmd_dna_mutation_term_zdb_id ='ZDB-TERM-130401-1965' and featzdb=fdmd_feature_zdb_id and recattrib_data_zdb_id=fdmd_zdb_id;
insert into featgenomemd (fgmdfeat,fmmdid,fgmdstrand,fgmdseqref,fgmdseqvar,pub) select distinct featzdb,featzdb,strand,'A','T',recattrib_source_zdb_id from featmkr,feature_dna_mutation_detail,record_attribution where feattype='POINT_MUTATION' and strand='+' and fdmd_dna_mutation_term_zdb_id ='ZDB-TERM-130401-1974' and featzdb=fdmd_feature_zdb_id and recattrib_data_zdb_id=fdmd_zdb_id;
insert into featgenomemd (fgmdfeat,fmmdid,fgmdstrand,fgmdseqref,fgmdseqvar,pub) select distinct featzdb,featzdb,strand,'T','C',recattrib_source_zdb_id from featmkr,feature_dna_mutation_detail,record_attribution where feattype='POINT_MUTATION' and strand='+' and fdmd_dna_mutation_term_zdb_id ='ZDB-TERM-130401-1962' and featzdb=fdmd_feature_zdb_id and recattrib_data_zdb_id=fdmd_zdb_id;
insert into featgenomemd (fgmdfeat,fmmdid,fgmdstrand,fgmdseqref,fgmdseqvar,pub) select distinct featzdb,featzdb,strand,'A','C',recattrib_source_zdb_id from featmkr,feature_dna_mutation_detail,record_attribution where feattype='POINT_MUTATION' and strand='+' and fdmd_dna_mutation_term_zdb_id ='ZDB-TERM-130401-1973' and featzdb=fdmd_feature_zdb_id and recattrib_data_zdb_id=fdmd_zdb_id;
insert into featgenomemd (fgmdfeat,fmmdid,fgmdstrand,fgmdseqref,fgmdseqvar,pub) select distinct featzdb,featzdb,strand,'A','G',recattrib_source_zdb_id from featmkr,feature_dna_mutation_detail,record_attribution where feattype='POINT_MUTATION' and strand='+' and fdmd_dna_mutation_term_zdb_id ='ZDB-TERM-130401-1964' and featzdb=fdmd_feature_zdb_id and recattrib_data_zdb_id=fdmd_zdb_id;

insert into featgenomemd (fgmdfeat,fmmdid,fgmdstrand,fgmdseqref,fgmdseqvar,pub) select distinct featzdb,featzdb,strand,'C','G',recattrib_source_zdb_id from featmkr,feature_dna_mutation_detail,record_attribution where feattype='POINT_MUTATION' and strand='+' and fdmd_dna_mutation_term_zdb_id ='ZDB-TERM-130401-1969' and featzdb=fdmd_feature_zdb_id and recattrib_data_zdb_id=fdmd_zdb_id;
insert into featgenomemd (fgmdfeat,fmmdid,fgmdstrand,fgmdseqref,fgmdseqvar,pub) select distinct featzdb,featzdb,strand,'G','C',recattrib_source_zdb_id from featmkr,feature_dna_mutation_detail,record_attribution where feattype='POINT_MUTATION' and strand='+' and fdmd_dna_mutation_term_zdb_id ='ZDB-TERM-130401-1975' and featzdb=fdmd_feature_zdb_id and recattrib_data_zdb_id=fdmd_zdb_id;


insert into featgenomemd (fgmdfeat,fmmdid,fgmdstrand,fgmdseqref,fgmdseqvar,pub) select distinct featzdb,featzdb,strand,'C','A',recattrib_source_zdb_id from featmkr,feature_dna_mutation_detail,record_attribution where feattype='POINT_MUTATION' and strand='-' and fdmd_dna_mutation_term_zdb_id ='ZDB-TERM-130401-1976' and featzdb=fdmd_feature_zdb_id and recattrib_data_zdb_id=fdmd_zdb_id;
insert into featgenomemd (fgmdfeat,fmmdid,fgmdstrand,fgmdseqref,fgmdseqvar,pub) select distinct featzdb,featzdb,strand,'G','A',recattrib_source_zdb_id from featmkr,feature_dna_mutation_detail,record_attribution where feattype='POINT_MUTATION' and strand='-' and fdmd_dna_mutation_term_zdb_id ='ZDB-TERM-130401-1960' and featzdb=fdmd_feature_zdb_id and recattrib_data_zdb_id=fdmd_zdb_id;
insert into featgenomemd (fgmdfeat,fmmdid,fgmdstrand,fgmdseqref,fgmdseqvar,pub) select distinct featzdb,featzdb,strand,'A','C',recattrib_source_zdb_id from featmkr,feature_dna_mutation_detail,record_attribution where feattype='POINT_MUTATION' and strand='-' and fdmd_dna_mutation_term_zdb_id ='ZDB-TERM-130401-1971' and featzdb=fdmd_feature_zdb_id and recattrib_data_zdb_id=fdmd_zdb_id;
insert into featgenomemd (fgmdfeat,fmmdid,fgmdstrand,fgmdseqref,fgmdseqvar,pub) select distinct featzdb,featzdb,strand,'A','T',recattrib_source_zdb_id from featmkr,feature_dna_mutation_detail,record_attribution where feattype='POINT_MUTATION' and strand='-' and fdmd_dna_mutation_term_zdb_id ='ZDB-TERM-130401-1970' and featzdb=fdmd_feature_zdb_id and recattrib_data_zdb_id=fdmd_zdb_id;
insert into featgenomemd (fgmdfeat,fmmdid,fgmdstrand,fgmdseqref,fgmdseqvar,pub) select distinct featzdb,featzdb,strand,'G','T',recattrib_source_zdb_id from featmkr,feature_dna_mutation_detail,record_attribution where feattype='POINT_MUTATION' and strand='-' and fdmd_dna_mutation_term_zdb_id ='ZDB-TERM-130401-1968' and featzdb=fdmd_feature_zdb_id and recattrib_data_zdb_id=fdmd_zdb_id;
insert into featgenomemd (fgmdfeat,fmmdid,fgmdstrand,fgmdseqref,fgmdseqvar,pub) select distinct featzdb,featzdb,strand,'C','T',recattrib_source_zdb_id from featmkr,feature_dna_mutation_detail,record_attribution where feattype='POINT_MUTATION' and strand='-' and fdmd_dna_mutation_term_zdb_id ='ZDB-TERM-130401-1965' and featzdb=fdmd_feature_zdb_id and recattrib_data_zdb_id=fdmd_zdb_id;
insert into featgenomemd (fgmdfeat,fmmdid,fgmdstrand,fgmdseqref,fgmdseqvar,pub) select distinct featzdb,featzdb,strand,'T','A',recattrib_source_zdb_id from featmkr,feature_dna_mutation_detail,record_attribution where feattype='POINT_MUTATION' and strand='-' and fdmd_dna_mutation_term_zdb_id ='ZDB-TERM-130401-1974' and featzdb=fdmd_feature_zdb_id and recattrib_data_zdb_id=fdmd_zdb_id;
insert into featgenomemd (fgmdfeat,fmmdid,fgmdstrand,fgmdseqref,fgmdseqvar,pub) select distinct featzdb,featzdb,strand,'A','G',recattrib_source_zdb_id from featmkr,feature_dna_mutation_detail,record_attribution where feattype='POINT_MUTATION' and strand='-' and fdmd_dna_mutation_term_zdb_id ='ZDB-TERM-130401-1962' and featzdb=fdmd_feature_zdb_id and recattrib_data_zdb_id=fdmd_zdb_id;
insert into featgenomemd (fgmdfeat,fmmdid,fgmdstrand,fgmdseqref,fgmdseqvar,pub) select distinct featzdb,featzdb,strand,'T','G',recattrib_source_zdb_id from featmkr,feature_dna_mutation_detail,record_attribution where feattype='POINT_MUTATION' and strand='-' and fdmd_dna_mutation_term_zdb_id ='ZDB-TERM-130401-1973' and featzdb=fdmd_feature_zdb_id and recattrib_data_zdb_id=fdmd_zdb_id;
insert into featgenomemd (fgmdfeat,fmmdid,fgmdstrand,fgmdseqref,fgmdseqvar,pub) select distinct featzdb,featzdb,strand,'T','C',recattrib_source_zdb_id from featmkr,feature_dna_mutation_detail,record_attribution where feattype='POINT_MUTATION' and strand='-' and fdmd_dna_mutation_term_zdb_id ='ZDB-TERM-130401-1964' and featzdb=fdmd_feature_zdb_id and recattrib_data_zdb_id=fdmd_zdb_id;
insert into featgenomemd (fgmdfeat,fmmdid,fgmdstrand,fgmdseqref,fgmdseqvar,pub) select distinct featzdb,featzdb,strand,'G','C',recattrib_source_zdb_id from featmkr,feature_dna_mutation_detail,record_attribution where feattype='POINT_MUTATION' and strand='-' and fdmd_dna_mutation_term_zdb_id ='ZDB-TERM-130401-1969' and featzdb=fdmd_feature_zdb_id and recattrib_data_zdb_id=fdmd_zdb_id;
insert into featgenomemd (fgmdfeat,fmmdid,fgmdstrand,fgmdseqref,fgmdseqvar,pub) select distinct featzdb,featzdb,strand,'C','G',recattrib_source_zdb_id from featmkr,feature_dna_mutation_detail,record_attribution where feattype='POINT_MUTATION' and strand='-' and fdmd_dna_mutation_term_zdb_id ='ZDB-TERM-130401-1975' and featzdb=fdmd_feature_zdb_id and recattrib_data_zdb_id=fdmd_zdb_id;

update featgenomemd set fmmdid=get_id('FGMD');

insert into zdb_active_data (zactvd_zdb_id) select fmmdid from featgenomemd;

insert into feature_genomic_mutation_detail(fgmd_zdb_id,fgmd_feature_zdb_id,fgmd_sequence_of_reference,fgmd_sequence_of_variation,fgmd_variation_strand) select fmmdid,fgmdfeat,fgmdseqref,fgmdseqvar,fgmdstrand from featgenomemd;
insert into record_Attribution(recattrib_Data_Zdb_id,recattrib_source_zdb_id) select fmmdid,pub from featgenomemd;




