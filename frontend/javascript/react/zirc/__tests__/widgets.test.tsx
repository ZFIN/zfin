import { describe, it, afterEach } from 'node:test';
import assert from 'node:assert/strict';
import { cleanup, screen, waitFor, fireEvent } from '@testing-library/react';
import {
    AMINO_ACIDS,
    TRANSCRIPT_CONSEQUENCES,
    renderForm,
    stringSchema,
} from './renderHelpers';

/**
 * Behavioural coverage for the widgets added in Phase 0 of the lesion-form
 * batch. These drive a real <JsonForms>, so a renderer is only reached if its
 * tester matches — the same dispatch the app uses.
 *
 * Complements widgetRegistry.test.ts, which checks that widgets declared by
 * the server are registered at all. This checks that they behave once they
 * are.
 */

afterEach(() => {
    cleanup();
});

const control = (widget: string, scope: string, options: Record<string, unknown> = {}) => ({
    type: 'VerticalLayout',
    elements: [{ type: 'Control', scope: `#/properties/${scope}`, options: { widget, ...options } }],
});

describe('nucleotideSequence widget', () => {
    it('normalizes typed input to bases and shows the count', async () => {
        const h = renderForm({
            schema: stringSchema('deletedSequence'),
            uischema: control('nucleotideSequence', 'deletedSequence'),
            data: { deletedSequence: '' },
        });
        const input = screen.getByLabelText('deletedSequence') as HTMLInputElement;

        fireEvent.change(input, { target: { value: 'acgt 123 nnxx acgt' } });

        await waitFor(() => {
            assert.equal(h.latest().deletedSequence, 'ACGTNNACGT');
        });
        assert.ok(screen.getByText('10 bp'), 'expected a live base count');
        h.cleanupFetch();
    });

    it('drops a FASTA header rather than mining it for bases', async () => {
        const h = renderForm({
            schema: stringSchema('deletedSequence'),
            uischema: control('nucleotideSequence', 'deletedSequence', { multi: true }),
            data: { deletedSequence: '' },
        });
        const input = screen.getByLabelText('deletedSequence') as HTMLTextAreaElement;

        fireEvent.change(input, { target: { value: '>seq1 description here\nACGT' } });

        await waitFor(() => {
            // "description" contributes C, T and N to a per-character filter.
            assert.equal(h.latest().deletedSequence, 'ACGT');
        });
        h.cleanupFetch();
    });

    it('honours a custom alphabet', async () => {
        const h = renderForm({
            schema: stringSchema('crisprSequence'),
            uischema: control('nucleotideSequence', 'crisprSequence', { alphabet: 'ACGT' }),
            data: { crisprSequence: '' },
        });
        fireEvent.change(screen.getByLabelText('crisprSequence'), { target: { value: 'ACGTN' } });
        await waitFor(() => {
            assert.equal(h.latest().crisprSequence, 'ACGT');
        });
        h.cleanupFetch();
    });
});

