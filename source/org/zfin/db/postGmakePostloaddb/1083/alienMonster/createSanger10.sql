--liquibase formatted sql
--changeset prita:createSanger10

create table tmp_sanger10location (ftrAbbrev varchar(50),ftrAssembly varchar(10), ftrChrom varchar(2), locStart int,ftrzdb varchar(50))
			     in tbldbs1
extent size 16 next size 16;

