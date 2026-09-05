import * as React from 'react';
import {
    and,
    ControlProps,
    isControl,
    JsonFormsRendererRegistryEntry,
    optionIs,
    rankWith,
} from '@jsonforms/core';
import { withJsonFormsControlProps, useJsonForms } from '@jsonforms/react';
import { viewConfigFrom, leafOf, commentsEnabled } from '../useViewConfig';
import { StatusBadge } from '../../components/StatusBadge';
import { FieldHistory } from '../../components/FieldHistory';
import { FieldComments } from '../../components/FieldComments';

type VendorCatalogOptions = {
    /** Sibling field holding the vendor; the bound field is the catalog number. */
    vendorField?: string;
    placeholder?: string;
    helpText?: string;
};

/**
 * Two narrow labelled boxes on one row: "Vendor [ ] Catalog # [ ]".
 *
 * <p>ZFIN-10419. The restriction-enzyme catalog field used to be a single box
 * whose placeholder read "vendor + cat #", i.e. one field doing two jobs. The
 * curators' mockup keeps the existing row label on the left and splits the value
 * cell into two labelled inputs, so this is a composite control rather than two
 * separate rows.
 *
 * <p>Sibling writing follows AminoAcidChangeRenderer: the bound path is the
 * catalog field, and the vendor sits alongside it under the same parent, named
 * by `options.vendorField` so the schema stays the only place field names are
 * declared.
 *
 * <p>No validation, per the ticket: both boxes are plain text.
 */
function VendorCatalogRenderer({
    data, handleChange, path, label, visible, uischema, config,
}: ControlProps) {
    const ctx = useJsonForms();

    if (visible === false) {return null;}

    const fieldName = leafOf(path);
    const catalogId = `fr-${fieldName}`;
    const labelId = `fr-label-${fieldName}`;
    const view = viewConfigFrom(config);

    const opts = ((uischema as { options?: VendorCatalogOptions } | undefined)?.options) ?? {};
    const vendorField = opts.vendorField;

    // Siblings sit alongside the bound path under the same parent, so swap the
    // last segment rather than assuming a top-level field.
    const siblingPath = (name: string) =>
        path.includes('.') ? `${path.slice(0, path.lastIndexOf('.') + 1)}${name}` : name;

    const root = (ctx.core?.data ?? {}) as Record<string, unknown>;
    const catalog = (data as string | undefined) ?? '';
    const vendor = vendorField ? ((root[vendorField] as string | undefined) ?? '') : '';

    if (view.readonly) {
        // "NEB R0580S" reads better than two separate rows for what curators
        // think of as one piece of information.
        const display = [vendor, catalog].filter((v) => v !== '').join(' ');
        return (
            <tr>
                <th className='text-nowrap pr-3' scope='row' style={{ width: '1%' }} id={labelId}>
                    <StatusBadge status={view.fieldStatus[fieldName]}/>
                    {label}
                </th>
                <td>
                    {display === '' ? <span className='text-muted'>&mdash;</span> : display}
                    <FieldHistory
                        recId={view.recId}
                        scope='field'
                        fieldName={fieldName}
                        label={label ?? fieldName}
                    />
                    {commentsEnabled(uischema) && (
                        <FieldComments
                            recId={view.recId}
                            scope='field'
                            fieldName={fieldName}
                            label={label ?? fieldName}
                        />
                    )}
                </td>
            </tr>
        );
    }

    // Narrow: these hold a short vendor abbreviation and a catalog number, not
    // free prose, and the mockup shows them noticeably smaller than the
    // enzyme-name box above.
    const boxStyle = { maxWidth: '12em' } as const;

    return (
        <tr>
            <th className='text-nowrap pr-3' scope='row' style={{ width: '1%' }} id={labelId}>
                <label htmlFor={catalogId} className='mb-0'>{label}</label>
            </th>
            <td>
                <div className='d-flex align-items-center' style={{ gap: 8, flexWrap: 'wrap' }}>
                    <label htmlFor={`${catalogId}-vendor`} className='mb-0 font-weight-bold'>
                        Vendor
                    </label>
                    <input
                        id={`${catalogId}-vendor`}
                        type='text'
                        className='form-control'
                        style={boxStyle}
                        value={vendor}
                        autoComplete='off'
                        disabled={!vendorField}
                        onChange={(e) => {
                            if (vendorField) {
                                handleChange(siblingPath(vendorField), e.target.value);
                            }
                        }}
                    />
                    <label htmlFor={catalogId} className='mb-0 font-weight-bold'>
                        Catalog #
                    </label>
                    <input
                        id={catalogId}
                        type='text'
                        className='form-control'
                        style={boxStyle}
                        value={catalog}
                        autoComplete='off'
                        onChange={(e) => handleChange(path, e.target.value)}
                    />
                </div>
                {opts.helpText && (
                    <small className='form-text text-muted'>{opts.helpText}</small>
                )}
            </td>
        </tr>
    );
}

export const vendorCatalogRendererEntry: JsonFormsRendererRegistryEntry = {
    tester: rankWith(30, and(isControl, optionIs('widget', 'vendorCatalog'))),
    renderer: withJsonFormsControlProps(VendorCatalogRenderer),
};
