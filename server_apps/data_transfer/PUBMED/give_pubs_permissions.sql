begin work ;

create temp table tmp_pubs_to_give_permissions (pub_zdb_id text);

\copy tmp_pubs_to_give_permissions from  'pubsToGivePermission.txt' with delimiter '|' ;

update publication
 set pub_can_show_images = 't'
 where exists (select 'x' from tmp_pubs_to_give_permissions where pub_zdb_id = zdb_id)
 and pub_can_show_images = 'f';


--rollback work ;

commit work;
