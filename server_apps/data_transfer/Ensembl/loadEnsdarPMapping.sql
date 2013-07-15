begin work ;

delete from ensdarg_ensdarp_mapping;

load from ensdarPMapping3.unl
 insert into ensdarg_ensdarp_mapping;

commit work;