--liquibase formatted sql
--changeset pkalita:PUB-657

-- list of IDs comes from:
--     select fig_zdb_id, img_zdb_id, fig_source_zdb_id, fig_label, img_image
--     from image inner join figure on img_fig_zdb_id = fig_zdb_id
--     where img_image in (select img_image from image group by img_image having count(*) > 1);
-- and keeping only one of the figures with a given img file

delete from figure where fig_zdb_id in (
    'ZDB-FIG-190723-1222',
    'ZDB-FIG-190725-37',
    'ZDB-FIG-190726-25',
    'ZDB-FIG-190725-24',
    'ZDB-FIG-190726-12',
    'ZDB-FIG-190725-61',
    'ZDB-FIG-190726-44',
    'ZDB-FIG-190725-74',
    'ZDB-FIG-190726-57',
    'ZDB-FIG-190725-75',
    'ZDB-FIG-190726-58',
    'ZDB-FIG-190725-62',
    'ZDB-FIG-190726-45',
    'ZDB-FIG-190725-63',
    'ZDB-FIG-190726-46',
    'ZDB-FIG-190725-51',
    'ZDB-FIG-190726-34',
    'ZDB-FIG-190725-27',
    'ZDB-FIG-190726-15',
    'ZDB-FIG-190725-28',
    'ZDB-FIG-190726-16',
    'ZDB-FIG-190725-29',
    'ZDB-FIG-190726-17',
    'ZDB-FIG-190725-30',
    'ZDB-FIG-190726-18',
    'ZDB-FIG-190725-31',
    'ZDB-FIG-190726-19',
    'ZDB-FIG-190725-32',
    'ZDB-FIG-190726-20',
    'ZDB-FIG-190725-33',
    'ZDB-FIG-190726-21',
    'ZDB-FIG-190725-64',
    'ZDB-FIG-190726-47',
    'ZDB-FIG-190725-65',
    'ZDB-FIG-190726-48',
    'ZDB-FIG-190725-66',
    'ZDB-FIG-190726-49',
    'ZDB-FIG-190725-67',
    'ZDB-FIG-190726-50',
    'ZDB-FIG-190725-68',
    'ZDB-FIG-190726-51',
    'ZDB-FIG-190725-48',
    'ZDB-FIG-190726-31',
    'ZDB-FIG-190725-49',
    'ZDB-FIG-190726-32',
    'ZDB-FIG-190725-50',
    'ZDB-FIG-190726-33',
    'ZDB-FIG-190725-38',
    'ZDB-FIG-190726-27',
    'ZDB-FIG-190725-39',
    'ZDB-FIG-190726-28',
    'ZDB-FIG-190725-15',
    'ZDB-FIG-190726-3',
    'ZDB-FIG-190725-46',
    'ZDB-FIG-190726-29',
    'ZDB-FIG-190725-56',
    'ZDB-FIG-190726-39',
    'ZDB-FIG-190725-57',
    'ZDB-FIG-190726-40',
    'ZDB-FIG-190725-58',
    'ZDB-FIG-190726-41',
    'ZDB-FIG-190725-59',
    'ZDB-FIG-190726-42',
    'ZDB-FIG-190725-60',
    'ZDB-FIG-190726-43',
    'ZDB-FIG-190724-1',
    'ZDB-FIG-190725-13',
    'ZDB-FIG-190726-1',
    'ZDB-FIG-190724-2',
    'ZDB-FIG-190725-14',
    'ZDB-FIG-190726-2',
    'ZDB-FIG-190725-52',
    'ZDB-FIG-190726-35',
    'ZDB-FIG-190725-53',
    'ZDB-FIG-190726-36',
    'ZDB-FIG-190725-26',
    'ZDB-FIG-190726-14',
    'ZDB-FIG-190725-69',
    'ZDB-FIG-190726-52',
    'ZDB-FIG-190725-70',
    'ZDB-FIG-190726-53',
    'ZDB-FIG-190725-71',
    'ZDB-FIG-190726-54',
    'ZDB-FIG-190725-72',
    'ZDB-FIG-190726-55',
    'ZDB-FIG-190725-73',
    'ZDB-FIG-190726-56',
    'ZDB-FIG-190725-35',
    'ZDB-FIG-190726-23',
    'ZDB-FIG-190725-36',
    'ZDB-FIG-190726-24',
    'ZDB-FIG-190725-47',
    'ZDB-FIG-190726-30',
    'ZDB-FIG-190725-16',
    'ZDB-FIG-190726-4',
    'ZDB-FIG-190725-17',
    'ZDB-FIG-190726-5',
    'ZDB-FIG-190725-18',
    'ZDB-FIG-190726-6',
    'ZDB-FIG-190725-77',
    'ZDB-FIG-190726-59',
    'ZDB-FIG-190725-78',
    'ZDB-FIG-190726-60',
    'ZDB-FIG-190725-79',
    'ZDB-FIG-190726-61',
    'ZDB-FIG-190725-34',
    'ZDB-FIG-190726-22',
    'ZDB-FIG-190725-76',
    'ZDB-FIG-190726-26'
);

