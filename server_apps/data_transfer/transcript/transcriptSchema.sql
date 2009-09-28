begin work ;

create table int_fdbcont_blastdb (ifb_fdbcont_zdb_id varchar(50) not null constraint ifb_fdbcont_zdb_id_not_null, 
       	     			  ifb_blastdb_zdb_id varchar(50) not null constraint ifb_blastdb_zdb_id_not_null)
in tbldbs2
extent size 32 next size 32;

create unique index ifb_primary_key_index 
 on int_fdbcont_blastdb (ifb_fdbcont_zdb_id, ifb_blastdb_zdb_id)
 using btree in idxdbs2;

create index ifb_blastdb_foreign_key_index 
  on int_fdbcont_blastdb (ifb_blastdb_zdb_id)
  using btree in idxdbs3;

create index ifb_fdbcont_zdb_id_foreign_key_index 
  on int_fdbcont_blastdb (ifb_fdbcont_zdb_id)
  using btree in idxdbs2;

alter table int_fdbcont_blastdb
  add constraint (foreign key (ifb_fdbcont_zdb_id)
  references foreign_db_contains on delete cascade 
  constraint ifb_fdbcont_foreign_key_odc);

alter table int_fdbcont_blastdb
  add constraint (foreign key (ifb_blastdb_zdb_id)
  references blast_database on delete cascade 
  constraint ifb_blastdb_foreign_key_odc);

alter table int_fdbcont_blastdb
  add constraint primary key (ifb_fdbcont_zdb_id, ifb_blastdb_zdb_id)
  constraint ifb_primary_key ;


alter table marker_types
  modify (mrkrtype_type_display varchar(60));

alter table marker_types
  modify (marker_type varchar(10) not null constraint mrktype_type_not_null);

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ("TSCRIPT","1","Transcript");

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ("PRTCDNG","1","Protein Coding");

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ("PRTCDNGIP","1","Protein Coding in Process");

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ("KPRTCDNG","1","Known Protein Coding");

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ("NPRTCDNG","1","Novel Protein Coding");

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ("NOVELCDS","1","Novel CDS");

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ("PUTPRTCDNG","1","Putative Protein Coding");

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ("PRTPRTCDNG","1","Protein Coding in progress");

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ("PRCSTSCRPT","1","Processed transcript");

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ("NCODING","1","Non-coding");

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ("MIRNA","1","miRNA");

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ("PIRNA","1","piRNA");

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ("RASRNA","1","rasRNA");

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ("SCRNA","1","scRNA");

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ("SIRNA","1","siRNA");

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ("SNRNA","1","snRNA");

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ("SNORNA","1","snoRNA");

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ("TRNA","1","tRNA");

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ("VAULTRNA","1","vault RNA");

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ("AMBORF","1","Ambiguous ORF");

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ("ANTISENSET","1","Antisense Transcript");

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ("RETINTRON","1","Retained Intron");

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ("PTPRTSCRPT","1","Putative processed transcript");

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ("PDPRTSRTPT","1","Predicted processed transcript");

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ("PTCDS","1","Putative CDS");

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ("GENEPT","1","Psuedogene transcript");

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ("PRGENEP","1","Processed pseudogene");

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ("UPRGENEP","1","Unprocessed pseudogene");

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ("TSCRBGENEP","1","Transcribed pseudogene");

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ("EXPGENEP","1","Expressed pseudogene");

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ("KNOWNCDS","1","Known CDS");

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ("IGGENE","1","Ig Gene");

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ("NMDECAY","1","Nonsense Mediated Decay");

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ("ARTIFACTT","1","Artifact Transcript");

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ("TSPSONT","1","Transposon Transcript");


insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ("DISDOMAIN","1","Disrupted Domain");


insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ("POLYGENEP","1","Polymorphic Pseudogene");

--MTGrp

insert into marker_type_group (mtgrp_name, mtgrp_comments)
  values ('TRANSCRIPT', "group to hold all sub-types of transcripts");

insert into marker_type_Group_member(mtgrpmem_mrkr_Type, mtgrpmem_mrkr_type_group)
  values ("RTCDNG","TRANSCRIPT");

insert into marker_type_Group_member(mtgrpmem_mrkr_Type, mtgrpmem_mrkr_type_group)
  values ("TSCRIPT","TRANSCRIPT");

insert into marker_type_Group_member(mtgrpmem_mrkr_Type, mtgrpmem_mrkr_type_group)
  values ("DISDOMAIN","TRANSCRIPT");

insert into marker_type_Group_member(mtgrpmem_mrkr_Type, mtgrpmem_mrkr_type_group)
  values ("IGGENE","TRANSCRIPT");

insert into marker_type_Group_member(mtgrpmem_mrkr_Type, mtgrpmem_mrkr_type_group)
  values ("TSPSONT","TRANSCRIPT");

insert into marker_type_Group_member(mtgrpmem_mrkr_Type, mtgrpmem_mrkr_type_group)
  values ("KPRTCDNG","TRANSCRIPT");

insert into marker_type_Group_member(mtgrpmem_mrkr_Type, mtgrpmem_mrkr_type_group)
  values ("NPRTCDNG","TRANSCRIPT");

insert into marker_type_Group_member(mtgrpmem_mrkr_Type, mtgrpmem_mrkr_type_group)
  values ("PUTPRTCDNG","TRANSCRIPT");

insert into marker_type_Group_member(mtgrpmem_mrkr_Type, mtgrpmem_mrkr_type_group)
  values ("PRTPRTCDNG","TRANSCRIPT");

