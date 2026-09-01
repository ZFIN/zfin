import { describe, it, afterEach } from 'node:test';
import assert from 'node:assert/strict';
import * as fs from 'node:fs';
import * as path from 'node:path';
import { cleanup, screen } from '@testing-library/react';
import { renderForm } from './renderHelpers';

/**
 * ZFIN-10407's inline minimum-length hint, driven from the committed assay
 * schema snapshot rather than a hand-written uiSchema fragment — so it cannot
 * pass while the server emits something different.
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

describe('primer minimum length (ZFIN-10407)', () => {
    afterEach(() => cleanup());

    it('the snapshot actually carries minBases on forward/reverse', () => {
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
        assert.deepEqual(withMin.sort(), [
            '#/properties/forwardPrimer',
            '#/properties/reversePrimer',
        ]);
    });

    it('shows the hint for a primer below the minimum', () => {
        assayForm({ assayType: 'pcr_gel', forwardPrimer: 'NATT' });

        // "At least 10 bases expected — 4 entered."
        const hint = screen.queryByText(/At least 10 bases expected/);
        assert.ok(hint, 'expected the minimum-length hint for a 4-base primer');
        assert.match(hint!.textContent ?? '', /4 entered/);
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

    it('does not flag primer fields that carry no minimum', () => {
        // ASA shows the WT/mutant/common trio, none of which has minBases.
        assayForm({ assayType: 'asa', wtSpecificPrimer: 'ACG' });

        assert.equal(screen.queryByText(/bases expected/), null,
            'only forward/reverse carry a minimum for now');
    });
});
