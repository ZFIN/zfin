import * as React from 'react';
import {
    GroupLayout,
    JsonFormsRendererRegistryEntry,
    LayoutProps,
    rankWith,
    uiTypeIs,
} from '@jsonforms/core';
import { ResolvedJsonFormsDispatch, withJsonFormsLayoutProps } from '@jsonforms/react';
import { viewConfigFrom } from '../useViewConfig';
import { StatusBadge } from '../../components/StatusBadge';
import { FieldHistory } from '../../components/FieldHistory';

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
    const sectionId = label
        ? label.toLowerCase().replace(/[^a-z0-9-_:.]/g, '-').replace(/-+/g, '-')
        : 'section';
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

    const view = viewConfigFrom(config);
    const sectionStatus = view.sectionStatus[label];
    const sectionUpdates = view.sectionUpdates[label];

    return (
        <section className='section' id={sectionId} aria-labelledby={headingId}>
            <h2 id={headingId} className='heading'>
                <StatusBadge status={sectionStatus}/>
                {label}
                <FieldHistory
                    fieldKey={`section-${sectionId}`}
                    label={label}
                    updates={sectionUpdates}
                />
            </h2>
            {isPlain ? (
                <div>{children}</div>
            ) : (
                <table className='table table-borderless'>
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
