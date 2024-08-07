import React, {useEffect, useState} from 'react';
import {useCurateConstructEditContext} from "./CurateConstructEditContext";
import {backendBaseUrl} from "./DomainInfo";

const calculatedDomain = backendBaseUrl();

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

    useEffect(() => {
        // https://cell-mac.zfin.org/action/construct/construct-do-update/ZDB-TGCONSTRCT-140416-4
        if (state.selectedConstructId) {
            fetch(`${calculatedDomain}/action/construct/construct-do-update/${state.selectedConstructId}`)
                .then(response => response.json())
                .then(data => {
                    if (data && data.length > 0) {
                        const normalizedSynonyms = data[0].constructAliases.map(syn => { return {label: syn.alias, zdbID: syn.aliasZdbID}});

                        setStateByProxy(proxy => {
                            proxy.selectedConstruct.synonyms = normalizedSynonyms;
                        });
                    }
                })
                .catch(error => console.error('Error fetching data:', error));
        }
    }, [state.selectedConstructId]);


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