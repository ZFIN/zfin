import React, {useState} from 'react';
import {useCurateConstructEditContext} from "./CurateConstructEditContext";

function CurateConstructSequenceEditor() {

    const {state, setStateByProxy} = useCurateConstructEditContext();
    const [sequenceTextValue, setSequenceTextValue] = useState('');

    function handleRemoveSequence(index) {
        const newSequences = [...state.selectedConstruct.sequences];
        newSequences.splice(index, 1);
        setStateByProxy(proxy => {proxy.selectedConstruct.sequences = newSequences;});
    }

    function handleAddSequence() {
        setStateByProxy(proxy => {
            if (sequenceTextValue) {
                proxy.selectedConstruct.sequences.push({label: sequenceTextValue, zdbID: null});
            }
        });
        setSequenceTextValue('');
    }

    function handleAddSequenceKeyDown(event) {
        if (event.key === 'Enter') {
            handleAddSequence();
        }
    }

    return <>
        {state.selectedConstruct.sequences && state.selectedConstruct.sequences.map((sequence, index) => {
            return <div key={index}>
                <span dangerouslySetInnerHTML={{__html: sequence.label}}></span>{' '}
                <a className='delete fa-trash fa' href='#' onClick={() => handleRemoveSequence(index)}></a>
            </div>
        })}
        <input
            autoComplete="off"
            type="text"
            size="50"
            value={sequenceTextValue}
            onChange={e => setSequenceTextValue(e.target.value)}
            onKeyDown={handleAddSequenceKeyDown}
        />
        <input type='button'
               value='+'
               disabled={!sequenceTextValue}
               onClick={handleAddSequence}
               />
        </>;
}

export default CurateConstructSequenceEditor;