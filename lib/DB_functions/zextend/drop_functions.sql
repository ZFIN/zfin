--		Define all or most of the C functions in zextend
-- For some reason you can't just reload the one function that changed, you
-- must reload all in a particular source file.

drop function sysexec;
drop function html_breaks (lvarchar);
drop function html_breaks (html);
drop function conc;
drop function expr(date);
drop function get_random_cookie;
drop function position;
drop function webhtml_like(html,lvarchar);

