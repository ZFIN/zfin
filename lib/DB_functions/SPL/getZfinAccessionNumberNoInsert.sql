--drop function getZfinAccessionNumberNoInsert;
create function getZfinAccessionNumberNoInsert (vType varchar(15))
returning varchar(40) ;

define sequenceValue int8;
define zfinAccession varchar(40);
define proteinPrefix varchar(15);
define RNAPrefix varchar(15);
define microRNAPrefix varchar(15);


let proteinPrefix = 'ZFINPROT';
let RNAPrefix = 'ZFINNUCL';

if (vType = proteinPrefix or vType = RNAPrefix)
 
  then

  let sequenceValue = (select accnum_sequence.nextval
       		          from single);

  let zfinAccession = upper(zero_pad(vtype||sequenceValue)) ;
   

else  
      raise exception -746, 0, 			-- !!! ERROR EXIT
      'Prefix must be either ZFINNUCL, ZFINPROT';

end if;


return zfinAccession;
end function;