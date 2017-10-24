CREATE temp TABLE tmp_cur_pub
(
  curator_zdb VARCHAR(50),
  pub_zdb     VARCHAR(50)
);

--and pub_completion_date > today - 30 units day

INSERT INTO tmp_cur_pub
  SELECT DISTINCT cur_curator_zdb_id,
    cur_pub_zdb_id
  FROM   curation,
    publication
  WHERE  cur_pub_zdb_id = zdb_id
         AND pub_completion_date BETWEEN '$DATE1' AND '$DATE2'
         AND cur_topic NOT LIKE 'Linked%'
         AND cur_curator_zdb_id NOT IN ( 'ZDB-PERS-980622-10',
                                         'ZDB-PERS-080515-1',
                                         'ZDB-PERS-131025-2',
                                         'ZDB-PERS-000914-2',
                                         'ZDB-PERS-060413-1',
                                         'ZDB-PERS-050121-1',
                                         'ZDB-PERS-030520-1 ',
                                         'ZDB-PERS-080324-3 ',
                                         'ZDB-PERS-110317-1',
                                         'ZDB-PERS-051031-1',
                                         'ZDB-PERS-050706-1',
                                         'ZDB-PERS-040127-1' )
  ORDER  BY cur_curator_zdb_id;

INSERT INTO tmp_cur_pub
  SELECT DISTINCT cur_curator_zdb_id,
    '' AS cur_pub_zdb_id
  FROM   curation
  WHERE  cur_curator_zdb_id NOT IN (SELECT curator_zdb
                                    FROM   tmp_cur_pub)
         AND cur_curator_zdb_id NOT IN ( 'ZDB-PERS-980622-10',
                                         'ZDB-PERS-080515-1',
                                         'ZDB-PERS-131025-2',
                                         'ZDB-PERS-000914-2',
                                         'ZDB-PERS-060413-1',
                                         'ZDB-PERS-050121-1',
                                         'ZDB-PERS-030520-1 ',
                                         'ZDB-PERS-080324-3 ',
                                         'ZDB-PERS-110317-1',
                                         'ZDB-PERS-051031-1',
                                         'ZDB-PERS-050706-1',
                                         'ZDB-PERS-040127-1',
                                         'ZDB-PERS-000417-1',
                                         'ZDB-PERS-000418-1',
                                         'ZDB-PERS-010716-1',
                                         'ZDB-PERS-960805-665',
                                         'ZDB-PERS-991202-1' );


CREATE temp TABLE tmp_cur_counts
(
  ccount_curator_zdb VARCHAR(50),
  ccount_category    VARCHAR(50),
  ccount_pub_zdb     INT
);

--getting count of closed pubs per curator
insert into tmp_cur_counts (ccount_curator_zdb,ccount_category,ccount_pub_zdb) 
select curator_zdb, 'PubsClosed',count(pub_zdb) from tmp_cur_pub
group by curator_zdb,pub_zdb;

insert into tmp_cur_counts (ccount_curator_zdb,ccount_category,ccount_pub_zdb) 
select curator_zdb, 'Features',count(recattrib_data_zdb_id) from tmp_cur_pub, record_attribution
where recattrib_Data_zdb_id like 'ZDB-ALT%'
and recattrib_source_zdb_id=pub_zdb
and recattrib_source_type='standard'
group by curator_zdb,pub_zdb;

insert into tmp_cur_counts (ccount_curator_zdb,ccount_category,ccount_pub_zdb) 
select curator_zdb, 'Genotypes',count(recattrib_data_zdb_id) from tmp_cur_pub, record_attribution
where recattrib_Data_zdb_id like 'ZDB-GENO%'
and recattrib_source_zdb_id=pub_zdb
group by curator_zdb,pub_zdb;

