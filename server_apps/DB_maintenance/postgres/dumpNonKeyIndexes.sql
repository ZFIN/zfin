begin work;
unload to nonKeyIndexes.sql
  SELECT 'create '|| 'index ' || c.idxname || ' on ' || a.tabname ||'('|| d.colname||')'
  	 FROM systables a, sysindexes c, syscolumns d
	 WHERE  a.tabid = c.tabid
	 and d.tabid = a.tabid
	 and c.idxtype != 'U'
	 AND (d.colno = c.part1 or
		d.colno = c.part2 or
		d.colno = c.part3 or
		d.colno = c.part4 or
		d.colno = c.part5 or
		d.colno = c.part6 or
		d.colno = c.part7 or
		d.colno = c.part8 or
		d.colno = c.part9 or
		d.colno = c.part10 or
		d.colno = c.part11 or
		d.colno = c.part12 or
		d.colno = c.part13 or
		d.colno = c.part14 or
		d.colno = c.part15 or
		d.colno = c.part16)
        and not exists (Select 'x' 
	    	       	       from sysconstraints f 
			       where f.idxname = c.idxname)
	ORDER BY a.tabname, d.colname;

commit work;
