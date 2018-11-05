--liquibase formatted sql
--changeset pm:DLOAD-561c_pre

create table featurezmp (
 allele text not null,
 geneabbrev not null
            ) ;
