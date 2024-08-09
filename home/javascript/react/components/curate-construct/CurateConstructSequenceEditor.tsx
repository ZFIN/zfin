import React from 'react';
import {useCurateConstructEditContext} from "./CurateConstructEditContext";

function CurateConstructSequenceEditor() {

    const {state, setStateByProxy} = useCurateConstructEditContext();

    function setSequenceTextValue(value) {
        setStateByProxy(proxy => {proxy.stagedSequence = value;});
    }

    function handleRemoveSequence(index) {
        const newSequences = [...state.selectedConstruct.sequences];
        newSequences.splice(index, 1);
        setStateByProxy(proxy => {proxy.selectedConstruct.sequences = newSequences;});
    }

    function handleAddSequence() {
        setStateByProxy(proxy => {
            if (state.stagedSequence) {
                proxy.selectedConstruct.sequences.push({label: state.stagedSequence, zdbID: null});
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
            value={state.stagedSequence}
            onChange={e => setSequenceTextValue(e.target.value)}
            onKeyDown={handleAddSequenceKeyDown}
        />
        <input type='button'
               value='+'
               disabled={!state.stagedSequence}
               onClick={handleAddSequence}
               />
        </>;
}

export default CurateConstructSequenceEditor;