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
            assert.equal(h.latest().deletedSequence, 'ACGTACGT');
        });
        assert.ok(screen.getByText('8 bp'), 'expected a live base count');
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

    it('honours a per-Control alphabet that widens the default', async () => {
        // Default is strict ACGT; a Control can opt back into N.
        const h = renderForm({
            schema: stringSchema('crisprSequence'),
            uischema: control('nucleotideSequence', 'crisprSequence', { alphabet: 'ACGTN' }),
            data: { crisprSequence: '' },
        });
        fireEvent.change(screen.getByLabelText('crisprSequence'), { target: { value: 'ACGTN' } });
        await waitFor(() => {
            assert.equal(h.latest().crisprSequence, 'ACGTN');
        });
        h.cleanupFetch();
    });

    it('drops N when the Control does not widen the alphabet', async () => {
        const h = renderForm({
            schema: stringSchema('deletedSequence'),
            uischema: control('nucleotideSequence', 'deletedSequence'),
            data: { deletedSequence: '' },
        });
        fireEvent.change(screen.getByLabelText('deletedSequence'), { target: { value: 'ACGTN' } });
        await waitFor(() => {
            assert.equal(h.latest().deletedSequence, 'ACGT');
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

describe('attachmentsList widget', () => {
    // The two buckets differ only by uischema options, so these assert that
    // each option actually reaches the DOM / the request — a dropped option
    // would look identical to the untouched bucket on screen.
    const schema = {
        type: 'object',
        properties: {
            protocolDocuments: {
                type: 'array',
                title: 'Protocol Documentation',
                maxItems: 10,
                items: { type: 'object', properties: { id: { type: 'number' } } },
            },
        },
    };
    const uischema = control('attachmentsList', 'protocolDocuments', {
        label: 'Protocol Documentation',
        attachmentKind: 'protocol_doc',
        accept: '.pdf,.docx,.doc,.txt,.rtf,.odt',
        helpText: 'Accepted file types: .pdf, .docx, .doc, .txt, .rtf, .odt.',
    });

    it('shows the bucket heading, the accepted types and a matching picker filter', () => {
        const h = renderForm({
            schema,
            uischema,
            data: { protocolDocuments: [] },
            config: { assayId: 7 },
        });

        assert.ok(screen.getByText('Protocol Documentation'), 'expected the bucket heading');
        assert.ok(
            screen.getByText('Accepted file types: .pdf, .docx, .doc, .txt, .rtf, .odt.'),
            'expected the helper text listing accepted types',
        );
        const input = h.container.querySelector('input[type=file]') as HTMLInputElement;
        assert.equal(input.getAttribute('accept'), '.pdf,.docx,.doc,.txt,.rtf,.odt');
        h.cleanupFetch();
    });

    it('posts the bucket kind alongside the file so the server files it correctly', async () => {
        const h = renderForm({
            schema,
            uischema,
            data: { protocolDocuments: [] },
            config: { assayId: 7 },
        });
        // renderForm's stub only understands the vocabulary endpoint; swap in
        // one that records the upload. Restored by cleanupFetch below.
        let seen: { url: string; body: FormData } | null = null;
        globalThis.fetch = (async (url: RequestInfo | URL, init?: RequestInit) => {
            seen = { url: String(url), body: init?.body as FormData };
            return new Response(JSON.stringify({ id: 7, protocolDocuments: [] }), {
                status: 200,
                headers: { 'Content-Type': 'application/json' },
            });
        }) as typeof globalThis.fetch;

        const input = h.container.querySelector('input[type=file]') as HTMLInputElement;
        const file = new File(['protocol'], 'protocol.docx', { type: 'application/octet-stream' });
        fireEvent.change(input, { target: { files: [file] } });

        await waitFor(() => {
            assert.ok(seen, 'expected an upload request');
        });
        assert.match(seen!.url, /\/assays\/7\/attachments$/);
        assert.equal(seen!.body.get('kind'), 'protocol_doc');
        assert.equal((seen!.body.get('file') as File).name, 'protocol.docx');
        h.cleanupFetch();
    });

    it('surfaces the server\'s reason for a rejected file, not just the status', async () => {
        // The point of validating the bucket is telling the curator which
        // types are accepted; RFC 7807 puts that in `detail` and the generic
        // status label in `title`, so a client that shows `title` reduces
        // every rejection to an unactionable "Bad Request".
        const h = renderForm({
            schema,
            uischema,
            data: { protocolDocuments: [] },
            config: { assayId: 7 },
        });
        globalThis.fetch = (async () => new Response(JSON.stringify({
            type: 'https://zfin.org/problems/bad-request',
            title: 'Bad Request',
            status: 400,
            detail: 'Protocol documentation must be one of: .pdf, .docx, .doc, .txt, .rtf, .odt',
        }), {
            status: 400,
            headers: { 'Content-Type': 'application/problem+json' },
        })) as typeof globalThis.fetch;

        const input = h.container.querySelector('input[type=file]') as HTMLInputElement;
        fireEvent.change(input, {
            target: { files: [new File(['x'], 'gel.png', { type: 'image/png' })] },
        });

        await waitFor(() => {
            assert.ok(
                screen.getByText(/must be one of: \.pdf, \.docx/),
                'expected the accepted-types list in the error',
            );
        });
        assert.equal(screen.queryByText('Bad Request'), null);
        h.cleanupFetch();
    });

    it('omits kind when the bucket declares none, letting the server default', async () => {
        const h = renderForm({
            schema: {
                type: 'object',
                properties: { attachments: { type: 'array', items: { type: 'object' } } },
            },
            uischema: control('attachmentsList', 'attachments', { label: 'Annotated gel images' }),
            data: { attachments: [] },
            config: { assayId: 7 },
        });
        let seen: FormData | null = null;
        globalThis.fetch = (async (_url: RequestInfo | URL, init?: RequestInit) => {
            seen = init?.body as FormData;
            return new Response(JSON.stringify({ id: 7, attachments: [] }), {
                status: 200,
                headers: { 'Content-Type': 'application/json' },
            });
        }) as typeof globalThis.fetch;

        const input = h.container.querySelector('input[type=file]') as HTMLInputElement;
        fireEvent.change(input, {
            target: { files: [new File(['x'], 'gel.png', { type: 'image/png' })] },
        });

        await waitFor(() => {
            assert.ok(seen, 'expected an upload request');
        });
        assert.equal(seen!.get('kind'), null);
        h.cleanupFetch();
    });
});
