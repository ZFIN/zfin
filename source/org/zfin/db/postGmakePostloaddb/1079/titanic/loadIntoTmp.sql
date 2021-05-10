begin work;

load from tt_gap1_test.txt 
 insert into tmp_gap_tt;

select * from tmp_gap_tt
 where expcondid = 'ZDB-EXPCOND-150327-12';

--rollback work;
commit work;
