--liquibase formatted sql
--changeset xshao:CUR-825

delete from feature_tracking where ft_feature_abbrev = 'nl18Tg';
