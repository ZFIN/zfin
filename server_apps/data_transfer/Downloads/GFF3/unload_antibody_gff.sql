! echo "unload_antibody_gff.sql -> antibody.gff3"

UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/antibody.gff3' DELIMITER "	"
select vg.seqname,
           "ZFIN_Antibody" source,
           case gene.mrkr_type when 'GENEP' then 'pseudogene'else 'gene' end  feature,
           min(vg.start) start,
           max(vg.end)   end,
           "." score,
           vg.strand,
           "." frame,
       'gene_id=' || gene.mrkr_zdb_id ||';Name='
       || gene.mrkr_abbrev  ||';Alias='|| gene.mrkr_zdb_id attribute

from db_link, marker_relationship, marker gene, gff3 vg, gff3 vt

where mrel_type = 'gene product recognized by antibody'
  and gene.mrkr_zdb_id   = mrel_mrkr_1_zdb_id --  idx-idx
  and gene.mrkr_zdb_id   = dblink_linked_recid--  idx-idx
  and vt.source = 'vega' and vt.feature = 'transcript'
  and vg.source = 'vega' and vg.feature = 'gene'
  and dblink_acc_num = vg.id
  and vt.parent = vg.id
group by 1,3,7,9;

-- to be valid the gff3 requires a header
! awk '{a[NR]=$0}END{a[0]=h;for(i=0;i<=NR;i++)print a[i]>FILENAME}' h="##gff-version 3" <!--|ROOT_PATH|-->/home/data_transfer/Downloads/antibody.gff3

