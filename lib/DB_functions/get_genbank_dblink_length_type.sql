CREATE OR REPLACE FUNCTION get_genbank_dblink_length_type(                              vDblinkAccNum          TEXT,
                                                                                        vDblinkLength          INTEGER,
                                                                                        vDblinkFdbcontZdbId    TEXT,
  OUT                                                                                   vDblinkFdbcontZdbIdOut TEXT,
  OUT                                                                                   vDblinkLengthOut       INT) AS $func$

DECLARE vAccbkLength  accession_bank.accbk_length%TYPE;
        vFdbcontType  foreign_db_data_type.fdbdt_data_type%TYPE;
        vFdbcontZdbID db_link.dblink_fdbcont_zdb_id%TYPE;
        vUpdateLength db_link.dblink_length%TYPE;

BEGIN

  IF (SELECT count(*)
      FROM accession_bank, foreign_db_contains, foreign_db
      WHERE accbk_acc_num = vDblinkAccNum
            AND fdb_db_pk_id = fdbcont_fdb_db_id
            AND accbk_fdbcont_zdb_id = fdbcont_zdb_id
            AND fdb_db_name = 'GenBank') > 0
  THEN
    IF vDblinkFdbcontZdbId IS NOT NULL
    THEN
      RAISE NOTICE 'fdbcontsecond: %', vDblinkFdbcontZdbId;
      SELECT fdbdt_data_type
      INTO vFdbcontType
      FROM foreign_db_contains, foreign_db_data_Type
      WHERE fdbcont_zdb_id = vDblinkFdbcontZdbId
            AND fdbcont_fdbdt_id = fdbdt_pk_id;

      IF vFdbcontType = 'other'
      THEN
        vDblinkFdbcontZdbIdOut = vDblinkFdbcontZdbId;
        vDblinkLengthOut = vDblinkLength;
      END IF;

    END IF;
    SELECT
      accbk_length,
      accbk_fdbcont_zdb_id
    INTO vAccbkLength, vFdbcontZdbId
    FROM accession_bank, foreign_db_contains, foreign_db_data_type, foreign_db
    WHERE accbk_acc_num = vDblinkAccNum
          AND accbk_fdbcont_zdb_id = fdbcont_zdb_id
          AND fdbcont_fdb_db_id = fdb_db_pk_id
          AND fdbcont_fdbdt_id = fdbdt_pk_id
          AND fdbdt_super_type = 'sequence'
          AND fdbcont_organism_common_name = 'Zebrafish'
          AND fdb_db_name = 'GenBank';
    vDblinkFdbcontZdbIdOut = vFdbcontZdbId;
    vDblinkLengthOut = vAccbkLength;

  ELSE
    vUpdateLength = (SELECT accbk_length
                     FROM accession_bank, foreign_db_contains, foreign_db
                     WHERE accbk_acc_num = vDblinkAccNum
                           AND fdbcont_fdb_db_id = fdb_db_pk_id
                           AND accbk_fdbcont_zdb_id = fdbcont_zdb_id
                           AND fdb_db_name != 'GenBank'
                           AND accbk_length IS NOT NULL);

    IF vDbLinkLength IS NULL AND vUpdateLength IS NOT NULL
    THEN
      vDbLinkLengthOut = vUpdateLength;
      vDblinkFdbcontZdbIdOut = vDblinkFdbcontZdbId;
    ELSE
      vDblinkFdbcontZdbIdOut = vDblinkFdbcontZdbId;
      vDblinkLengthOut = vDblinkLength;
    END IF;

  END IF;

END;
$func$ LANGUAGE plpgsql;
