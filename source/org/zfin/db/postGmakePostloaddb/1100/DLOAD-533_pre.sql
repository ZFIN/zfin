--liquibase formatted sql
--changeset pm:DLOAD-533_pre

create table feature_data (
 allele text not null,
        geneid text not null
            ) ;

