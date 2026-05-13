// Container plumbing for schema-driven autosaved forms.
//
// The hook owns: the DTO state, the last-committed snapshot, the save
// queue (one in-flight save at a time), monotonic save-event sequence,
// and the latest server-assigned id. Containers just declare the DTO
// shape, supply load/save callbacks, and render `<FormRenderer>` against
// `dto` + `commit`.
//
// Why a queue: (1) with an empty initial id, two concurrent saves would
// each trigger create-on-first-save and produce duplicate rows. (2)
// Out-of-order responses can revert newer edits if responses arrive
// after their successor's. The queue serializes saves so neither
// scenario is possible.

import {useEffect, useRef, useState} from 'react';
import type {SaveEvent} from '../zirc/SaveToast';

export interface UseAutosavedFormOpts<TDto> {
    /** Empty string for the "new" flow — the first save assigns an id. */
    initialId: string;
    /** Factory for the initial DTO when initialId is empty. */
    emptyDto: () => TDto;
    /** Load the DTO from the server. */
    load: (id: string) => Promise<TDto>;
    /**
     * Save one path's worth of change. Receives the latest known id
     * (empty string when none yet), the current DTO (with the change
     * applied — the hook tracks it via an internal ref so save() always
     * sees the post-setDto state, even when commit follows onChange
     * synchronously in the same handler), the path, and the value.
     * Returns the server's updated DTO; the hook reads the saved path
     * off the response to normalize whatever the server changed
     * (case-folding, trimming, etc.) without clobbering concurrent
     * edits to other paths.
     */
    save: (id: string, dto: TDto, path: string, value: unknown) => Promise<TDto>;
    /** Pull the id out of a DTO. Default assumes a ZFIN-style `zdbID`. */
    extractId?: (dto: TDto) => string;
    /** DTO key the id lives at; the hook writes the assigned id here on
     *  first save so a `readonly` field bound to this path can refresh.
     *  Defaults to 'zdbID'. */
    idPath?: string;
    /** Fires when the server assigns an id on first save (history.replaceState etc). */
    onIdAssigned?: (newId: string) => void;
    /** Map a path to a human-readable label for SaveToast. Default capitalises the path. */
    labelForPath?: (path: string) => string;
}

export interface UseAutosavedFormResult<TDto> {
    /** Current DTO, or null while the initial load is in flight. */
    dto: TDto | null;
    /** Non-empty when the initial load failed. */
    loadError: string;
    /** Update the DTO directly — use for keystroke-level changes. */
    setDto: (next: TDto) => void;
    /**
     * Commit one path. Skips if the value already matches the
     * last-saved snapshot. Optimistically marks the path as committed
     * so a re-blur with the same value won't trigger another POST.
     */
    commit: (path: string, value: unknown) => void;
    /** Latest save event for `<SaveToast event={…}/>`. */
    saveEvent: SaveEvent | null;
}

const defaultExtractId = <TDto>(dto: TDto): string => {
    if (dto && typeof dto === 'object' && 'zdbID' in dto) {
        const id = (dto as {zdbID: unknown}).zdbID;
        return typeof id === 'string' ? id : '';
    }
    return '';
};

const defaultLabelForPath = (path: string): string => {
    // 'maternalBackground' -> 'Maternal Background'
    return path
        .replace(/([A-Z])/g, ' $1')
        .replace(/^./, c => c.toUpperCase());
};

