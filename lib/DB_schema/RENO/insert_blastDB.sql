-- insert blast DB

begin work;

insert into blast_database (
    blastdb_zdb_id,
    blastdb_name,
    blastdb_abbrev,
    blastdb_wget_path,
    blastdb_description,
    blastdb_public
)
values (
    get_id ('BLASTDB'),
    'Ensembl Zebrafish Transcripts',
    'ensembl_zf',
    '/research/zblastdb/db/Current/',
    'Ensembl Zebrafish Transcripts',
    't'
);


insert into blast_database (
    blastdb_zdb_id,
    blastdb_name,
    blastdb_abbrev,
    blastdb_wget_path,
    blastdb_description,
    blastdb_public
)
values (
    get_id ('BLASTDB'),
    'EST Human',
    'gbk_est_hs',
    '/research/zblastdb/db/Current/',
    'Subset of GenBank that contains Human sequences from EST division.',
    'f'
);


insert into blast_database (
    blastdb_zdb_id,
    blastdb_name,
    blastdb_abbrev,
    blastdb_wget_path,
    blastdb_description,
    blastdb_public
)
values (
    get_id ('BLASTDB'),
    'EST Mouse',
    'gbk_est_ms',
    '/research/zblastdb/db/Current/',
    'Subset of GenBank that contains Mouse sequences from EST division.',
    'f'
);


insert into blast_database (
    blastdb_zdb_id,
    blastdb_name,
    blastdb_abbrev,
    blastdb_wget_path,
    blastdb_description,
    blastdb_public
)
values (
    get_id ('BLASTDB'),
    'EST Zebrafish',
    'gbk_est_zf',
    '/research/zblastdb/db/Current/',
    'Subset of GenBank that contains zebrafish sequences from EST division. Zebrafish sequences are parsed from the EST divisions by examining the organism lines for entries "Danio rerio"',
    't'
);


insert into blast_database (
    blastdb_zdb_id,
    blastdb_name,
    blastdb_abbrev,
    blastdb_wget_path,
    blastdb_description,
    blastdb_public
)
values (
    get_id ('BLASTDB'),
    'GenBank Human',
    'gbk_gb_hs',
    '/research/zblastdb/db/Current/',
    'Subset of GenBank that contains human sequences from these divisions: vetebrate and high-throughput cDNAs (HTC). ESTs, genome survey sequences (GSS), and high-throughput genomic sequences (HTGs) are not included.',
    'f'
);


insert into blast_database (
    blastdb_zdb_id,
    blastdb_name,
    blastdb_abbrev,
    blastdb_wget_path,
    blastdb_description,
    blastdb_public
)
values (
    get_id ('BLASTDB'),
    'GenBank Mouse',
    'gbk_gb_ms',
    '/research/zblastdb/db/Current/',
    'Subset of GenBank that contains mouse sequences from these divisions: vetebrate and high-throughput cDNAs (HTC). ESTs, genome survey sequences (GSS), and high-throughput genomic sequences (HTGs) are not included.',
    'f'
);


insert into blast_database (
    blastdb_zdb_id,
    blastdb_name,
    blastdb_abbrev,
    blastdb_wget_path,
    blastdb_description,
    blastdb_public
)
values (
    get_id ('BLASTDB'),
    'GenBank Zebrafish',
    'gbk_gb_zf',
    '/research/zblastdb/db/Current/',
    'Subset of GenBank that contains zebrafish sequences from these divisions: vetebrate and high-throughput cDNAs (HTC). ESTs, genome survey sequences (GSS), and high-throughput genomic sequences (HTGs) are not included.',
    't'
);


insert into blast_database (
    blastdb_zdb_id,
    blastdb_name,
    blastdb_abbrev,
    blastdb_wget_path,
    blastdb_description,
    blastdb_public
)
values (
    get_id ('BLASTDB'),
    'GSS Zebrafish',
    'gbk_gss_zf',
    '/research/zblastdb/db/Current/',
    'Subset of GenBank that contains zebrafish Genome Survey Sequences (GSS). Zebrafish sequences are parsed from the GSS division by examining the organism line for entries "Danio rerio"',
    't'
);


insert into blast_database (
    blastdb_zdb_id,
    blastdb_name,
    blastdb_abbrev,
    blastdb_wget_path,
    blastdb_description,
    blastdb_public
)
values (
    get_id ('BLASTDB'),
    'Human DNA',
    'gbk_hs_dna',
    '/research/zblastdb/db/Current/',
    'Subset of GenBank that contains human sequences from all divisions (including EST) that are of type DNA.',
    'f'
);


