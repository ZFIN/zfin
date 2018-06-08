begin work ;

delete from ensdarg_ensdarp_mapping;

\copy from ensdarPMapping3.unl
 insert into ensdarg_ensdarp_mapping;

commit work;
