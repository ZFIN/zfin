--liquibase formatted sql
--changeset pkalita:PUB-410

ALTER TABLE pub_tracking_history DROP CONSTRAINT pth_pub_fk;

ALTER TABLE pub_tracking_history ADD CONSTRAINT (
  FOREIGN KEY (pth_pub_zdb_id) REFERENCES publication
  ON DELETE CASCADE CONSTRAINT pth_pub_fk_odc
);
