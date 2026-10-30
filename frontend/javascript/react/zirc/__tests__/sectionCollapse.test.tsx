import { describe, it, afterEach } from 'node:test';
import assert from 'node:assert/strict';
import { act, cleanup, fireEvent, screen } from '@testing-library/react';
import { renderForm, stringSchema } from './renderHelpers';

/**
 * The sections inside a mutation card (Genes, Lesions, Genotyping Assays, ...)
 * collapse from their heading (ZFIN-10543). The toggle is opt-in through the
 * view config's collapsibleSections, which only the mutation editors set, so
 * the submission's own sections keep a plain heading.
 */

const GROUP = {
    type: 'Group',
    label: 'Genes',
    elements: [{ type: 'Control', scope: '#/properties/note' }],
};

function section(config: Record<string, unknown>) {
    return renderForm({ schema: stringSchema('note'), uischema: GROUP, config });
}

function body(): HTMLElement {
    const el = document.getElementById('mutation-1-genes-body');
    assert.ok(el, 'collapsible section has no body wrapper');
    return el;
}

afterEach(() => {
    cleanup();
    window.location.hash = '';
});

describe('collapsible mutation sections', () => {
    it('has no toggle unless the view config asks for one', () => {
        const h = section({ idPrefix: 'mutation-1' });
        try {
            assert.equal(screen.queryByRole('button', { name: /genes/i }), null);
            assert.equal(document.getElementById('mutation-1-genes-body'), null);
        } finally {
            h.cleanupFetch();
        }
    });

    it('starts open and hides the body without unmounting it', () => {
        const h = section({ idPrefix: 'mutation-1', collapsibleSections: true });
        try {
            assert.equal(body().hidden, false);
            fireEvent.click(screen.getByRole('button', { name: 'Collapse Genes' }));
            assert.equal(body().hidden, true);
            // Still mounted, so a pending edit in the section is kept.
            assert.ok(body().querySelector('input, textarea'));
            const toggle = screen.getByRole('button', { name: 'Expand Genes' });
            assert.equal(toggle.getAttribute('aria-expanded'), 'false');
            fireEvent.click(toggle);
            assert.equal(body().hidden, false);
        } finally {
            h.cleanupFetch();
        }
    });

    it('opens when a nav link points at the collapsed section', () => {
        const h = section({ idPrefix: 'mutation-1', collapsibleSections: true });
        try {
            fireEvent.click(screen.getByRole('button', { name: 'Collapse Genes' }));
            assert.equal(body().hidden, true);
            act(() => {
                window.location.hash = '#mutation-1-genes';
                window.dispatchEvent(new HashChangeEvent('hashchange'));
            });
            assert.equal(body().hidden, false);
        } finally {
            h.cleanupFetch();
        }
    });
});
