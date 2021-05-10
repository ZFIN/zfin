copy (select gff_seqname,
        'ZFIN' gff_source,
        gff_feature,
        gff_start,
        gff_end,
        gff_score,
        gff_strand,
        gff_frame,
        'ID=' || feature_zdb_id
        ||';Name=' || feature_abbrev
        ||';Alias='|| feature_zdb_id || ','
        || feature_abbrev || ','
        || feature_name   || ';' as attribute
      from  gff3 join feature on substring(feature_abbrev from 1 for 8) = gff_id
      where gff_source = 'BurgessLin'
            and gff_feature = 'Transgenic_insertion'
      order by 1,4,5,9 ) to '/opt/zfin/www_homes/punkt/home/data_transfer/Downloads/zfin_tginsertion.gff3' DELIMITER '	'

