import React from 'react';

export type FieldType = 'text' | 'textarea' | 'bool';

export interface FieldDef<T extends string = string> {
    field: T;
    label: string;
    type: FieldType;
    /** Optional id prefix for the input — useful when multiple components
     *  on the page would otherwise collide on `ls-field-name` etc. Defaults
     *  to "ls-field" for the line-submission form. */
    idPrefix?: string;
}

/**
 * Render a value (string | boolean | null | undefined) into the string
 * representation a controlled `<input>` / `<textarea>` / radio expects.
 * Bools become `'true'` / `'false'`; null / undefined become `''`.
 */
export function valueToInputString(v: string | boolean | null | undefined): string {
    if (v === null || v === undefined) {
        return '';
    }
    if (typeof v === 'boolean') {
        return v ? 'true' : 'false';
    }
    return v;
}

interface FieldRowProps<T extends string> {
    def: FieldDef<T>;
    value: string;
    onChange: (next: string) => void;
    onCommit: (next: string) => void;
}

export const FieldRow = <T extends string>({def, value, onChange, onCommit}: FieldRowProps<T>) => {
    const prefix = def.idPrefix ?? 'ls-field';
    const inputId = `${prefix}-${def.field}`;
    const labelId = `${prefix}-label-${def.field}`;

    function renderInput() {
        if (def.type === 'textarea') {
            return (
                <textarea
                    id={inputId}
                    className='form-control'
                    rows={3}
                    value={value}
                    onChange={e => onChange(e.target.value)}
                    onBlur={() => onCommit(value)}
                />
            );
        }
        if (def.type === 'bool') {
            const groupName = `${prefix}-group-${def.field}`;
            // Two-state radio: Yes / No. Initial null shows nothing checked,
            // but once the user picks a value there's no way to unset it
            // (matches the YAML form spec — null is "not yet answered").
            const options: Array<[string, string]> = [['true', 'Yes'], ['false', 'No']];
            return (
                <div role='radiogroup' aria-labelledby={labelId}>
                    {options.map(([v, lbl]) => {
                        const radioId = `${inputId}-${v}`;
                        return (
                            <div className='form-check form-check-inline' key={v}>
                                <input
                                    type='radio'
                                    id={radioId}
                                    className='form-check-input'
                                    name={groupName}
                                    value={v}
                                    checked={value === v}
                                    onChange={() => { onChange(v); onCommit(v); }}
                                />
                                <label className='form-check-label' htmlFor={radioId}>{lbl}</label>
                            </div>
                        );
                    })}
                </div>
            );
        }
        return (
            <input
                id={inputId}
                type='text'
                className='form-control'
                value={value}
                onChange={e => onChange(e.target.value)}
                onBlur={() => onCommit(value)}
            />
        );
    }

    return (
        <tr>
            <th className='w-25' scope='row' id={labelId}>
                {def.type === 'bool'
                    ? def.label
                    : <label htmlFor={inputId} className='mb-0'>{def.label}</label>}
            </th>
            <td>{renderInput()}</td>
        </tr>
    );
};

export interface SectionProps {
    id: string;
    title: string;
    children: React.ReactNode;
}

export const Section = ({id, title, children}: SectionProps) => (
    <section className='section' id={id} aria-labelledby={`${id}-heading`}>
        <h2 id={`${id}-heading`} className='heading'>{title}</h2>
        {children}
    </section>
);

export const FieldsTable = ({children}: {children: React.ReactNode}) => (
    <table className='table table-borderless'>
        <tbody>{children}</tbody>
    </table>
);
