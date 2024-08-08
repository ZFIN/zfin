import React, {useState} from 'react';
import {useCurateConstructEditContext} from "./CurateConstructEditContext";

function CurateConstructSynonymEditor() {

    const {state, setStateByProxy} = useCurateConstructEditContext();
    const [synonymTextValue, setSynonymTextValue] = useState('');

    function handleRemoveSynonym(index) {
        const newSynonyms = [...state.selectedConstruct.synonyms];
        newSynonyms.splice(index, 1);
        setStateByProxy(proxy => {proxy.selectedConstruct.synonyms = newSynonyms;});
    }

    function handleAddSynonym() {
        setStateByProxy(proxy => {
            if (synonymTextValue) {
                proxy.selectedConstruct.synonyms.push({label: synonymTextValue, zdbID: null});
            }
        });
        setSynonymTextValue('');
    }

    function handleAddSynonymKeyDown(event) {
        if (event.key === 'Enter') {
            handleAddSynonym();
        }
    }

    return <>
        {state.selectedConstruct.synonyms && state.selectedConstruct.synonyms.map((synonym, index) => {
            return <div key={index}>
                <span dangerouslySetInnerHTML={{__html: synonym.label}}></span>{' '}
                <a className='delete fa-trash fa' href='#' onClick={() => handleRemoveSynonym(index)}></a>
            </div>
        })}
        <input
            autoComplete="off"
            type="text"
            size="50"
            value={synonymTextValue}
            onChange={e => setSynonymTextValue(e.target.value)}
            onKeyDown={handleAddSynonymKeyDown}
        />
        <input type='button'
               value='+'
               disabled={!synonymTextValue}
               onClick={handleAddSynonym}
               />
        </>;
}

export default CurateConstructSynonymEditor;