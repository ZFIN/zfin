import * as React from 'react';
import type { LineSubmissionDTO } from '../api/types';
import type { FieldStatus } from './StatusBadge';

type Props = {
    submission: LineSubmissionDTO;
    sectionStatus: Record<string, FieldStatus>;
};

// Top-level sections in display order; mirrors ZircFormSchema.uiSchema()
// Group labels (and LineSubmissionStatusComputer.bySection keys).
const SECTIONS = ['Overview', 'Mutations', 'Linked Features', 'Background', 'Additional Info'];

// Status abbreviation → cell background. Matches the legacy
// line-submission-detail.jsp status-overview-bar palette.
const CELL_BG: Record<string, string> = {
    M:  '#dc3545', // Missing  — red
    IP: '#ffc107', // In Progress — amber
    C:  '#4f9c4a', // Complete — green
    A:  '#007bff', // Approved — blue
};

function domId(label: string): string {
    return label.toLowerCase().replace(/[^a-z0-9-_:.]/g, '-').replace(/-+/g, '-');
}

const cellBase: React.CSSProperties = {
    border: '1px solid #333',
    fontSize: '0.75rem',
    lineHeight: 1.1,
    textAlign: 'center',
    verticalAlign: 'middle',
    padding: '0.35rem 0.4rem',
    minWidth: '4.5em',
    maxWidth: '6.5em',
};

/**
 * Workflow status bar pinned at the top of the detail page. First two
 * cells show identity data (Line name, Date Started); the rest mirror the
 * top-level sections and take their fill colour from each section's
 * rolled-up status. Clicking a section cell jumps to its anchor.
 */
export function StatusOverviewBar({ submission, sectionStatus }: Props) {
    // One LineSubmission can carry many Mutations. Each mutation contributes
    // one entry: the display label is the server-resolved alleleName when
    // alleleInZfin=true (DTO sets it from Feature.abbreviation), otherwise
    // the raw alleleDesignation typed in by the curator. If the designation
    // is a ZDB-ID we render it as a link to the feature page (/{zdbID});
    // free-text designations are plain text. Empty entries are dropped.
    type AlleleEntry = { label: string; href: string | null };
    const alleleEntries: AlleleEntry[] = (submission.mutations ?? [])
        .map((m) => {
            const id = m.alleleDesignation;
            const label = m.alleleName ?? id;
            if (!label || label.trim().length === 0) {return null;}
            const href = id && id.startsWith('ZDB-') ? `/${id}` : null;
            return { label, href };
        })
        .filter((e): e is AlleleEntry => e !== null);
    // Designations can be longer than the section cells; let this column
    // stretch but keep a hard cap so it never crowds the section cells.
    const alleleCell: React.CSSProperties = { ...cellBase, minWidth: '8em', maxWidth: '16em' };
    const alleleTitle = alleleEntries.map((e) => e.label).join(', ');
    return (
        <div className='mb-4'>
            <h5 className='mt-3 mb-1'>Status Overview</h5>
            <div className='d-flex align-items-center' style={{ gap: '1.5rem' }}>
                <table style={{ borderCollapse: 'collapse' }}>
                    <thead>
                        <tr>
                            <th style={{ ...cellBase, fontWeight: 600, background: '#fff' }}>Line</th>
                            <th style={{ ...alleleCell, fontWeight: 600, background: '#fff' }}>
                                Allele<br/>Designation
                            </th>
                            <th style={{ ...cellBase, fontWeight: 600, background: '#fff' }}>
                                Date<br/>Started
                            </th>
                            {SECTIONS.map((s) => (
                                <th key={s} style={{ ...cellBase, fontWeight: 600, background: '#fff' }}>
                                    {s}
                                </th>
                            ))}
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td style={{ ...cellBase, background: '#fffbe6', fontWeight: 500 }}>
                                {submission.name ?? <span className='text-muted'>&mdash;</span>}
                            </td>
                            <td title={alleleTitle} style={{ ...alleleCell, background: '#fffbe6', fontWeight: 500, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                                {alleleEntries.length === 0
                                    ? <span className='text-muted'>&mdash;</span>
                                    : alleleEntries.map((e, i) => (
                                        <React.Fragment key={i}>
                                            {i > 0 && ', '}
                                            {e.href
                                                ? <a href={e.href}>{e.label}</a>
                                                : e.label}
                                        </React.Fragment>
                                    ))}
                            </td>
                            <td style={{ ...cellBase, background: '#fffbe6', fontWeight: 500 }}>
                                {submission.createdAt ?? <span className='text-muted'>&mdash;</span>}
                            </td>
                            {SECTIONS.map((s) => {
                                const st = sectionStatus[s];
                                const bg = st ? (CELL_BG[st.abbreviation] ?? '#fff') : '#fff';
                                return (
                                    <td
                                        key={s}
                                        title={st?.displayName ?? ''}
                                        style={{ ...cellBase, background: bg, cursor: 'pointer', height: '2.2em' }}
                                        onClick={() => {
                                            const el = document.getElementById(domId(s));
                                            if (el) {el.scrollIntoView({ behavior: 'smooth', block: 'start' });}
                                        }}
                                    />
                                );
                            })}
                        </tr>
                    </tbody>
                </table>
                <div className='small text-muted'>
                    <div><span className='badge badge-danger'>M</span>&nbsp;Missing</div>
                    <div><span className='badge badge-warning'>IP</span>&nbsp;In&nbsp;Progress</div>
                    <div><span className='badge badge-success'>C</span>&nbsp;Complete</div>
                    <div><span className='badge badge-primary'>A</span>&nbsp;Approved</div>
                </div>
            </div>
        </div>
    );
}
