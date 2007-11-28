-- These db changes are part of the Anatomy Browser modifications.
-- Included are a migration of the passwords from unix crypt() to MD5 encryption.
-- The new encrypted passwords are stored in a file called zdb_md5.unl
begin work;

-- password conversion to MD5
create temp table passwordTemp (
 zdbid   varchar(50),
 password varchar(40)
 ) with no log;

load from '/research/zusers/tomc/Projects/ZFIN_WWW/server_apps/sysexecs/encryptpass/zdb_md5.unl' insert into passwordTemp;

update zdb_submitters set password = (
 select password from passwordTemp where zdbid = zdb_id);

--rollback work;
commit work;
