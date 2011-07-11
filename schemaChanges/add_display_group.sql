insert into foreign_db_contains_display_group (fdbcdg_name,fdbcdg_definition) values ('other gene / marker pages','Displayed on the other gene / marker section of gene page.');

insert into foreign_db_contains_display_group_member (fdbcdgm_fdbcont_zdb_id,fdbcdgm_group_id) values ('ZDB-FDBCONT-040412-1',18);

insert into foreign_db_contains_display_group_member (fdbcdgm_fdbcont_zdb_id,fdbcdgm_group_id) values ('ZDB-FDBCONT-040412-14',18);


insert into foreign_db_contains_display_group_member (fdbcdgm_fdbcont_zdb_id,fdbcdgm_group_id) values ('ZDB-FDBCONT-061018-1',18);

update foreign_db_contains_display_group 
set fdbcdg_name = 'other marker pages'
where
fdbcdg_name = 'other gene / marker pages' ;

