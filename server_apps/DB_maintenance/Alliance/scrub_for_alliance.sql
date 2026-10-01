-- ZFIN-10509 -- strip confidential data from a zfindb copy destined for the Alliance.
--
-- Run this against a SCRATCH database restored from a production dump, never
-- against a live one. pg_dump can exclude tables but not columns, so a single
-- dump invocation cannot produce this file; the sequence is
--
--   createdb zfindb_alliance
--   pg_restore -d zfindb_alliance <production dump>
--   psql -v ON_ERROR_STOP=1 -d zfindb_alliance -f scrub_for_alliance.sql
--   ./verify_no_pii.sh <dump of zfindb_alliance>      <-- the gate
--   pg_dump zfindb_alliance > ZFIN_alliance_<date>.sql
--
-- The verify step is not a formality. The column list below is necessary but
-- NOT sufficient: addresses also sit in free-text columns that no column name
-- advertises. Measured on a recent copy, before scrubbing:
--
--   updates.new_value   20,688 rows matching an email pattern
--   updates.old_value    8,807
--   updates.comments        66
--   person.pers_bio          7
--   person.address           4
--
-- Nulling person.email would have left all eleven person rows exposed. Treat
-- verify_no_pii.sh's exit code as the release criterion, not this file.

\set ON_ERROR_STOP on

BEGIN;

-- Guard. This script nulls every password and login in zdb_submitters; running
-- it on the live database would lock every user out of ZFIN. Refuse by name.
-- A scratch restore gets its own database name, so this costs nothing there.
DO $$
BEGIN
    IF current_database() = 'zfindb' THEN
        RAISE EXCEPTION
            'refusing to scrub a database named zfindb (this looks like the live one). '
            'Restore the dump into a scratch database first, e.g. zfindb_alliance.';
    END IF;
END $$;


-- ---------------------------------------------------------------------------
-- 1. The curation audit log.
--
-- 2.18M rows and by far the largest concentration of addresses in the schema
-- (~29,500 rows carry one in old_value/new_value/comments, because curators
-- edit person.email and the log records both sides of every edit). It is
-- internal curation history with no value to the Alliance, so it goes whole
-- rather than being cleaned field by field.
-- ---------------------------------------------------------------------------
TRUNCATE TABLE updates;


-- ---------------------------------------------------------------------------
-- 2. Author correspondence.
--
-- pub_correspondence_sent_email.pubcse_text is the message BODY -- free prose
-- written to authors, containing addresses, names and whatever else was said.
-- The recipient and received-email tables hold addresses outright.
--
-- Deliberately NOT truncated: pub_correspondence_need, _need_reason,
-- _need_resolution, _need_resolution_type and _subject. Those are controlled
-- vocabularies and per-publication workflow state (ids, ordering, template
-- text) with no personal data in them.
--
-- No CASCADE: the tables below are listed exhaustively, so a foreign key error
-- here means something outside this set references correspondence and the
-- omission should be looked at, not silently truncated away.
-- ---------------------------------------------------------------------------
TRUNCATE TABLE
    pub_correspondence_sent_email_contains_subject,
    pub_correspondence_sent_tracker,
    pub_correspondence_recipient,
    pub_correspondence_received_email,
    pub_correspondence_sent_email,
    publication_correspondence;


