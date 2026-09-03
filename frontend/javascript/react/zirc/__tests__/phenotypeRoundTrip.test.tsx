import { describe, it, afterEach } from 'node:test';
import assert from 'node:assert/strict';
import * as fs from 'node:fs';
import * as path from 'node:path';
import { cleanup, screen } from '@testing-library/react';
import { renderForm } from './renderHelpers';

/**
 * Does the phenotype form render the values the server sends back?
 *
 * Reproduction attempt for a report that every field except the description
 * came back empty after a reload, even though the database held the values and
 * GET /api/zirc/phenotypes/1 returned them all.
 */

const SNAPSHOT = path.resolve(
    __dirname, '../../../../../test/resources/zirc/snapshot/phenotype.form-schema.json',
);

// Exactly what the endpoint returned, minus the server-managed keys that
// seedFromDto strips (id, sortOrder, mutationId).
const SERVED = {
    description: 'Description',
    hpfStart: 48,
    hpfEnd: 72,
    stage: null,
    zfinImagePermission: true,
    zircImagePermission: false,
    nonMendelianPercentage: null,
    nonMendelianComment: null,
    segregation: 'Mendelian recessive',
    type: 'Zygotic (Z)',
    backgroundDependent: true,
    backgroundComment: 'asdad',
};

function phenotypeForm(data: Record<string, unknown>) {
    const snap = JSON.parse(fs.readFileSync(SNAPSHOT, 'utf8'));
    return renderForm({ schema: snap.schema, uischema: snap.uiSchema, data });
}

describe('phenotype form round trip', () => {
    afterEach(() => cleanup());

    it('renders the description', () => {
        phenotypeForm(SERVED);
        assert.ok(screen.queryByDisplayValue('Description'), 'description should render');
    });

    it('renders the timing values', () => {
        phenotypeForm(SERVED);
        assert.ok(screen.queryByDisplayValue('48'), 'hpfStart 48 should render');
        assert.ok(screen.queryByDisplayValue('72'), 'hpfEnd 72 should render');
    });

    it('renders the two dropdowns', () => {
        phenotypeForm(SERVED);
        assert.ok(screen.queryByDisplayValue('Mendelian recessive'), 'segregation should render');
        assert.ok(screen.queryByDisplayValue('Zygotic (Z)'), 'phenotype type should render');
    });

    it('renders the yes/no answers', () => {
        phenotypeForm(SERVED);
        const checked = screen.queryAllByRole('radio')
            .filter((r) => (r as HTMLInputElement).checked);
        assert.ok(checked.length >= 3,
            `expected zfin/zirc/background answers checked, got ${checked.length}`);
    });

    it('reveals the background comment and its text', () => {
        phenotypeForm(SERVED);
        assert.ok(screen.queryByDisplayValue('asdad'), 'background comment should render');
    });

    it('uses the schema title for the timing row label', () => {
        phenotypeForm(SERVED);
        assert.ok(screen.queryByText(/Optimal temporal window/),
            'timing row should take its label from the schema title, not "Timing"');
    });
});