insert into blast_database (
    blastdb_zdb_id,
    blastdb_name,
    blastdb_abbrev,
    blastdb_wget_path,
    blastdb_description,
    blastdb_public
)
values (
    get_id ('BLASTDB'),
    'Human mRNA',
    'gbk_hs_mrna',
    '/research/zblastdb/db/Current/',
    'Subset of GenBank that contains human sequences from all divisions (including EST) that are of type mRNA. Human mRNA sequences are parsed by examining the locus line for entries containing "mRNA".',
    'f'
);


insert into blast_database (
    blastdb_zdb_id,
    blastdb_name,
    blastdb_abbrev,
    blastdb_wget_path,
    blastdb_description,
    blastdb_public
)
values (
    get_id ('BLASTDB'),
    'HTG Zebrafish',
    'gbk_htg_zf',
    '/research/zblastdb/db/Current/',
    'Subset of GenBank that contains zebrafish High-throughput Genomic sequences (HTG). Zebrafish sequences are parsed from the HTG division by examining the organism line for entries "Danio rerio".',
    't'
);


insert into blast_database (
    blastdb_zdb_id,
    blastdb_name,
    blastdb_abbrev,
    blastdb_wget_path,
    blastdb_description,
    blastdb_public
)
values (
    get_id ('BLASTDB'),
    'Mouse DNA',
    'gbk_ms_dna',
    '/research/zblastdb/db/Current/',
    'Subset of GenBank that contains mouse sequences from all divisions (including EST) that are of type DNA. Zebrafish DNA sequences are parsed by examining the locus line for entries containing "DNA"',
    'f'
);


insert into blast_database (
    blastdb_zdb_id,
    blastdb_name,
    blastdb_abbrev,
    blastdb_wget_path,
    blastdb_description,
    blastdb_public
)
values (
    get_id ('BLASTDB'),
    'Mouse mRNA',
    'gbk_ms_mrna',
    '/research/zblastdb/db/Current/',
    'Subset of GenBank that contains mouse sequences from all divisions (including EST) that are of type mRNA. Zebrafish mRNA sequences are parsed by examining the locus line for entries containing "mRNA".,
    'f'
);


insert into blast_database (
    blastdb_zdb_id,
    blastdb_name,
    blastdb_abbrev,
    blastdb_wget_path,
    blastdb_description,
    blastdb_public
)
values (
    get_id ('BLASTDB'),
    'All Zebrafish',
    'gbk_zf_all',
    '/research/zblastdb/db/Current/',
    'All zebrafish sequences in GenBank?',
    't'
);


insert into blast_database (
    blastdb_zdb_id,
    blastdb_name,
    blastdb_abbrev,
    blastdb_wget_path,
    blastdb_description,
    blastdb_public
)
values (
    get_id ('BLASTDB'),
    'Zebrafish DNA',
    'gbk_zf_dna',
    '/research/zblastdb/db/Current/',
    'Subset of GenBank that contains zebrafish sequences from all divisions (including EST) that are of type DNA. Zebrafish DNA sequences are parsed by examining the organism lines for entries "Danio rerio" and locus line for entries containing "DNA".',
    't');


insert into blast_database (
    blastdb_zdb_id,
    blastdb_name,
    blastdb_abbrev,
    blastdb_wget_path,
    blastdb_description,
    blastdb_public
)
values (
    get_id ('BLASTDB'),
    'Zebrafish mRNA',
    'gbk_zf_mrna',
    '/research/zblastdb/db/Current/',
    'Subset of GenBank that contains zebrafish sequences from all divisions (including EST) that are of type mRNA. Zebrafish mRNA sequences are parsed by examining the organism lines for entries "Danio rerio" and locus line for entries containing "mRNA".',
    't'
);


insert into blast_database (
    blastdb_zdb_id,
    blastdb_name,
    blastdb_abbrev,
    blastdb_wget_path,
    blastdb_description,
    blastdb_public
)
values (
    get_id ('BLASTDB'),
    'RefSeq Zebrafish Protein',
    'refseq_zf_aa',
    '/research/zblastdb/db/Current/',
    'NCBI RefSeq zebrafish protein sequences. (Zebrafish sequences of type: NP_, XP_).',
    't'
);


insert into blast_database (
    blastdb_zdb_id,
    blastdb_name,
    blastdb_abbrev,
    blastdb_wget_path,
    blastdb_description,
    blastdb_public
)
values (
    get_id ('BLASTDB'),
    'RefSeq Zebrafish mRNA',
    'refseq_zf_rna',
    '/research/zblastdb/db/Current/',
    'NCBI RefSeq Zebrafish transcripts (Zebrafish RefSeqs of type: NM_, NR, XM_).',
    't'
);


