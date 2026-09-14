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
 * Also covers the mutation form's reveal of the CRISPR / TALEN reagent boxes
 * off the mutagenesis protocol (ZFIN-10475). Those boxes used to hang off a
 * per-lesion checklist that asked the same question the protocol picklist
 * already answers; the pinned-down part is that each of the four relevant
 * protocols opens the right boxes and no others.
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
 * The mutation form's Mutagenesis group, rendered against the full mutation
 * schema. Only that group, because the surrounding form mounts the genes /
 * lesions / assays / phenotypes list widgets, which fetch endpoints the
 * harness's stub deliberately refuses — and none of them bear on these rules.
 */
function mutagenesisGroup(data: Record<string, unknown>) {
    const parsed = JSON.parse(fs.readFileSync(MUTATION_SNAPSHOT, 'utf8'));
    const group = parsed.uiSchema.elements
        .find((e: { label?: string }) => e.label === 'Mutagenesis');
    assert.ok(group, 'mutation snapshot has no Mutagenesis group');
    return renderForm({ schema: parsed.schema, uischema: group, data });
}

afterEach(() => {
    cleanup();
});

describe('lesion conditional reveals', () => {
    it('no longer asks about the reagent per lesion', async () => {
        // ZFIN-10475 moved the checklist and the boxes it revealed onto the
        // mutation; nothing about them should survive on any lesion type.
        for (const lesionType of ['insertion', 'indel', 'deletion']) {
            const h = lesionForm({ lesionType });
            await waitFor(() => {
                assert.ok(screen.getByLabelText('Transcript consequences'));
            });
            assert.equal(screen.queryByText('The insertion is a consequence of'), null);
            for (const box of ['CRISPR sequence', 'TALEN sequence 1', 'TALEN sequence 2']) {
                assert.equal(screen.queryByLabelText(box), null,
                    `${box} should not be on a ${lesionType} lesion`);
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

describe('mutagenesis reagent reveals', () => {
    it('opens the guide box for a CRISPR protocol and nothing else', async () => {
        const h = mutagenesisGroup({ mutagenesisProtocol: 'CRISPR' });
        await waitFor(() => {
            assert.ok(screen.getByLabelText('CRISPR sequence'));
        });
        assert.equal(screen.queryByLabelText('TALEN sequence 1'), null);
        assert.equal(screen.queryByLabelText('TALEN sequence 2'), null);
        h.cleanupFetch();
    });

    it('opens both arms for a TALEN protocol', async () => {
        // A TALEN cuts as a pair, so one box would be asking for half an answer.
        const h = mutagenesisGroup({ mutagenesisProtocol: 'TALEN' });
        await waitFor(() => {
            assert.ok(screen.getByLabelText('TALEN sequence 1'));
        });
        assert.ok(screen.getByLabelText('TALEN sequence 2'));
        assert.equal(screen.queryByLabelText('CRISPR sequence'), null);
        h.cleanupFetch();
    });

    it('counts the combined DNA-and-X protocols as using the reagent', async () => {
        // "DNA and CRISPR" still used a guide; matching only the bare tokens
        // would drop the box for half the submissions that need it.
        for (const [protocol, shown, hidden] of [
            ['DNA and CRISPR', 'CRISPR sequence', 'TALEN sequence 1'],
            ['DNA and TALEN', 'TALEN sequence 1', 'CRISPR sequence'],
        ]) {
            const h = mutagenesisGroup({ mutagenesisProtocol: protocol });
            await waitFor(() => {
                assert.ok(screen.getByLabelText(shown), `expected ${shown} for ${protocol}`);
            });
            assert.equal(screen.queryByLabelText(hidden), null,
                `${hidden} should stay hidden for ${protocol}`);
            h.cleanupFetch();
            cleanup();
        }
    });

    it('asks for no sequence under a protocol that uses neither reagent', async () => {
        for (const protocol of ['ENU', 'Spontaneous', 'G-rays']) {
            const h = mutagenesisGroup({ mutagenesisProtocol: protocol });
            await waitFor(() => {
                assert.ok(screen.getByLabelText('Mutagenesis Protocol'));
            });
            for (const box of ['CRISPR sequence', 'TALEN sequence 1', 'TALEN sequence 2']) {
                assert.equal(screen.queryByLabelText(box), null,
                    `${box} should not appear for ${protocol}`);
            }
            h.cleanupFetch();
            cleanup();
        }
    });

    it('constrains the reagent boxes to bases', async () => {
        // Same nucleotideSequence widget the lesion sequence fields use; its
        // alphabet hint is what makes that observable.
        const h = mutagenesisGroup({ mutagenesisProtocol: 'TALEN' });
        await waitFor(() => {
            assert.ok(screen.getByLabelText('TALEN sequence 1'));
        });
        assert.equal(screen.getAllByText(/A \/ C \/ G \/ T only/).length, 2);
        h.cleanupFetch();
    });
});