delete from zdb_active_data where zactvd_zdb_id in (
    'ZDB-FIG-190723-1222',
    'ZDB-FIG-190725-37',
    'ZDB-FIG-190726-25',
    'ZDB-FIG-190725-24',
    'ZDB-FIG-190726-12',
    'ZDB-FIG-190725-61',
    'ZDB-FIG-190726-44',
    'ZDB-FIG-190725-74',
    'ZDB-FIG-190726-57',
    'ZDB-FIG-190725-75',
    'ZDB-FIG-190726-58',
    'ZDB-FIG-190725-62',
    'ZDB-FIG-190726-45',
    'ZDB-FIG-190725-63',
    'ZDB-FIG-190726-46',
    'ZDB-FIG-190725-51',
    'ZDB-FIG-190726-34',
    'ZDB-FIG-190725-27',
    'ZDB-FIG-190726-15',
    'ZDB-FIG-190725-28',
    'ZDB-FIG-190726-16',
    'ZDB-FIG-190725-29',
    'ZDB-FIG-190726-17',
    'ZDB-FIG-190725-30',
    'ZDB-FIG-190726-18',
    'ZDB-FIG-190725-31',
    'ZDB-FIG-190726-19',
    'ZDB-FIG-190725-32',
    'ZDB-FIG-190726-20',
    'ZDB-FIG-190725-33',
    'ZDB-FIG-190726-21',
    'ZDB-FIG-190725-64',
    'ZDB-FIG-190726-47',
    'ZDB-FIG-190725-65',
    'ZDB-FIG-190726-48',
    'ZDB-FIG-190725-66',
    'ZDB-FIG-190726-49',
    'ZDB-FIG-190725-67',
    'ZDB-FIG-190726-50',
    'ZDB-FIG-190725-68',
    'ZDB-FIG-190726-51',
    'ZDB-FIG-190725-48',
    'ZDB-FIG-190726-31',
    'ZDB-FIG-190725-49',
    'ZDB-FIG-190726-32',
    'ZDB-FIG-190725-50',
    'ZDB-FIG-190726-33',
    'ZDB-FIG-190725-38',
    'ZDB-FIG-190726-27',
    'ZDB-FIG-190725-39',
    'ZDB-FIG-190726-28',
    'ZDB-FIG-190725-15',
    'ZDB-FIG-190726-3',
    'ZDB-FIG-190725-46',
    'ZDB-FIG-190726-29',
    'ZDB-FIG-190725-56',
    'ZDB-FIG-190726-39',
    'ZDB-FIG-190725-57',
    'ZDB-FIG-190726-40',
    'ZDB-FIG-190725-58',
    'ZDB-FIG-190726-41',
    'ZDB-FIG-190725-59',
    'ZDB-FIG-190726-42',
    'ZDB-FIG-190725-60',
    'ZDB-FIG-190726-43',
    'ZDB-FIG-190724-1',
    'ZDB-FIG-190725-13',
    'ZDB-FIG-190726-1',
    'ZDB-FIG-190724-2',
    'ZDB-FIG-190725-14',
    'ZDB-FIG-190726-2',
    'ZDB-FIG-190725-52',
    'ZDB-FIG-190726-35',
    'ZDB-FIG-190725-53',
    'ZDB-FIG-190726-36',
    'ZDB-FIG-190725-26',
    'ZDB-FIG-190726-14',
    'ZDB-FIG-190725-69',
    'ZDB-FIG-190726-52',
    'ZDB-FIG-190725-70',
    'ZDB-FIG-190726-53',
    'ZDB-FIG-190725-71',
    'ZDB-FIG-190726-54',
    'ZDB-FIG-190725-72',
    'ZDB-FIG-190726-55',
    'ZDB-FIG-190725-73',
    'ZDB-FIG-190726-56',
    'ZDB-FIG-190725-35',
    'ZDB-FIG-190726-23',
    'ZDB-FIG-190725-36',
    'ZDB-FIG-190726-24',
    'ZDB-FIG-190725-47',
    'ZDB-FIG-190726-30',
    'ZDB-FIG-190725-16',
    'ZDB-FIG-190726-4',
    'ZDB-FIG-190725-17',
    'ZDB-FIG-190726-5',
    'ZDB-FIG-190725-18',
    'ZDB-FIG-190726-6',
    'ZDB-FIG-190725-77',
    'ZDB-FIG-190726-59',
    'ZDB-FIG-190725-78',
    'ZDB-FIG-190726-60',
    'ZDB-FIG-190725-79',
    'ZDB-FIG-190726-61',
    'ZDB-FIG-190725-34',
    'ZDB-FIG-190726-22',
    'ZDB-FIG-190725-76',
    'ZDB-FIG-190726-26',
    'ZDB-IMAGE-190723-1262',
    'ZDB-IMAGE-190725-28',
    'ZDB-IMAGE-190726-26',
    'ZDB-IMAGE-190725-15',
    'ZDB-IMAGE-190726-15',
    'ZDB-IMAGE-190725-51',
    'ZDB-IMAGE-190726-44',
    'ZDB-IMAGE-190725-62',
    'ZDB-IMAGE-190726-57',
    'ZDB-IMAGE-190725-63',
    'ZDB-IMAGE-190726-58',
    'ZDB-IMAGE-190725-52',
    'ZDB-IMAGE-190726-45',
    'ZDB-IMAGE-190725-53',
    'ZDB-IMAGE-190726-48',
    'ZDB-IMAGE-190725-4',
    'ZDB-IMAGE-190726-34',
    'ZDB-IMAGE-190725-18',
    'ZDB-IMAGE-190726-18',
    'ZDB-IMAGE-190725-19',
    'ZDB-IMAGE-190726-19',
    'ZDB-IMAGE-190725-20',
    'ZDB-IMAGE-190726-20',
    'ZDB-IMAGE-190725-21',
    'ZDB-IMAGE-190726-21',
    'ZDB-IMAGE-190725-22',
    'ZDB-IMAGE-190726-22',
    'ZDB-IMAGE-190725-23',
    'ZDB-IMAGE-190726-23',
    'ZDB-IMAGE-190725-24',
    'ZDB-IMAGE-190726-2',
    'ZDB-IMAGE-190725-54',
    'ZDB-IMAGE-190726-49',
    'ZDB-IMAGE-190725-55',
    'ZDB-IMAGE-190726-50',
    'ZDB-IMAGE-190725-34',
    'ZDB-IMAGE-190726-51',
    'ZDB-IMAGE-190725-56',
    'ZDB-IMAGE-190726-52',
    'ZDB-IMAGE-190725-57',
    'ZDB-IMAGE-190726-53',
    'ZDB-IMAGE-190725-39',
    'ZDB-IMAGE-190726-31',
    'ZDB-IMAGE-190725-40',
    'ZDB-IMAGE-190726-32',
    'ZDB-IMAGE-190725-41',
    'ZDB-IMAGE-190726-33',
    'ZDB-IMAGE-190725-29',
    'ZDB-IMAGE-190726-1',
    'ZDB-IMAGE-190725-1',
    'ZDB-IMAGE-190726-28',
    'ZDB-IMAGE-190725-6',
    'ZDB-IMAGE-190726-6',
    'ZDB-IMAGE-190725-37',
    'ZDB-IMAGE-190726-29',
    'ZDB-IMAGE-190725-46',
    'ZDB-IMAGE-190726-39',
    'ZDB-IMAGE-190725-47',
    'ZDB-IMAGE-190726-40',
    'ZDB-IMAGE-190725-48',
    'ZDB-IMAGE-190726-41',
    'ZDB-IMAGE-190725-49',
    'ZDB-IMAGE-190726-42',
    'ZDB-IMAGE-190725-50',
    'ZDB-IMAGE-190726-43',
    'ZDB-IMAGE-190724-1',
    'ZDB-IMAGE-190725-3',
    'ZDB-IMAGE-190726-3',
    'ZDB-IMAGE-190724-2',
    'ZDB-IMAGE-190725-5',
    'ZDB-IMAGE-190726-4',
    'ZDB-IMAGE-190725-42',
    'ZDB-IMAGE-190726-35',
    'ZDB-IMAGE-190725-43',
    'ZDB-IMAGE-190726-36',
    'ZDB-IMAGE-190725-17',
    'ZDB-IMAGE-190726-17',
    'ZDB-IMAGE-190725-2',
    'ZDB-IMAGE-190726-54',
    'ZDB-IMAGE-190725-58',
    'ZDB-IMAGE-190726-55',
    'ZDB-IMAGE-190725-59',
    'ZDB-IMAGE-190726-56',
    'ZDB-IMAGE-190725-60',
    'ZDB-IMAGE-190726-46',
    'ZDB-IMAGE-190725-61',
    'ZDB-IMAGE-190726-47',
    'ZDB-IMAGE-190725-26',
    'ZDB-IMAGE-190726-24',
    'ZDB-IMAGE-190725-27',
    'ZDB-IMAGE-190726-25',
    'ZDB-IMAGE-190725-38',
    'ZDB-IMAGE-190726-30',
    'ZDB-IMAGE-190725-7',
    'ZDB-IMAGE-190726-7',
    'ZDB-IMAGE-190725-8',
    'ZDB-IMAGE-190726-8',
    'ZDB-IMAGE-190725-9',
    'ZDB-IMAGE-190726-9',
    'ZDB-IMAGE-190725-65',
    'ZDB-IMAGE-190726-59',
    'ZDB-IMAGE-190725-66',
    'ZDB-IMAGE-190726-60',
    'ZDB-IMAGE-190725-67',
    'ZDB-IMAGE-190726-61',
    'ZDB-IMAGE-190725-25',
    'ZDB-IMAGE-190726-5',
    'ZDB-IMAGE-190725-64',
    'ZDB-IMAGE-190726-27'
);