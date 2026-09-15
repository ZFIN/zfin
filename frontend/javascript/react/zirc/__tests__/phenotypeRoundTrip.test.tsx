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
    attachments: [],
};

/** One uploaded image, as PhenotypeFileDTO serializes it. */
const ONE_IMAGE = [{
    id: 7,
    originalFilename: 'lateral-view.png',
    contentType: 'image/png',
    fileSize: 2048,
    uploadedAt: '2026-09-03T12:00:00Z',
}];

function phenotypeForm(data: Record<string, unknown>) {
    const snap = JSON.parse(fs.readFileSync(SNAPSHOT, 'utf8'));
    // phenotypeId is what the attachments widget reads to know whose upload
    // endpoint to call; the real editor supplies it the same way.
    return renderForm({
        schema: snap.schema,
        uischema: snap.uiSchema,
        data,
        config: { phenotypeId: 1 },
    });
}

/** Field order for the whole form, read off the committed snapshot. */
function fieldOrder(): string[] {
    const snap = JSON.parse(fs.readFileSync(SNAPSHOT, 'utf8'));
    const out: string[] = [];
    const walk = (n: unknown) => {
        if (Array.isArray(n)) {n.forEach(walk); return;}
        if (n && typeof n === 'object') {
            const e = n as { scope?: unknown; elements?: unknown };
            if (typeof e.scope === 'string') {out.push(e.scope.replace('#/properties/', ''));}
            if (e.elements) {walk(e.elements);}
        }
    };
    walk(snap.uiSchema);
    return out;
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

    // --- ZFIN-10449 item 2: phenotype images ---

    it('renders the upload section with its heading', () => {
        phenotypeForm(SERVED);
        assert.ok(screen.queryByText('Phenotype images'), 'heading should render');
        assert.ok(screen.queryByText('No attachments yet.'), 'empty state should render');
    });

    it('sits between the timing row and the image permissions', () => {
        // The mockup order: upload the images, then answer who may use them.
        const order = fieldOrder();
        const timing = order.indexOf('hpfStart');
        const files = order.indexOf('attachments');
        const perm = order.indexOf('zfinImagePermission');
        assert.ok(timing >= 0 && files >= 0 && perm >= 0, order.join(', '));
        assert.ok(timing < files && files < perm,
            `expected timing < attachments < permissions, got ${order.join(', ')}`);
    });

    it('links an uploaded file to the phenotype endpoint, not the assay one', () => {
        // The owner plumbing is the whole point of the generalisation: a
        // default-to-assay renderer would build /assays/attachments/7/content
        // here and 404 (or worse, serve someone else's file id).
        phenotypeForm({ ...SERVED, attachments: ONE_IMAGE });

        const link = screen.queryByText('lateral-view.png') as HTMLAnchorElement | null;
        assert.ok(link, 'the filename should render as a link');
        assert.equal(link!.getAttribute('href'),
            '/action/api/zirc/phenotypes/attachments/7/content');
    });

    it('shows the file metadata', () => {
        phenotypeForm({ ...SERVED, attachments: ONE_IMAGE });
        assert.ok(screen.queryByText(/image\/png/), 'content type should render');
        assert.ok(screen.queryByText(/2\.0 KB/), 'size should render');
    });

    it('offers an enabled file input below the cap', () => {
        phenotypeForm({ ...SERVED, attachments: ONE_IMAGE });
        const input = document.querySelector('input[type=file]') as HTMLInputElement | null;
        assert.ok(input, 'file input should render');
        assert.equal(input!.disabled, false, '1 of 10 images is below the cap');
    });

    it('disables the file input at the cap', () => {
        // maxItems comes from MAX_ATTACHMENTS_PER_PHENOTYPE via the schema, so
        // the disabled state cannot disagree with what the server rejects.
        const full = Array.from({ length: 10 }, (_, i) => ({
            ...ONE_IMAGE[0], id: i + 1, originalFilename: `img-${i}.png`,
        }));
        phenotypeForm({ ...SERVED, attachments: full });

        const input = document.querySelector('input[type=file]') as HTMLInputElement | null;
        assert.equal(input!.disabled, true, '10 of 10 should disable the input');
    });
});
