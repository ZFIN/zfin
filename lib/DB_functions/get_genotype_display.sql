-- get_genotype_display uses a large sql query to gather together the relationships between genotypes and marker/features.
-- This is the result of extracting that query into a view to make the function easier to read.
-- This query could potentially be broken down further.
CREATE OR REPLACE VIEW genotype_name_components_from_relationships AS
        -- start of result set 1
        SELECT DISTINCT
            get_feature_abbrev_display (feature_zdb_id) AS fad,
            zyg_allele_display,
            CASE WHEN mrkr_abbrev IS NULL THEN
                lower(get_feature_abbrev_display (feature_zdb_id))
            ELSE
                lower(mrkr_abbrev) || get_feature_abbrev_display (feature_zdb_id)
            END AS fad2,
            feature_Abbrev AS feature_abbrev,
            feature_type AS feature_type,
            zyg_abbrev AS zyg_abbrev,
            mrkr_abbrev AS mrkr_abbrev,
            gcs_significance,
            genofeat_geno_zdb_id,
            fmrel_type
        FROM
            feature,
            genotype_feature,
            zygocity,
            feature_type,
            feature_marker_relationship,
            genotype_component_significance,
            marker
        WHERE genofeat_feature_zdb_id = feature_zdb_id
            AND genofeat_zygocity = zyg_zdb_id
            AND fmrel_ftr_zdb_id = feature_zdb_id
            AND fmrel_mrkr_zdb_id = mrkr_zdb_id
            AND feature_type = ftrtype_name
            AND fmrel_type = gcs_fmrel_type
            AND gcs_mrkr_type = mrkr_type
            AND gcs_ftr_type = feature_type
            AND fmrel_type = 'is allele of'
        -- end of result set 1
        UNION
        -- start of result set 2
        SELECT DISTINCT
            get_feature_abbrev_display (feature_zdb_id) AS fad,
            zyg_allele_display,
            CASE WHEN fmrel_type IN ('contains innocuous sequence feature',
                'created by',
                'contains phenotypic sequence feature') THEN
                mrkr_abbrev
            ELSE
                lower(get_feature_abbrev_display (feature_zdb_id))
            END AS fad2,
            feature_Abbrev AS feature_abbrev,
            feature_type AS feature_type,
            zyg_abbrev AS zyg_abbrev,
            feature_abbrev AS feature_abbrev,
            CASE WHEN fmrel_type = 'contains innocuous sequence feature' THEN
                24
            ELSE
                gcs_significance --,
            END as gcs_significance,
            genofeat_geno_zdb_id,
            fmrel_type
        FROM
            feature,
            genotype_feature,
            zygocity,
            feature_type,
            feature_marker_relationship AS fm1,
            genotype_component_significance,
            marker
        WHERE genofeat_feature_zdb_id = feature_zdb_id
            AND genofeat_zygocity = zyg_zdb_id
            AND fmrel_ftr_zdb_id = feature_zdb_id
            AND fmrel_mrkr_zdb_id = mrkr_zdb_id
            AND feature_type = ftrtype_name
            AND feature_Type = 'DEFICIENCY'
            AND fmrel_type = gcs_fmrel_type
            AND gcs_mrkr_type = mrkr_type
            AND gcs_ftr_type = feature_type
            AND NOT EXISTS (
                SELECT
                    'x'
                FROM
                    feature_marker_relationship AS fm2
                WHERE
                    fm2.fmrel_ftr_zdb_id = fm1.fmrel_ftr_zdb_id
                    AND fm1.fmrel_type IN ('contains innocuous sequence feature', 'contains phenotypic sequence feature')
                    AND fm2.fmrel_type = 'is allele of')
          AND NOT EXISTS (
                SELECT
                    'x'
                FROM
                    feature_marker_relationship
                WHERE
                    fmrel_ftr_zdb_id = feature_Zdb_id
                AND fmrel_type = 'is allele of')
        -- end of result set 2
        UNION
        -- start of result set 3
        SELECT DISTINCT
            get_feature_abbrev_display (feature_zdb_id) AS fad,
            zyg_allele_display,
            CASE WHEN fmrel_type IN ('contains innocuous sequence feature',
                'created by',
                'contains phenotypic sequence feature') THEN
                mrkr_abbrev
            ELSE
                lower(get_feature_abbrev_display (feature_zdb_id))
            END AS fad2,
            feature_Abbrev AS feature_abbrev,
            feature_type AS feature_type,
            zyg_abbrev AS zyg_abbrev,
            feature_abbrev AS feature_abbrev,
            CASE WHEN fmrel_type = 'contains innocuous sequence feature' THEN
                24
            ELSE
                gcs_significance --,
            END as gcs_significance,
            genofeat_geno_zdb_id,
            fmrel_type
        FROM
            feature,
            genotype_feature,
            zygocity,
            feature_type,
            feature_marker_relationship AS fm1,
            genotype_component_significance,
            marker
        WHERE genofeat_feature_zdb_id = feature_zdb_id
            AND genofeat_zygocity = zyg_zdb_id
            AND fmrel_ftr_zdb_id = feature_zdb_id
            AND fmrel_mrkr_zdb_id = mrkr_zdb_id
            AND feature_type = ftrtype_name
            AND (feature_Type = 'TRANSLOC'
                OR feature_type = 'INVERSION')
            AND fmrel_type = gcs_fmrel_type
            AND gcs_mrkr_type = mrkr_type
            AND gcs_ftr_type = feature_type
            AND NOT EXISTS (
                SELECT
                    'x'
                FROM
                    feature_marker_relationship AS fm2
                WHERE
                    fm2.fmrel_ftr_zdb_id = fm1.fmrel_ftr_zdb_id
                    AND fm1.fmrel_type IN ('contains innocuous sequence feature', 'contains phenotypic sequence feature')
                    AND fm2.fmrel_type = 'is allele of')
            AND NOT EXISTS (
                SELECT
                    'x'
                FROM
                    feature_marker_relationship
                WHERE
                    fmrel_ftr_zdb_id = feature_Zdb_id
                    AND fmrel_type = 'is allele of')
        -- end of result set 3
        UNION
        -- start of result set 4
        SELECT DISTINCT
            get_feature_abbrev_display (feature_zdb_id) AS fad,
            zyg_allele_display,
            CASE WHEN fmrel_type IN ('contains innocuous sequence feature',
                'created by',
                'contains phenotypic sequence feature') THEN
                mrkr_abbrev
            ELSE
                lower(get_feature_abbrev_display (feature_zdb_id))
            END AS fad2,
            feature_Abbrev AS feature_abbrev,
            feature_type AS feature_type,
            zyg_abbrev AS zyg_abbrev,
            feature_abbrev AS feature_abbrev,
            CASE WHEN fmrel_type = 'contains innocuous sequence feature' THEN
                24
            ELSE
                gcs_significance --,
            END as gcs_significance,
            genofeat_geno_zdb_id,
            fmrel_type
        FROM
            feature,
            genotype_feature,
            zygocity,
            feature_type,
            feature_marker_relationship AS fm1,
            genotype_component_significance,
            marker
        WHERE genofeat_feature_zdb_id = feature_zdb_id
            AND genofeat_zygocity = zyg_zdb_id
            AND fmrel_ftr_zdb_id = feature_zdb_id
            AND fmrel_mrkr_zdb_id = mrkr_zdb_id
            AND feature_type = ftrtype_name
            AND feature_Type != 'DEFICIENCY'
            AND fmrel_type = gcs_fmrel_type
            AND fmrel_type NOT IN ('is allele of', 'created by', 'markers present', 'markers moved')
            AND gcs_mrkr_type = mrkr_type
            AND gcs_ftr_type = feature_type
            AND NOT EXISTS (
                SELECT
                    'x'
                FROM
                    feature_marker_relationship AS fm2
                WHERE
                    fm2.fmrel_ftr_zdb_id = fm1.fmrel_ftr_zdb_id
                    AND fm1.fmrel_type IN ('contains innocuous sequence feature', 'contains phenotypic sequence feature')
                    AND fm2.fmrel_type = 'is allele of')
            AND NOT EXISTS (
                SELECT
                    'x'
                FROM
                    feature_marker_relationship
                WHERE
                    fmrel_ftr_zdb_id = feature_Zdb_id
                    AND fmrel_type = 'is allele of');
        -- end of result set 4


