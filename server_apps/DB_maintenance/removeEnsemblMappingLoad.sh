#!/bin/bash
#
# Undo a Load-Ensembl-Mapping run: remove the Ensembl(GRCz12tu / GRCz12ab / GRCz13ab) db_links that were attributed
# to the publication the load was run with. The loader itself never deletes, so this is the way back.
#
#   removeEnsemblMappingLoad.sh ZDB-PUB-261020-6            report what would be removed (changes nothing)
#   removeEnsemblMappingLoad.sh ZDB-PUB-261020-6 --delete   remove it
#
# What it removes, for db_links in the Zebrafish Ensembl(GRCz12tu|GRCz12ab|GRCz13ab) reference databases that are
# attributed to the publication:
#   * a link whose ONLY attribution is this publication is deleted, with its assembly rows and attribution (the
#     delete cascades from zdb_active_data). The loader makes such links, so these are the ones it added.
#   * a link that also has another attribution already existed before the load (the loader only attributes
#     existing links), so it is kept and only this publication's attribution is removed.
# db_link_assembly rows the load added to a kept link cannot be told apart from older ones and are left alone.
#
# Connects with the usual libpq settings (PGHOST, PGPORT, PGUSER, PGDATABASE, PGPASSWORD). In the sidecar:
#   PGHOST=db PGUSER=postgres PGDATABASE=zfindb removeEnsemblMappingLoad.sh ZDB-PUB-261020-6
# Everything runs in one transaction; without --delete it is rolled back.

set -euo pipefail

PUB="${1:-}"
MODE="${2:-}"

if [[ ! "$PUB" =~ ^ZDB-PUB-[0-9]{6}-[0-9]+$ ]] || [[ -n "$MODE" && "$MODE" != "--delete" ]]; then
    echo "usage: $0 ZDB-PUB-yymmdd-n [--delete]" >&2
    exit 1
fi

if [[ "$MODE" == "--delete" ]]; then
    FINISH="commit"
    HEADING="DELETING"
else
    FINISH="rollback"
    HEADING="DRY RUN (nothing is changed; pass --delete to remove)"
fi

psql -v ON_ERROR_STOP=1 -v pub="$PUB" -v heading="$HEADING" <<SQL
\\set QUIET on
\\pset footer off
begin;

create temp table attributed_link on commit drop as
select dblink_zdb_id as id,
       dblink_linked_recid as gene,
       dblink_acc_num as accession,
       fdb_db_name as reference_db,
       not exists (select 1 from record_attribution other
                    where other.recattrib_data_zdb_id = dblink_zdb_id
                      and other.recattrib_source_zdb_id <> :'pub') as only_this_pub
  from record_attribution
  join db_link on dblink_zdb_id = recattrib_data_zdb_id
  join foreign_db_contains on fdbcont_zdb_id = dblink_fdbcont_zdb_id
  join foreign_db on fdb_db_pk_id = fdbcont_fdb_db_id
 where recattrib_source_zdb_id = :'pub'
   and recattrib_source_type = 'standard'
   and fdbcont_organism_common_name = 'Zebrafish'
   and fdb_db_name in ('Ensembl(GRCz12tu)', 'Ensembl(GRCz12ab)', 'Ensembl(GRCz13ab)');

\\echo :heading
\\echo 'Publication: ' :pub
\\echo
\\echo 'Links attributed to the publication, by reference database:'
select reference_db,
       count(*) as links,
       count(*) filter (where only_this_pub) as deleted,
       count(*) filter (where not only_this_pub) as kept_attribution_removed
  from attributed_link group by reference_db order by reference_db;

delete from zdb_active_data where zactvd_zdb_id in (select id from attributed_link where only_this_pub);
delete from record_attribution
 where recattrib_source_zdb_id = :'pub' and recattrib_source_type = 'standard'
   and recattrib_data_zdb_id in (select id from attributed_link where not only_this_pub);

\\echo
\\echo 'Left over after the removal (should be 0 links, 0 attributions):'
select (select count(*) from db_link where dblink_zdb_id in (select id from attributed_link where only_this_pub)) as links,
       (select count(*) from record_attribution where recattrib_source_zdb_id = :'pub'
           and recattrib_data_zdb_id in (select id from attributed_link)) as attributions;

${FINISH};
SQL
