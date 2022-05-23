--liquibase formatted sql
--changeset rtaylor:ZFIN-7922

-- Fix tbx4^mq6/+
INSERT INTO zdb_active_data VALUES('ZDB-GENOFEAT-220505-9');
INSERT INTO "public"."genotype_feature" ("genofeat_zdb_id", "genofeat_geno_zdb_id", "genofeat_feature_zdb_id", "genofeat_dad_zygocity", "genofeat_mom_zygocity", "genofeat_zygocity") VALUES ('ZDB-GENOFEAT-220505-9',  'ZDB-GENO-160609-8', 'ZDB-ALT-121205-2', 'ZDB-ZYG-070117-7', 'ZDB-ZYG-070117-7', 'ZDB-ZYG-070117-2');

-- Fix ta^w181/+ (AB)
INSERT INTO zdb_active_data VALUES('ZDB-GENOFEAT-220505-10');
INSERT INTO "public"."genotype_feature" ("genofeat_zdb_id", "genofeat_geno_zdb_id", "genofeat_feature_zdb_id", "genofeat_dad_zygocity", "genofeat_mom_zygocity", "genofeat_zygocity") VALUES ('ZDB-GENOFEAT-220505-10', 'ZDB-GENO-160613-1', 'ZDB-ALT-160613-1', 'ZDB-ZYG-070117-7', 'ZDB-ZYG-070117-7', 'ZDB-ZYG-070117-2');

-- Fix gz35Tg/+ ; gz34Tg/+ ; gz23Tg/+
INSERT INTO zdb_active_data VALUES('ZDB-GENOFEAT-220505-11');
INSERT INTO zdb_active_data VALUES('ZDB-GENOFEAT-220505-12');
INSERT INTO "public"."genotype_feature" ("genofeat_zdb_id", "genofeat_geno_zdb_id", "genofeat_feature_zdb_id", "genofeat_dad_zygocity", "genofeat_mom_zygocity", "genofeat_zygocity") VALUES ('ZDB-GENOFEAT-220505-11', 'ZDB-GENO-160601-1', 'ZDB-ALT-160413-1', 'ZDB-ZYG-070117-7', 'ZDB-ZYG-070117-7', 'ZDB-ZYG-070117-2');
INSERT INTO "public"."genotype_feature" ("genofeat_zdb_id", "genofeat_geno_zdb_id", "genofeat_feature_zdb_id", "genofeat_dad_zygocity", "genofeat_mom_zygocity", "genofeat_zygocity") VALUES ('ZDB-GENOFEAT-220505-12', 'ZDB-GENO-160601-1', 'ZDB-ALT-160413-2', 'ZDB-ZYG-070117-7', 'ZDB-ZYG-070117-7', 'ZDB-ZYG-070117-2');

-- Fix ordering issues:
UPDATE "public"."genotype" SET geno_display_name = 'slc24a5<sup>b1/+</sup>; gin2<sup>zf57/zf57</sup>; slc45a2<sup>zf67/+</sup>' WHERE "geno_zdb_id" = 'ZDB-GENO-071219-10' and "geno_display_name" = 'gin2<sup>zf57/zf57</sup>; slc45a2<sup>zf67/+</sup>';
UPDATE "public"."genotype" SET geno_display_name = 'slc24a5<sup>b1/+</sup>; gin5<sup>zf60/zf60</sup>; pig5<sup>zf68/+</sup>' WHERE "geno_zdb_id" = 'ZDB-GENO-080108-1' and "geno_display_name" = 'gin5<sup>zf60/zf60</sup>; pig5<sup>zf68/+</sup>';
UPDATE "public"."genotype" SET geno_display_name = 'mir126a<sup>bns2/bns2</sup>; mir126b<sup>bns3/bns3</sup>; egfl7<sup>s981/s981</sup>' WHERE "geno_zdb_id" = 'ZDB-GENO-180824-7' and "geno_display_name" = 'egfl7<sup>s981/s981</sup>';