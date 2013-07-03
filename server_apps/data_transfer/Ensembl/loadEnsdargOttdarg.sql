begin work ;

delete from ensdarg_ottdarg_mapping;

load from ensdargOttdarg3.unl
 insert into ensdarg_ottdarg_mapping;

commit work;