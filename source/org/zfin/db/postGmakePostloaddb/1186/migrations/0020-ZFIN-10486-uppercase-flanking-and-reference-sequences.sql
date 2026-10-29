--liquibase formatted sql
--changeset rtaylor:ZFIN-10486-uppercase-flanking-and-reference-sequences

-- The NCBI FASTA is soft-masked (lower case = repeat-masked, not different bases), and that
-- casing leaked into sequences read from it. Normalise to upper case and enforce it.
update variant_flanking_sequence
   set vfseq_five_prime_flanking_sequence = upper(vfseq_five_prime_flanking_sequence),
       vfseq_three_prime_flanking_sequence = upper(vfseq_three_prime_flanking_sequence),
       vfseq_sequence = upper(vfseq_sequence),
       vfseq_variation = upper(vfseq_variation)
 where vfseq_five_prime_flanking_sequence <> upper(vfseq_five_prime_flanking_sequence)
    or vfseq_three_prime_flanking_sequence <> upper(vfseq_three_prime_flanking_sequence)
    or vfseq_sequence <> upper(vfseq_sequence)
    or vfseq_variation <> upper(vfseq_variation);

update feature_genomic_mutation_detail
   set fgmd_sequence_of_reference = upper(fgmd_sequence_of_reference),
       fgmd_sequence_of_variation = upper(fgmd_sequence_of_variation)
 where fgmd_sequence_of_reference <> upper(fgmd_sequence_of_reference)
    or fgmd_sequence_of_variation <> upper(fgmd_sequence_of_variation);

alter table variant_flanking_sequence
    add constraint vfseq_flanking_sequence_uppercase_check
        check (vfseq_five_prime_flanking_sequence = upper(vfseq_five_prime_flanking_sequence)
           and vfseq_three_prime_flanking_sequence = upper(vfseq_three_prime_flanking_sequence));

alter table feature_genomic_mutation_detail
    add constraint fgmd_sequence_uppercase_check
        check (fgmd_sequence_of_reference = upper(fgmd_sequence_of_reference)
           and fgmd_sequence_of_variation = upper(fgmd_sequence_of_variation));
