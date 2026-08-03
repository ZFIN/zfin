import * as React from 'react';

/**
 * Identifies one specific history scope the right-hand ChangeHistoryPanel
 * should focus on. Set by a FieldHistory icon click; cleared by the panel's
 * own clear button. While non-null, the panel ignores its scroll-spy
 * section filter and renders only the focused entries.
 */
export type HistoryFocus = {
    recId: string;
    scope: 'field' | 'section' | 'entity';
    fieldName?: string | null;
    sectionName?: string | null;
    // Display label used in the panel header ("Change History of <label>").
    label: string;
    // Viewport-relative top of the icon that was clicked, captured at
    // click time. ChangeHistoryPanel uses it as the sticky `top:` so the
    // panel lands at the same vertical level as the focused field/section
    // and then sticks there during subsequent scrolling. Unset → panel
    // falls back to its default top offset.
    anchorY?: number;
} | null;

type Ctx = {
    focus: HistoryFocus;
    setFocus: (f: HistoryFocus) => void;
};

// Default no-op so components mounted outside the detail-page provider
// (e.g. preview pages) don't crash; they just don't drive any panel.
export const HistoryFocusContext = React.createContext<Ctx>({
    focus: null,
    setFocus: () => undefined,
});
