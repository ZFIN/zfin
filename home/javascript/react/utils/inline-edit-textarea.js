import React, { useState } from 'react';

const InlineEditTextarea = ({
    text: initialText = '',
    error: initialError = '',
    onSave = () => {},
    wrapperClass = '',
    errorClass = 'error',
    defaultText = 'Click to add',
    textAreaClass = 'form-control form-group',
    saveButtonClass = 'btn btn-primary',
    cancelButtonClass = 'btn btn-outline-secondary',
    useIcons = false,
    useInput = false
}) => {
    const [text, setText] = useState(initialText);
    const [error, setError] = useState(initialError);
    const [originalText, setOriginalText] = useState('');
    const [editing, setEditing] = useState(false);
    const [saving, setSaving] = useState(false);

    const beginEdit = () => {
        setOriginalText(text);
        setEditing(true);
    };

    const cancelEdit = () => {
        setText(originalText);
        setEditing(false);
    };

    const saveEdit = async () => {
        setSaving(true);
        try {
            await onSave(text);
            setEditing(false);
            setError('');
        } catch (response) {
            if (response && response.message) {
                setError(response.message);
            }
        } finally {
            setSaving(false);
        }
    };

    return (
        <div className={wrapperClass}>
            {error && <div className={errorClass}>{error}</div>}
            {!editing && (
                <div onClick={beginEdit} className='inline-edit' title='Click to edit'>
                    {text ? <div dangerouslySetInnerHTML={{ __html: text }}/> : <div className='muted'>{defaultText}</div>}
                </div>
            )}
            {editing && (
                <div>
                    {useInput ? (
                        <input type='text' value={text} onChange={(e) => setText(e.target.value)} className={textAreaClass} />
                    ) : (
                        <textarea value={text} onChange={(e) => setText(e.target.value)} className={textAreaClass} rows='5' />
                    )}
                    <button type='button' onClick={cancelEdit} className={cancelButtonClass}>
                        {useIcons ? <i className='fas fa-fw fa-times'/> : 'Cancel'}
                    </button>
                    <button type='button' onClick={saveEdit} className={saveButtonClass} disabled={saving}>
                        {saving ? (
                            <i className='fas fa-spinner fa-spin'/>
                        ) : useIcons ? (
                            <i className='fas fa-fw fa-check'/>
                        ) : (
                            'Save'
                        )}
                    </button>
                </div>
            )}
        </div>
    );
};

export default InlineEditTextarea;
