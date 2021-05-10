--liquibase formatted sql
--changeset pm:DLOAD-561a_pre

create table featuregenedata (
 allele text not null,
        geneid text not null
            ) ;

