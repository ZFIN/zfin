# React 18 upgrade (2026-04-03)

## What changed

Upgraded React from 16.14.0 to 18.2.0. This is a major version jump (skipping 17)
but the codebase was in good shape — no deprecated lifecycle methods, no string refs,
no legacy context API.

### ReactDOM.render() → createRoot()

React 18 replaces the old `ReactDOM.render()` entry point with `createRoot()`.
Only 2 call sites needed updating:

- **`home/javascript/react/index.js`** — The main entry point that mounts all React
  components on the page. Scans for `.__react-root` divs and dynamically imports
  the matching container component.

- **`home/javascript/react/containers/QuickFigure.js`** — Renders a dialog into
  a jQuery popover. Stores the root ref so it can be reused without calling
  `createRoot()` twice on the same element.

### Event delegation: document → root container

React 16 attached all event listeners to `document`. React 18 attaches them to the
root container element passed to `createRoot()` (the `.__react-root` div).

**This breaks any pattern where a third-party library moves React-managed DOM elements
outside the React root.** In this codebase, the jQuery Modal plugin moves `.jq-modal`
elements into a blocker overlay appended to `<body>`. Once moved, click/submit events
on buttons inside the modal no longer bubble through the React root, so React `onClick`
and `onSubmit` handlers silently stop firing.

**Fix:** The `Modal` component (`home/javascript/react/components/Modal.js`) was
rewritten to use `ReactDOM.createPortal()`. The portal container element is what jQuery
Modal operates on. React attaches event listeners directly to portal containers, so
when jQuery moves the container into its blocker overlay, the listeners travel with it
and synthetic events continue to work.

**If you add new jQuery/third-party UI that moves React-rendered DOM outside the React
root, you will hit this same issue.** Use `createPortal` to keep React's event system
intact.

### react-form replacement

`react-form@4.0.1` was the only blocking dependency — it's unmaintained (last release
2020) and hard-pegged to React 16 via `peerDependencies`. Rather than migrating to
`@tanstack/react-form` (completely different API requiring a rewrite of all 12 consumer
files), we created a lightweight drop-in replacement:

**`home/javascript/react/hooks/useFormLite.js`** (~170 lines)

Provides the exact same API surface used by this codebase:
- `useForm({ defaultValues, onSubmit })` → `{ Form, values, meta, setMeta, reset, ... }`
- `useField(field, fieldOptions)` → `{ value, meta, getInputProps, setValue }`
- `splitFormProps(props)` → `[field, fieldOptions, rest]`

The implementation was built by reading the actual react-form source code and matching
only the features this codebase uses. It does not attempt to replicate the full
react-form library.

**Key implementation details:**

- **Null defaultValues:** When `defaultValues` is `null` (e.g., no modal item selected),
  `values` must also be `null` — not `{}`. All modal-based components use `values && <>`
  guards that depend on this. Spreading `{...null}` produces `{}` which is truthy.

- **Stable Form component:** The `Form` component returned by `useForm` must have a
  stable identity across renders. If created via `useCallback` with changing deps, React
  treats each new function as a different component type and unmounts/remounts the entire
  form subtree on every state change. The fix uses `useState(() => function Form(...){})` 
  with refs to read the latest context and submit handler.

**Files updated to use useFormLite (12 total):**
- `hooks/useAddEditDeleteForm.js`
- `components/form/InputField.js`
- `components/form/InputCheckbox.js`
- `components/marker-edit/MarkerNameForm.js`
- `components/marker-edit/EditOrthologyNote.js`
- `components/marker-edit/EditOrthologyEvidenceCell.js`
- `components/marker-edit/MarkerPublicNoteForm.js`
- `containers/MarkerEditCloneData.js`
- `containers/SequenceTargetingReagentEditSequence.js`
- `containers/TranscriptEditAttributes.js`
- `containers/NewSequenceTargetingReagentForm.js`
- `containers/AntibodyEditDetails.js`

### react-draggable added

`react-draggable` was added as an explicit dependency. It's a transitive dependency
of `@jbrowse/plugin-variants` that wasn't being resolved correctly, causing a webpack
build error. This was a pre-existing issue masked by React 16's more lenient peer
dependency resolution.

## What didn't change

- **16 class components** — Class components are fully supported in React 18. No
  migration to function components was needed.
- **Files using `defaultProps`** — Still works in React 18 but triggers console
  warnings. `LoadingSpinner` was migrated to default parameters; remaining files
  can be migrated as encountered.
- **JSP mount points** — The `.__react-root` div pattern works identically.
- **All other dependencies** — react-dropzone, react-timeago, @jbrowse, immer all
  support React 18 already.

## Dependency compatibility matrix

| Package | Version | React 18 compatible | Notes |
|---------|---------|-------------------|-------|
| react-dropzone | 14.2.3 | Yes | peer: `>= 16.8 \|\| 18.0.0` |
| react-timeago | 7.1.0 | Yes | peer: `^16 \|\| ^17 \|\| ^18` |
| @jbrowse/react-app | 2.18.0 | Yes | peer: `>=16.8.0` |
| @jbrowse/react-linear-genome-view | 2.18.0 | Yes | peer: `>=16.8.0` |
| immer | 9.0.16 | Yes | No React peer dep |
| react-form | 4.0.1 | **No** | Replaced with useFormLite |

## Testing

- `npm test` — passes
- `npm run lint` — passes
- `npm run build` (webpack) — compiles with no errors

### Manual testing checklist

These pages use the replaced react-form and should be verified in the browser.
URLs assume the app is running at the default base (e.g., `https://zfin.org`).
Replace ZDB IDs with real values from your database.

**react-form replacement (highest risk):**

- [ ] Marker edit — `/action/marker/clone/edit/{ZDB-GENE-ID}`
  Tests: MarkerNameForm, MarkerEditCloneData, MarkerPublicNoteForm
- [ ] Antibody edit — `/action/marker/antibody/edit/{ZDB-ATB-ID}`
  Tests: AntibodyEditDetails
- [ ] Transcript edit — `/action/marker/transcript/edit/{ZDB-TSCRIPT-ID}`
  Tests: TranscriptEditAttributes
- [ ] STR edit — `/action/marker/str/edit/{ZDB-MRPHLNO-ID}`
  Tests: SequenceTargetingReagentEditSequence
- [ ] STR add — `/action/marker/sequence-targeting-reagent-add`
  Tests: NewSequenceTargetingReagentForm
- [ ] Orthology note/evidence editing (accessed from marker edit pages)
  Tests: EditOrthologyNote, EditOrthologyEvidenceCell

**createRoot() migration:**

- [ ] QuickFigure popover — `/action/curation/{ZDB-PUB-ID}` (click the quick-add figure button)
- [ ] All React components mount correctly (every `.__react-root` div on every page)

**Smoke tests (should work unchanged but verify):**

- [ ] Publication tracker — `/action/publication/{ZDB-PUB-ID}/track`
- [ ] Publication dashboard — `/action/publication/dashboard`
- [ ] JBrowse genome viewer — `/jbrowse2`
- [ ] Construct curation — `/action/curation/{ZDB-PUB-ID}` (construct tab)

## Future: React 19

When upgrading to React 19, the main changes would be:
- Remove remaining `defaultProps` usage (use default parameters instead)
- `forwardRef` no longer needed (ref becomes a regular prop)
- `useContext` replaced by `use(Context)`
- Class components still work but are officially discouraged
