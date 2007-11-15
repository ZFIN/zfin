
update accession
  set acc_lnk_db = 'Entrez Gene'
  where acc_lnk_db= 'ENTREZ' ;

update accession
  set acc_lnk_db = 'GenBank'
  where acc_lnk_db= 'Genbank' ;

update accession
  set acc_lnk_db = 'GenBank'
  where acc_lnk_db= 'Genbank' ;

update accession
  set acc_lnk_db = 'Vega_Trans'
  where acc_lnk_db= 'VTRAN' ;

update accession
  set acc_lnk_db = 'UniProt'
  where acc_lnk_db= 'SWISS-PROT' ;

!echo "NOVEL" ;

update accession
  set acc_lnk_db = 'NovelGene'
  where acc_lnk_db= 'NONE' ;

update accession
  set acc_lnk_db = 'PreVega'
  where acc_lnk_db = 'VEGA' ;

alter table accession 
  add (acc_type varchar(40));

update accession
  set acc_type = 'cDNA'
  where acc_lnk_db ='GenBank';

update accession
  set acc_type = 'Vega Transcript'
  where acc_lnk_db ='PreVega';

!echo "This is the vega_trans" ;

update accession
  set acc_type = 'Vega Transcript'
  where acc_lnk_db ='Vega_Trans';

!echo "NOVEL" ;

update accession
  set acc_type = 'other'
  where acc_lnk_db ='NovelGene';

update accession
  set acc_type = 'Polypeptide'
  where acc_lnk_db in ('GenPept','UniProt', 'UniGene');

update accession
  set acc_type = 'other'
  where acc_lnk_db in ('ZFIN','Entrez Gene');

update accession
  set acc_species = 'Human'
  where acc_species = "Homo Sapiens";


update accession
  set acc_species = 'Human'
  where acc_species = "Homo sapiens";

update accession
  set acc_species = 'Mouse'
  where acc_species = "Mus musculus";

update accession
  set acc_species = 'Zebrafish'
  where acc_species = "Danio rerio";