CREATE OR REPLACE FUNCTION get_genotype_display (genoZdbId text)
    RETURNS text
    AS $genoDisplayHtml$
    -- ---------------------------------------------------------------------
    -- Given the ZDB ID of a genotype, returns the genotype display name in
    -- html format.
    -- [the same value that is stored in the genotype table. this function
    --  generates the value that is stored.]
    --
    --  INPUT VARS:
    --     genoZdbId
    --
    --  OUTPUT VARS:
    --     none
    --
    --  RETURNS:
    --     genotyp display with proper HTML formatting.
    --     NULL if genoZdbId does not exist in genotype table.
    --
    --  EFFECTS:
    --     none
    --------------------------------------------------------------------------
DECLARE
    genoDisplayHtml genotype.geno_display_name % TYPE;
    featAbbrev feature.feature_abbrev % TYPE;
    gcs genotype_component_significance.gcs_significance % TYPE;
    featAbbrevHtml text;
    fad2 text;
    genoBackground text;
    zygAllele text;
    tgRepeat boolean;
    tgLastFeat text;
    tgFirstFeatHtml text;
    featCount integer;
    tgLastMrkr text;
    featOrder varchar(2);
    fmrelType feature_marker_relationship.fmrel_type % TYPE;
    featSig feature_type.ftrtype_significance % TYPE;
    startName genotype.geno_display_name % TYPE;
    wildtype genotype.geno_is_wildtype % TYPE;
    featType feature.feature_type % TYPE;
    zygOrder zygocity.zyg_abbrev % TYPE;
    mrkrAbbrev marker.mrkr_abbrev % TYPE;
    featMrkrAbbrev varchar(255);
    --set debug file to '/tmp/debug.txt';
    --trace on;
