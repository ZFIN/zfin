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
 *
 * When the view config sets collapsibleSections (the sections inside a
 * mutation card), a labelled section's heading carries a caret that hides
 * and shows its body. The body stays mounted while collapsed, so an
 * in-progress edit or a nested editor's pending autosave is not dropped.
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
    const bodyId = `${sectionId}-body`;
    const layoutOption = (layout.options as { layout?: string } | undefined)?.layout;
    const isPlain = layoutOption === 'plain';
    const collapsible = view.collapsibleSections && !!label && layoutOption !== 'subsection';

    const [open, setOpen] = React.useState(true);
    // A left-nav link (#mutation-2-genes) to a collapsed section opens it.
    React.useEffect(() => {
        if (!collapsible) {return undefined;}
        const openFromHash = () => {
            if (window.location.hash === `#${sectionId}`) {setOpen(true);}
        };
        openFromHash();
        window.addEventListener('hashchange', openFromHash);
        return () => window.removeEventListener('hashchange', openFromHash);
    }, [collapsible, sectionId]);

    // Hidden by a uiSchema rule (e.g. the per-assayType groups in the assay
    // schema). Without this gate, group-level rules are silently ignored —
    // unlike Controls, which inherit rule handling from withJsonFormsControlProps.
    // Checked after the hooks above so their call order never changes.
    if (visible === false) {return null;}

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

    // A Group nested inside another section's table (e.g. Veterinarian under
    // Additional Info): a subheading row followed by its Control rows, all
    // inside the parent's <tbody>. No section status/history/comments — those
    // are tracked per top-level section.
    if (layoutOption === 'subsection') {
        return (
            <>
                <tr id={sectionId}>
                    <th colSpan={2} scope='colgroup' className='pt-3'>
                        <h3 className='h6 font-weight-bold mb-0'>{label}</h3>
                    </th>
                </tr>
                {children}
            </>
        );
    }

    const sectionStatus = view.sectionStatus[label];

    const body = isPlain ? (
        <div>{children}</div>
    ) : (
        <table className='table table-borderless'>
            <tbody>{children}</tbody>
        </table>
    );

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
                    {collapsible && (
                        <button
                            type='button'
                            className='btn btn-link p-0 mr-1 text-body'
                            onClick={() => setOpen((o) => !o)}
                            aria-expanded={open}
                            aria-controls={bodyId}
                            aria-label={`${open ? 'Collapse' : 'Expand'} ${label}`}
                        >
                            <i className={`fas fa-fw fa-caret-${open ? 'down' : 'right'}`}/>
                        </button>
                    )}
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
            {collapsible ? <div id={bodyId} hidden={!open}>{body}</div> : body}
        </section>
    );
}

export const sectionRendererEntry: JsonFormsRendererRegistryEntry = {
    tester: rankWith(10, uiTypeIs('Group')),
    renderer: withJsonFormsLayoutProps(SectionRenderer),
};
