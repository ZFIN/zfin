// What's left of FormPrimitives.tsx after the schema-driven renderer
// took over rendering and Autocomplete moved into form-renderer/.
//
// Just one helper now: valueToInputString, kept here because the
// existing lineSubmission test suite imports it. The renderer has its
// own internal valueAsInputString; this export survives as a stable
// seam for unit tests.

/**
 * Render a value (string | boolean | null | undefined) into the string
 * representation a controlled `<input>` / `<textarea>` / radio expects.
 * Bools become `'true'` / `'false'`; null / undefined become `''`.
 */
export function valueToInputString(v: string | boolean | null | undefined): string {
    if (v === null || v === undefined) {
        return '';
    }
    if (typeof v === 'boolean') {
        return v ? 'true' : 'false';
    }
    return v;
}
