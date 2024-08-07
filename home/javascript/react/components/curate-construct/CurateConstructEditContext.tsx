import React, {createContext, useState, useEffect} from 'react';
import {Cassette, MarkerNameAndZdbId} from './ConstructTypes';
import produce from "immer";

interface ConstructUnderEdit {
    chosenType: string;
    prefix: string;
    publicNote: string;
    cassettes: Cassette[];
    synonyms: MarkerNameAndZdbId[];
    sequences: MarkerNameAndZdbId[];
    notes: MarkerNameAndZdbId[];
    editCassetteMode: boolean;
    editCassetteIndex: number;
    addCassetteMode: boolean;
}

interface CurateConstructEditState {
    publicationId: string;
    selectedConstructId: string;
    selectedConstruct: ConstructUnderEdit;
}

interface CurateConstructEditStateAndSetter {
    state: CurateConstructEditState;
    // setState: React.Dispatch<React.SetStateAction<CurateConstructEditState>>;
    setStateByProxy: (fn: (draft: CurateConstructEditState) => void) => void;
}

export function blankConstruct() : ConstructUnderEdit {
    return {
        chosenType: 'Tg',
        prefix: '',
        publicNote: '',
        cassettes: [],
        synonyms: [],
        sequences: [],
        notes: [],
        editCassetteMode: false,
        editCassetteIndex: null,
        addCassetteMode: false
    };
}

export function blankState() {
    return {
        publicationId: '',
        selectedConstructId: '',
        selectedConstruct: blankConstruct()
    };
}

export const CurateConstructEditProvider = ({publicationId, selectedConstructId, children}) => {
    const blank = blankState();
    const [state, setState] = useState<CurateConstructEditState>({...blank, publicationId, selectedConstructId});

    useEffect(() => {
        setState(prevState => ({
            ...prevState,
            publicationId,
            selectedConstructId
        }));
    }, [publicationId, selectedConstructId]);

    function setStateByProxy(fn) {
        return setState(produce(fn));
    }

    return (
        <CurateConstructEditContext.Provider value={{state, setStateByProxy}}>
            {children}
        </CurateConstructEditContext.Provider>
    );
}

const CurateConstructEditContext = createContext<CurateConstructEditStateAndSetter>(null);

export const useCurateConstructEditContext = () => {
    return React.useContext(CurateConstructEditContext);
}

