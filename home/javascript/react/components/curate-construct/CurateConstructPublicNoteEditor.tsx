import React from 'react';
import {useCurateConstructEditContext} from "./CurateConstructEditContext";

function CurateConstructPublicNoteEditor() {

    const {state, setStateByProxy} = useCurateConstructEditContext();

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