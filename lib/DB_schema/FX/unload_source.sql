begin work ;

execute procedure set_session_params();

unload to ./source.unl 
select zdb_id,source from publication 
 where source is not null
 and source != '' 
 order by source;

commit work ;