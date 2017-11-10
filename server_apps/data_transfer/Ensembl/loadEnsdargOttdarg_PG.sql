begin work ;

delete from ensdarg_ottdarg_mapping;

\copy from ./ensdargOttdarg3.unl
 insert into ensdarg_ottdarg_mapping;

commit work;
