begin work ;


--
-- Table structure for table accession
--

CREATE TABLE accession (
  acc_id int,
  acc_lnk_db varchar(40),
  acc_accession varchar(50),
  acc_length int,
  acc_display varchar(50),
  acc_defline varchar(150),
  acc_species varchar(30)
  ) 
fragment by round robin in tbldbs1, tbldbs2, tbldbs3 
extent size 2048 next size 2048;

--
-- Dumping data for table accession
--


--
-- Table structure for table auth
--

CREATE TABLE auth (
  username varchar(10),
  password varchar(32),
  auth_timestmp datetime year to second,
  auth_on_time datetime year to second,
  auth_off_time datetime year to second,
  auth_role varchar(20),
  auth_display varchar(80)
) ;

--
-- Dumping data for table auth

--
-- Table structure for table candidate
--

CREATE TABLE candidateR (
  cnd_id int,
  cnd_priority int ,
  cnd_flag varchar(10),
  cnd_note text,
  cnd_type varchar(25)
 ) fragment by round robin in tbldbs1, tbldbs2, tbldbs3 extent size 1024 next size 1024;

--
-- Dumping data for table candidateR
--


--
-- Table structure for table cnd_acc
--

CREATE TABLE cnd_acc (
  cndacc_cnd_id int,
  cndacc_acc_id int
) fragment by round robin in tbldbs1, tbldbs2, tbldbs3 extent size 1024 next size 1024;

--
-- Dumping data for table cnd_acc
--



--
-- Table structure for table curation
--


CREATE TABLE entrez_accession (
  ent_acc_txid int,
  ent_acc_gene_id int,
  ent_acc_nucl_acc varchar(40),
  ent_acc_prot_acc varchar(40),
  ent_acc_id int
) fragment by round robin in tbldbs1, tbldbs2, tbldbs3 extent size 1024 next size 1024;


--
-- Table structure for table hit
--

CREATE TABLE hit (
  hit_id int,
  hit_rpt_id int,
  hit_order int,
  hit_score int,
  hit_bits int,
  hit_expect double precision,
  hit_prob float,
  hit_identites_num int,
  hit_identites_denom int,
  hit_positives_num int,
  hit_positives_denom int,
  hit_strand varchar(30),
  hit_alignment lvarchar(20000) )
  fragment by round robin in tbldbs1, tbldbs2, tbldbs3 
          extent size 2048 next size 2048;

CREATE TABLE link (
  lnk_db varchar(40),
  lnk_type varchar(40),
  lnk_url varchar(40)
 ) in tbldbs1 extent size 8 next size 8;

--
-- Dumping data for table link
--

--
-- Table structure for table orthology_acc

CREATE TABLE orthology_acc (
  orthacc_cur_run_id int,
  orthacc_cur_cnd_id int,
  orthacc_acc_id int,
  orthacc_evd varchar(10)

) fragment by round robin in tbldbs1, tbldbs2, tbldbs3 extent size 1024 next size 1024;

--
-- Dumping data for table orthology_acc
--

--
-- Table structure for table query_acc
--

CREATE TABLE query_acc (
  qryacc_rpt_id int,
  qryacc_acc_id int
) fragment by round robin in tbldbs1, tbldbs2, tbldbs3 extent size 1024 next size 1024 ;

--
-- Dumping data for table query_acc
--

--
-- Table structure for table report
--

CREATE TABLE report (
  rpt_id int,
  rpt_run_id int,
  rpt_cnd_id int,
  rpt_curator varchar,
  rpt_exitcode int,
  rpt_checkout datetime year to second,
  rpt_checkin datetime year to second,
  rpt_details lvarchar
) fragment by round robin in tbldbs1, tbldbs2, tbldbs3
extent size 2048 next size 2048;

--
-- Dumping data for table report
--


--
-- Table structure for table run
--

CREATE TABLE runR (
  runR_id int,
  runR_name varchar(80),
  runR_host varchar(20),
  runR_datetime datetime year to second,
  runR_program varchar(10),
  runR_version varchar(10),
  runR_blast_db varchar(255),
  runR_parameters varchar(150),
  runR_matrix varchar(50)
) in tbldbs1 extent size 8 next size 8 ;

--
-- Dumping data for table runR
--
--
-- Table structure for table target_acc
--

CREATE TABLE target_acc (
  trgtacc_hit_id int,
  trgtacc_acc_id int
) fragment by round robin in tbldbs1, tbldbs2, tbldbs3 extent size 1024 next size 1024;

--
-- Dumping data for table target_acc
--

commit work ;

--rollback work ;