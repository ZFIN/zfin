# Follow-up: `/images` is still owned by the archival-content repo

**Status:** open, deferred. Not blocking the `static-file-refactor` branch.
**Context:** ZFIN-10382 (static file extraction). No ticket filed yet.
**Written:** 2026-07-30, against `zfin-static` @ `35feb46` and this branch @ `87f33d121a`.

## Summary

The static-file refactor moved `home/{zf_info,images,ZFIN,robots.txt,favicon.ico,analytics.js}`
into the standalone `zfin-static` repo, deployed as a pinned release tarball. A
follow-up commit (`87f33d121a`) pulled the three root files back into this repo
under `docroot/`, on the grounds that they belong to the *application*, not to
the archival content.

**`images/` has the same problem, in a worse form: 51 of its 83 entries are
referenced only by the live app, and only 6 only by the static pages.** The
running Java app cannot render its own header or footer without a tagged release
of a 322 MB archival-content repo.

This was deliberately deferred to keep the refactor PR reviewable. This document
records the analysis so it doesn't have to be redone.

## The coupling

`zfin-static/src/images/` deploys to `/opt/zfin/static/images`, symlinked into the
Apache DocumentRoot, and served at `/images/…`. There are 115 absolute
`/images/…` references in this repo's `home/`, `source/`, `frontend/` and `conf/`.

The site chrome is the sharpest case — every logo in the header and footer lives
in the archival repo:

| File | Referenced from |
|---|---|
| `zfinlogo_lg.gif` | `home/WEB-INF/tags/layout/pageHeader.tag` |
| `ZfinWordmarkWhite.gif` | `home/WEB-INF/tags/layout/pageFooter.tag` |
| `AllianceLogoWhite.png` | `home/WEB-INF/tags/layout/pageFooter.tag` |
| `UOSignature-WHT.png` | `home/WEB-INF/tags/layout/pageFooter.tag` |
| `GCBR-Logo-Light-Foreground-Transparent.svg` | `home/WEB-INF/tags/layout/pageFooter.tag` |

Note the loop this closes: the static pages fetch `/action/layout/{header,footer}`
from Tomcat at runtime (`zfin-chrome.js`), and that markup then points back at
images shipped by the static release. Neither side is self-contained.

Beyond the chrome, the app-only images are ordinary UI furniture — BLAST
alignment bars (`black.gif`, `blue.gif`, `green.gif`, `purple.gif`, `red.gif`,
`scale.gif`, `transp.gif`, `query_no_scale.gif`), expand/collapse controls
(`plus.png`, `minus.png`, `plus-13.png`, `minus-13.png`, `plus-symbol.png`,
`minus-symbol.png`), sort arrows, spinners (`ajax-loader.gif`, `ajax-loader1.gif`),
image-gallery arrows, facet checkboxes, curation-tool icons.

Some are referenced from compiled Java, not just JSPs — e.g.
`source/org/zfin/search/service/SolrService.java` (`icon-checked.png`),
`source/org/zfin/framework/presentation/tags/ShowFacetLinksTag.java`
(`icon-check-empty.png`), `source/org/zfin/gwt/root/util/NoctuaLink.java`
(`noctua_icon.png`).

## Classification of all 83 entries

Method: for each entry in `zfin-static/src/images/`, grep for `images/<name>`
across this repo's `home/ source/ frontend/ conf/` (the app side) and across
`zfin-static/src/{zf_info,ZFIN,_includes}` (the static side).

| Category | Count |
|---|---|
| Referenced **only by the live app** | 51 |
| Referenced by **both** | 0 |
| Referenced **only by static pages** | 6 |
| **No reference found** | 24 |
| Subdirectories (`LOCAL/`, `ANIMATED/`, counted separately below) | 2 |

### Static-only (6) — these are genuinely archival

`bioeyes.jpg`, `fish-for-science.png`, `pdficon.gif` (7 pages), `thumbs_up.gif`,
`zfic.jpg`, `zK12.gif`

### Subdirectories

| Entry | App refs | Static refs |
|---|---|---|
| `LOCAL/jpeg.gif` | 0 | **79** |
| `LOCAL/comment.gif` | 0 | 1 |
| `LOCAL/smallogo.gif` | 2 | 0 |
| `ANIMATED/perlorangeblink.gif` | 0 | 1 |

### No reference found (24)

