import React, {useState} from 'react';
import {useCurateConstructEditContext} from "./CurateConstructEditContext";

function CurateConstructNoteEditor() {

    const {state, setStateByProxy} = useCurateConstructEditContext();
    const [noteTextValue, setNoteTextValue] = useState('');

    function handleRemoveNote(index) {
        const newNotes = [...state.selectedConstruct.notes];
        newNotes.splice(index, 1);
        setStateByProxy(proxy => {proxy.selectedConstruct.notes = newNotes;});
    }

    function handleAddNote() {
        setStateByProxy(proxy => {
            if (noteTextValue) {
                proxy.selectedConstruct.notes.push({label: noteTextValue, zdbID: null});
            }
        });
        setNoteTextValue('');
    }

    return <>
        {state.selectedConstruct.notes && state.selectedConstruct.notes.map((note, index) => {
            return <div key={index}>
                <span dangerouslySetInnerHTML={{__html: note.label}}></span>{' '}
                <a className='delete fa-trash fa' href='#' onClick={() => handleRemoveNote(index)}></a>
            </div>
        })}
        <textarea
            autoComplete="off"
            value={noteTextValue}
            rows={3}
            cols={50}
            onChange={e => setNoteTextValue(e.target.value)}
        />
        <br/>
        <input type='button'
               value='Add Note'
               disabled={!noteTextValue}
               onClick={handleAddNote}
               />
        </>;
}

export default CurateConstructNoteEditor;