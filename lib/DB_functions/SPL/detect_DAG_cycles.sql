-- detect cycles in a DAG
-- basic idea: Leafs  are not part of a cycle.(neither are Roots come to think of it)
-- if by plucking leafs (and roots) you can delete the graph, then there was no cycle.

-- Tom Conlin 2002-Mar-08

{
I cannot figure out how to pass a tablename as a parameter -- perhaps I can't. 

Kludge:
To to make this general I am assuming there is a disposable table named 'DAG' 
with columns named 'parent' and 'child'. 
And that this table may have all of it's rows deleted.

A useful side effect is you can postmortum the DAG table and 
find which rows are part of some cycle. (or trapped between cycles) 

}

drop function detect_DAG_cycle;

create function detect_DAG_cycle()
  returning integer 
  define i int; 
  delete from dag where child  is null;   
  delete from dag where parent is null;

  select (count(*) + 1) into i from dag; 
  while (i <> (select count(*) from dag))
    select count(*) into i from dag; 
    select parent from dag into temp root with no log;  -- overkill
    select child  from dag into temp leaf with no log;
    delete from dag where child  not in (select * from root);
    delete from dag where parent not in (select * from leaf);
    drop table root;
    drop table leaf;
  end while
  return i;-- _AT_LEAST_ the number of rows involved in cycle(s) 
end function;


