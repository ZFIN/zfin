--begin work;


CREATE temp TABLE thisse_blast(
  Query         varchar(30) DEFAULT '' NOT NULL,
  Subject       varchar(30) DEFAULT '' NOT NULL,
  Identity      decimal(5,2)  NOT NULL,
  Length        integer DEFAULT 0 NOT NULL,
  Mismatches    integer DEFAULT 0 NOT NULL,
  Gaps          integer DEFAULT 0 NOT NULL,
  QStart        integer DEFAULT 0 NOT NULL,
  QEnd          integer DEFAULT 0 NOT NULL,
  SStart        integer DEFAULT 0 NOT NULL,
  SEnd          integer DEFAULT 0 NOT NULL,
  Expect        varchar(10)  NOT NULL,
  Score         varchar(10)  NOT NULL
)with no log;

!echo "Load thisse2gb.unl, and unload to thisse2gb.fin with ZFIN gene"

load from thisse2gb.unl insert into thisse_blast;

unload to thisse2gb.fin
	select Query, Subject, mrkr_abbrev, Identity, Length, Mismatches, 
		Gaps, QStart, QEnd, SStart, SEnd, Expect, Score
 	  from thisse_blast, outer(db_link, marker)
  	 where Subject = acc_num
   	   and linked_recid = mrkr_zdb_id
	   and db_name = "Genbank";

delete from thisse_blast;



!echo "Load thisse2est.unl, and unload to thisse2est.fin with ZFIN gene"
  
load from thisse2est.unl insert into thisse_blast;

unload to thisse2est.fin
	select Query, Subject, mrkr_abbrev, Identity, Length, Mismatches, 
		Gaps, QStart, QEnd, SStart, SEnd, Expect, Score
 	  from thisse_blast, outer(db_link, marker)
  	 where Subject = acc_num
   	   and linked_recid = mrkr_zdb_id
	   and db_name = "Genbank";

delete from thisse_blast;


!echo "Load thisse2sp.unl, and unload to thisse2sp.fin with ZFIN gene"
  
load from thisse2sp.unl insert into thisse_blast;

unload to thisse2sp.fin
	select Query, Subject, mrkr_abbrev, Identity, Length, Mismatches, 
		Gaps, QStart, QEnd, SStart, SEnd, Expect, Score
 	  from thisse_blast, outer(db_link, marker)
  	 where Subject = acc_num
   	   and linked_recid = mrkr_zdb_id;

delete from thisse_blast;

