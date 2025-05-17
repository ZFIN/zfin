
-- make a fasta defline that will also work as a gff3 attribute column

\copy (select '>ID=;Name=' || m.mrkr_abbrev || ';zdb_id='|| m.mrkr_zdb_id || ';~' || s.seq_sequence from marker m inner join marker_sequence s on s.seq_mrkr_zdb_id = m.mrkr_zdb_id where m.mrkr_type in ('MRPHLNO', 'CRISPR') And m.mrkr_zdb_id not in ('ZDB-CRISPR-230804-4','ZDB-CRISPR-250226-1')) to 'mo_seq.fa_line' delimiter ' '