-- ---------------------------------------------------------------------------
-- 3. Credentials.
--
-- cookie and is_curator are NOT NULL, so they take placeholder values rather
-- than NULL -- a plain nulling pass fails on every row.
--
-- cookie is also UNIQUE, so every row needs a DIFFERENT placeholder: a blanket
-- '' fails on the second row with
--   duplicate key value violates unique constraint "zdb_submitters_cookie_index"
-- Deriving it from zdb_id (the primary key) is unique by construction, is not a
-- credential, and is obviously not a real session cookie to anyone reading it.
--
-- login is unique too but nullable, and Postgres permits many NULLs in a unique
-- index, so nulling it wholesale is fine.
--
-- is_curator / is_student are flattened because ZFIN-10509 names them, but
-- they are access flags rather than secrets. If anything downstream of this
-- dump distinguishes curated from submitted records, flattening them will
-- change its behaviour; drop the last two assignments in that case.
-- ---------------------------------------------------------------------------
-- login is replaced rather than nulled: a distinct placeholder keeps accounts
-- distinguishable and keeps anything that renders a submitter showing
-- something, at no cost to privacy. Derived from zdb_id, so it is unique by
-- construction (zdb_id is the primary key) which the unique index requires.
--
-- Deliberately NOT md5(login) or any hash of the real value. ZFIN logins are
-- low-entropy and formulaic -- first initial plus surname -- and person still
-- carries full_name/first_name/last_name in this same dump, so a hash of
-- ~11,000 such logins falls to a dictionary attack immediately. A value
-- derived from zdb_id is not derived from the secret at all.
--
-- Note this is relabeling, not anonymisation: zdb_id stays (the foreign keys
-- need it), so rows remain linkable to an account. What it removes is the
-- credential -- the string someone would type into a login form -- which is
-- what ZFIN-10509 names.
UPDATE zdb_submitters
   SET login                 = 'user-' || zdb_id,
       password              = NULL,
       password_reset_key    = NULL,
       password_reset_date   = NULL,
       password_last_updated = NULL,
       previous_login        = NULL,
       access                = NULL,
       cookie                = 'scrubbed-' || zdb_id,
       is_curator            = false,
       is_student            = false;


-- ---------------------------------------------------------------------------
-- 4. Address columns.
--
-- person.email is the one ZFIN-10509 names. The other four came out of a
-- schema-wide search for columns whose name implies an address, and are just
-- as exposed. submission_log is empty today but is scrubbed anyway, so the
-- script stays correct if it starts being written to.
-- ---------------------------------------------------------------------------
UPDATE person                         SET email               = NULL WHERE email               IS NOT NULL;
UPDATE company                        SET email               = NULL WHERE email               IS NOT NULL;
UPDATE lab                            SET email               = NULL WHERE email               IS NOT NULL;
UPDATE submission_log                 SET sublog_email        = NULL WHERE sublog_email        IS NOT NULL;
UPDATE zebrashare_submission_metadata SET zsm_submitter_email = NULL WHERE zsm_submitter_email IS NOT NULL;


-- ---------------------------------------------------------------------------
-- 5. Addresses embedded in free text.
--
-- pers_bio is legitimate public content on the person page and address is a
-- postal address, so these are redacted in place rather than nulled: only the
-- matched address is replaced, the surrounding text survives.
-- ---------------------------------------------------------------------------
UPDATE person
   SET pers_bio = regexp_replace(pers_bio,
                                 '[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}',
                                 '[email removed]', 'gi')
 WHERE pers_bio ~* '[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}';

UPDATE person
   SET address = regexp_replace(address,
                                '[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}',
                                '[email removed]', 'gi')
 WHERE address ~* '[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}';


-- ---------------------------------------------------------------------------
-- 6. Addresses anywhere else in the schema.
--
-- Sections 4 and 5 work from a list somebody wrote down, and the first run
-- against a production copy proved that is not enough. After every column
-- ZFIN-10509 names had been scrubbed, 4,370 rows still carried an address:
--
--   publication_note.pnote_text            4192
--   publication.pub_abstract                111
--   data_note.dnote_text                     22
--   marker_history.mhist_comments            12
--   publication_file.pf_original_file_name   10   (PDFs named after the sender)
--   publication_file.pf_file_name             9
--   publication.pub_errata_and_notes          4
--   term.term_comment                         3
--   probe_lib.pl_description                  2
--   person.url                                2
--   marker.mrkr_comments                      1
--   lab.url                                   1
--   source_url.srcurl_url                     1
--
-- Curator notes on publications are where correspondence with authors gets
-- pasted, so that is where the bulk sits -- and pf_file_name shows how little
-- the column name tells you, because people name PDFs after whoever sent them.
--
-- So this pass is driven by the schema instead of by a list: every text column
-- of every base table, redacted in place. Only the matched address is replaced,
-- so abstracts and notes keep their prose. Over-redaction is the safe direction
-- for a file that leaves the building.
--
-- Safe with respect to section 1: none of the triggers on these tables write to
-- `updates`, so redacting them cannot repopulate the audit log truncated there.
-- ---------------------------------------------------------------------------
DO $$
DECLARE
    r        record;
    n        bigint;
    total    bigint := 0;
    re       constant text := '[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}';
