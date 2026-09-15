import { describe, it } from 'node:test';
import assert from 'node:assert/strict';
import * as fs from 'node:fs';
import * as path from 'node:path';

/**
 * Field order within one assay type, read off the committed schema snapshot.
 *
 * Worth pinning because the layout is a flat list of Groups, each revealed for
 * a set of assay types, and a type's visible order is whatever falls out of
 * that list. Two tickets have asked for an order that interleaves a type's own
 * fields with a shared group:
 *
 *   ZFIN-10439  ASA's primer trio must precede the expected-PCR products
 *   ZFIN-10442  SSLP's metadata precedes the shared forward/reverse pair,
 *               and its own two PCR products follow it
 *   ZFIN-10440  KASP's genomic sequence precedes the primer trio, without
 *               moving the trio itself -- it is shared with ASA
 *
 * SSLP is the reason this file exists: satisfying it took splitting one group
 * into two that bracket the shared primer group, and nothing about the
 * resulting layout makes that arrangement look deliberate. Reordering the
 * list, or merging the halves back together, breaks these assertions rather
 * than quietly shuffling a curator's form.
 */

const SNAPSHOT = path.resolve(
    __dirname, '../../../../../test/resources/zirc/snapshot/assay.form-schema.json',
);

type Elem = {
    type?: string;
    scope?: string;
    options?: Record<string, unknown>;
    elements?: Elem[];
    rule?: { condition?: { schema?: { enum?: string[] } } };
};

function snapshot() {
    return JSON.parse(fs.readFileSync(SNAPSHOT, 'utf8')) as {
        uiSchema: Elem;
        schema: { properties?: Record<string, { title?: string }> };
    };
}

/**
 * The field names an assay type shows, in render order: every Control whose
 * enclosing Group either has no reveal rule or names this type.
 */
function fieldOrderFor(assayType: string): string[] {
    const out: string[] = [];
    const walk = (node: Elem, visible: boolean) => {
        const types = node.rule?.condition?.schema?.enum;
        const showing = types ? visible && types.includes(assayType) : visible;
        if (node.scope && showing) {
            out.push(node.scope.replace('#/properties/', ''));
        }
        (node.elements ?? []).forEach((child) => walk(child, showing));
    };
    walk(snapshot().uiSchema, true);
    return out;
}

/** Find one Control by scope, anywhere in the layout. */
function control(field: string): Elem {
    let found: Elem | undefined;
    const walk = (node: Elem) => {
        if (node.scope === `#/properties/${field}`) {found = node;}
        (node.elements ?? []).forEach(walk);
    };
    walk(snapshot().uiSchema);
    assert.ok(found, `no Control for ${field}`);
    return found!;
}

