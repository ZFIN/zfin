begin work ;

delete from ensdarg_ensdarp_mapping;

\copy ensdarg_ensdarp_mapping from 'ensdarPMapping3.unl';

commit work;
