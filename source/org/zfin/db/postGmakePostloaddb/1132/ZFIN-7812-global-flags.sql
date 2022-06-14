--liquibase formatted sql
--changeset rtaylor:ZFIN-7812-global-flags

-- ----------------------------
-- Table structure for zdb_feature_flag
-- ----------------------------
DROP TABLE IF EXISTS "public"."zdb_feature_flag";
CREATE TABLE "public"."zdb_feature_flag" (
                                             "zfeatflag_name" text COLLATE "pg_catalog"."default" NOT NULL,
                                             "zfeatflag_enabled" bool NOT NULL,
                                             "zfeatflag_last_modified" TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP

)
;
ALTER TABLE "public"."zdb_flag" OWNER TO "informix";

-- ----------------------------
-- Primary Key structure for table zdb_flag
-- ----------------------------
ALTER TABLE "public"."zdb_feature_flag" ADD CONSTRAINT "zdb_feature_flag_primary_key" PRIMARY KEY ("zfeatflag_name");

INSERT INTO "public"."zdb_feature_flag" ("zfeatflag_name", "zfeatflag_enabled", "zfeatflag_last_modified") VALUES ('jBrowse', 'f', '2022-06-10 16:49:14.522388');
INSERT INTO "public"."zdb_feature_flag" ("zfeatflag_name", "zfeatflag_enabled", "zfeatflag_last_modified") VALUES ('Placeholder For Future Feature', 'f', '2022-06-10 16:49:32.411754');

