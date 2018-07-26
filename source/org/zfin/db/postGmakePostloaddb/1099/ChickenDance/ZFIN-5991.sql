--liquibase formatted sql
--changeset xshao:ZFIN-5991

delete from feature_tracking where ft_feature_abbrev in ('scf300', 'scf301', 'scf302', 'scf303', 'scf304');


