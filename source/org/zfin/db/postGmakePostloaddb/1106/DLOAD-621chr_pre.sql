--liquibase formatted sql
--changeset pm:DLOAD-621chr_pre

drop table if exists sanger_location ;
create table  sanger_location (
 allele text not null,
        assembly text not null,
           chromosome text not null,
            location integer) ;

