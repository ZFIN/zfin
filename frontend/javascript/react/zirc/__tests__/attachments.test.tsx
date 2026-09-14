import { describe, it, afterEach } from 'node:test';
import assert from 'node:assert/strict';
import { cleanup, screen, waitFor, fireEvent } from '@testing-library/react';
import { renderForm } from './renderHelpers';

/**
 * Per-bucket accepted file types on the attachments widget
 * (ZFIN-10413 gel images, ZFIN-10417 chromatograms).
 *
 * The four buckets are the same widget differing only by uischema options, so
 * these drive a real <JsonForms> with the options the server publishes from
 * ZircAttachmentKind. The renderer's `accept` attribute and helper text must
 * follow that list, and a wrong file type must be refused before any upload
 * request goes out — renderHelpers' fetch stub throws on any non-vocabulary
 * URL, so an attempted upload fails the test rather than passing quietly.
 */

afterEach(() => {
    cleanup();
});

const IMAGE_EXTENSIONS = ['tif', 'tiff', 'jpg', 'jpeg', 'png', 'gif', 'pdf'];
const TRACE_EXTENSIONS = ['abi', 'ab1', 'scf', 'fa', 'txt', 'docx'];

/** Mirrors the attachments schema property the server emits. */
const attachmentsSchema = {
    type: 'object',
    properties: {
        attachments: {
            type: 'array',
            title: 'Attachments',
            maxItems: 10,
            items: { type: 'object', properties: { id: { type: 'number' } } },
        },
    },
};

const bucket = (label: string, acceptedExtensions?: string[]) => ({
    type: 'VerticalLayout',
    elements: [{
        type: 'Control',
        scope: '#/properties/attachments',
        options: {
            widget: 'attachmentsList',
            managesOwnPersistence: true,
            label,
            ...(acceptedExtensions ? { acceptedExtensions } : {}),
        },
    }],
});

/** The bucket's file input; assayId is what enables it. */
function renderBucket(label: string, acceptedExtensions?: string[]) {
    const h = renderForm({
        schema: attachmentsSchema,
        uischema: bucket(label, acceptedExtensions),
        data: { attachments: [] },
        config: { assayId: 42 },
    });
    const input = document.querySelector('input[type="file"]') as HTMLInputElement;
    assert.ok(input, 'expected a file input');
    return { h, input };
}

/**
 * Put a File on the input and fire change. jsdom's `files` is read-only, so
 * it has to be redefined rather than assigned.
 */
function pick(input: HTMLInputElement, filename: string) {
    const file = new File(['x'], filename, { type: 'application/octet-stream' });
    Object.defineProperty(input, 'files', { value: [file], configurable: true });
    fireEvent.change(input);
}

describe('attachments bucket accepted file types', () => {
    it('publishes the chromatogram trace formats to the file picker', () => {
        const { h, input } = renderBucket('Chromatograms', TRACE_EXTENSIONS);

        assert.equal(input.getAttribute('accept'), '.abi,.ab1,.scf,.fa,.txt,.docx');
        assert.ok(
            screen.getByText('Accepted file types: .abi, .ab1, .scf, .fa, .txt, .docx'),
            'expected helper text naming the accepted extensions',
        );
        h.cleanupFetch();
    });

    it('publishes the gel-image formats to the file picker', () => {
        const { h, input } = renderBucket('Annotated gel images', IMAGE_EXTENSIONS);

        assert.equal(input.getAttribute('accept'), '.tif,.tiff,.jpg,.jpeg,.png,.gif,.pdf');
        assert.ok(
            screen.getByText('Accepted file types: .tif, .tiff, .jpg, .jpeg, .png, .gif, .pdf'),
            'expected helper text naming the accepted extensions',
        );
        h.cleanupFetch();
    });

    /**
     * The melt-curve bucket ships no acceptedExtensions, so it must leave the
     * picker unfiltered rather than emit an empty accept that filters
     * everything out.
     */
    it('leaves an unrestricted bucket unfiltered and unlabelled', () => {
        const { h, input } = renderBucket('Annotated melt curve files');

        assert.equal(input.getAttribute('accept'), null);
        assert.equal(screen.queryByText(/Accepted file types:/), null);
        h.cleanupFetch();
    });

    /**
     * `accept` only filters the picker's default view — "All files" and
     * drag-and-drop both get past it — so the renderer must refuse the file
     * itself, naming what it wanted.
     */
    it('refuses a gel image dropped into the chromatogram bucket', async () => {
        const { h, input } = renderBucket('Chromatograms', TRACE_EXTENSIONS);

        pick(input, 'gel.png');

        await waitFor(() => {
            assert.ok(
                screen.getByRole('alert').textContent?.includes(
                    'gel.png is not an accepted file type',
                ),
                'expected the rejection to name the offending file',
            );
        });
        assert.ok(
            screen.getByRole('alert').textContent?.includes(
                'Accepted: .abi, .ab1, .scf, .fa, .txt, .docx',
            ),
            'expected the rejection to name the accepted types',
        );
        // Cleared so the same name can be re-picked after the curator converts it.
        assert.equal(input.value, '');
        h.cleanupFetch();
    });

    it('refuses a trace file dropped into the gel-image bucket', async () => {
        const { h, input } = renderBucket('Annotated gel images', IMAGE_EXTENSIONS);

        pick(input, 'trace.ab1');

        await waitFor(() => {
            assert.ok(
                screen.getByRole('alert').textContent?.includes(
                    'trace.ab1 is not an accepted file type',
                ),
                'expected the rejection to name the offending file',
            );
        });
        h.cleanupFetch();
    });

    /** A curator's file off a Windows box may well be uppercase. */
    it('accepts a correct file whose extension is uppercase', async () => {
        const { h, input } = renderBucket('Chromatograms', TRACE_EXTENSIONS);

        pick(input, 'TRACE.AB1');

        // No rejection: the extension check is case-insensitive. The upload
        // itself would hit the stubbed fetch, so only the absence of the
        // client-side refusal is asserted here.
        await waitFor(() => {
            const alert = screen.queryByRole('alert');
            assert.equal(
                alert?.textContent?.includes('is not an accepted file type') ?? false,
                false,
                'uppercase extension should not be refused',
            );
        });
        h.cleanupFetch();
    });

    /** No extension means we cannot show it matches, so it is refused. */
    it('refuses a file with no extension in a restricted bucket', async () => {
        const { h, input } = renderBucket('Annotated gel images', IMAGE_EXTENSIONS);

        pick(input, 'README');

        await waitFor(() => {
            assert.ok(
                screen.getByRole('alert').textContent?.includes(
                    'README is not an accepted file type',
                ),
                'expected a file with no extension to be refused',
            );
        });
        h.cleanupFetch();
    });
});
