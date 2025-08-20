import React, {createContext, useState, useEffect} from 'react';
import {Cassette, MarkerLabelAndZdbId} from './ConstructTypes';
import produce from 'immer';

interface ConstructUnderEdit {
    chosenType: string;
    prefix: string;
    publicNote: string;
    cassettes: Cassette[];
    synonyms: MarkerLabelAndZdbId[];
    sequences: MarkerLabelAndZdbId[];
    notes: MarkerLabelAndZdbId[];
    editCassetteMode: boolean;
    editCassetteIndex: number;
    addCassetteMode: boolean;
}

interface CurateConstructEditState {
    publicationId: string;
    selectedConstructId: string;
    selectedConstruct: ConstructUnderEdit;
    stagedSynonym: string;
    stagedSequence: string;
    stagedNote: string;
    stagedCassette: Cassette;
}

interface CurateConstructEditStateAndSetter {
    state: CurateConstructEditState;
    setStateByProxy: (fn: (draft: CurateConstructEditState) => void) => void;
}

export function blankCassette() : Cassette {
    return {
        promoter: [],
        coding: []
    };
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

function blankState() {
    return {
        publicationId: '',
        selectedConstructId: '',
        selectedConstruct: blankConstruct(),
        stagedSynonym: '',
        stagedSequence: '',
        stagedNote: '',
        stagedCassette: blankCassette()
    };
}

export const CurateConstructEditProvider = ({publicationId, selectedConstructId, children}) => {
    const blank = blankState();
    const [state, setState] = useState<CurateConstructEditState>({...blank, publicationId, selectedConstructId});

    useEffect(() => {
        const blank = blankState();
        setState({
            ...blank,
            publicationId,
            selectedConstructId
        });
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

