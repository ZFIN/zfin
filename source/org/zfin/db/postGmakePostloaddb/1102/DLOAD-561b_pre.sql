--liquibase formatted sql
--changeset pm:DLOAD-561b_pre

create table featurenogenedata (
 allele text not null
            ) ;

