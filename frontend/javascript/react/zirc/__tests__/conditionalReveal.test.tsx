import { describe, it, afterEach } from 'node:test';
import assert from 'node:assert/strict';
import * as fs from 'node:fs';
import * as path from 'node:path';
import { cleanup, screen, waitFor } from '@testing-library/react';
import {
    AMINO_ACIDS,
    PROTEIN_CONSEQUENCES,
    TRANSCRIPT_CONSEQUENCES,
    renderForm,
} from './renderHelpers';

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
        vocabularies: {
            transcript_consequence_term: TRANSCRIPT_CONSEQUENCES,
            protein_consequence_term: PROTEIN_CONSEQUENCES,
            amino_acid_term: AMINO_ACIDS,
        },
    });
}

afterEach(() => {
    cleanup();
});

describe('lesion conditional reveals', () => {
    it('hides the origin checklist for a lesion type that cannot carry an insertion', async () => {
        const h = lesionForm({ lesionType: 'deletion' });
        await waitFor(() => {
            assert.ok(screen.getByLabelText('Deleted sequence'));
        });
        assert.equal(screen.queryByText('The insertion is a consequence of'), null);
        h.cleanupFetch();
    });

    it('offers the checklist on an insertion with every follow-up hidden', async () => {
        const h = lesionForm({ lesionType: 'insertion', insertionOrigins: [] });
        await waitFor(() => {
            assert.ok(screen.getByLabelText('CRISPR'));
        });
        assert.ok(screen.getByLabelText('TALEN'));
        assert.equal(screen.queryByLabelText('CRISPR sequence'), null);
        assert.equal(screen.queryByLabelText('TALEN sequence'), null);
        h.cleanupFetch();
    });

    it('offers only the two mutagenesis boxes', async () => {
        // ZFIN-10403b: the construct / other / unknown boxes are not wanted.
        const h = lesionForm({ lesionType: 'insertion', insertionOrigins: [] });
        await waitFor(() => {
            assert.ok(screen.getByLabelText('CRISPR'));
        });
        for (const box of ['Construct or other species DNA', 'Other', 'Unknown']) {
            assert.equal(screen.queryByLabelText(box), null, `${box} should be gone`);
        }
        h.cleanupFetch();
    });

    it('reveals only the follow-up belonging to each ticked box', async () => {
        // The old form opened CRISPR and TALEN together off one "yes"; each
        // box now stands alone.
        const h = lesionForm({ lesionType: 'insertion', insertionOrigins: ['talen'] });
        await waitFor(() => {
            assert.ok(screen.getByLabelText('TALEN sequence'));
        });
        assert.equal(screen.queryByLabelText('CRISPR sequence'), null);
        h.cleanupFetch();
    });

    it('reveals both follow-ups when origins combine', async () => {
        // A lesion made with both mechanisms is why this is a checklist and
        // not a single choice.
        const h = lesionForm({
            lesionType: 'insertion',
            insertionOrigins: ['crispr', 'talen'],
        });
        await waitFor(() => {
            assert.ok(screen.getByLabelText('CRISPR sequence'));
        });
        assert.ok(screen.getByLabelText('TALEN sequence'));
        h.cleanupFetch();
    });

    it('does not leak a follow-up to another lesion type when the tokens persist', async () => {
        // The AND condition's reason for being: a stale token from an earlier
        // insertion must not surface a CRISPR box on a deletion.
        const h = lesionForm({
            lesionType: 'deletion',
            insertionOrigins: ['crispr', 'talen'],
        });
        await waitFor(() => {
            assert.ok(screen.getByLabelText('Deleted sequence'));
        });
        assert.equal(screen.queryByLabelText('CRISPR sequence'), null);
        assert.equal(screen.queryByLabelText('TALEN sequence'), null);
        h.cleanupFetch();
    });

    it('offers the same checklist on an indel', async () => {
        // One option list for both types that can carry an insertion.
        const h = lesionForm({ lesionType: 'indel' });
        await waitFor(() => {
            assert.ok(screen.getByLabelText('CRISPR'));
        });
        assert.ok(screen.getByLabelText('TALEN'));
        h.cleanupFetch();
    });

    it('carries the size inline on the sequence box, with no separate row', async () => {
        // The read-only size fields were redundant: the sequence box already
        // counts what it holds. Only the count remains, and it names itself.
        const indel = lesionForm({
            lesionType: 'indel',
            deletedSequence: 'CACCAGAATGAAA',
            insertedSequence: 'ACGT',
        });
        await waitFor(() => {
            assert.ok(screen.getByText('Deletion size: 13 bp'));
        });
        assert.ok(screen.getByText('Insertion size: 4 bp'));
        // The separate read-only rows are gone, not merely relabelled.
        assert.equal(screen.queryByLabelText('Deletion size (bp)'), null);
        assert.equal(screen.queryByLabelText('Insertion size (bp)'), null);
        assert.equal(screen.queryByLabelText('Lesion size (bp)'), null);
        indel.cleanupFetch();
        cleanup();

        const deletion = lesionForm({
            lesionType: 'deletion',
            deletedSequence: 'CACCAGAATGAAA',
        });
        await waitFor(() => {
            assert.ok(screen.getByText('Lesion size: 13 bp'));
        });
        assert.equal(screen.queryByLabelText('Lesion size (bp)'), null);
        deletion.cleanupFetch();
    });

    it('keeps the read-only size row for a point mutation, which has no sequence box', async () => {
        // 1 bp is definitional there and has nowhere else to appear, so this
        // row is not the redundancy the others were.
        const h = lesionForm({ lesionType: 'point_mutation' });
        await waitFor(() => {
            assert.ok(screen.getByLabelText('Lesion size (bp)'));
        });
        h.cleanupFetch();
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
        assert.equal(screen.getAllByText(/A \/ C \/ G \/ T only/).length, 4);
        h.cleanupFetch();
    });

    it('declares an alphabet on every sequence Control rather than relying on the default', async () => {
        // Two defaults that can drift is the failure mode this guards: the
        // schema names the alphabet, so the widget never has to guess.
        const parsed = JSON.parse(fs.readFileSync(SNAPSHOT, 'utf8'));
        const missing: string[] = [];
        const walk = (n: unknown) => {
            if (Array.isArray(n)) { n.forEach(walk); return; }
            if (!n || typeof n !== 'object') { return; }
            const node = n as Record<string, unknown>;
            const opts = node.options as Record<string, unknown> | undefined;
            if (opts?.widget === 'nucleotideSequence' && typeof opts.alphabet !== 'string') {
                missing.push(String(node.scope));
            }
            Object.values(node).forEach(walk);
        };
        walk(parsed.uiSchema);
        assert.deepEqual(missing, [], 'these sequence Controls declare no alphabet');
    });

    it('shows the amino-acid section on a deletion as well as a point mutation', async () => {
        // ZFIN-10380. A deletion can remove residues, so it wants the
        // amino-acid change and the protein-consequence list.
        for (const lesionType of ['point_mutation', 'deletion']) {
            const h = lesionForm({ lesionType });
            await waitFor(() => {
                assert.ok(
                    screen.getByLabelText('Protein consequences'),
                    `expected protein consequences for ${lesionType}`,
                );
            });
            assert.ok(screen.getByLabelText('Amino Acid Change from'));
            h.cleanupFetch();
            cleanup();
        }
    });

    it('keeps the amino-acid section off lesion types that have no protein effect', async () => {
        const h = lesionForm({ lesionType: 'transgene' });
        await waitFor(() => {
            assert.ok(screen.getByLabelText('Transcript consequences'));
        });
        assert.equal(screen.queryByLabelText('Protein consequences'), null);
        assert.equal(screen.queryByLabelText('Amino Acid Change from'), null);
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
