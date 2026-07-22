--liquibase formatted sql
--changeset rtaylor:0030-ZFIN-10352-link-photoconvertible-efg-colors.sql

-- ZFIN-10352: The "Construct -> Reporter Color" search facet is moving from a
-- static Solr synonym file to being DB-derived (construct -> EFG ->
-- fpProtein_efg -> fluorescent_protein emission color). Several EFGs that the
-- old static file colored have no fpProtein_efg link and so would lose their
-- color under the new approach.
--
-- Root cause: the original auto-linker (add_colors.sql, changeset 1124) matched
--   lower(mrkr_name) = lower(fp_name)
-- exactly. FPBase stores multi-state (photoconvertible) proteins as two rows
-- with a state suffix -- "Kaede (Green)" / "Kaede (Red)" -- so no row is ever
-- named exactly "Kaede", and every photoconvertible was skipped. Version drift
-- (marker "KikGR" vs protein "KikGR1", "Eos" vs "EosFP") skipped others.
--
-- This links those EFGs to their FPBase protein rows -- both states for the
-- photoconvertibles, so they keep appearing in both color facets (e.g. Green
-- AND Red). fpProtein_efg is never rewritten by import (importMissingProteins
-- inserts fluorescent_protein by UUID; create_color_info only recomputes colors
-- from wavelength), so these links are durable across reimports.
--
-- Matching is by EFG abbreviation + exact FPBase protein name, and is guarded by
-- NOT EXISTS so the script is idempotent. Only proteins that carry an emission
-- color are linked.

insert into fpProtein_efg (fe_mrkr_zdb_id, fe_fl_protein_id)
select m.mrkr_zdb_id, p.fp_pk_id
from (values
    -- photoconvertible: link both (Green) and (Red) states
    ('Kaede',    'Kaede (Green)'),
    ('Kaede',    'Kaede (Red)'),
    ('KikGR',    'KikGR1 (Green)'),
    ('KikGR',    'KikGR1 (Red)'),
    ('Dendra',   'Dendra (Green)'),
    ('Dendra',   'Dendra (Red)'),
    ('Dendra2',  'Dendra2 (Green)'),
    ('Dendra2',  'Dendra2 (Red)'),
    ('Eos',      'EosFP (Green)'),
    ('Eos',      'EosFP (Red)'),
    ('mClavGR2', 'mClavGR2 (Green)'),
    ('mClavGR2', 'mClavGR2 (Red)'),
    -- single-state fluorophores the static file colored
    ('Dronpa',   'Dronpa (On)'),
    ('ZsGreen',  'ZsGreen'),
    ('sGFP',     'SGFP2'),
    ('mRFP',     'mRFP1'),
    -- Generic color-named reporter EFGs. These are catch-all markers used on
    -- thousands of constructs (bare "GFP" alone is on ~1100), and the fluorophore
    -- name is definitional of its color. Link each to a canonical representative
    -- protein of that color so those constructs keep their reporter color (the
    -- old name-based analyzer colored them via the "gfp"/"rfp" substring).
    ('GFP',      'avGFP'),
    ('RFP',      'DsRed'),
    ('cpEGFP',   'EGFP')
  ) as pairs(mrkr_abbrev, fp_name)
join marker m on m.mrkr_abbrev = pairs.mrkr_abbrev and m.mrkr_type = 'EFG'
join fluorescent_protein p on p.fp_name = pairs.fp_name and p.fp_emission_color is not null
where not exists (
    select 1 from fpProtein_efg fe
    where fe.fe_mrkr_zdb_id = m.mrkr_zdb_id
      and fe.fe_fl_protein_id = p.fp_pk_id
  );