describe('vocabularySelect widget', () => {
    it('offers the vocabulary with amino-acid abbreviations and stores the term id', async () => {
        const h = renderForm({
            schema: stringSchema('aaChangeFrom'),
            uischema: control('vocabularySelect', 'aaChangeFrom', { vocabulary: 'amino_acid_term' }),
            data: { aaChangeFrom: '' },
            vocabularies: { amino_acid_term: AMINO_ACIDS },
        });

        await waitFor(() => {
            assert.ok(screen.getByRole('option', { name: 'Ala [A]' }));
        });
        // "Stop" has no abbreviation, so it stays a bare label.
        assert.ok(screen.getByRole('option', { name: 'Stop' }));

        fireEvent.change(screen.getByLabelText('aaChangeFrom'), { target: { value: 'ZDB-TERM-2' } });
        await waitFor(() => {
            assert.equal(h.latest().aaChangeFrom, 'ZDB-TERM-2');
        });
        h.cleanupFetch();
    });

    it('keeps an option for a stored value the vocabulary no longer offers', async () => {
        const h = renderForm({
            schema: stringSchema('aaChangeFrom'),
            uischema: control('vocabularySelect', 'aaChangeFrom', { vocabulary: 'amino_acid_term' }),
            data: { aaChangeFrom: 'ZDB-TERM-RETIRED' },
            vocabularies: { amino_acid_term: AMINO_ACIDS },
        });

        await waitFor(() => {
            assert.ok(screen.getByRole('option', { name: /unrecognized/ }));
        });
        // The select must still reflect the stored value, not silently show
        // the placeholder while the field holds something.
        assert.equal((screen.getByLabelText('aaChangeFrom') as HTMLSelectElement).value,
            'ZDB-TERM-RETIRED');
        h.cleanupFetch();
    });

    it('disables itself and says so when the vocabulary fails to load', async () => {
        const h = renderForm({
            schema: stringSchema('aaChangeFrom'),
            uischema: control('vocabularySelect', 'aaChangeFrom', { vocabulary: 'amino_acid_term' }),
            data: { aaChangeFrom: '' },
            // No fixtures -> the stub returns 500.
        });

        await waitFor(() => {
            assert.ok(screen.getByText(/Could not load/));
        });
        assert.equal((screen.getByLabelText('aaChangeFrom') as HTMLSelectElement).disabled, true);
        h.cleanupFetch();
    });
});

describe('vocabularyMultiSelect widget', () => {
    const uischema = {
        type: 'VerticalLayout',
        elements: [{
            type: 'Control',
            scope: '#/properties/transcriptConsequences',
            options: { widget: 'vocabularyMultiSelect', vocabulary: 'transcript_consequence_term' },
        }],
    };
    const schema = {
        type: 'object',
        properties: {
            transcriptConsequences: {
                type: 'array',
                title: 'transcriptConsequences',
                items: { type: 'string' },
            },
        },
    };

    it('adds a term, stores an array of ids, and stops offering it', async () => {
        const h = renderForm({
            schema,
            uischema,
            data: { transcriptConsequences: [] },
            vocabularies: { transcript_consequence_term: TRANSCRIPT_CONSEQUENCES },
        });

        await waitFor(() => {
            assert.ok(screen.getByRole('option', { name: 'premature stop' }));
        });
        fireEvent.change(screen.getByLabelText('transcriptConsequences'),
            { target: { value: 'ZDB-TERM-10' } });
        fireEvent.click(screen.getByRole('button', { name: '+ Add' }));

        await waitFor(() => {
            assert.deepEqual(h.latest().transcriptConsequences, ['ZDB-TERM-10']);
        });
        // Already chosen, so it drops out of the picker.
        assert.equal(screen.queryByRole('option', { name: 'premature stop' }), null);
        h.cleanupFetch();
    });

    it('removes a chosen term', async () => {
        const h = renderForm({
            schema,
            uischema,
            data: { transcriptConsequences: ['ZDB-TERM-10', 'ZDB-TERM-11'] },
            vocabularies: { transcript_consequence_term: TRANSCRIPT_CONSEQUENCES },
        });

        await waitFor(() => {
            assert.ok(screen.getByRole('button', { name: 'Remove premature stop' }));
        });
        fireEvent.click(screen.getByRole('button', { name: 'Remove premature stop' }));

        await waitFor(() => {
            assert.deepEqual(h.latest().transcriptConsequences, ['ZDB-TERM-11']);
        });
        h.cleanupFetch();
    });
});

