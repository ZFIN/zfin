
update tmp_term
 set term_id = (Select term_zdb_id from term where term_ont_id = so_term_id);

update tmp_term
 set term_abbrev = (Select term_name from term where term_ont_id = so_term_id)
 where term_type = 'dna_mutation_term';

select first 10 * from tmp_term;


create table mutation_detail_controlled_vocabulary (mdcv_term_zdb_id varchar(50),
						     mdcv_term_display_name varchar(255),
						     mdcv_term_abbreviation varchar(255),
						     mdcv_term_order int8,
						     mdcv_used_in varchar(100))
in tbldbs2
extent size 8 next size 8
lock mode row;


alter table mutation_detail_controlled_vocabulary
 add constraint primary key (mdcv_term_zdb_id)
 constraint mutation_detail_controlled_vocabulary_primary_key;


alter table mutation_detail_controlled_vocabulary
 add constraint (foreign key (mdcv_term_zdb_id)
 references term constraint mdcv_term_zdb_id_fk);


insert into mutation_detail_controlled_vocabulary (mdcv_term_zdb_id, mdcv_term_display_name, mdcv_used_in, mdcv_term_abbreviation, mdcv_term_order)
 select term_id, term_abbrev, term_type, term_letter, term_order
  from tmp_term
 where term_type = 'dna_mutation_term';

insert into mutation_detail_controlled_vocabulary (mdcv_term_zdb_id, mdcv_term_display_name, mdcv_used_in, mdcv_term_abbreviation, mdcv_term_order)
 select term_id, (Select term_name from term where term_ont_id = so_term_id), term_type, term_letter, term_order
  from tmp_term
 where term_type != 'dna_mutation_term';



update mutation_detail_controlled_vocabulary
 set mdcv_term_display_name = (Select term_abbrev from tmp_term where term_id = mdcv_term_zdb_id and term_type = 'amino_acid_term')
 where mdcv_used_in = 'amino_acid_term';

select first 10 * from mutation_detail_controlled_vocabulary;

update mutation_detail_controlled_vocabulary
  set mdcv_term_display_name = 'A>T'
 where mdcv_term_display_name like 'A_to_T%';

update mutation_detail_controlled_vocabulary
  set mdcv_term_display_name = 'A>C'
 where mdcv_term_display_name like 'A_to_C%';

update mutation_detail_controlled_vocabulary
  set mdcv_term_display_name = 'A>G'
 where mdcv_term_display_name like 'A_to_G%';

update mutation_detail_controlled_vocabulary
  set mdcv_term_display_name = 'T>A'
 where mdcv_term_display_name like 'T_to_A%';

update mutation_detail_controlled_vocabulary
  set mdcv_term_display_name = 'T>C'
 where mdcv_term_display_name like 'T_to_C%';

update mutation_detail_controlled_vocabulary
  set mdcv_term_display_name = 'T>G'
 where mdcv_term_display_name like 'T_to_G%';


update mutation_detail_controlled_vocabulary
  set mdcv_term_display_name = 'C>A'
 where mdcv_term_display_name like 'C_to_A%';

update mutation_detail_controlled_vocabulary
  set mdcv_term_display_name = 'C>T'
 where mdcv_term_display_name like 'C_to_T%';

update mutation_detail_controlled_vocabulary
  set mdcv_term_display_name = 'C>G'
 where mdcv_term_display_name like 'C_to_G%';

update mutation_detail_controlled_vocabulary
  set mdcv_term_display_name = 'G>A'
 where mdcv_term_display_name like 'G_to_A%';

update mutation_detail_controlled_vocabulary
  set mdcv_term_display_name = 'G>T'
 where mdcv_term_display_name like 'G_to_T%';

update mutation_detail_controlled_vocabulary
  set mdcv_term_display_name = 'G>C'
 where mdcv_term_display_name like 'G_to_C%';

