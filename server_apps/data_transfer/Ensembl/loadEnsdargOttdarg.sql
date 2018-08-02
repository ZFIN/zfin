begin work ;

delete from ensdarg_ottdarg_mapping;

\copy ensdarg_ottdarg_mapping from './ensdargOttdarg3.unl';

commit work;