insert into blast_database (
    blastdb_zdb_id,
    blastdb_name,
    blastdb_abbrev,
    blastdb_wget_path,
    blastdb_description,
    blastdb_public
)
values (
    get_id ('BLASTDB'),
    'UniProt Human',
    'sptr_hs',
    '/research/zblastdb/db/Current/',
    'Human protein sequences from the "non-redundant" set of sequences from UniProt.',
    'f'
);


insert into blast_database (
    blastdb_zdb_id,
    blastdb_name,
    blastdb_abbrev,
    blastdb_wget_path,
    blastdb_description,
    blastdb_public
)
values (
    get_id ('BLASTDB'),
    'UniProt Mouse',
    'sptr_ms',
    '/research/zblastdb/db/Current/',
    'Mouse protein sequences from the "non-redundant" set of sequences from UniProt.',
    'f'
);


insert into blast_database (
    blastdb_zdb_id,
    blastdb_name,
    blastdb_abbrev,
    blastdb_wget_path,
    blastdb_description,
    blastdb_public
)
values (
    get_id ('BLASTDB'),
    'UniProt Zebrafish',
    'sptr_zf',
    '/research/zblastdb/db/Current/',
    'Zebrafish protein sequences from the "non-redundant" set of sequences from UniProt. Zebrafish sequences are defined as those entries "Danio rerio" in the species lines.',
    't'
);


insert into blast_database (
    blastdb_zdb_id,
    blastdb_name,
    blastdb_abbrev,
    blastdb_wget_path,
    blastdb_description,
    blastdb_public
)
values (
    get_id ('BLASTDB'),
    'TIGR Zebrafish Clusters',
    'tigr_zf',
    '/research/zblastdb/db/Current/',
    "Tentative consensus transcript sequences from TIGR's Zebrafish Gene Index (ZGI).",
    't'
);


insert into blast_database (
    blastdb_zdb_id,
    blastdb_name,
    blastdb_abbrev,
    blastdb_wget_path,
    blastdb_description,
    blastdb_public
)
values (
    get_id ('BLASTDB'),
    'ZFIN Vega Transcripts',
    'vega_zfin',
    '/research/zblastdb/db/Current/',
    'Vega zebrafish transcripts that are associated with ZFIN zebrafish genes.',
    't'
);


insert into blast_database (
    blastdb_zdb_id,
    blastdb_name,
    blastdb_abbrev,
    blastdb_wget_path,
    blastdb_description,
    blastdb_public
)
values (
    get_id ('BLASTDB'),
    'Zebrafish Trace Archive',
    'wgs_zf',
    '/research/zblastdb/db/Current/',
    'Zebrafish sequences from NCBI Trace Archive database.',
    't'
);


insert into blast_database (
    blastdb_zdb_id,
    blastdb_name,
    blastdb_abbrev,
    blastdb_wget_path,
    blastdb_description,
    blastdb_public
)
values (
    get_id ('BLASTDB'),
    'ZFIN cDNA Sequences',
    'zfin_cdna',
    '/research/zblastdb/db/Current/',
    'Combination of GenBank zebrafish cDNA sequences from all divisions (including EST) that are associated with ZFIN zebrafish genes or markers or clones, and Vega zebrafish transcripts that are associated with ZFIN zebrafish genes.',
    't'
);


insert into blast_database (
    blastdb_zdb_id,
    blastdb_name,
    blastdb_abbrev,
    blastdb_wget_path,
    blastdb_description,
    blastdb_public
)
values (
    get_id ('BLASTDB'),
    'ZFIN MicroRNA Sequences',
    'zfin_microRNA',
    '/research/zblastdb/db/Current/',
    'MicroRNA sequences in ZFIN.',
    't'
);


insert into blast_database (
    blastdb_zdb_id,
    blastdb_name,
    blastdb_abbrev,
    blastdb_wget_path,
    blastdb_description,
    blastdb_public
)
values (
    get_id ('BLASTDB'),
    'ZFIN Morpholino Sequences',
    'zfin_mrph',
    '/research/zblastdb/db/Current/',
    'Morpholino sequences in ZFIN.',
    't'
);


insert into blast_database (
    blastdb_zdb_id,
    blastdb_name,
    blastdb_abbrev,
    blastdb_wget_path,
    blastdb_description,
    blastdb_public
)
values (
    get_id ('BLASTDB'),
    'name',
    'zfin_seq',
    '/research/zblastdb/db/Current/',
    'All non protein zebrafish sequences in ZFIN',
    't'
);


--
rollback work;

--commit work;