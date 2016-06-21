--liquibase formatted sql
create  table sangerLocations(ftrAbbrev varchar(50),ftrAssembly varchar(10), ftrChrom varchar(2), locStart integer);