export function useAutosavedForm<TDto>(opts: UseAutosavedFormOpts<TDto>): UseAutosavedFormResult<TDto> {
    const extractId = opts.extractId ?? defaultExtractId;
    const labelForPath = opts.labelForPath ?? defaultLabelForPath;

    const initialDto = opts.initialId ? null : opts.emptyDto();
    const [dto, setDtoState] = useState<TDto | null>(initialDto);
    const [loadError, setLoadError] = useState<string>('');
    const [saveEvent, setSaveEvent] = useState<SaveEvent | null>(null);
    /**
     * Last-saved value per path. Reference-or-JSON-equal to the
     * argument of `commit` means "no-op, skip the save."
     */
    const committedRef = useRef<Map<string, unknown>>(new Map());
    /** Latest id known to save closures, ahead of React's render. */
    const idRef = useRef<string>(opts.initialId);
    /**
     * Mirror of `dto` updated synchronously alongside every `setDtoState`
     * call. The save callback runs as a microtask after onCommit fires —
     * before React commits the state update from a sibling onChange — so
     * a setState-driven view of the dto would be stale. The ref is
     * always current.
     */
    const dtoRef = useRef<TDto | null>(initialDto);
    /** Promise chain serializing all saves. */
    const queueRef = useRef<Promise<unknown>>(Promise.resolve());
    /** Monotonic toast event counter (ref so concurrent emits don't share a snapshot). */
    const seqRef = useRef<number>(0);

    function applyDto(next: TDto | null) {
        dtoRef.current = next;
        setDtoState(next);
    }

    function applyDtoUpdate(updater: (prev: TDto | null) => TDto | null) {
        const next = updater(dtoRef.current);
        dtoRef.current = next;
        setDtoState(next);
    }

    // Seed committedRef from emptyDto for the new-row flow so a no-op
    // commit (e.g. user tabs through an untouched field) doesn't fire
    // a create-on-first-save POST with the empty value.
    useEffect(() => {
        if (opts.initialId === '' && dto) {
            seedCommittedFromDto(dto, committedRef.current);
        }
        // run-once for the initial dto reference; further seeding happens
        // after a successful load below.
    }, []);

    useEffect(() => {
        if (!opts.initialId) {
            return;
        }
        let cancelled = false;
        opts.load(opts.initialId)
            .then(data => {
                if (cancelled) {
                    return;
                }
                applyDto(data);
                idRef.current = extractId(data);
                committedRef.current = new Map();
                seedCommittedFromDto(data, committedRef.current);
            })
            .catch(e => {
                if (!cancelled) {
                    setLoadError(e instanceof Error ? e.message : 'Load failed');
                }
            });
        return () => { cancelled = true; };
    }, [opts.initialId]);

    function emit(event: Omit<SaveEvent, 'seq'>) {
        seqRef.current += 1;
        setSaveEvent({...event, seq: seqRef.current});
    }

    function enqueue<T>(fn: () => Promise<T>): Promise<T> {
        const next = queueRef.current.then(fn, fn);
        queueRef.current = next.catch(() => undefined);
        return next;
    }

    function setDto(next: TDto) {
        applyDto(next);
    }

    function commit(path: string, value: unknown) {
        if (isCommitted(committedRef.current, path, value)) {
            return;
        }
        const label = labelForPath(path);
        emit({status: 'saving', label});
        // Optimistic: mark committed now so a re-blur with the same
        // value short-circuits via isCommitted above.
        committedRef.current.set(path, value);
        enqueue(async () => {
            const id = idRef.current;
            const currentDto = dtoRef.current;
            if (currentDto == null) {
                throw new Error('commit() called before DTO is initialized');
            }
            const data = await opts.save(id, currentDto, path, value);
            const newId = extractId(data);
            const wasNew = id === '' && newId !== '';
            if (wasNew) {
                idRef.current = newId;
                opts.onIdAssigned?.(newId);
            }
            // Apply only the saved path from the response. Updating the
            // whole DTO would clobber concurrent edits to other paths if
            // responses arrived out of order — the queue makes that less
            // likely but per-path updates remove the foot-gun entirely.
            // On the first save we *also* apply the server-assigned id
            // (under opts.idPath, default 'zdbID') so any readonly field
            // bound to that path refreshes from the placeholder to the
            // real id without having to reload.
            const normalized = (data as unknown as Record<string, unknown>)[path];
            const idPath = opts.idPath ?? 'zdbID';
            const idValue = (data as unknown as Record<string, unknown>)[idPath];
            applyDtoUpdate(prev => {
                if (!prev) {
                    return data;
                }
                const next: Record<string, unknown> = {...(prev as Record<string, unknown>), [path]: normalized};
                if (wasNew) {
                    next[idPath] = idValue;
                }
                return next as TDto;
            });
            committedRef.current.set(path, normalized);
        })
            .then(() => emit({status: 'saved', label}))
            .catch(e => emit({
                status: 'error',
                label,
                message: e instanceof Error ? e.message : 'Save failed',
            }));
    }

    return {dto, loadError, setDto, commit, saveEvent};
}

// ─── Helpers ──────────────────────────────────────────────────────────────

function seedCommittedFromDto(dto: unknown, committed: Map<string, unknown>): void {
    if (!dto || typeof dto !== 'object') {
        return;
    }
    Object.entries(dto as Record<string, unknown>).forEach(([k, v]) => {
        committed.set(k, v);
    });
}

function isCommitted(committed: Map<string, unknown>, path: string, value: unknown): boolean {
    if (!committed.has(path)) {
        return false;
    }
    const prev = committed.get(path);
    if (prev === value) {
        return true;
    }
    // Reference inequality with objects/arrays — fall back to JSON compare
    // so a fresh array with identical contents still dedupes.
    if (prev != null && value != null && (typeof prev === 'object' || typeof value === 'object')) {
        try {
            return JSON.stringify(prev) === JSON.stringify(value);
        } catch {
            return false;
        }
    }
    // Treat null and undefined as equal — both mean "no value" on the wire.
    if ((prev === null || prev === undefined) && (value === null || value === undefined)) {
        return true;
    }
    return false;
}
