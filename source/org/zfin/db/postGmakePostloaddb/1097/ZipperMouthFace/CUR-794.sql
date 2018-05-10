--liquibase formatted sql
--changeset pm:CUR-794

delete from zdb_active_data where zactvd_zdb_id='ZDB-ALT-171004-2';
delete from feature_tracking where where ft_feature_zdb_id='ZDB-ALT-171004-2';





