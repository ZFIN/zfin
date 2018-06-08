-- load all ensdarg records
CREATE temp TABLE ensembl_gene_raw
  (
     ensembl_id VARCHAR(50) not null,
     uniprot_id VARCHAR(20) not null
  ) ;

\copy from ensembl-uniprot.unl
  insert into ensembl_gene_raw;

--select * from ensembl_gene_raw;
--select * from uniprot;

-- delete all ensdarg-uniprot records whose ensdarg record is not found in ZFIN
DELETE FROM ensembl_gene_raw
WHERE  NOT EXISTS (SELECT *
                   FROM   db_link
                   WHERE  dblink_acc_num = ensembl_id);


-- delete all ensdarg-uniprot records that are already in ZFIN
DELETE FROM ensembl_gene_raw
WHERE  EXISTS (SELECT *
               FROM   db_link AS ensdarg,
                      db_link AS uniprot
               WHERE  ensdarg.dblink_acc_num = ensembl_id
                      AND uniprot.dblink_acc_num = uniprot_id
                      AND ensdarg.dblink_linked_recid =
                          uniprot.dblink_linked_recid
                      AND uniprot.dblink_fdbcont_zdb_id =
                          'ZDB-FDBCONT-040412-47');

DELETE FROM ensembl_gene_raw
WHERE  EXISTS (SELECT *
               FROM   db_link AS uniprot
               WHERE  uniprot.dblink_acc_num = uniprot_id
                      AND uniprot.dblink_fdbcont_zdb_id =
                          'ZDB-FDBCONT-040412-47');


CREATE temp TABLE double_gene_uniprot
  (
     uniprot_id VARCHAR(20),
     count      INTEGER
  ) ;

INSERT INTO double_gene_uniprot
SELECT uniprot_id,
       Count(*)
FROM   ensembl_gene_raw
GROUP  BY uniprot_id
HAVING Count(*) > 1;

select * From double_gene_uniprot;

--!echo "uniprot records that are associated to more than one gene";

select count(*) From double_gene_uniprot;

DELETE FROM ensembl_gene_raw AS eg
WHERE  EXISTS (SELECT *
               FROM   double_gene_uniprot AS dgu
               WHERE  dgu.uniprot_id = eg.uniprot_id);

select * from ensembl_gene_raw;



-- load all uniprot records
CREATE temp TABLE uniprot
  (
     uniprot_id   VARCHAR(20),
     length      INTEGER,
     gene_symbol VARCHAR(50)
  ) with no log;

\copy from uniprot.unl
  insert into uniprot;

CREATE temp TABLE ensembl_gene
  (
     ensembl_id VARCHAR(50) not null,
     uniprot_id VARCHAR(20) not null,
     length integer
  ) with no log;

INSERT INTO ensembl_gene
SELECT eg.ensembl_id,
       eg.uniprot_id,
       0
FROM   ensembl_gene_raw as eg;

UPDATE ensembl_gene as eg
SET    length = (SELECT length
                 FROM   uniprot as u
                 WHERE  u.uniprot_id = eg.uniprot_id); 

select * from ensembl_gene;

-- find the ZDB-GENE id from the ensdarg id as a unique db_link

-- create new temp table to hold the ZFIN id as well
CREATE temp TABLE ensembl_gene_with_zdb
  (
     ensembl_id VARCHAR(50) NOT NULL,
     zdb_id     VARCHAR(50) NOT NULL,
     dblink_zdb_id     VARCHAR(50) NOT NULL,
     uniprot_id VARCHAR(20) NOT NULL,
     length     INTEGER
  ) ;

INSERT INTO ensembl_gene_with_zdb
SELECT eg.ensembl_id,
       '',
       '',
       eg.uniprot_id,
       eg.length
FROM   ensembl_gene AS eg;

SELECT * FROM   ensembl_gene_with_zdb;

-- delete those ensembl records that have multiple ZFIN gene records (currently 34 records)
delete from ensembl_gene_with_zdb
where exists (
SELECT dblink_acc_num, count(*)
                 FROM   db_link
                 WHERE  dblink_acc_num = ensembl_id
                 group by dblink_acc_num
                 having count(*) >1
);

-- ad ZFIN id to each ensembl record
UPDATE ensembl_gene_with_zdb
SET    zdb_id = (SELECT dblink_linked_recid
                 FROM   db_link
                 WHERE  dblink_acc_num = ensembl_id);

update ensembl_gene_with_zdb set dblink_zdb_id = get_id('DBLINK');

INSERT INTO zdb_active_data
SELECT dblink_zdb_id
FROM   ensembl_gene_with_zdb;


INSERT INTO db_link
SELECT zdb_id,
       uniprot_id,
       'Missing-UniprotId-Load from ensembl data',
       dblink_zdb_id,
       uniprot_id,
       length,
       'ZDB-FDBCONT-040412-47'
FROM   ensembl_gene_with_zdb;

-- report created new DB Links
\copy to 'new_uniprot_ids'
SELECT * FROM  ensembl_gene_with_zdb;

-- attribute given pub to these new uniprot IDs
INSERT INTO record_attribution
            (recattrib_data_zdb_id,
             recattrib_source_zdb_id,
             recattrib_source_type)
SELECT dblink_zdb_id,
                     'ZDB-PUB-170502-16',
                     'standard'
              FROM   ensembl_gene_with_zdb ;
