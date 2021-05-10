--liquibase formatted sql
--changeset pm:HBurgessChr_pre

create table  burgess_location (
 allele text not null,
        assembly text not null,
           chromosome text not null,
            location integer) ;
