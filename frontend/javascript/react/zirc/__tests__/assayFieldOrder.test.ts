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
    return JSON.parse(fs.readFileSync(SNAPSHOT, 'utf8')) as { uiSchema: Elem };
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
});
