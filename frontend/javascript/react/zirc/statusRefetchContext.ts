import * as React from 'react';

/**
 * Lets deep descendants (e.g. FieldComments) ask the detail page to
 * refetch its status payload after a change that can move a badge —
 * posting/closing a comment flips a field's open-comment IN_PROGRESS
 * overlay, which the server recomputes and we re-render.
 *
 * Default is a no-op so the component is safe to use outside the detail
 * page (e.g. the edit page, where status badges aren't shown).
 */
export const StatusRefetchContext = React.createContext<() => void>(() => { /* no-op */ });
