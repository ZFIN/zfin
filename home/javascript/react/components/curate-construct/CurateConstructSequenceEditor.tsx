import React, {useEffect, useState} from 'react';
import {useCurateConstructEditContext} from "./CurateConstructEditContext";
import {backendBaseUrl} from "./DomainInfo";

const calculatedDomain = backendBaseUrl();

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

    useEffect(() => {
        // https://cell-mac.zfin.org/action/construct/construct-do-update/ZDB-TGCONSTRCT-140416-4
        if (state.selectedConstructId) {
            fetch(`${calculatedDomain}/action/construct/construct-do-update/${state.selectedConstructId}`)
                .then(response => response.json())
                .then(data => {
                    if (data && data.length > 0) {
                        const normalizedSequences = data[0].constructSequences.map(seq => { return {label: seq.view, zdbID: seq.zdbID}});

                        setStateByProxy(proxy => {
                            proxy.selectedConstruct.sequences = normalizedSequences;
                        });
                    }
                })
                .catch(error => console.error('Error fetching data:', error));
        }
    }, [state.selectedConstructId]);


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