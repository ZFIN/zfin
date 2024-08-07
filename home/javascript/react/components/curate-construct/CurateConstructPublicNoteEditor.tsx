import React, {useEffect} from 'react';
import {useCurateConstructEditContext} from "./CurateConstructEditContext";
import {backendBaseUrl} from "./DomainInfo";

const calculatedDomain = backendBaseUrl();

function CurateConstructPublicNoteEditor() {

    const {state, setStateByProxy} = useCurateConstructEditContext();

    useEffect(() => {
        // https://cell-mac.zfin.org/action/construct/construct-do-update/ZDB-TGCONSTRCT-140416-4
        if (state.selectedConstructId) {
            fetch(`${calculatedDomain}/action/construct/construct-do-update/${state.selectedConstructId}`)
                .then(response => response.json())
                .then(data => {
                    if (data && data.length > 0) {
                        setStateByProxy(proxy => {
                            proxy.selectedConstruct.publicNote = data[0].constructComments;
                        });
                    }
                })
                .catch(error => console.error('Error fetching data:', error));
        }
    }, [state.selectedConstructId]);


    return <>
        <textarea
            autoComplete="off"
            value={state.selectedConstruct.publicNote || ''}
            rows={3}
            cols={50}
            onChange={e => setStateByProxy(proxy => {proxy.selectedConstruct.publicNote = e.target.value;})}
        />
        </>;
}

export default CurateConstructPublicNoteEditor;