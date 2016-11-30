begin work ;

create temp table tmp_report (zdb_id varchar(50),
       	    	  	      symbol varchar(255),
			      name varchar(255),
			      pubs_with_go int default 0,
			      pubs_with_xpat int default 0,
			      number_go int default 0,
			      number_xpat int default 0)
with no log;

insert into tmp_report (zdb_id, symbol, name)
 select mrkr_Zdb_id, mrkr_abbrev, mrkr_name 
 from marker
 where mrkr_type = 'GENE';

create unique index mrkr_indx 
 on tmp_report(zdb_id)
 using  btree in idxdbs3;

update tmp_report
 set pubs_with_go = (Select count(distinct mrkrgoev_source_zdb_id)
     		    	    from marker_go_term_evidence, publication
			    where jtype = 'Journal'
			    and mrkrgoev_source_zdb_id = publication.zdb_id
			    and mrkrgoev_mrkr_zdb_id = tmp_report.zdb_id)
 where exists (Select 'x' from marker_go_term_evidence
			    where mrkrgoev_mrkr_zdb_id = zdb_id);

update tmp_report
 set number_go = (select count(*) from marker_go_term_evidence
     	       	 	 where mrkrgoev_mrkr_zdb_id = zdb_id)
 where exists (Select 'x' from marker_go_term_evidence
			    where mrkrgoev_mrkr_zdb_id = zdb_id);

select count(*) from tmp_report
 where number_go !=0
and (pubs_with_go = 0 or pubs_with_go = '');

select first 1 * from tmp_report
 where number_go != 0;

update tmp_report
 set pubs_with_xpat = (select count(distinct xpatex_source_Zdb_id)
     		      	      from expression_experiment2, publication
			    where jtype = 'Journal'
			    and publication.zdb_id = xpatex_source_zdb_id
			      and xpatex_gene_zdb_id = tmp_report.zdb_id)
 where exists (Select 'x' from expression_experiment2
			      where xpatex_gene_zdb_id = zdb_id);

update tmp_report
 set number_xpat = (select count(*)
     		      	      from expression_experiment2
			      where xpatex_gene_zdb_id = zdb_id)
 where exists (Select 'x' from expression_experiment2
			      where xpatex_gene_zdb_id = zdb_id);


select count(*) from tmp_report
 where number_xpat !=0
and (pubs_with_xpat = 0 or pubs_with_xpat = '');

select first 1 * from tmp_report
 where number_xpat != 0;


unload to machineLearningReport_161129.txt
 select zdb_id,
 	symbol ,
	name,
	pubs_with_go,
	pubs_with_xpat,
	number_go,
	number_xpat
 from tmp_Report;
--commit work;

rollback work;
