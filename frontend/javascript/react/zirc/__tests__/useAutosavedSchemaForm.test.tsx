import { describe, it, afterEach } from 'node:test';
import assert from 'node:assert/strict';
import * as React from 'react';
import { act, cleanup, render, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { useAutosavedSchemaForm } from '../schemaForm/useAutosavedSchemaForm';
import { FormFor } from '../api/formHelpers';

/**
 * Regression coverage: collapsing a card ("Done") right after typing used to
 * unmount the editor before the 800ms autosave debounce fired, silently
 * discarding the edit — see useAutosavedSchemaForm.ts's unmount-flush
 * effect. These tests drive the hook directly (no JsonForms, no
 * schema-driven renderers) since the bug lives in the hook's own effect
 * lifecycle, not in any particular field widget.
 */

afterEach(() => {
    cleanup();
});

type Entity = { id: number; additionalInfo: string };

/** No Controls flagged, so every path autosaves and none refresh the parent. */
const PLAIN_UI_SCHEMA = { type: 'VerticalLayout', elements: [] };

type PatchCall = { url: string; body: { path: string; value: unknown } };

function installFetchStub(patches: PatchCall[]): () => void {
    const original = globalThis.fetch;
    globalThis.fetch = (async (input: RequestInfo | URL, init?: RequestInit) => {
        const url = String(input);
        const method = (init?.method ?? 'GET').toUpperCase();
        if (method === 'GET' && /\/test\/form-schema$/.test(url)) {
            return new Response(
                JSON.stringify({ schema: { type: 'object', properties: {} }, uiSchema: PLAIN_UI_SCHEMA }),
                { status: 200, headers: { 'Content-Type': 'application/json' } },
            );
        }
        if (method === 'PATCH') {
            const body = JSON.parse(String(init?.body ?? '{}'));
            patches.push({ url, body });
            return new Response(JSON.stringify({}), {
                status: 200,
                headers: { 'Content-Type': 'application/json' },
            });
        }
        throw new Error(`unexpected fetch in test: ${method} ${url}`);
    }) as typeof globalThis.fetch;
    return () => { globalThis.fetch = original; };
}

/** Exposes the hook's setFormData/status to the test outside of React render. */
type Handle = { setFormData: React.Dispatch<React.SetStateAction<FormFor<Entity> | null>> | null; status: string };

function Harness(props: { entity: Entity; onSaved: () => void; onRefreshParent: () => void; handle: Handle }) {
    const { formData, setFormData, status } = useAutosavedSchemaForm<Entity>({
        entity: props.entity,
        entityId: props.entity.id,
        schemaQueryKey: 'test-schema',
        schemaEndpoint: '/test/form-schema',
        patchEndpointFor: (id) => `/test/${id}`,
        onSaved: props.onSaved,
        onRefreshParent: props.onRefreshParent,
    });
    props.handle.setFormData = formData == null ? null : setFormData;
    props.handle.status = status;
    return null;
}

function renderHarness(entity: Entity, onSaved: () => void, onRefreshParent: () => void) {
    const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false, gcTime: 0 } } });
    const handle: Handle = { setFormData: null, status: 'idle' };
    const result = render(
        <QueryClientProvider client={queryClient}>
            <Harness entity={entity} onSaved={onSaved} onRefreshParent={onRefreshParent} handle={handle} />
        </QueryClientProvider>,
    );
    return { ...result, handle };
}

describe('useAutosavedSchemaForm unmount flush', () => {
    it('flushes a pending debounced edit instead of discarding it when the editor unmounts', async () => {
        const patches: PatchCall[] = [];
        const restoreFetch = installFetchStub(patches);
        let savedCalls = 0;

        const { handle, unmount } = renderHarness(
            { id: 1, additionalInfo: 'old value' },
            () => { savedCalls += 1; },
            () => { throw new Error('refreshesParent was not flagged; onRefreshParent should not fire'); },
        );

        await waitFor(() => assert.ok(handle.setFormData, 'expected the seed effect to populate formData'));

        // Edit the field, then immediately collapse the card — well inside the
        // 800ms debounce window, which is exactly the sequence the bug report
        // described ("fill in Additional Info... click Done").
        act(() => {
            handle.setFormData!((prev) => ({ ...(prev as FormFor<Entity>), additionalInfo: 'new value' }));
        });
        unmount();

        await waitFor(() => assert.equal(patches.length, 1, 'expected the flushed PATCH to have gone out'));
        assert.equal(patches[0].url, '/action/api/zirc/test/1');
        assert.deepEqual(patches[0].body, { path: '/additionalInfo', value: 'new value' });
        await waitFor(() => assert.equal(savedCalls, 1, 'expected onSaved to fire so the entity cache gets invalidated'));

        restoreFetch();
    });

    it('does not fire a stray PATCH on unmount when nothing changed', async () => {
        const patches: PatchCall[] = [];
        const restoreFetch = installFetchStub(patches);

        const { handle, unmount } = renderHarness(
            { id: 2, additionalInfo: 'unchanged' },
            () => { throw new Error('onSaved should not fire when nothing changed'); },
            () => { throw new Error('onRefreshParent should not fire when nothing changed'); },
        );

        await waitFor(() => assert.ok(handle.setFormData));
        unmount();

        // Give any errant async work a turn before asserting the negative.
        await new Promise((resolve) => setTimeout(resolve, 50));
        assert.equal(patches.length, 0);

        restoreFetch();
    });

    it('calls onSaved after an ordinary (non-unmount) autosave completes', async () => {
        const patches: PatchCall[] = [];
        const restoreFetch = installFetchStub(patches);
        let savedCalls = 0;

        const { handle } = renderHarness(
            { id: 3, additionalInfo: 'old value' },
            () => { savedCalls += 1; },
            () => { throw new Error('refreshesParent was not flagged; onRefreshParent should not fire'); },
        );

        await waitFor(() => assert.ok(handle.setFormData));
        act(() => {
            handle.setFormData!((prev) => ({ ...(prev as FormFor<Entity>), additionalInfo: 'new value' }));
        });

        // Real timers: the debounce is a plain setTimeout (AUTOSAVE_DEBOUNCE_MS
        // = 800), same rationale as widgets.test.tsx's dismissal test — faking
        // timers here would deadlock waitFor's own polling.
        await waitFor(() => assert.equal(patches.length, 1), { timeout: 4000, interval: 100 });
        await waitFor(() => assert.equal(savedCalls, 1), { timeout: 4000, interval: 100 });

        restoreFetch();
    });
});
