--		Define all or most of the C functions in zextend
-- For some reason you can't just reload the one function that changed, you
-- must reload all in a particular source file.

create function sysexec(key lvarchar, args lvarchar)
returning lvarchar
external name
"<!--|ROOT_PATH|-->/lib/DB_functions/zextend.so"
language c
end function;

create function html_breaks(lvarchar)
returning lvarchar
external name
"<!--|ROOT_PATH|-->/lib/DB_functions/zextend.so"
language c
end function;

create function html_breaks(html)
returning html
external name
"<!--|ROOT_PATH|-->/lib/DB_functions/zextend.so(html_breaks_html)"
language c
end function;

create function conc(lvarchar, lvarchar)
returning lvarchar
with (handlesnulls)
external name
"<!--|ROOT_PATH|-->/lib/DB_functions/zextend.so"
language c
end function;

create function get_id(lvarchar)
returning lvarchar
external name
"<!--|ROOT_PATH|-->/lib/DB_functions/zextend.so"
language c
end function;

create function expr(date)
returning date
external name
"<!--|ROOT_PATH|-->/lib/DB_functions/zextend.so"
language c
end function;

create function get_random_cookie()
returning lvarchar
external name
"<!--|ROOT_PATH|-->/lib/DB_functions/zextend.so"
language c
end function;

create function position(lvarchar, lvarchar)
returning integer
external name
"<!--|ROOT_PATH|-->/lib/DB_functions/zextend.so"
language c
end function;

create function webhtml_like(html, lvarchar)
returning boolean
external name
"<!--|ROOT_PATH|-->/lib/DB_functions/zextend.so(webhtml_like)"
language c
end function;
