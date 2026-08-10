import { describe, it } from 'node:test';
import assert from 'node:assert/strict';
import {
    DEFAULT_ALPHABET,
    baseCount,
    caretAfterNormalize,
    normalizeSequence,
} from '../schemaForm/nucleotides';

describe('normalizeSequence', () => {
    it('uppercases', () => {
        assert.equal(normalizeSequence('acgt'), 'ACGT');
    });

    it('drops N by default', () => {
        // N was accepted until curator feedback asked for strict bases only.
        assert.equal(normalizeSequence('ACNGT'), 'ACGT');
    });

    it('drops characters outside the alphabet', () => {
        assert.equal(normalizeSequence('ACGT-XYZ-ACGT'), 'ACGTACGT');
    });

    it('flattens a pasted FASTA record to bases', () => {
        const fasta = '>seq1 description here\nACGTACGT\nGGTTAACC\n';
        assert.equal(normalizeSequence(fasta), 'ACGTACGTGGTTAACC');
    });

    it('drops the FASTA header as a line, not character by character', () => {
        // Regression guard. "description" contributes C, T and N to the
        // per-character filter, so a header stripped only by the alphabet
        // filter would silently prepend three junk bases.
        assert.equal(normalizeSequence('>seq1 description here\nACGT'), 'ACGT');
        assert.equal(normalizeSequence('; old-style comment\nACGT'), 'ACGT');
    });

    it('strips the whitespace and digits of numbered sequencing output', () => {
        assert.equal(normalizeSequence('   1  acgt acgt\n  9  ggtt aacc'), 'ACGTACGTGGTTAACC');
    });

    it('honours a custom alphabet', () => {
        // The alphabet is per-Control, so a field that does want N can say so.
        assert.equal(normalizeSequence('ACGTN', 'ACGTN'), 'ACGTN');
    });

    it('returns empty for input with no bases', () => {
        assert.equal(normalizeSequence('---'), '');
    });
});

describe('caretAfterNormalize', () => {
    it('keeps the caret in place when nothing is stripped', () => {
        // "ACG|T" -> caret stays after three surviving characters.
        assert.equal(caretAfterNormalize('ACGT', 3), 3);
    });

    it('keeps the caret in place when a lowercase base is typed mid-sequence', () => {
        // "AC" + typed "g" + "T", caret after the g. Uppercasing changes the
        // string, so without this the caret would jump to the end on a
        // perfectly ordinary keystroke.
        assert.equal(caretAfterNormalize('ACgT', 3), 3);
    });

    it('pulls the caret back past stripped characters', () => {
        // "AC-G|T": the hyphen before the caret disappears, so the caret
        // lands after three characters, not four.
        assert.equal(caretAfterNormalize('AC-GT', 4), 3);
    });

    it('handles a caret at position zero', () => {
        assert.equal(caretAfterNormalize('ACGT', 0), 0);
    });

    it('handles a caret at the end after a paste', () => {
        assert.equal(caretAfterNormalize('ac gt', 5), 4);
    });
});

describe('baseCount', () => {
    it('counts only what survives normalization', () => {
        assert.equal(baseCount('ACGT ACGT'), 8);
        assert.equal(baseCount('>hdr\nACGT'), 4);
    });

    it('is zero for empty input', () => {
        assert.equal(baseCount(''), 0);
    });

    it('defaults to the ACGT alphabet', () => {
        assert.equal(DEFAULT_ALPHABET, 'ACGT');
        assert.equal(baseCount('ACGTN'), 4);
    });
});
