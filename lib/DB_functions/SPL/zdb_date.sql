-- to return a date string formated the way zdbid dates are formatted 

create function zdb_date(t date) 
returning char(8);

define result char(8);
define y varchar(4);
define m varchar(2);
define d varchar(2);

--define t datetime year to day;
--let t = current;

let y = year(t);
let m = month(t);
let d = day(t);

if length(m) < 2 then 
	let m = '0'|| m;
end if;

if length(d) < 2 then 
	let d = '0'|| d;
end if;

let result = '-' || y[3,4] || m || d || '-';

return result;
end function;