BEGIN
    FOR r IN
        SELECT c.table_name, c.column_name
          FROM information_schema.columns c
          JOIN information_schema.tables t
            ON t.table_schema = c.table_schema
           AND t.table_name   = c.table_name
           AND t.table_type   = 'BASE TABLE'
         WHERE c.table_schema = 'public'
           AND c.data_type IN ('text', 'character varying')
         ORDER BY c.table_name, c.column_name
    LOOP
        EXECUTE format(
            'UPDATE public.%I SET %I = regexp_replace(%I, %L, %L, %L) WHERE %I ~* %L',
            r.table_name, r.column_name, r.column_name,
            re, '[email removed]', 'gi',
            r.column_name, re);
        GET DIAGNOSTICS n = ROW_COUNT;
        IF n > 0 THEN
            RAISE NOTICE 'redacted % row(s) in %.%', n, r.table_name, r.column_name;
            total := total + n;
        END IF;
    END LOOP;
    RAISE NOTICE 'redacted % row(s) in total', total;
END $$;


-- ---------------------------------------------------------------------------
-- 7. Routines that embed credentials.
--
-- Everything above scrubs table DATA. pg_dump also emits routine bodies as
-- DDL, and those are not covered by any amount of UPDATE -- which is how a
-- dump that passed every check still shipped eight ZFIN logins:
--
--   CREATE FUNCTION public.add_users() ...
--     userlist text[] := ARRAY['rtaylor','staylor','ryanm','cmpich', ...];
--     EXECUTE format('CREATE user %s WITH superuser', username);
--
-- Worse than the names, it states which accounts are superusers. add_users()
-- is an environment-bootstrap helper for standing up a fresh database; it has
-- no purpose in a dump for the Alliance, so it goes rather than being
-- rewritten.
--
-- Checked when this was written: add_users is the ONLY routine in the database
-- whose source mentions any of those logins. If that changes, this needs to
-- become a scan of pg_proc.prosrc rather than a named drop -- the query is
--   SELECT proname FROM pg_proc p JOIN pg_namespace n ON n.oid = p.pronamespace
--    WHERE n.nspname NOT IN ('pg_catalog','information_schema')
--      AND p.prosrc ~* '<login>|<login>|...';
-- ---------------------------------------------------------------------------
DROP FUNCTION IF EXISTS public.add_users();


COMMIT;

-- A last look before the dump is taken. Every count here must be zero;
-- verify_no_pii.sh re-checks the same thing against the dump file itself,
-- which is what actually leaves the building.
\echo ''
\echo 'post-scrub residue (all counts must be 0):'
SELECT 'zdb_submitters.password'  AS check, count(*) FROM zdb_submitters WHERE password IS NOT NULL
-- login is now a placeholder rather than NULL, so the check is that every row
-- holds exactly the derived value: anything else is a surviving real login.
UNION ALL SELECT 'zdb_submitters.login',    count(*) FROM zdb_submitters WHERE login IS DISTINCT FROM 'user-' || zdb_id
UNION ALL SELECT 'add_users() routine',     count(*) FROM pg_proc p JOIN pg_namespace n ON n.oid = p.pronamespace WHERE n.nspname = 'public' AND p.proname = 'add_users'
UNION ALL SELECT 'logins in any routine',   count(*) FROM pg_proc p JOIN pg_namespace n ON n.oid = p.pronamespace WHERE n.nspname NOT IN ('pg_catalog','information_schema') AND p.prosrc ~* '(rtaylor|staylor|ryanm|cmpich|zfishweb|zfinner)'
UNION ALL SELECT 'person.email',            count(*) FROM person  WHERE email IS NOT NULL
UNION ALL SELECT 'company.email',           count(*) FROM company WHERE email IS NOT NULL
UNION ALL SELECT 'lab.email',               count(*) FROM lab     WHERE email IS NOT NULL
UNION ALL SELECT 'person.pers_bio',         count(*) FROM person  WHERE pers_bio ~* '[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}'
UNION ALL SELECT 'person.address',          count(*) FROM person  WHERE address  ~* '[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}'
UNION ALL SELECT 'publication_note.pnote_text', count(*) FROM publication_note WHERE pnote_text  ~* '[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}'
UNION ALL SELECT 'publication.pub_abstract',    count(*) FROM publication      WHERE pub_abstract ~* '[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}'
UNION ALL SELECT 'updates',                 count(*) FROM updates
UNION ALL SELECT 'pub_correspondence_sent_email', count(*) FROM pub_correspondence_sent_email
ORDER BY 1;
