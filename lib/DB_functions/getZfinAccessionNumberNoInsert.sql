--drop function getZfinAccessionNumberNoInsert;
create or replace function getZfinAccessionNumberNoInsert (vType varchar(15))
returns varchar(40) as $zfinAccession$ 

declare sequenceValue int8;
 zfinAccession varchar(40);
 proteinPrefix varchar(15) := 'ZFINPROT';
 RNAPrefix varchar(15) := 'ZFINNUCL';
 microRNAPrefix varchar(15);

begin
if (vType = proteinPrefix or vType = RNAPrefix)
 
  then

   sequenceValue = (select accnum_sequence.nextval
       		          from single);

   zfinAccession = upper(zero_pad(vtype||sequenceValue)) ;
   

else  
      raise exception 'Prefix must be either ZFINNUCL, ZFINPROT';

end if;


return zfinAccession;
end
$zfinAccession$ LANGUAGE plpgsql
