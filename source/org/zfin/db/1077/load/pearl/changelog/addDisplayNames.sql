begin work;

update mutation_detail_controlled_vocabulary 
 set mdcv_term_display_name = 'nonsynonymous'
 where mdcv_term_zdb_id = (Select term_zdb_id from term
       			  	  where term_ont_id = 'SO:0001992');

update mutation_detail_controlled_vocabulary 
 set mdcv_term_display_name = 'stop gained'
 where mdcv_term_zdb_id = (Select term_zdb_id from term
       			  	  where term_ont_id = 'SO:0001587');

update mutation_detail_controlled_vocabulary 
 set mdcv_term_display_name = 'missense'
 where mdcv_term_zdb_id = (Select term_zdb_id from term
       			  	  where term_ont_id = 'SO:0001583');

update mutation_detail_controlled_vocabulary 
 set mdcv_term_display_name = 'frameshift'
 where mdcv_term_zdb_id = (Select term_zdb_id from term
       			  	  where term_ont_id = 'SO:0001589');


update mutation_detail_controlled_vocabulary 
 set mdcv_term_display_name = 'frameshift truncation'
 where mdcv_term_zdb_id = (Select term_zdb_id from term
       			  	  where term_ont_id = 'SO:0001910');

update mutation_detail_controlled_vocabulary 
 set mdcv_term_display_name = "3' UTR variant"
 where mdcv_term_zdb_id = (Select term_zdb_id from term
       			  	  where term_ont_id = 'SO:0001624');


update mutation_detail_controlled_vocabulary 
 set mdcv_term_display_name = "5' UTR variant"
 where mdcv_term_zdb_id = (Select term_zdb_id from term
       			  	  where term_ont_id = 'SO:0001623');

update mutation_detail_controlled_vocabulary 
 set mdcv_term_display_name = 'splicing variant'
 where mdcv_term_zdb_id = (Select term_zdb_id from term
       			  	  where term_ont_id = 'SO:0001568');


update mutation_detail_controlled_vocabulary 
 set mdcv_term_display_name = 'gain'
 where mdcv_term_zdb_id = (Select term_zdb_id from term
       			  	  where term_ont_id = 'SO:0001573');


update mutation_detail_controlled_vocabulary 
 set mdcv_term_display_name = 'loss'
 where mdcv_term_zdb_id = (Select term_zdb_id from term
       			  	  where term_ont_id = 'SO:0001572');

update mutation_detail_controlled_vocabulary 
 set mdcv_term_display_name = 'splice site'
 where mdcv_term_zdb_id = (Select term_zdb_id from term
       			  	  where term_ont_id = 'SO:0001629');

update mutation_detail_controlled_vocabulary 
 set mdcv_term_display_name = 'cryptic splice site'
 where mdcv_term_zdb_id = (Select term_zdb_id from term
       			  	  where term_ont_id = 'SO:0001569');

update mutation_detail_controlled_vocabulary 
 set mdcv_term_display_name = 'start loss'
 where mdcv_term_zdb_id = (Select term_zdb_id from term
       			  	  where term_ont_id = 'SO:0002012');

update mutation_detail_controlled_vocabulary 
 set mdcv_term_display_name = 'inframe deletion'
 where mdcv_term_zdb_id = (Select term_zdb_id from term
       			  	  where term_ont_id = 'SO:0001822');

update mutation_detail_controlled_vocabulary 
 set mdcv_term_display_name = 'stop loss'
 where mdcv_term_zdb_id = (Select term_zdb_id from term
       			  	  where term_ont_id = 'SO:0001578');

update mutation_detail_controlled_vocabulary 
 set mdcv_term_display_name = 'cryptic acceptor splice site'
 where mdcv_term_zdb_id = (Select term_zdb_id from term
       			  	  where term_ont_id = 'SO:0001570');
update mutation_detail_controlled_vocabulary 
 set mdcv_term_display_name = 'cryptic donor splice site'
 where mdcv_term_zdb_id = (Select term_zdb_id from term
       			  	  where term_ont_id = 'SO:0001571');

update mutation_detail_controlled_vocabulary 
 set mdcv_term_display_name = 'polypeptide truncation'
 where mdcv_term_zdb_id = (Select term_zdb_id from term
       			  	  where term_ont_id = 'SO:0001617');
update mutation_detail_controlled_vocabulary 
 set mdcv_term_display_name = 'amino acid substitution'
 where mdcv_term_zdb_id = (Select term_zdb_id from term
       			  	  where term_ont_id = 'SO:0001606');
update mutation_detail_controlled_vocabulary 
 set mdcv_term_display_name = 'non conservative amino acid substitution'
 where mdcv_term_zdb_id = (Select term_zdb_id from term
       			  	  where term_ont_id = 'SO:0001608');
update mutation_detail_controlled_vocabulary 
 set mdcv_term_display_name = 'amino acid deletion'
 where mdcv_term_zdb_id = (Select term_zdb_id from term
       			  	  where term_ont_id = 'SO:0001604');
update mutation_detail_controlled_vocabulary 
 set mdcv_term_display_name = 'elongated polypeptide'
 where mdcv_term_zdb_id = (Select term_zdb_id from term
       			  	  where term_ont_id = 'SO:0001609');
update mutation_detail_controlled_vocabulary 
 set mdcv_term_display_name = 'polypeptide fusion '
 where mdcv_term_zdb_id = (Select term_zdb_id from term
       			  	  where term_ont_id = 'SO:0001616');
update mutation_detail_controlled_vocabulary 
 set mdcv_term_display_name = 'donor splice site'
 where mdcv_term_zdb_id = (Select term_zdb_id from term
       			  	  where term_ont_id = 'SO:0000163');
update mutation_detail_controlled_vocabulary 
 set mdcv_term_display_name = 'acceptor splice site'
 where mdcv_term_zdb_id = (Select term_zdb_id from term
       			  	  where term_ont_id = 'SO:0000164');
update mutation_detail_controlled_vocabulary 
 set mdcv_term_display_name = 'splice junction'
 where mdcv_term_zdb_id = (Select term_zdb_id from term
       			  	  where term_ont_id = 'SO:0001421');
update mutation_detail_controlled_vocabulary 
 set mdcv_term_display_name = 'promoter'
 where mdcv_term_zdb_id = (Select term_zdb_id from term
       			  	  where term_ont_id = 'SO:0000167');
update mutation_detail_controlled_vocabulary 
 set mdcv_term_display_name = 'start codon'
 where mdcv_term_zdb_id = (Select term_zdb_id from term
       			  	  where term_ont_id = 'SO:0000318');
update mutation_detail_controlled_vocabulary 
 set mdcv_term_display_name = "5' UTR"
 where mdcv_term_zdb_id = (Select term_zdb_id from term
       			  	  where term_ont_id = 'SO:0000204');
update mutation_detail_controlled_vocabulary 
 set mdcv_term_display_name = "3' UTR"
 where mdcv_term_zdb_id = (Select term_zdb_id from term
       			  	  where term_ont_id = 'SO:0000205');
update mutation_detail_controlled_vocabulary 
 set mdcv_term_display_name = 'enhancer'
 where mdcv_term_zdb_id = (Select term_zdb_id from term
       			  	  where term_ont_id = 'SO:0000165');

commit work;

--rollback work;
