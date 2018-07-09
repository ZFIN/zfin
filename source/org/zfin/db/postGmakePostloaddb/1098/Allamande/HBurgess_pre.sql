--liquibase formatted sql
--changeset pm:HBurgess_pre

create table feature_data (
 feature_abb text not null,
        line_num text not null,
        other_feature_id text,
        construct_id text not null,
        pub_id text
        ) ;

