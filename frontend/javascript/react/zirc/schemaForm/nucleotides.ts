/**
 * Pure helpers behind the `nucleotideSequence` widget. Kept out of the
 * renderer so the fiddly parts — what survives normalization, and where the
 * caret lands afterwards — are testable without a DOM.
 */

/**
 * Accepted bases when a Control doesn't name its own alphabet.
 *
 * The lesion schema names one on every sequence Control, so this is a
 * fallback rather than the operative value — kept in step with
 * ZircLesionFormSchema.NUCLEOTIDE_ALPHABET so an un-annotated Control
 * behaves the same as an annotated one.
 */
export const DEFAULT_ALPHABET = 'ACGT';

/**
 * Reduce raw input to bases: drop FASTA description lines, uppercase the
 * rest, then drop everything outside the alphabet.
 *
 * Dropping rather than rejecting is what makes paste work. Sequence arrives
 * from all sorts of places — FASTA records, sequencing output numbered in
 * the left margin, anything space-grouped in tens — and each normalizes to
 * the bases a curator meant to paste. The cost is that a genuinely wrong
 * character disappears silently rather than announcing itself; the live bp
 * count next to the field is what makes that visible, since a paste that
 * lost content reads short.
 *
 * FASTA headers need removing as a *line*, before the per-character filter,
 * because a description is prose and prose contains bases: ">seq1
 * description here" leaves behind C, T and N once everything else is
 * stripped, silently prepending three junk bases to the sequence.
 */
export function normalizeSequence(raw: string, alphabet: string = DEFAULT_ALPHABET): string {
    const accepted = alphabet.toUpperCase();
    let out = '';
    for (const line of raw.split(/\r?\n/)) {
        // ">" is the FASTA description marker; ";" is the older comment form.
        if (/^\s*[>;]/.test(line)) {continue;}
        for (const ch of line.toUpperCase()) {
            if (accepted.indexOf(ch) !== -1) {out += ch;}
        }
    }
    return out;
}

/**
 * Where the caret belongs after normalization: the number of characters
 * before the old caret position that survived.
 *
 * Without this the input is unusable for anything but appending. The value
 * is controlled, so React re-renders it from the normalized string, and a
 * browser puts the caret at the end of a programmatically-changed value.
 * Typing a lowercase base mid-sequence changes the string (it uppercases),
 * so the caret would jump to the end on nearly every keystroke — lowercase
 * input is normal, not an edge case.
 */
export function caretAfterNormalize(
    raw: string,
    caret: number,
    alphabet: string = DEFAULT_ALPHABET,
): number {
    return normalizeSequence(raw.slice(0, caret), alphabet).length;
}

/**
 * Base count for display. Counts the normalized string, so it always agrees
 * with what is stored rather than with what was typed.
 *
 * Deliberately narrower than AutoSizeRenderer's `[A-Za-z]` tally, which
 * measures already-saved values that predate this widget and may hold
 * anything. Both count the same thing once a field has been through here.
 */
export function baseCount(raw: string, alphabet: string = DEFAULT_ALPHABET): number {
    return normalizeSequence(raw, alphabet).length;
}
