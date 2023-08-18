create or replace function get_sequence_from_id(zdbId varchar) returns varchar as $body$

declare sequencePart varchar;

begin   

  sequencePart = substring(zdbId from 'ZDB-[A-Z_]+-\d+-(\d+)') ;

return sequencePart;

end
$body$ LANGUAGE plpgsql;