insert into tmp_cur_counts (ccount_curator_zdb,ccount_category,ccount_pub_zdb) 
select curator_zdb, 'Markers',count(recattrib_data_zdb_id) from tmp_cur_pub, record_attribution
where recattrib_Data_zdb_id in (Select mrkr_Zdb_id from marker)
and recattrib_source_zdb_id=pub_zdb
group by curator_zdb,pub_zdb;

insert into tmp_cur_counts (ccount_curator_zdb,ccount_category,ccount_pub_zdb) 
select curator_zdb, 'Expression',count(efs_xpatex_zdb_id)
from expression_figure_stage,figure,expression_result2,tmp_cur_pub
where efs_fig_zdb_id=fig_zdb_id
and xpatres_efs_id=efs_pk_id
and fig_source_zdb_id=pub_zdb
group by curator_zdb,pub_zdb;

insert into tmp_cur_counts (ccount_curator_zdb,ccount_category,ccount_pub_zdb) 
select curator_zdb, 'Phenotype',count(distinct phenos_pk_id) from tmp_cur_pub,phenotype_Experiment,figure, phenotype_statement
where phenox_fig_zdb_id=fig_zdb_id
and phenos_phenox_pk_id=phenox_pk_id
and fig_source_zdb_id=pub_zdb
group by curator_zdb,pub_zdb;

insert into tmp_cur_counts (ccount_curator_zdb,ccount_category,ccount_pub_zdb) 
select curator_zdb, 'Environment',count(distinct recattrib_Data_zdb_id) from tmp_cur_pub,record_attribution
where recattrib_data_zdb_id like 'ZDB-EXPCOND-%'
and recattrib_source_zdb_id=pub_zdb
group by curator_zdb,pub_zdb;

insert into tmp_cur_counts (ccount_curator_zdb,ccount_category,ccount_pub_zdb) 
select curator_zdb, 'GO',count(distinct mrkrgoev_zdb_id) from tmp_cur_pub, marker_go_term_evidence
where mrkrgoev_source_zdb_id=pub_zdb
group by curator_zdb,pub_zdb
order by curator_zdb;

CREATE temp TABLE tmp_total_counts
(
  totalcount_curator_zdb VARCHAR(50),
  totalcount_category    VARCHAR(50),
  totalcount_pub_zdb     INT
);

INSERT INTO tmp_total_counts
(totalcount_curator_zdb,
 totalcount_category,
 totalcount_pub_zdb)
  SELECT ccount_curator_zdb,
    ccount_category,
    Sum(ccount_pub_zdb)
  FROM   tmp_cur_counts
  GROUP  BY ccount_curator_zdb,
    ccount_category
  ORDER  BY ccount_curator_zdb;

select totalcount_curator_zdb,full_name,
MAX(CASE WHEN totalcount_category='PubsClosed' THEN totalcount_pub_zdb END)||'' AS totalcount_pub_zdb,
MAX(CASE WHEN totalcount_category='Markers' THEN totalcount_pub_zdb END)||'' AS totalcount_pub_zdb,
MAX(CASE WHEN totalcount_category='Features' THEN totalcount_pub_zdb END)||'' AS totalcount_pub_zdb,
MAX(CASE WHEN totalcount_category='Genotypes' THEN totalcount_pub_zdb END)||'' AS totalcount_pub_zdb,
MAX(CASE WHEN totalcount_category='GO' THEN totalcount_pub_zdb END)||'' AS totalcount_pub_zdb,
MAX(CASE WHEN totalcount_category='Expression' THEN totalcount_pub_zdb END) ||''AS totalcount_pub_zdb,
MAX(CASE WHEN totalcount_category='Phenotype' THEN totalcount_pub_zdb END) ||''AS totalcount_pub_zdb,
MAX(CASE WHEN totalcount_category='Environment' THEN totalcount_pub_zdb END) ||''AS totalcount_pub_zdb
from tmp_total_counts, person
where totalcount_curator_zdb=zdb_id
group by totalcount_curator_zdb,full_name;

drop table tmp_total_counts;
drop table tmp_cur_counts;
drop table tmp_cur_pub;
