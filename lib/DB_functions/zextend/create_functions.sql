--		Define all or most of the C functions in zextend
-- For some reason you can't just reload the one function that changed, you
-- must reload all in a particular source file.

create function sysexec(key lvarchar, args lvarchar)
returning lvarchar
external name
"/private/lib/c_functions/zextend.so"
language c
end function;

create function html_breaks(lvarchar)
returning lvarchar
external name
"/private/lib/c_functions/zextend.so"
language c
end function;

create function html_breaks(html)
returning html
external name
"/private/lib/c_functions/zextend.so(html_breaks_html)"
language c
end function;

create function conc(lvarchar, lvarchar)
returning lvarchar
with (handlesnulls)
external name
"/private/lib/c_functions/zextend.so"
language c
end function;

create function get_random_cookie()
returning lvarchar
external name
"/private/lib/c_functions/zextend.so"
language c
end function;

create function position(lvarchar, lvarchar)
returning integer
external name
"/private/lib/c_functions/zextend.so"
language c
end function;
