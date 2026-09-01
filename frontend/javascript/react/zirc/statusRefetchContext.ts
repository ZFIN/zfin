import * as React from 'react';

/**
 * Lets deep descendants (e.g. FieldComments) ask the detail page to
 * refetch its status payload after a change that can move a badge —
 * posting/closing a comment flips a field's open-comment IN_PROGRESS
 * overlay, which the server recomputes and we re-render.
 *
 * Default is a no-op so the component is safe to use outside a page that
 * provides it. Both the detail page and the mutation edit page do provide it
 * (the edit page gained status badges in ZFIN-10407).
 */
export const StatusRefetchContext = React.createContext<() => void>(() => { /* no-op */ });
