--liquibase formatted sql
--changeset pm:DLOAD-554_pre

create table  sanger_location (
 allele text not null,
        assembly text not null,
           chromosome text not null,
            location integer) ;