`action_delete.png`, `alliance_hexes2.png`, `alliance_hexes3.png`,
`ALLIANCE-logo-nobackground_foundingmember.png`, `betterfish.jpg`, `clone1.png`,
`darrow.gif`, `delete-attribution.png`, `edit.png`, `feed-icon-28x28.png`,
`help.gif`, `info.gif`, `new-attribution.png`, `new1.gif`,
`popup-link-icon-hover.png`, `popup-link-icon.png`, `search-background.png`,
`test_zfin_org_header.png`, `toggle.gif`, `zdbhome-background.jpg`,
`zfin-logo-optimized.svg`, `zfin-logo.svg`, `zfinlabs_zfin_org_header.png`,
`zfinlinkout.png`

**Do not treat this list as proven-dead.** The grep only covers the two source
trees and matches on the `images/` prefix. Anything referenced from DB-stored
content, another repo, or by bare filename would not appear. `zfin-logo.svg` and
`zfin-logo-optimized.svg` land in this bucket despite being *deliberately*
retained in `zfin-static` commit `6fdab30` ("images: retain zfin-logo.svg +
zfin-logo-optimized.svg"), which is direct evidence that at least some have
references this method can't see. Audit each before deleting.

## Two related defects found along the way

### 1. Six byte-identical duplicate files

These exist in both `frontend/images/` (relative-referenced from `frontend/css`,
so webpack content-hashes them into `/dist`) and `zfin-static/src/images/`
(served at `/images/…`). Verified byte-identical with `cmp`:

`external.png`, `tabs.png`, `sort-arrow-up-selected.png`,
`sort-arrow-up-unselected.png`, `sort-arrow-down-selected.png`,
`sort-arrow-down-unselected.png`

Origin: commit `c2fb3153dc` ("frontend: fix broken image refs + optimize search
background") added copies under `home/images/`, which later became
`frontend/images/` in `7011c08553`. The sort arrows are referenced *both* ways —
`frontend/css/searchresults.css` (relative) and
`home/WEB-INF/jsp/reno/candidate-inqueue.jsp` (absolute `/images/…`) — which is
why both copies are load-bearing today.

### 2. `search-background.png` is 1.4 MB of the 2.5 MB `images/` tree, and is dead

`c2fb3153dc` replaced it with `search-background.avif` (39 KB) +
`search-background.webp` (51 KB), referenced from `frontend/css/zdbhome.scss:24-27`
via `image-set()`. Nothing references the PNG. It is **56% of the entire
`images/` tree by size** and can almost certainly go.

## Recommended fix

Move `images/` into `docroot/images/` in this repo and drop it from the
`zfin-static` release. Mechanically the same change as `87f33d121a`:

1. `git mv` the tree from `zfin-static/src/images` to this repo's `docroot/images`.
2. No Gradle change needed — `docroot;deployFiles` already copies the whole
   `docroot/` tree to `$TARGETROOT/home`, so `docroot/images/x.png` → `/images/x.png`
   with no new task.
3. Cut a `zfin-static` release without `images/` and bump `zfinStaticVersion`.
   Both repos must land together: the release tag must exist before `make` will
   succeed, and the docroot name must not be owned by both deploy tasks at once
   (`linkEntriesIntoDocroot` does `rm -rf` then `ln -s` per top-level entry).
4. Update the enumerations that list the release's top-level entries:
   `build.gradle` (the `zfinStaticRepo` block and the `home;static;deployFromRelease`
   header), `docker/httpd/conf-local`, and in `zfin-static`: `README.md`,
   `scripts/copy-assets.mjs`, `scripts/package.mjs`.

The 6 static-only images can move too — the static pages request `/images/…`
from the same origin either way, so they don't care which tree serves it. Keeping
them behind in `zfin-static` would split one URL space (`/images/`) across two
trees, which is precisely the "one URL space, two homes" problem this refactor
set out to eliminate.

Worth folding in while the tree is being touched: delete
`search-background.png`, audit the other 23 no-reference files, and collapse the
6 duplicates onto one copy (point `frontend/css` at `../../docroot/images/`, or
keep the webpack copies and convert the absolute `/images/…` JSP references to
use the bundled asset).

## Verification

Any change here is a broken-image risk with no compile-time check, so verify by
request rather than by inspection:

- Hit a BLAST result, a search-results page with sort arrows, an expression image
  gallery, a facet sidebar, and the home page background.
- Check the header and footer logos on both a dynamic `/action` page and a static
  `/zf_info/…` page.
- Diff the `/images/…` request set before and after: crawl a page sample with
  the browser devtools network panel filtered to images and compare 404s.

## What is *not* in scope

`zfin-static/src/ZFIN/` (23 legacy help and `misc_html` pages) is correctly
placed — frozen archival HTML, same class as `zf_info`. Leave it.
