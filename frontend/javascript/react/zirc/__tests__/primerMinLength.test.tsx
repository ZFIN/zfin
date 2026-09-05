import { describe, it, afterEach } from 'node:test';
import assert from 'node:assert/strict';
import * as fs from 'node:fs';
import * as path from 'node:path';
import { cleanup, screen } from '@testing-library/react';
import { renderForm } from './renderHelpers';

/**
 * The inline minimum-length hint from ZFIN-10407, widened to the ASA / KASP
 * primer trio by ZFIN-10439. Driven from the committed assay schema snapshot
 * rather than a hand-written uiSchema fragment — so it cannot pass while the
 * server emits something different.
 *
 * Curators asked for a flag, not a block: the message appears under a short
 * primer but nothing prevents the value being saved.
 */

const SNAPSHOT = path.resolve(
    __dirname, '../../../../../test/resources/zirc/snapshot/assay.form-schema.json',
);

function assayForm(data: Record<string, unknown>) {
    const snap = JSON.parse(fs.readFileSync(SNAPSHOT, 'utf8'));
    return renderForm({ schema: snap.schema, uischema: snap.uiSchema, data });
}

describe('primer minimum length (ZFIN-10407, ZFIN-10439)', () => {
    afterEach(() => cleanup());

    it('the snapshot carries minBases on exactly the five checked primers', () => {
        const snap = JSON.parse(fs.readFileSync(SNAPSHOT, 'utf8'));
        const withMin: string[] = [];
        const walk = (n: unknown) => {
            if (Array.isArray(n)) {n.forEach(walk); return;}
            if (n && typeof n === 'object') {
                const e = n as { scope?: unknown; options?: { minBases?: unknown } };
                if (e.scope && e.options?.minBases) {withMin.push(String(e.scope));}
                Object.values(e).forEach(walk);
            }
        };
        walk(snap.uiSchema);
        // Mirrors GenotypingAssayStatusComputer.LENGTH_CHECKED_PRIMERS. Notably
        // absent: sequencingPrimer, and the dCAPS mismatch question (a
        // Forward/Reverse choice, not a sequence).
        assert.deepEqual(withMin.sort(), [
            '#/properties/commonPrimer',
            '#/properties/forwardPrimer',
            '#/properties/mutSpecificPrimer',
            '#/properties/reversePrimer',
            '#/properties/wtSpecificPrimer',
        ]);
    });

    it('shows the hint for a primer below the minimum', () => {
        assayForm({ assayType: 'pcr_gel', forwardPrimer: 'ACGTA' });

        // "At least 10 bases expected — 5 entered."
        const hint = screen.queryByText(/At least 10 bases expected/);
        assert.ok(hint, 'expected the minimum-length hint for a 5-base primer');
        assert.match(hint!.textContent ?? '', /5 entered/);
    });

    it('counts only real bases, so N does not pad a primer to the minimum', () => {
        // ZFIN-10416 dropped N from the alphabet: the widget strips it, so this
        // 10-character value is really 9 bases and must still be flagged.
        assayForm({ assayType: 'pcr_gel', forwardPrimer: 'ACGTACGTAN' });

        const hint = screen.queryByText(/At least 10 bases expected/);
        assert.ok(hint, 'N must not count toward the minimum');
        assert.match(hint!.textContent ?? '', /9 entered/);
    });

    it('shows no hint once the primer reaches the minimum', () => {
        assayForm({ assayType: 'pcr_gel', forwardPrimer: 'ACGTACGTAC' });

        assert.equal(screen.queryByText(/bases expected/), null,
            'a 10-base primer should not be flagged');
    });

    it('shows no hint for an empty primer', () => {
        // Empty is "not filled in yet", covered by the MISSING status badge on
        // the detail page, not by an inline hint while the user is about to type.
        assayForm({ assayType: 'pcr_gel', forwardPrimer: '' });

        assert.equal(screen.queryByText(/bases expected/), null,
            'an empty primer should not show the length hint');
    });

    it('flags a short primer in the ASA trio too (ZFIN-10439)', () => {
        assayForm({ assayType: 'asa', wtSpecificPrimer: 'ACG' });

        const hint = screen.queryByText(/At least 10 bases expected/);
        assert.ok(hint, 'the ASA WT-specific primer should carry the minimum');
        assert.match(hint!.textContent ?? '', /3 entered/);
    });

    it('does not flag the sequencing primer, which carries no minimum', () => {
        assayForm({ assayType: 'pcr_sequencing', sequencingPrimer: 'ACG' });

        assert.equal(screen.queryByText(/bases expected/), null,
            'sequencingPrimer has no minimum — no ticket has asked for one');
    });

    it('puts the ASA primer boxes above the expected PCR products', () => {
        // ZFIN-10439 asks for them "right after assay type". Reading the order
        // off the snapshot keeps the two groups from drifting back apart.
        const snap = JSON.parse(fs.readFileSync(SNAPSHOT, 'utf8'));
        const scopes: string[] = [];
        const walk = (n: unknown) => {
            if (Array.isArray(n)) {n.forEach(walk); return;}
            if (n && typeof n === 'object') {
                const e = n as { scope?: unknown };
                if (typeof e.scope === 'string') {scopes.push(e.scope);}
                Object.values(e).forEach(walk);
            }
        };
        walk(snap.uiSchema);

        const wt = scopes.indexOf('#/properties/wtSpecificPrimer');
        const product = scopes.indexOf('#/properties/expectedWtPcr');
        assert.ok(wt >= 0 && product >= 0, 'both fields should be in the layout');
        assert.ok(wt < product,
            `expected the primer trio (${wt}) before the PCR products (${product})`);
    });
});
