import * as React from 'react';
import {
    and,
    ControlProps,
    isControl,
    JsonFormsRendererRegistryEntry,
    optionIs,
    rankWith,
} from '@jsonforms/core';
import { withJsonFormsControlProps } from '@jsonforms/react';
import { viewConfigFrom, leafOf, commentsEnabled } from '../useViewConfig';
import { DEFAULT_ALPHABET, baseCount, caretAfterNormalize, normalizeSequence } from '../nucleotides';
import { StatusBadge } from '../../components/StatusBadge';
import { FieldHistory } from '../../components/FieldHistory';
import { FieldComments } from '../../components/FieldComments';
import { ValueDisplay } from '../../components/ValueDisplay';

type NucleotideOptions = {
    alphabet?: string;
    /** Names the count, e.g. "Lesion size: 13 bp". Bare "13 bp" when unset. */
    sizeLabel?: string;
    multi?: boolean;
    placeholder?: string;
    helpText?: string;
    infoHref?: string;
};

/**
 * Constrained DNA-sequence input. Everything typed or pasted is uppercased
 * and reduced to `options.alphabet` (default ACGTN), and the field carries a
 * live base count so a paste that silently lost content reads short.
 *
 * Ranks above both TextareaRowRenderer (20) and RowControlRenderer (10), so
 * a Control can keep `options.multi` for its textarea shape and still land
 * here.
 *
 * The count shown here is advisory. The stored lesion sizes are recomputed
 * server-side on every save (ZircSubmissionService#recalcLesionSizes) and
 * displayed by AutoSizeRenderer; this is just immediate feedback while
 * typing, which is why nothing is written back to a size field from here.
 */
function NucleotideSequenceRenderer({
    data,
    handleChange,
    path,
    label,
    required,
    errors,
    visible,
    uischema,
    config,
}: ControlProps) {
    const inputRef = React.useRef<HTMLInputElement & HTMLTextAreaElement>(null);
    // Set during onChange, applied after the controlled re-render paints.
    const caretRef = React.useRef<number | null>(null);

    React.useLayoutEffect(() => {
        const caret = caretRef.current;
        caretRef.current = null;
        if (caret != null && inputRef.current) {
            inputRef.current.setSelectionRange(caret, caret);
        }
    });

    if (visible === false) {return null;}

    const fieldName = leafOf(path);
    const inputId = `fr-${fieldName}`;
    const labelId = `fr-label-${fieldName}`;
    const opts = ((uischema as { options?: NucleotideOptions } | undefined)?.options) ?? {};
    const alphabet = opts.alphabet ?? DEFAULT_ALPHABET;
    const sizeText = (v: string) =>
        `${opts.sizeLabel ? `${opts.sizeLabel}: ` : ''}${baseCount(v, alphabet)} bp`;
    const view = viewConfigFrom(config);
    const value = (data as string | undefined) ?? '';

    if (view.readonly) {
        return (
            <tr>
                <th className='text-nowrap pr-3' scope='row' style={{ width: '1%' }} id={labelId}>
                    <StatusBadge status={view.fieldStatus[fieldName]}/>
                    {label}
                </th>
                <td>
                    <div style={{ whiteSpace: 'pre-wrap', wordBreak: 'break-all' }}>
                        <ValueDisplay value={data}/>
                    </div>
                    {value !== '' && (
                        <small className='form-text text-muted'>{sizeText(value)}</small>
                    )}
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

    const onChange = (
        e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>,
    ) => {
        const raw = e.target.value;
        const caret = e.target.selectionStart ?? raw.length;
        caretRef.current = caretAfterNormalize(raw, caret, alphabet);
        handleChange(path, normalizeSequence(raw, alphabet));
    };

    const shared = {
        id: inputId,
        className: 'form-control',
        value,
        onChange,
        placeholder: opts.placeholder,
        spellCheck: false,
        autoComplete: 'off' as const,
        // Sequence is read in blocks; a proportional font makes miscounting
        // easy and makes the runs of identical bases hard to scan.
        style: { fontFamily: 'monospace' },
    };

    return (
        <tr>
            <th className='text-nowrap pr-3' scope='row' style={{ width: '1%' }} id={labelId}>
                <label htmlFor={inputId} className='mb-0'>
                    {label}{required ? ' *' : ''}
                </label>
                {opts.infoHref && (
                    <a
                        href={opts.infoHref}
                        target='_blank'
                        rel='noopener noreferrer'
                        className='ml-1 small'
                        aria-label={`More info about ${label}`}
                        title='More info'
                    >
                        (info)
                    </a>
                )}
            </th>
            <td>
                <div style={{ maxWidth: '40em' }}>
                    {opts.multi
                        ? <textarea {...shared} ref={inputRef} rows={3}/>
                        : <input {...shared} ref={inputRef} type='text'/>}
                    <small className='form-text text-muted'>
                        {value === ''
                            ? `${alphabet.toUpperCase().split('').join(' / ')} only`
                            : sizeText(value)}
                        {opts.helpText && <> — {opts.helpText}</>}
                    </small>
                    {errors && <small className='text-danger'>{errors}</small>}
                </div>
            </td>
        </tr>
    );
}

export const nucleotideSequenceRendererEntry: JsonFormsRendererRegistryEntry = {
    tester: rankWith(30, and(isControl, optionIs('widget', 'nucleotideSequence'))),
    renderer: withJsonFormsControlProps(NucleotideSequenceRenderer),
};
