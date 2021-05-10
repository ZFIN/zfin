
-- current list of withdrawn ottdarTs
unload to 'select_withdrawn.txt' delimiter '	'
select tscript_load_id from transcript 
 where tscript_status_id == 1
   and tscript_load_id[1,8] == 'OTTDART0'
;
