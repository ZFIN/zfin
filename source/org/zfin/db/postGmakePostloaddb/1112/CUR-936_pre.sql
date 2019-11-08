--liquibase formatted sql
--changeset pm:CUR-936_pre

drop table if exists seqvar;
create table seqvar (featureid text,featureabb text,geneid text,geneabb text,mutterm text,mutname text,location text,mutdesc text,refseq text,varseq text);