insert into marker_type_Group_member(mtgrpmem_mrkr_Type, mtgrpmem_mrkr_type_group)
  values ("PRCSTSCRPT","TRANSCRIPT");

insert into marker_type_Group_member(mtgrpmem_mrkr_Type, mtgrpmem_mrkr_type_group)
  values ("NCODING","TRANSCRIPT");

insert into marker_type_Group_member(mtgrpmem_mrkr_Type, mtgrpmem_mrkr_type_group)
  values ("MIRNA","TRANSCRIPT");

insert into marker_type_Group_member(mtgrpmem_mrkr_Type, mtgrpmem_mrkr_type_group)
  values ("PIRNA","TRANSCRIPT");

insert into marker_type_Group_member(mtgrpmem_mrkr_Type, mtgrpmem_mrkr_type_group)
  values ("RASRNA","TRANSCRIPT");

insert into marker_type_Group_member(mtgrpmem_mrkr_Type, mtgrpmem_mrkr_type_group)
  values ("SCRNA","TRANSCRIPT");

insert into marker_type_Group_member(mtgrpmem_mrkr_Type, mtgrpmem_mrkr_type_group)
  values ("SIRNA","TRANSCRIPT");

insert into marker_type_Group_member(mtgrpmem_mrkr_Type, mtgrpmem_mrkr_type_group)
  values ("SNRNA","TRANSCRIPT");

insert into marker_type_Group_member(mtgrpmem_mrkr_Type, mtgrpmem_mrkr_type_group)
  values ("SNORNA","TRANSCRIPT");

insert into marker_type_Group_member(mtgrpmem_mrkr_Type, mtgrpmem_mrkr_type_group)
  values ("TRNA","TRANSCRIPT");

insert into marker_type_Group_member(mtgrpmem_mrkr_Type, mtgrpmem_mrkr_type_group)
  values ("VAULTRNA","TRANSCRIPT");

insert into marker_type_Group_member(mtgrpmem_mrkr_Type, mtgrpmem_mrkr_type_group)
  values ("AMBORF","TRANSCRIPT");

insert into marker_type_Group_member(mtgrpmem_mrkr_Type, mtgrpmem_mrkr_type_group)
  values ("ANTISENSE","TRANSCRIPT");

insert into marker_type_Group_member(mtgrpmem_mrkr_Type, mtgrpmem_mrkr_type_group)
  values ("RETINTRON","TRANSCRIPT");

insert into marker_type_Group_member(mtgrpmem_mrkr_Type, mtgrpmem_mrkr_type_group)
  values ("PTPRTSRTPT","TRANSCRIPT");

insert into marker_type_Group_member(mtgrpmem_mrkr_Type, mtgrpmem_mrkr_type_group)
  values ("PDPRTSRTPT","TRANSCRIPT");

insert into marker_type_Group_member(mtgrpmem_mrkr_Type, mtgrpmem_mrkr_type_group)
  values ("GENEPT","TRANSCRIPT");

insert into marker_type_Group_member(mtgrpmem_mrkr_Type, mtgrpmem_mrkr_type_group)
  values ("PRGENEP","TRANSCRIPT");

insert into marker_type_Group_member(mtgrpmem_mrkr_Type, mtgrpmem_mrkr_type_group)
  values ("UPRGENEP","TRANSCRIPT");

insert into marker_type_Group_member(mtgrpmem_mrkr_Type, mtgrpmem_mrkr_type_group)
  values ("TSCRBGENEP","TRANSCRIPT");

insert into marker_type_Group_member(mtgrpmem_mrkr_Type, mtgrpmem_mrkr_type_group)
  values ("NMDECAY","TRANSCRIPT");

insert into marker_type_Group_member(mtgrpmem_mrkr_Type, mtgrpmem_mrkr_type_group)
  values ("ARTIFACTT","TRANSCRIPT");


!echo 'zdb_object_type'
insert into zdb_object_type (zobjtype_name, zobjtype_day, zobjtype_seq, zobjtype_home_table, zobjtype_home_zdb_id_column, zobjtype_is_data, zobjtype_is_source, zobjtype_attribution_display_tier)
 values ("TSCRIPT", CURRENT, 1, "marker", "mrkr_zdb_id", "t","f",1);

--insert into zdb_object_type (zobjtype_name, zobjtype_day, zobjtype_seq, zobjtype_home_table, zobjtype_home_zdb_id_column, zobjtype_is_data, zobjtype_is_source, zobjtype_attribution_display_tier)
-- values ("PRCSTSCRPT", CURRENT, 1, "marker", "mrkr_zdb_id", "t","f",1);

insert into marker_relationship_type (mreltype_name, mreltype_mrkr_type_group_1, mreltype_mrkr_type_group_2, mreltype_1_to_2_comments, mreltype_2_to_1_comments)
  values ('Gene produces transcript','GENEDOM','TRANSCRIPT','genes produces transcript','transcript produced by gene');

alter table db_link
  add (dblink_retrieve_display_seq boolean default 'f' not null constraint 
  dblink_retrieve_display_seq_not_null);

alter table db_link
  add (dblink_primary_blastdb_zdb_id varchar(50));

--don't need this for now.

--alter table db_link 
--  add (dblink_is_supporting_sequence boolean default 'f' not null constraint
--  dblink_is_supporting_sequence_not_null);



commit work ;

--rollback work ;

