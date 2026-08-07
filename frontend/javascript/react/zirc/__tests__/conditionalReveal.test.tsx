import { describe, it, afterEach } from 'node:test';
import assert from 'node:assert/strict';
import * as fs from 'node:fs';
import * as path from 'node:path';
import { cleanup, screen, waitFor } from '@testing-library/react';
import { renderForm, TRANSCRIPT_CONSEQUENCES } from './renderHelpers';

/**
 * Reveal rules on the real lesion form, driven from the committed schema
 * snapshot rather than a hand-written fragment — so this cannot pass while
 * the server emits something different.
 *
 * The case worth pinning down is ZFIN-10400's AND condition. The follow-up
 * fields are gated on the lesion type *and* the answer, because a rule on the
 * answer alone leaks: the boolean survives a later change of lesion type, so
 * a CRISPR box would reappear under a deletion. That leak is invisible in the
 * common path and only shows up when a curator changes their mind about the
 * type, which is exactly the kind of thing that reaches production.
 */

const SNAPSHOT = path.resolve(
    __dirname, '../../../../../test/resources/zirc/snapshot/lesion.form-schema.json',
);

function lesionForm(data: Record<string, unknown>) {
    const parsed = JSON.parse(fs.readFileSync(SNAPSHOT, 'utf8'));
    return renderForm({
        schema: parsed.schema,
        uischema: parsed.uiSchema,
        data,
        vocabularies: { transcript_consequence_term: TRANSCRIPT_CONSEQUENCES },
    });
}

afterEach(() => {
    cleanup();
});

describe('lesion conditional reveals', () => {
    it('hides the origin questions for a lesion type that is not asked', async () => {
        const h = lesionForm({ lesionType: 'deletion' });
        await waitFor(() => {
            assert.ok(screen.getByLabelText('Deleted sequence'));
        });
        assert.equal(
            screen.queryByText(/Is the insertion a consequence of mutagenesis/), null);
        h.cleanupFetch();
    });

    it('asks both questions on an insertion, with follow-ups hidden until answered', async () => {
        const h = lesionForm({
            lesionType: 'insertion',
            insertionFromMutagenesis: null,
            insertionFromConstruct: null,
        });
        await waitFor(() => {
            assert.ok(screen.getByText(/Is the insertion a consequence of mutagenesis/));
        });
        assert.ok(screen.getByText(/Is the insertion due to insertion of construct/));
        assert.equal(screen.queryByLabelText('CRISPR sequence'), null);
        assert.equal(screen.queryByLabelText('TALEN sequence'), null);
        assert.equal(screen.queryByLabelText('Construct name'), null);
        h.cleanupFetch();
    });

    it('reveals the CRISPR and TALEN boxes once mutagenesis is answered yes', async () => {
        const h = lesionForm({
            lesionType: 'insertion',
            insertionFromMutagenesis: true,
            insertionFromConstruct: null,
        });
        await waitFor(() => {
            assert.ok(screen.getByLabelText('CRISPR sequence'));
        });
        assert.ok(screen.getByLabelText('TALEN sequence'));
        // The other branch stays closed — the two questions are independent.
        assert.equal(screen.queryByLabelText('Construct name'), null);
        h.cleanupFetch();
    });

    it('reveals only the construct name when that question is answered yes', async () => {
        const h = lesionForm({
            lesionType: 'insertion',
            insertionFromMutagenesis: null,
            insertionFromConstruct: true,
        });
        await waitFor(() => {
            assert.ok(screen.getByLabelText('Construct name'));
        });
        assert.equal(screen.queryByLabelText('CRISPR sequence'), null);
        h.cleanupFetch();
    });

    it('does not leak the follow-ups to another lesion type when the answer persists', async () => {
        // The point of the AND condition. A stale "yes" from an earlier
        // insertion must not surface a CRISPR box on a deletion.
        const h = lesionForm({
            lesionType: 'deletion',
            insertionFromMutagenesis: true,
            insertionFromConstruct: true,
        });
        await waitFor(() => {
            assert.ok(screen.getByLabelText('Deleted sequence'));
        });
        assert.equal(screen.queryByLabelText('CRISPR sequence'), null);
        assert.equal(screen.queryByLabelText('TALEN sequence'), null);
        assert.equal(screen.queryByLabelText('Construct name'), null);
        h.cleanupFetch();
    });

    it('asks an indel about mutagenesis but not about a construct', async () => {
        // ZFIN-10403's mockup carries the CRISPR/TALEN question over to indel
        // and stops there, which is why the two questions have separate type
        // lists rather than one shared one.
        const h = lesionForm({ lesionType: 'indel' });
        await waitFor(() => {
            assert.ok(screen.getByText(/Is the insertion a consequence of mutagenesis/));
        });
        assert.equal(
            screen.queryByText(/Is the insertion due to insertion of construct/), null);
        h.cleanupFetch();
    });

    it('names the deleted-sequence size per lesion type', async () => {
        // Same property and same server-side derivation; only the label
        // differs. On an indel it sits opposite an insertion size, where
        // "Lesion size" would be ambiguous.
        const indel = lesionForm({ lesionType: 'indel' });
        await waitFor(() => {
            assert.ok(screen.getByLabelText('Deletion size (bp)'));
        });
        assert.ok(screen.getByLabelText('Insertion size (bp)'));
        assert.equal(screen.queryByLabelText('Lesion size (bp)'), null);
        indel.cleanupFetch();
        cleanup();

        const deletion = lesionForm({ lesionType: 'deletion' });
        await waitFor(() => {
            assert.ok(screen.getByLabelText('Lesion size (bp)'));
        });
        assert.equal(screen.queryByLabelText('Deletion size (bp)'), null);
        deletion.cleanupFetch();
    });

    it('constrains every sequence field on an indel to bases', async () => {
        // "Validate the sequence input is DNA" (ZFIN-10403). The nucleotide
        // widget announces its alphabet when empty, so its presence on a
        // field is observable.
        const h = lesionForm({ lesionType: 'indel' });
        await waitFor(() => {
            assert.ok(screen.getByLabelText('Deleted sequence'));
        });
        for (const field of ['Deleted sequence', 'Inserted sequence',
            '5′ flanking sequence', '3′ flanking sequence']) {
            const input = screen.getByLabelText(field);
            assert.equal(input.tagName, 'TEXTAREA', `${field} should be a sequence box`);
        }
        // One alphabet hint per constrained sequence field on this lesion
        // type. Matched loosely because the flanking fields append their own
        // help text to the same node.
        assert.equal(screen.getAllByText(/A \/ C \/ G \/ T \/ N only/).length, 4);
        h.cleanupFetch();
    });

    it('shows transcript consequences regardless of lesion type', async () => {
        // ZFIN-10399 applies to every type, so it carries no reveal rule.
        for (const lesionType of ['insertion', 'deletion', 'point_mutation', 'transgene']) {
            const h = lesionForm({ lesionType });
            await waitFor(() => {
                assert.ok(
                    screen.getByLabelText('Transcript consequences'),
                    `expected transcript consequences for ${lesionType}`,
                );
            });
            h.cleanupFetch();
            cleanup();
        }
    });
});
