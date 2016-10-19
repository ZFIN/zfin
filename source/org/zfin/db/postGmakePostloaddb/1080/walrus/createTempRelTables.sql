--liquibase formatted sql
--changeset sierra:createTempRelTables


create table tmprel
(mrkrzdb varchar(50),
tgtzdb varchar(50),
reltype varchar(50),
pubzdb varchar(50),
mrelid varchar(50),
cmrelid varchar(50))
 in tbldbs2;
