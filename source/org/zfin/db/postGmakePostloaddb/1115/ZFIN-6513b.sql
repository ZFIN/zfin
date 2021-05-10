--liquibase formatted sql
--changeset pm:ZFIN-6513b




select get_id('DALIAS') as dalias_id, get_id('NOMEN') as nomen_id  into temp tmp_ids;

insert into zdb_active_data select dalias_id from tmp_ids;

insert into data_alias (dalias_zdb_id, dalias_data_zdb_id, dalias_alias, dalias_group_id)
                            select dalias_id, 'ZDB-BR-071004-117', 'zgc:174162', '1'
                              from tmp_ids;

insert into zdb_active_data select nomen_id from tmp_ids;

insert into marker_history (mhist_zdb_id, mhist_mrkr_zdb_id, mhist_event, mhist_reason, mhist_date,
                                                          mhist_mrkr_name_on_mhist_date, mhist_mrkr_abbrev_on_mhist_date, mhist_comments,mhist_dalias_zdb_id)
                              select nomen_id, 'ZDB-BR-071004-117', 'merged', 'same marker', NOW(),
                                    'line element zfl2-1', 'br.line.zfl2-1', 'none', dalias_id
                                from tmp_ids;


