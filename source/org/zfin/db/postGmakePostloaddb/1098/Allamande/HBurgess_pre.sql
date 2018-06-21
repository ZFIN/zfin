--liquibase formatted sql
--changeset pm:HBurgess_pre

create table feature_data (
 featureAbb text not null,
        lineNum text not null,
        otherFeatureId text,
        constructId text not null,
        pubID text
        ) ;