describe('assay field order', () => {
    it('SSLP renders in the order ZFIN-10442 asks for', () => {
        assert.deepEqual(fieldOrderFor('sslp'), [
            'assayType',
            'sslpInducedBackground',
            'sslpOutcrossedBackground',
            'sslpMarkerName',
            'sslpDistance',
            'sslpGenomicLocation',
            'forwardPrimer',
            'reversePrimer',
            'sslpInducedPcr',
            'sslpOutcrossedPcr',
            'attachments',
            // ZFIN-10415 adds a second bucket below the results one, on
            // every assay type, so it lands here on SSLP too.
            'protocolDocuments',
            'additionalInfo',
        ]);
    });

    it('puts the ASA primer trio above the expected PCR products (ZFIN-10439)', () => {
        const order = fieldOrderFor('asa');
        assert.ok(order.indexOf('wtSpecificPrimer') < order.indexOf('expectedWtPcr'),
            `trio should precede the products, got ${order.join(', ')}`);
        // ASA shows the trio instead of the shared pair, never both.
        assert.equal(order.includes('forwardPrimer'), false);
    });

    it('puts KASP\'s genomic sequence right after the assay type (ZFIN-10440)', () => {
        // "right after assay type; next all of the primers". The genomic
        // sequence used to render last, below the PCR products, so the
        // submitter met the primer boxes before the sequence they are
        // designed against.
        const order = fieldOrderFor('kasp');
        assert.deepEqual(order.slice(0, 5), [
            'assayType',
            'kaspGenomicSequence',
            'wtSpecificPrimer',
            'mutSpecificPrimer',
            'commonPrimer',
        ], `got ${order.join(', ')}`);
        assert.ok(order.indexOf('commonPrimer') < order.indexOf('expectedWtPcr'),
            'the primer trio should still precede the products');
    });

    it('leaves ASA\'s order untouched by the KASP move (ZFIN-10440)', () => {
        // The genomic-sequence group is KASP-only, so moving it above the
        // shared primer trio must not shift ASA. Asserted because the trio's
        // group is shared and the obvious way to satisfy ZFIN-10440 -- moving
        // that group -- would have dragged ASA with it, undoing ZFIN-10439.
        const order = fieldOrderFor('asa');
        assert.equal(order.includes('kaspGenomicSequence'), false,
            'kaspGenomicSequence must not appear on ASA');
        assert.equal(order[1], 'wtSpecificPrimer',
            `ASA should still open with the trio, got ${order.join(', ')}`);
    });

    it('leaves the SSLP-only fields off every other assay type', () => {
        const sslpOnly = [
            'sslpInducedBackground', 'sslpOutcrossedBackground', 'sslpMarkerName',
            'sslpDistance', 'sslpGenomicLocation', 'sslpInducedPcr', 'sslpOutcrossedPcr',
        ];
        for (const type of ['pcr_gel', 'pcr_sequencing', 'rflp', 'dcaps', 'asa', 'kasp', 'hrma']) {
            const order = fieldOrderFor(type);
            for (const field of sslpOnly) {
                assert.equal(order.includes(field), false,
                    `${field} should not render for ${type}`);
            }
        }
    });

    it('offers Proximal / Distal as a closed list (ZFIN-10442)', () => {
        const opts = control('sslpGenomicLocation').options ?? {};
        assert.equal(opts.widget, 'selectWithOther');
        assert.deepEqual(opts.standardValues, ['Proximal', 'Distal']);
        assert.equal(opts.noOther, true, 'the ticket names exactly two choices');
    });

    it('leaves the marker name a plain text box', () => {
        // It used to carry placeholder "Search ZFIN SSLP markers…", advertising
        // a lookup that was never built.
        const opts = control('sslpMarkerName').options ?? {};
        assert.equal(opts.placeholder, undefined);
        assert.equal(opts.widget, undefined);
    });

    it('hints the background fields with example strains', () => {
        for (const field of ['sslpInducedBackground', 'sslpOutcrossedBackground']) {
            const opts = control(field).options ?? {};
            assert.match(String(opts.placeholder), /AB/, `${field} should hint e.g. AB, TU`);
        }
    });

    it('sizes every expected-product box the same way (ZFIN-10408)', () => {
        // The ask was for consistency, so this asserts the exact set: a new
        // product-size box added without the shared options, or one of these
        // losing them, fails here rather than showing up as a form that mixes
        // widths.
        const sized: string[] = [];
        const walk = (node: Elem) => {
            const opts = node.options ?? {};
            if (node.scope && opts.boxSize === 'short') {
                sized.push(node.scope.replace('#/properties/', ''));
            }
            (node.elements ?? []).forEach(walk);
        };
        walk(snapshot().uiSchema);

        assert.deepEqual(sized.sort(), [
            'expectedMutDigest',
            'expectedMutPcr',
            'expectedWtDigest',
            'expectedWtPcr',
            'sslpInducedPcr',
            'sslpOutcrossedPcr',
        ]);

        for (const field of sized) {
            assert.equal(control(field).options?.suffix, 'bp',
                `${field} should carry the bp suffix outside the box`);
        }
    });

    it('asks the enzyme-cleaves questions as Yes/No radios (ZFIN-10420)', () => {
        // Not a checkbox. Both columns are nullable booleans, so an unanswered
        // question and an explicit "no" are different answers, and a checkbox
        // collapses them into one unchecked box. Asserting the widget rather
        // than the rendering is what keeps them from quietly reverting.
        for (const field of ['enzymeCleavesWt', 'enzymeCleavesMut']) {
            assert.equal(control(field).options?.widget, 'yesNoRadio',
                `${field} must be a Yes/No radio, not a checkbox`);
        }
    });

    it('tells submitters the digest boxes take several numbers (ZFIN-10420)', () => {
        // Digest only: an RFLP digest usually yields two fragments, while a
        // PCR or SSLP product is a single amplicon, where the same hint would
        // be wrong advice. Pinning both halves -- present here, absent there --
        // because widening the shared constant is the easy mistake.
        const digest = ['expectedWtDigest', 'expectedMutDigest'];
        for (const field of digest) {
            assert.match(String(control(field).options?.helpText), /comma-separated/,
                `${field} should say more than one number is expected`);
        }
        for (const field of ['expectedWtPcr', 'expectedMutPcr',
            'sslpInducedPcr', 'sslpOutcrossedPcr']) {
            assert.equal(control(field).options?.helpText, undefined,
                `${field} reports one amplicon and should carry no fragment hint`);
        }

        // ...and the label says it too, for anyone who skips the hint.
        const props = snapshot().schema.properties ?? {};
        for (const field of digest) {
            assert.match(String(props[field]?.title), /product\(s\) after digest/,
                `${field} title should pluralise product`);
        }
    });
});
