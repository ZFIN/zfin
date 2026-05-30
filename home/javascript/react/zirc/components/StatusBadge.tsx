import * as React from 'react';

export type FieldStatus = {
    abbreviation: string;
    displayName: string;
    cssClass: string;
};

type Props = {
    status?: FieldStatus | null;
};

// Inline so the slot reserves space on any page that mounts the schema-form,
// without depending on the legacy detail-page <style> block. Matches the
// width/margin originally defined for .status-slot in line-submission-detail.jsp.
const SLOT_STYLE: React.CSSProperties = {
    display: 'inline-block',
    width: '2.25em',
    textAlign: 'center',
    marginRight: '0.4em',
};

export function StatusBadge({ status }: Props) {
    if (!status) {return <span className='status-slot' style={SLOT_STYLE}/>;}
    return (
        <span className='status-slot' style={SLOT_STYLE}>
            <span className={`badge ${status.cssClass}`} title={status.displayName}>
                {status.abbreviation}
            </span>
        </span>
    );
}
