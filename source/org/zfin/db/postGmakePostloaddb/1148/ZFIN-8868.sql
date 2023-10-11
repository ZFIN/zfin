--liquibase formatted sql
--changeset rtaylor:ZFIN-8868.sql

INSERT INTO public.gene_description VALUES (36611, 'ZDB-NCRNAG-030131-4', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'Is expressed in margin; myotome; notochord; somite; and yolk syncytial layer.');
INSERT INTO public.gene_description VALUES (36717, 'ZDB-NCRNAG-031010-1', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'Is expressed in EVL; epidermis; periderm; and pharynx.');
INSERT INTO public.gene_description VALUES (36621, 'ZDB-NCRNAG-041001-1', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'Is expressed in basal plate midbrain region; central nervous system; myotome; neurons; and somite.');

-- not used anymore
drop table ensdarg_post_proc;
drop table inputensdarg;


