--liquibase formatted sql
--changeset rtaylor:ZFIN-8237-correspondence-need.sql

-- ----------------------------
-- Table structure for pub_correspondence_need_reason
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."pub_correspondence_need_reason" (
                                                             "pcnr_pk_id" serial8,
                                                             "pcnr_name" text COLLATE "pg_catalog"."default" NOT NULL,
                                                             "pcnr_order" int
);

-- ----------------------------
-- Primary Key structure for table pub_correspondence_need_reason
-- ----------------------------
ALTER TABLE "public"."pub_correspondence_need_reason" ADD CONSTRAINT "pub_correspondence_need_reason_pkey" PRIMARY KEY ("pcnr_pk_id");
ALTER TABLE "public"."pub_correspondence_need_reason" ADD CONSTRAINT "pcnr_no_duplicates_constraint" UNIQUE ("pcnr_name");


-- ----------------------------
-- Table structure for pub_correspondence_need
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."pub_correspondence_need" (
                                                      "pcn_pk_id" serial8,
                                                      "pcn_pcnr_id" int8 NOT NULL,
                                                      "pcn_pub_zdb_id" text COLLATE "pg_catalog"."default" NOT NULL
);

-- ----------------------------
-- Uniques structure for table pub_correspondence_need
-- ----------------------------
ALTER TABLE "public"."pub_correspondence_need" ADD CONSTRAINT "pcn_no_duplicates_constraint" UNIQUE ("pcn_pcnr_id", "pcn_pub_zdb_id");

-- ----------------------------
-- Primary Key structure for table pub_correspondence_need
-- ----------------------------
ALTER TABLE "public"."pub_correspondence_need" ADD CONSTRAINT "pub_correspondence_need_pkey1" PRIMARY KEY ("pcn_pk_id");

-- ----------------------------
-- Foreign Keys structure for table pub_correspondence_need
-- ----------------------------
ALTER TABLE "public"."pub_correspondence_need" ADD CONSTRAINT "pcnr_fkid" FOREIGN KEY ("pcn_pcnr_id") REFERENCES "public"."pub_correspondence_need_reason" ("pcnr_pk_id") ON DELETE RESTRICT ON UPDATE RESTRICT;
ALTER TABLE "public"."pub_correspondence_need" ADD CONSTRAINT "pcnr_pub_zdb_fkid" FOREIGN KEY ("pcn_pub_zdb_id") REFERENCES "public"."publication" ("zdb_id") ON DELETE NO ACTION ON UPDATE NO ACTION;



-- ----------------------------
-- Table structure for pub_correspondence_need_resolution_type
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."pub_correspondence_need_resolution_type" (
                                                                      "pcnrt_pk_id" serial8,
                                                                      "pcnrt_name" text COLLATE "pg_catalog"."default" NOT NULL,
                                                                      "pcnrt_order" int
);

-- ----------------------------
-- Primary Key structure for table pub_correspondence_need_resolution_type
-- ----------------------------
ALTER TABLE "public"."pub_correspondence_need_resolution_type" ADD CONSTRAINT "pub_correspondence_need_resolution_type_pkey1" PRIMARY KEY ("pcnrt_pk_id");
ALTER TABLE "public"."pub_correspondence_need_resolution_type" ADD CONSTRAINT "pcnrt_no_duplicates_constraint" UNIQUE ("pcnrt_name");

-- ----------------------------
-- Table structure for pub_correspondence_need_resolution
-- ----------------------------
CREATE TABLE IF NOT EXISTS "public"."pub_correspondence_need_resolution" (
                                                                 "pcnres_pk_id" serial8,
                                                                 "pcnres_pcnrt_id" int8 NOT NULL,
                                                                 "pcnres_pub_zdb_id" text COLLATE "pg_catalog"."default" NOT NULL
);

-- ----------------------------
-- Uniques structure for table pub_correspondence_need_resolution
-- ----------------------------
ALTER TABLE "public"."pub_correspondence_need_resolution" ADD CONSTRAINT "pub_correspondence_need_resolution_uniques" UNIQUE ("pcnres_pcnrt_id", "pcnres_pub_zdb_id");

-- ----------------------------
-- Primary Key structure for table pub_correspondence_need_resolution
-- ----------------------------
ALTER TABLE "public"."pub_correspondence_need_resolution" ADD CONSTRAINT "pub_correspondence_need_resolution_pkey" PRIMARY KEY ("pcnres_pk_id");

-- ----------------------------
-- Foreign Keys structure for table pub_correspondence_need_resolution
-- ----------------------------
ALTER TABLE "public"."pub_correspondence_need_resolution" ADD CONSTRAINT "pcnres_pcnr_id_fkey" FOREIGN KEY ("pcnres_pcnrt_id") REFERENCES "public"."pub_correspondence_need_resolution_type" ("pcnrt_pk_id") ON DELETE RESTRICT ON UPDATE RESTRICT;
ALTER TABLE "public"."pub_correspondence_need_resolution" ADD CONSTRAINT "pcnres_pub_zdb_id_fkey" FOREIGN KEY ("pcnres_pub_zdb_id") REFERENCES "public"."publication" ("zdb_id") ON DELETE NO ACTION ON UPDATE NO ACTION;


-- ----------------------------
-- INSERTS
-- ----------------------------
INSERT INTO pub_correspondence_need_reason (pcnr_name, pcnr_order) VALUES ('Missing Allele information', 1),
                                                                ('Gene can''t be ID', 2),
                                                                ('Incorrect primers -Gene', 3),
                                                                ('Set up line designation (Lab-line designation link)', 4),
                                                                ('STR sequence missing', 5),
                                                                ('STR Sequence question', 6),
                                                                ('Missing Antibody information', 7),
                                                                ('Other', 8);

INSERT INTO pub_correspondence_need_resolution_type (pcnrt_name, pcnrt_order) VALUES ('No Author response', 1),
                                                                    ('Author responded -needed more emails', 2),
                                                                    ('Author responded with needed info', 3);
