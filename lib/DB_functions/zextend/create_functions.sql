--		Define all or most of the C functions in zextend
-- For some reason you can't just reload the one function that changed, you
-- must reload all in a particular source file.
-- FOLLOWING LINE JUST FOR TESTING---LEAVE IN AS COMMENT FOR NOW Mar 8, 2000 AEK
connect to 'ztest' user 'informix' using 'KilAsq.';
drop function lower;
drop function upper;
drop function sysexec;
drop function replace;
drop function html_breaks (lvarchar);
drop function html_breaks (html);
drop function html_to_lvarchar(html);
drop function conc;
drop function get_id;
drop function get_id_test;
drop function expr(date);
drop function expr;
drop function get_random_cookie;
drop function now;
drop function todays_date;
drop function position;
drop function webhtml_like(html,lvarchar);

-- create function lower(str lvarchar)
-- returning lvarchar
-- external name
-- "/research/zfin/develop/informix/zextend/zfin/zextend.o(lower)"
-- language c
-- end function;

-- create function upper(str lvarchar)
-- returning lvarchar
-- external name
-- "/research/zfin/develop/informix/zextend/zfin/zextend.so(upper)"
-- language c
-- end function;

create function sysexec(key lvarchar, args lvarchar)
returning lvarchar
external name
"/research/zfin/develop/informix/zextend/zfin/zextend.so"
language c
end function;

create function replace(old lvarchar, new lvarchar, src lvarchar, n
integer)
returning lvarchar
external name
"/research/zfin/develop/informix/zextend/zfin/zextend.so"
language c
end function;

create function html_breaks(lvarchar)
returning lvarchar
external name
"/research/zfin/develop/informix/zextend/zfin/zextend.so"
language c
end function;

create function html_breaks(html)
returning html
external name
"/research/zfin/develop/informix/zextend/zfin/zextend.so(html_breaks_html)"
language c
end function;

create function conc(lvarchar, lvarchar)
returning lvarchar
with (handlesnulls)
external name
"/research/zfin/develop/informix/zextend/zfin/zextend.so"
language c
end function;

create function get_id(lvarchar)
returning lvarchar
external name
"/research/zfin/develop/informix/zextend/zfin/zextend.so"
language c
end function;

create function get_id_test(name lvarchar, n integer)
returning lvarchar
external name
"/research/zfin/develop/informix/zextend/zfin/zextend.so"
language c
end function;

create function expr(date)
returning date
external name
"/research/zfin/develop/informix/zextend/zfin/zextend.so"
language c
end function;

create function expr(datetime year to fraction)
returning datetime year to fraction
external name
"/research/zfin/develop/informix/zextend/zfin/zextend.so"
language c
end function;

create function get_random_cookie()
returning lvarchar
external name
"/research/zfin/develop/informix/zextend/zfin/zextend.so"
language c
end function;

create function now()
returning datetime year to fraction
external name
"/research/zfin/develop/informix/zextend/zfin/zextend.so"
language c
end function;

create function todays_date()
returning date
external name
"/research/zfin/develop/informix/zextend/zfin/zextend.so"
language c
end function;

create function position(lvarchar, lvarchar)
returning integer
external name
"/research/zfin/develop/informix/zextend/zfin/zextend.so"
language c
end function;

create function webhtml_like(html, lvarchar)
returning boolean
external name
"/research/zfin/develop/informix/zextend/zfin/zextend.so(webhtml_like)"
language c
end function;
