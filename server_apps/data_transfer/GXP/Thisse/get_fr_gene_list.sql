begin work;

-- FR_GENE_TMP --
--------------------------

create temp table fr_acc_tmp ( 
	fracc_fr	varchar(10) not null,
	fracc_acc   varchar(30) not null primary key
)with no log;

load from './frAcc.unl' insert into fr_acc_tmp; 

create temp table fr_gene_tmp ( 
	frgn_fr			varchar(10) not null,
	frgn_gene_zdb_id	varchar(50) not null,
	frgn_gene_symbol	varchar(50) not null,
	frgn_est_zdb_id		varchar(50) not null,
	frgn_est_name 		varchar(50) not null
)with no log;

insert into fr_gene_tmp 
	select fracc_fr, g.mrkr_zdb_id, g.mrkr_abbrev, 
		   c.mrkr_zdb_id, c.mrkr_name
	  from fr_acc_tmp, marker g, marker c, db_link,
		   expression_pattern
     where fracc_acc = dblink_acc_num
	   and dblink_linked_recid = xpat_probe_zdb_id
	   and dblink_info = 'gxp load' || TODAY
	   and xpat_probe_zdb_id = c.mrkr_zdb_id
	   and xpat_gene_zdb_id = g.mrkr_zdb_id;

unload to 'fr_gene.txt'
	select *
      from fr_gene_tmp
	order by frgn_gene_symbol; 

commit work;