describe('aminoAcidChange widget', () => {
    const schema = {
        type: 'object',
        properties: {
            aaChangeFrom: { type: 'string', title: 'Amino Acid Change' },
            aaChangeTo: { type: 'string' },
            aaPosition: { type: 'integer' },
        },
    };
    const uischema = control('aminoAcidChange', 'aaChangeFrom', {
        vocabulary: 'amino_acid_term',
        toField: 'aaChangeTo',
        positionField: 'aaPosition',
    });

    it('writes the named sibling fields, not just the bound one', async () => {
        const h = renderForm({
            schema,
            uischema,
            data: { aaChangeFrom: '', aaChangeTo: '', aaPosition: null },
            vocabularies: { amino_acid_term: AMINO_ACIDS },
        });

        // Wait for an option, not merely for the select: both residue selects
        // are disabled until the vocabulary resolves, and fireEvent.change on
        // a disabled control is a silent no-op.
        await waitFor(() => {
            assert.ok(screen.getAllByRole('option', { name: 'Ala [A]' }).length > 0);
        });

        fireEvent.change(screen.getByLabelText('Amino Acid Change from'),
            { target: { value: 'ZDB-TERM-2' } });
        await waitFor(() => {
            assert.equal(h.latest().aaChangeFrom, 'ZDB-TERM-2');
        });

        fireEvent.change(screen.getByLabelText('Amino Acid Change to'),
            { target: { value: 'ZDB-TERM-3' } });
        await waitFor(() => {
            assert.equal(h.latest().aaChangeTo, 'ZDB-TERM-3');
        });

        fireEvent.change(screen.getByLabelText('Amino Acid Change position'), { target: { value: '12' } });
        await waitFor(() => {
            assert.equal(h.latest().aaPosition, 12);
        });

        // The point of the widget: one Control, three fields written, and the
        // earlier writes survive the later ones.
        assert.equal(h.latest().aaChangeFrom, 'ZDB-TERM-2');
        assert.equal(h.latest().aaChangeTo, 'ZDB-TERM-3');
        h.cleanupFetch();
    });

    it('writes a position range when an end field is configured', async () => {
        const rangeSchema = {
            type: 'object',
            properties: {
                aaChangeFrom: { type: 'string', title: 'Amino Acid Change' },
                aaChangeTo: { type: 'string' },
                aaPositionStart: { type: 'integer' },
                aaPositionEnd: { type: 'integer' },
            },
        };
        const rangeUi = control('aminoAcidChange', 'aaChangeFrom', {
            vocabulary: 'amino_acid_term',
            toField: 'aaChangeTo',
            positionField: 'aaPositionStart',
            positionEndField: 'aaPositionEnd',
        });
        const h = renderForm({
            schema: rangeSchema,
            uischema: rangeUi,
            data: { aaChangeFrom: '', aaChangeTo: '', aaPositionStart: null, aaPositionEnd: null },
            vocabularies: { amino_acid_term: AMINO_ACIDS },
        });

        await waitFor(() => {
            assert.ok(screen.getByLabelText('Amino Acid Change position end'));
        });
        fireEvent.change(screen.getByLabelText('Amino Acid Change position'),
            { target: { value: '12' } });
        await waitFor(() => {
            assert.equal(h.latest().aaPositionStart, 12);
        });
        fireEvent.change(screen.getByLabelText('Amino Acid Change position end'),
            { target: { value: '15' } });
        await waitFor(() => {
            assert.equal(h.latest().aaPositionEnd, 15);
        });
        assert.equal(h.latest().aaPositionStart, 12);
        h.cleanupFetch();
    });

    it('renders a single position box when no end field is configured', async () => {
        // The end is opt-in, so a form that wants one coordinate gets one box
        // rather than a range with a permanently empty half.
        const h = renderForm({
            schema,
            uischema,
            data: { aaChangeFrom: '', aaChangeTo: '', aaPosition: null },
            vocabularies: { amino_acid_term: AMINO_ACIDS },
        });
        await waitFor(() => {
            assert.ok(screen.getByLabelText('Amino Acid Change position'));
        });
        assert.equal(screen.queryByLabelText('Amino Acid Change position end'), null);
        h.cleanupFetch();
    });

    it('clears the position to null rather than NaN when emptied', async () => {
        const h = renderForm({
            schema,
            uischema,
            data: { aaChangeFrom: '', aaChangeTo: '', aaPosition: 7 },
            vocabularies: { amino_acid_term: AMINO_ACIDS },
        });

        await waitFor(() => {
            assert.ok(screen.getByLabelText('Amino Acid Change position'));
        });
        fireEvent.change(screen.getByLabelText('Amino Acid Change position'), { target: { value: '' } });

        await waitFor(() => {
            assert.equal(h.latest().aaPosition, null);
        });
        h.cleanupFetch();
    });
});
