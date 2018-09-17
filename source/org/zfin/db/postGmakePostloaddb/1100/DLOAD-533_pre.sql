--liquibase formatted sql
--changeset pm:DLOAD-533_pre

create table featuredata (
 allele text not null,
        geneid text not null
            ) ;

