--drop function getZfinAccessionNumber;
create or replace function getZfinAccessionNumber (vType varchar(15))
returns varchar(40) as $zfinAccession$

declare sequenceValue int8;
 zfinAccession varchar(40);
 proteinPrefix varchar(15) := 'ZFINPROT';
 RNAPrefix varchar(15) := 'ZFINNUCL';
 microRNAPrefix varchar(15);

begin
if (vType = proteinPrefix or vType = RNAPrefix)
 
  then

   sequenceValue = (select nextval('accnum_sequence'));

   zfinAccession = upper(zero_pad(vtype||sequenceValue)) ;

  insert into zfin_accession_number(za_prefix, 
				     za_sequence_number,
				     za_acc_num)
     values (vType, 
     	     sequenceValue,
	     zfinAccession );
   

else  
      raise exception 'Prefix must be either ZFINNUCL, ZFINPROT';

end if;


return zfinAccession;

end
$zfinAccession$ LANGUAGE plpgsql
