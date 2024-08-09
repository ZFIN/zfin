import React from 'react';
import {useCurateConstructEditContext} from './CurateConstructEditContext';

export default function CurateConstructNoteEditor() {

    const {state, setStateByProxy} = useCurateConstructEditContext();

    function setNoteTextValue(value) {
        setStateByProxy(proxy => {proxy.stagedNote = value;});
    }

    function handleRemoveNote(index) {
        const newNotes = [...state.selectedConstruct.notes];
        newNotes.splice(index, 1);
        setStateByProxy(proxy => {proxy.selectedConstruct.notes = newNotes;});
    }

    function handleAddNote() {
        setStateByProxy(proxy => {
            if (state.stagedNote) {
                proxy.selectedConstruct.notes.push({label: state.stagedNote, zdbID: null});
            }
        });
        setNoteTextValue('');
    }

    return <>
        {state.selectedConstruct.notes && state.selectedConstruct.notes.map((note, index) => {
            return <div key={index}>
                <span dangerouslySetInnerHTML={{__html: note.label}}/>{' '}
                <a className='delete fa-trash fa' href='#' onClick={() => handleRemoveNote(index)}/>
            </div>
        })}
        <textarea
            autoComplete='off'
            value={state.stagedNote}
            rows={3}
            cols={50}
            onChange={e => setNoteTextValue(e.target.value)}
        />
        <br/>
        <input
            type='button'
            value='Add Note'
            disabled={!state.stagedNote}
            onClick={handleAddNote}
        />
    </>;
}
