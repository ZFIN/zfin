begin work;

alter table phenotype_source_generated drop pg_pre_eap_phenotype;
alter table phenotype_source_generated_bkup drop pg_pre_eap_phenotype;
alter table phenotype_source_generated_temp drop pg_pre_eap_phenotype;

alter table phenotype_observation_generated add psg_pre_eap_phenotype boolean default 'f';
alter table phenotype_observation_generated_bkup add psg_pre_eap_phenotype boolean default 'f';
alter table phenotype_observation_generated_temp add psg_pre_eap_phenotype boolean default 'f';

-- rollback work;
commit work;