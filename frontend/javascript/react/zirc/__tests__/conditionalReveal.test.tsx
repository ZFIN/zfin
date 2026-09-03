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
 * Reveal rules on the real lesion and mutation forms, driven from the
 * committed schema snapshots rather than hand-written fragments — so these
 * cannot pass while the server emits something different.
 */

const SNAPSHOT = path.resolve(
    __dirname, '../../../../../test/resources/zirc/snapshot/lesion.form-schema.json',
);
const MUTATION_SNAPSHOT = path.resolve(
    __dirname, '../../../../../test/resources/zirc/snapshot/mutation.form-schema.json',
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

/**
 * Just the Mutagenesis group of the real mutation form. Narrowed to that
 * group rather than mounting the whole page: the sibling Genes / Lesions /
 * Assays / Phenotypes groups are self-persisting list widgets that would call
 * endpoints this harness deliberately does not stub. The schema stays whole,
 * so the Controls resolve exactly as they do in the app.
 */
function mutagenesisForm(data: Record<string, unknown>) {
    const parsed = JSON.parse(fs.readFileSync(MUTATION_SNAPSHOT, 'utf8'));
    const group = (parsed.uiSchema.elements as { label?: string }[])
        .find((e) => e.label === 'Mutagenesis');
    assert.ok(group, 'expected a Mutagenesis group in the mutation snapshot');
    return renderForm({ schema: parsed.schema, uischema: group, data });
}

afterEach(() => {
    cleanup();
});

describe('lesion conditional reveals', () => {
    it('no longer asks where an insertion came from', async () => {
        // The checklist and all four follow-ups moved off this form: the
        // CRISPR and TALEN sequences went to the mutation's Mutagenesis
        // Protocol, and curators dropped the rest of the question with them.
        for (const lesionType of ['insertion', 'indel']) {
            const h = lesionForm({ lesionType });
            await waitFor(() => {
                assert.ok(screen.getByLabelText('Inserted sequence'));
            });
            assert.equal(
                screen.queryByText('The insertion is a consequence of'), null,
                `checklist should be gone for ${lesionType}`,
            );
            for (const gone of ['CRISPR sequence', 'TALEN sequence',
                'Construct name', 'Other origin']) {
                assert.equal(screen.queryByLabelText(gone), null,
                    `${gone} should be gone for ${lesionType}`);
            }
            h.cleanupFetch();
            cleanup();
        }
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

describe('mutagenesis protocol conditional reveals', () => {
    it('shows the CRISPR sequence only for CRISPR/Cas9', async () => {
        const h = mutagenesisForm({ mutagenesisProtocol: 'CRISPR/Cas9' });
        await waitFor(() => {
            assert.ok(screen.getByLabelText('CRISPR sequence'));
        });
        // TALEN's pair belongs to the other protocol.
        assert.equal(screen.queryByLabelText('TALEN 1 sequence'), null);
        assert.equal(screen.queryByLabelText('TALEN 2 sequence'), null);
        h.cleanupFetch();
    });

    it('shows both TALEN sequences only for TALEN', async () => {
        // TALENs act as a pair, so the submitter has two sequences to give —
        // the single field this replaced could only hold one.
        const h = mutagenesisForm({ mutagenesisProtocol: 'TALEN' });
        await waitFor(() => {
            assert.ok(screen.getByLabelText('TALEN 1 sequence'));
        });
        assert.ok(screen.getByLabelText('TALEN 2 sequence'));
        assert.equal(screen.queryByLabelText('CRISPR sequence'), null);
        h.cleanupFetch();
    });

    it('shows no sequence box for a protocol that carries none', async () => {
        for (const protocol of ['ENU', 'ZFN', 'ionizing radiation', 'spontaneous']) {
            const h = mutagenesisForm({ mutagenesisProtocol: protocol });
            await waitFor(() => {
                assert.ok(screen.getByLabelText('Mutagenesis Protocol'));
            });
            for (const box of ['CRISPR sequence', 'TALEN 1 sequence',
                'TALEN 2 sequence']) {
                assert.equal(screen.queryByLabelText(box), null,
                    `${box} should be hidden for ${protocol}`);
            }
            h.cleanupFetch();
            cleanup();
        }
    });

    it('shows no sequence box before a protocol is chosen', async () => {
        // An unset protocol must fail closed rather than revealing everything.
        const h = mutagenesisForm({});
        await waitFor(() => {
            assert.ok(screen.getByLabelText('Mutagenesis Protocol'));
        });
        for (const box of ['CRISPR sequence', 'TALEN 1 sequence',
            'TALEN 2 sequence']) {
            assert.equal(screen.queryByLabelText(box), null);
        }
        h.cleanupFetch();
    });

    it('constrains the protocol sequences to bases', async () => {
        // Same nucleotide widget these fields carried on the lesion form.
        const h = mutagenesisForm({ mutagenesisProtocol: 'TALEN' });
        await waitFor(() => {
            assert.ok(screen.getByLabelText('TALEN 1 sequence'));
        });
        for (const field of ['TALEN 1 sequence', 'TALEN 2 sequence']) {
            assert.equal(screen.getByLabelText(field).tagName, 'TEXTAREA',
                `${field} should be a sequence box`);
        }
        assert.equal(screen.getAllByText(/A \/ C \/ G \/ T only/).length, 2);
        h.cleanupFetch();
    });
});
