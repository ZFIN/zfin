--liquibase formatted sql
--changeset rtaylor:ZFIN-8868.sql

INSERT INTO public.gene_description VALUES (36611, 'ZDB-NCRNAG-030131-4', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'Is expressed in margin; myotome; notochord; somite; and yolk syncytial layer.');
INSERT INTO public.gene_description VALUES (36717, 'ZDB-NCRNAG-031010-1', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'Is expressed in EVL; epidermis; periderm; and pharynx.');
INSERT INTO public.gene_description VALUES (36621, 'ZDB-NCRNAG-041001-1', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'Is expressed in basal plate midbrain region; central nervous system; myotome; neurons; and somite.');

INSERT INTO public.snp_download VALUES (1466643, 'rs179717047', 'ZDB-NCRNAG-090313-1');
INSERT INTO public.snp_download VALUES (1466644, 'rs179717048', 'ZDB-NCRNAG-090313-1');
INSERT INTO public.snp_download VALUES (1466645, 'rs179717049', 'ZDB-NCRNAG-090313-1');
INSERT INTO public.snp_download VALUES (1466646, 'rs179717050', 'ZDB-NCRNAG-090313-1');
INSERT INTO public.snp_download VALUES (1466647, 'rs179717051', 'ZDB-NCRNAG-090313-1');
INSERT INTO public.snp_download VALUES (1466648, 'rs179717052', 'ZDB-NCRNAG-090313-1');
INSERT INTO public.snp_download VALUES (1466649, 'rs179717053', 'ZDB-NCRNAG-090313-1');
INSERT INTO public.snp_download VALUES (1466650, 'rs179717054', 'ZDB-NCRNAG-090313-1');
INSERT INTO public.snp_download VALUES (1466651, 'rs179717055', 'ZDB-NCRNAG-090313-1');
INSERT INTO public.snp_download VALUES (1466652, 'rs179717056', 'ZDB-NCRNAG-090313-1');
INSERT INTO public.snp_download VALUES (1466653, 'rs179717057', 'ZDB-NCRNAG-090313-1');

-- not used anymore
drop table ensdarg_post_proc;
drop table inputensdarg;