BEGIN
    SELECT
        geno_display_name,
        geno_is_wildtype INTO startName,
        wildtype
    FROM
        genotype
    WHERE
        geno_zdb_id = genoZdbId;
    fad2 = '';
    tgRepeat = 'f';
    tgLastMrkr = '';
    tgLastFeat = '';
    fmrelType = '';
    featCount = 0;
    IF (wildtype != 't') THEN
        genoDisplayHtml = '';
        FOR featAbbrevHtml,
        zygAllele,
        mrkrAbbrev,
        featAbbrev,
        featType,
        zygOrder,
        featMrkrAbbrev,
        gcs IN
            SELECT DISTINCT
                fad,
                zyg_allele_display,
                gncfr.fad2,
                feature_abbrev,
                feature_type,
                zyg_abbrev,
                mrkr_abbrev,
                gcs_significance
            from
                genotype_name_components_from_relationships as gncfr -- VIEW defined above
            where 
                genofeat_geno_zdb_id = genoZdbId
            ORDER BY
                gcs_significance ASC,
                mrkr_abbrev ASC,
                zyg_abbrev,
                fad2,
                fad DESC
            LOOP
                RAISE notice 'fad2 %', fad2;
                RAISE notice 'featAbbrevHtml %', featAbbrevHtml;
                RAISE notice 'zygAllele %', zygAllele;
                RAISE notice 'genoDisplayHtml %', genoDisplayHtml;
                IF (fad2 = featAbbrevHtml) THEN
                    CONTINUE;
                ELSE
                    IF (featAbbrev LIKE '%unspecified') THEN
                        featAbbrev = 'unspecified';
                    END IF;
                    IF (featAbbrev LIKE '%unrecovered') THEN
                        featAbbrev = 'unrecovered';
                    END IF;
                    IF (zygAllele = '/allele') THEN
                        zygAllele = '/' || featAbbrev;
                    ELSE
                    END IF;
                    IF (zygAllele IS NULL) THEN
                        zygAllele = '';
                    END IF;
                    IF (genoDisplayHtml = '') THEN
                        genoDisplayHtml = featAbbrevHtml;
                    ELSE
                        genoDisplayHtml = genoDisplayHtml || '; ' || featAbbrevHtml;
                    END IF;
                    IF (zygAllele != '') THEN
                        IF (featAbbrevHtml LIKE '%<sup>%') THEN
                            genoDisplayHtml = genoDisplayHtml || '<sup>' || zygAllele || '</sup>';
                        ELSE
                            genoDisplayHtml = genoDisplayHtml || zygAllele;
                        END IF;
                    END IF;
                    fad2 = featAbbrevHtml;
                END IF;
            END LOOP;
        genoDisplayHTML = replace(genoDisplayHTML, '</sup><sup>', '');
    ELSE
        genoDisplayHTML = startName;
    END IF;

    -- Special Case as Leyla indicated in ZFIN-7922
    IF ('ZDB-GENO-200107-21' = genoZdbId and 'en.boxc3.sgca<sup>upo351/upo351</sup>' = genoDisplayHTML) THEN
        genoDisplayHTML = 'en.boxb2.sgca<sup>upo351/upo351</sup>';
    END IF;

    RETURN genoDisplayHtml;
END
$genoDisplayHtml$
LANGUAGE plpgsql
