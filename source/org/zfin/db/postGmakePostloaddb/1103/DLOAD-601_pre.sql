--liquibase formatted sql
--changeset pm:DLOAD-601_pre

drop table featuregenedata;
create table featuregenedata (
 allele text not null,
        geneid text not null
            ) ;

