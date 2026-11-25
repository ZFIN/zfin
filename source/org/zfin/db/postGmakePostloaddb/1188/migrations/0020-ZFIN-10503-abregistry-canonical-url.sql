--liquibase formatted sql
--changeset rtaylor:ZFIN-10503-abregistry-canonical-url

-- The Antibody Registry moved to https://www.antibodyregistry.org/ (a JavaScript app that
-- serves a record at /AB_<id>). The old http://antibodyregistry.org/ prefix only works through
-- redirects, and the antibody page used to hardcode a search.php?q= URL instead of this prefix.
update foreign_db
   set fdb_db_query = 'https://www.antibodyregistry.org/'
 where fdb_db_name = 'ABRegistry'
   and fdb_db_query <> 'https://www.antibodyregistry.org/';

--rollback update foreign_db set fdb_db_query = 'https://antibodyregistry.org/' where fdb_db_name = 'ABRegistry';
