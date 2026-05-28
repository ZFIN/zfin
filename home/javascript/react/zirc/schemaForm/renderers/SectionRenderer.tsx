import * as React from 'react';
import {
    GroupLayout,
    JsonFormsRendererRegistryEntry,
    LayoutProps,
    rankWith,
    uiTypeIs,
} from '@jsonforms/core';
import { ResolvedJsonFormsDispatch, withJsonFormsLayoutProps } from '@jsonforms/react';
import { viewConfigFrom, commentsEnabled } from '../useViewConfig';
import { StatusBadge } from '../../components/StatusBadge';
import { FieldHistory } from '../../components/FieldHistory';
import { FieldComments } from '../../components/FieldComments';

/**
 * Renders a uiSchema "Group" element as a ZFIN-styled section: section.section
 * with an h2.heading.
 *
 * Default body layout is a table.table-borderless whose <tr> rows come from
 * the row-style Control renderers. Groups whose uiSchema sets
 * options.layout = 'plain' get a plain <div> wrapper instead — for sections
 * like Mutations whose body is a list of cards, not a table of fields.
 */
function SectionRenderer({
    uischema,
    schema,
    path,
    enabled,
    visible,
    renderers,
    cells,
    config,
}: LayoutProps) {
    // Hidden by a uiSchema rule (e.g. the per-assayType groups in the assay
    // schema). Without this gate, group-level rules are silently ignored —
    // unlike Controls, which inherit rule handling from withJsonFormsControlProps.
    if (visible === false) {return null;}
    const layout = uischema as GroupLayout;
    const label = layout.label ?? schema?.title ?? '';
    const view = viewConfigFrom(config);
    const slug = label
        ? label.toLowerCase().replace(/[^a-z0-9-_:.]/g, '-').replace(/-+/g, '-')
        : 'section';
    // When idPrefix is set (e.g. "mutation-1" for a nested mutation card),
    // scope this section's id so it doesn't collide with sibling cards'
    // sections that share the same label.
    const sectionId = view.idPrefix ? `${view.idPrefix}-${slug}` : slug;
    const headingId = `${sectionId}-heading`;
    const isPlain =
        (layout.options as { layout?: string } | undefined)?.layout === 'plain';

    const children = (layout.elements ?? []).map((child, index) => (
        <ResolvedJsonFormsDispatch
            key={`${path}-${index}`}
            uischema={child}
            schema={schema}
            path={path}
            enabled={enabled}
            renderers={renderers}
            cells={cells}
        />
    ));

    const sectionStatus = view.sectionStatus[label];

    // Headless groups are used as visibility/layout containers inside the
    // inline aggregate editors (Lesion/Gene/Assay/Phenotype) where inner
    // sub-section headings would clutter the card. The visibility rule and
    // table wrapper still apply; only the <h2> and its section-scope
    // history/comments accessories are skipped.
    const sectionProps = label
        ? { id: sectionId, 'aria-labelledby': headingId }
        : {};

    return (
        <section className='section' {...sectionProps}>
            {label && (
                <h2 id={headingId} className='heading'>
                    {sectionStatus && <StatusBadge status={sectionStatus}/>}
                    {label}
                    <FieldHistory
                        recId={view.recId}
                        scope='section'
                        sectionName={label}
                        label={label}
                    />
                    {commentsEnabled(uischema) && (
                        <FieldComments
                            recId={view.recId}
                            scope='section'
                            sectionName={label}
                            label={label}
                        />
                    )}
                </h2>
            )}
            {isPlain ? (
                <div className='ml-4' style={{ marginLeft: '1.5rem' }}>
                    {children}
                </div>
            ) : (
                <table
                    className='table table-borderless'
                    style={{ marginLeft: '1.5rem' }}
                >
                    <tbody>{children}</tbody>
                </table>
            )}
        </section>
    );
}

export const sectionRendererEntry: JsonFormsRendererRegistryEntry = {
    tester: rankWith(10, uiTypeIs('Group')),
    renderer: withJsonFormsLayoutProps(SectionRenderer),
};
