-- check that sequence_feature_chromosome_location table is correct
-- in regards of the sfcl_chromosome_reference_accession_number column:


SELECT *
FROM sequence_feature_chromosome_location
WHERE
    (sfcl_chromosome_reference_accession_number != 'NC_007112.7' and sfcl_chromosome = '1') OR
    (sfcl_chromosome_reference_accession_number != 'NC_007113.7' and sfcl_chromosome = '2') OR
    (sfcl_chromosome_reference_accession_number != 'NC_007114.7' and sfcl_chromosome = '3') OR
    (sfcl_chromosome_reference_accession_number != 'NC_007115.7' and sfcl_chromosome = '4') OR
    (sfcl_chromosome_reference_accession_number != 'NC_007116.7' and sfcl_chromosome = '5') OR
    (sfcl_chromosome_reference_accession_number != 'NC_007117.7' and sfcl_chromosome = '6') OR
    (sfcl_chromosome_reference_accession_number != 'NC_007118.7' and sfcl_chromosome = '7') OR
    (sfcl_chromosome_reference_accession_number != 'NC_007119.7' and sfcl_chromosome = '8') OR
    (sfcl_chromosome_reference_accession_number != 'NC_007120.7' and sfcl_chromosome = '9') OR
    (sfcl_chromosome_reference_accession_number != 'NC_007121.7' and sfcl_chromosome = '10') OR
    (sfcl_chromosome_reference_accession_number != 'NC_007122.7' and sfcl_chromosome = '11') OR
    (sfcl_chromosome_reference_accession_number != 'NC_007123.7' and sfcl_chromosome = '12') OR
    (sfcl_chromosome_reference_accession_number != 'NC_007124.7' and sfcl_chromosome = '13') OR
    (sfcl_chromosome_reference_accession_number != 'NC_007125.7' and sfcl_chromosome = '14') OR
    (sfcl_chromosome_reference_accession_number != 'NC_007126.7' and sfcl_chromosome = '15') OR
    (sfcl_chromosome_reference_accession_number != 'NC_007127.7' and sfcl_chromosome = '16') OR
    (sfcl_chromosome_reference_accession_number != 'NC_007128.7' and sfcl_chromosome = '17') OR
    (sfcl_chromosome_reference_accession_number != 'NC_007129.7' and sfcl_chromosome = '18') OR
    (sfcl_chromosome_reference_accession_number != 'NC_007130.7' and sfcl_chromosome = '19') OR
    (sfcl_chromosome_reference_accession_number != 'NC_007131.7' and sfcl_chromosome = '20') OR
    (sfcl_chromosome_reference_accession_number != 'NC_007132.7' and sfcl_chromosome = '21') OR
    (sfcl_chromosome_reference_accession_number != 'NC_007133.7' and sfcl_chromosome = '22') OR
    (sfcl_chromosome_reference_accession_number != 'NC_007134.7' and sfcl_chromosome = '23') OR
    (sfcl_chromosome_reference_accession_number != 'NC_007135.7' and sfcl_chromosome = '24') OR
    (sfcl_chromosome_reference_accession_number != 'NC_007136.7' and sfcl_chromosome = '25');

-- running this one should make sure no feature is out of sync.