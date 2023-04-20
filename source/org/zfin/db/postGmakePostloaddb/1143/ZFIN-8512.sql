--liquibase formatted sql
--changeset rtaylor:ZFIN-8512.sql

DROP TABLE IF EXISTS temp_af_map;

CREATE TEMPORARY TABLE temp_af_map
(
    featid varchar(255),
    featname varchar(255),
    af_filename varchar(255)
);

-- define the mappings from feature name to the amsterdam file name
INSERT INTO temp_af_map (featname, af_filename)
VALUES
    ('hi2765Tg', '591.htm'),
    ('hi1167Tg', '343.htm'),
    ('hi1126Tg', '933.htm'),
    ('hi1364aTg', '577b.htm'),
    ('hi1386Tg', '1370.htm'),
    ('hi1944Tg', '1055b.htm'),
    ('hi2019Tg', '962.htm'),
    ('hi2069Tg', '1002.htm'),
    ('hi2108Tg', '642.htm'),
    ('hi2122Tg', '642.htm'),
    ('hi2188aTg', '2092.htm'),
    ('hi2404Tg', '2404.htm'),
    ('hi2505aTg', '199.htm'),
    ('hi2558Tg', '1520.htm'),
    ('hi2587Tg', '4.htm'),
    ('hi2639cTg', '1715.htm'),
    ('hi2705aTg', '2689.htm'),
    ('hi2715Tg', '1463.htm'),
    ('hi2720Tg', '557.htm'),
    ('hi2729aTg', '318.htm'),
    ('hi2839bTg', '1058.htm'),
    ('hi2857Tg', '2080.htm'),
    ('hi2877bTg', '2618.htm'),
    ('hi2911bTg', '37.htm'),
    ('hi3018cTg', '1326.htm'),
    ('hi3020Tg', '272.htm'),
    ('hi3074Tg', '1411.htm'),
    ('hi3112aTg', '486.htm'),
    ('hi3198Tg', '887.htm'),
    ('hi3205Tg', '1244.htm'),
    ('hi3245Tg', '1373.htm'),
    ('hi3357Tg', '954.htm'),
    ('hi3439Tg', '1437.htm'),
    ('hi3594aTg', '2696.htm'),
    ('hi3669Tg', '1872.htm'),
    ('hi3689Tg', '929.htm'),
    ('hi3714Tg', '2109.htm'),
    ('hi3783Tg', '1019.htm'),
    ('hi3936Tg', '1371.htm'),
    ('hi3988Tg', '1444.htm'),
    ('hi4042Tg', '1433.htm'),
    ('hi4049Tg', '4049.htm'),
    ('hi4055Tg', '3079.htm'),
    ('hi4070Tg', '1159.htm'),
    ('hi780Tg', '4.htm'),
    ('hi821bTg', '4.htm'),
    ('hi840Tg', '781.htm');

-- set the mapping table to include the feature zdb id
UPDATE temp_af_map t1
SET featid = feature_zdb_id
FROM feature t2
WHERE t1.featname = t2.feature_name;

-- update the amsterdam file table with the new file location based on the mapping table
UPDATE amsterdam_file t1
SET af_file_location = t2.af_filename,
    af_is_overlapping_file = t2.af_filename
FROM temp_af_map t2
WHERE t1.af_feature_zdb_id = t2.featid;
