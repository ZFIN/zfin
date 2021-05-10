--liquibase formatted sql
--changeset pm:dload508a

insert into foreign_db (fdb_db_name, fdb_db_query,fdb_db_display_name,fdb_db_significance) values ('CZRC', 'http://www.zfish.cn/TargetDetail.aspx?id=','CZRC',2);

