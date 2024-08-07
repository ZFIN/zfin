import React, {useEffect, useState} from 'react';
import {useCurateConstructEditContext} from "./CurateConstructEditContext";
import {backendBaseUrl} from "./DomainInfo";

const calculatedDomain = backendBaseUrl();

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

    useEffect(() => {
        // https://cell-mac.zfin.org/action/construct/construct-do-update/ZDB-TGCONSTRCT-140416-4
        if (state.selectedConstructId) {
            fetch(`${calculatedDomain}/action/construct/construct-do-update/${state.selectedConstructId}`)
                .then(response => response.json())
                .then(data => {
                    if (data && data.length > 0) {
                        const normalizedNotes = data[0].constructCuratorNotes.map(note => { return {label: note.noteData, zdbID: note.zdbID}});

                        setStateByProxy(proxy => {
                            proxy.selectedConstruct.notes = normalizedNotes;
                        });
                    }
                })
                .catch(error => console.error('Error fetching data:', error));
        }
    }, [state.selectedConstructId]);


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