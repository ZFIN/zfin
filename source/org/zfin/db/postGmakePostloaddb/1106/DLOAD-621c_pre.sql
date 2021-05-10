--liquibase formatted sql
--changeset pm:DLOAD-621c_pre

drop table featuredata;
create table featuredata (
 alleleid text not null,
        ensdarg1 text ,
        ensdarg2 text
            ) ;

