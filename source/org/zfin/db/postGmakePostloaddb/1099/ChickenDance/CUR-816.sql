--liquibase formatted sql
--changeset xshao:CUR-816

delete from feature_tracking where ft_feature_abbrev = 'cl1602' or ft_feature_abbrev = 'cl1603';

