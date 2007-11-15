begin work;

create view orthology_view (vorthy_orthologue_zdb_id, vorthy_gene_zdb_id, vorthy_chromosome, vorthy_species,
 vorthy_gene_abbrev, vorthy_position) as
  select distinct zdb_id, c_gene_id, ortho_chromosome, organism, ortho_abbrev, ortho_position
 from orthologue
 union
 select distinct marker_id, marker_id, or_lg, 'Zebrafish', mrkr_abbrev, ''
  from mapped_marker, marker
 where mrkr_type ='GENE'
 and marker_id = mrkr_zdb_id
 and exists (select c_gene_id from orthologue
         where c_gene_id = mrkr_zdb_id)
 union
 select distinct mrkr_zdb_id, mrkr_zdb_id, lnkg_or_lg,
    'Zebrafish', mrkr_abbrev, ''
   from linkage_member, linkage, marker
   where lnkgmem_linkage_zdb_id = lnkg_zdb_id
   and mrkr_type = 'GENE'
   and mrkr_zdb_id = lnkgmem_member_zdb_id
   and exists (select 'x'
         from orthologue
         where c_Gene_id = mrkr_zdb_id)
  union
  select distinct mrkr_zdb_id, mrkr_zdb_id, lnkg_or_lg,
    'Zebrafish', mrkr_abbrev, ''
   from linkage_member, linkage, marker, marker_relationship
   where lnkgmem_linkage_zdb_id = lnkg_zdb_id
   and mrkr_type = 'GENE'
   and mrel_mrkr_1_zdb_id = lnkgmem_member_zdb_id
   and mrel_mrkr_2_zdb_id = mrkr_zdb_id
   and exists (select 'x'
         from orthologue
         where c_Gene_id = mrkr_zdb_id)
  union
  select distinct c_gene_id, c_gene_id, 'unknown', 'Zebrafish', mrkr_abbrev, ''
    from marker, orthologue
    where c_gene_id = mrkr_zdb_id
    and not exists (Select 'x'
            from mapped_marker
            where marker_id = mrkr_zdb_id
            and marker_id = c_gene_id)
   and not exists (Select 'x'
            from linkage_member, marker_relationship
            where lnkgmem_member_zdb_id = mrel_mrkr_1_zdb_id
            and mrel_mrkr_2_zdb_id = mrkr_zdb_id
            and mrel_mrkr_2_zdb_id = c_gene_id)
   and not exists (select 'x'
            from linkage_member
            where lnkgmem_member_zdb_id = c_gene_id) ;

grant select on orthology_view to zfinner;

commit work;
