--liquibase formatted sql
--changeset sierra:RGNS-82

insert into marker_relationship_type (mreltype_name, 
                                      mreltype_mrkr_type_group_1, 
                                      mreltype_mrkr_type_group_2, 
                                      mreltype_1_to_2_comments, 
                                      mreltype_2_to_1_comments)
 values ('BAC contains GENEDOM',
                    'BAC',
                    'GENEDOM',
                    'Contains',
                    'Contained in');

insert into marker_relationship_type (mreltype_name, 
                                      mreltype_mrkr_type_group_1, 
                                      mreltype_mrkr_type_group_2, 
                                      mreltype_1_to_2_comments, 
                                      mreltype_2_to_1_comments)
 values ('BAC contains NTR',
                    'BAC',
                    'NONTSCRBD_REGION',
                    'Contains',
                    'Contained in');

insert into marker_relationship_type (mreltype_name, 
                                      mreltype_mrkr_type_group_1, 
                                      mreltype_mrkr_type_group_2, 
                                      mreltype_1_to_2_comments, 
                                      mreltype_2_to_1_comments)
 values ('GENEDOM contains NTR',
                    'GENEDOM',
                    'NONTSCRBD_REGION',
                    'Contains',
                    'Contained in');
