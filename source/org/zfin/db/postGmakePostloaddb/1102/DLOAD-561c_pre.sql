--liquibase formatted sql
--changeset pm:DLOAD-561c_pre

create table featurezmpdata (
 allele text not null,
        ensdargid text not null,
        geneabbrev text not null
            ) ;
