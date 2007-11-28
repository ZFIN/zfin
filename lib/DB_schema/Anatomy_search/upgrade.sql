-- These db changes are part of the Anatomy Browser modifications.
-- Included are a migration of the passwords from unix crypt() to MD5 encryption.
-- The new encrypted passwords are stored in a file called zdb_md5.unl
begin work;

update anatomy_relationship_type set areltype_1_to_2_display = 'has subtype', areltype_2_to_1_display = 'is a type of'
 where areltype_dagedit_id = 'is_a';

update anatomy_relationship_type set areltype_2_to_1_display = 'is part of'
 where areltype_dagedit_id = 'part_of';

update anatomy_relationship_type set areltype_1_to_2_display = 'has parts'
 where areltype_dagedit_id = 'part_of';

-- password conversion to MD5
create temp table passwordTemp (
 zdbid   varchar(50),
 password varchar(40)
 ) with no log;

load from '/research/zusers/tomc/Projects/ZFIN_WWW/server_apps/sysexecs/encryptpass/zdb_md5.unl' insert into passwordTemp;

alter table zdb_submitters add (password_old varchar(20));

update zdb_submitters set password_old = password;

-- need to drop and recreate the password column as it is too
-- short to contain the MD5 passwords
alter table zdb_submitters drop password;

alter table zdb_submitters add (password varchar(40));

update zdb_submitters set password = (
 select password from passwordTemp where zdbid = zdb_id);

-- end password conversion

                                      
-- strip off trailing spaces in the object_type column defined as char(32)
alter table anatomy_stats add (type_old varchar(32));

update anatomy_stats set type_old = trim(anatstat_object_type);

alter table anatomy_stats drop anatstat_object_type;

alter table anatomy_stats add (anatstat_object_type varchar(32));

update anatomy_stats set anatstat_object_type = type_old;

alter table anatomy_stats drop type_old;

-- end of strip trailing spaces

--rollback work;
commit work;
