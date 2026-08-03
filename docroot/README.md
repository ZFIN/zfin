# docroot/

Files served verbatim by Apache from the site root. Path on disk = path in the
URL: `docroot/robots.txt` is served as `/robots.txt`.

`make` copies this tree into the Apache DocumentRoot (`$TARGETROOT/home`) via the
`docroot;deployFiles` Gradle task. Nothing here is compiled, bundled, hashed, or
templated — if a file needs any of that, it belongs somewhere else:

| If the file is…                                  | It goes in…                        |
|--------------------------------------------------|------------------------------------|
| served as-is at a fixed URL                      | here                               |
| source compiled/bundled into `/dist` by webpack  | `frontend/{css,javascript,images}` |
| frozen archival content (`/zf_info`, `/ZFIN`)    | the `zfin-static` repo             |
| generated per-instance at deploy or runtime      | a Gradle task / the app           |

These files live here rather than in `zfin-static` because they belong to the
*application*, not to the archival content: the running app references
`/analytics.js` and `/favicon.ico` directly, and `robots.txt` changes for app
reasons (a new `Disallow:` for an expensive `/action` route). Keeping them here
means shipping such a change is an ordinary app deploy, not a tagged release of
a 322 MB static-content repo.
