import React, {useState} from 'react';
import ConstructRegulatoryCodingUnitList from './ConstructRegulatoryCodingUnitList';
import {Cassette, ConstructComponent, normalizeConstructComponents} from './ConstructTypes';
import {useCurateConstructEditContext} from './CurateConstructEditContext';

interface ConstructCassetteEditorProps {
    onChange: (cassette: Cassette) => void;
    onSave: (cassette: Cassette) => void;
    onCancel: () => void;
}

const ConstructCassetteEditor = ({onChange, onSave, onCancel}: ConstructCassetteEditorProps) => {
    const blankCassette = () => {return {promoter: [], coding: []};};
    const [rerenderKey, setRerenderKey] = useState<number>(0); // Key to force rerender
    const {state, setStateByProxy} = useCurateConstructEditContext();

    const setCassetteForEdit = (cassette: Cassette) => {
        setStateByProxy(proxy => {
            proxy.stagedCassette = cassette;
        });
    }

    const handleRegulatoryCodingUnitChange = (constructComponents: ConstructComponent[], type) => {
        //the last item should have its separator set to ''
        const transformedConstructComponents = normalizeConstructComponents(constructComponents);
        const newState = {
            ...state.stagedCassette,
            [type]: transformedConstructComponents
        }
        setCassetteForEdit(newState);

        if (onChange) {
            onChange(newState);
        }
    }

    const shouldDisableDoneButton = () => {
        return !isValidCassette(state.stagedCassette);
    }

    const handleAddCassette = () => {
        onSave(state.stagedCassette);
    }

    const handleCancelCassette = () => {
        setCassetteForEdit(blankCassette());
        setRerenderKey(rerenderKey + 1);
        onCancel();
    }

    return <div>
        <b>Promoter</b>
        <ConstructRegulatoryCodingUnitList
            onChange={(items) => handleRegulatoryCodingUnitChange(items, 'promoter') }
            type='promoter'
            key={`promoter-${rerenderKey}`}
        />

        <b>Coding</b>
        <ConstructRegulatoryCodingUnitList
            onChange={(items) => handleRegulatoryCodingUnitChange(items, 'coding') }
            type='coding'
            key={`coding-${rerenderKey}`}
        />
        <input style={{marginTop: '10px'}} type='button' onClick={handleAddCassette} value='Save Cassette' disabled={shouldDisableDoneButton()}/>
        <input style={{marginTop: '10px'}} type='button' onClick={handleCancelCassette} value='Cancel' />
    </div>;
}


const isValidCassette = (cassette) => {
    if (!cassette) {
        return false;
    }
    if (!cassette.promoter && !cassette.coding) {
        return false;
    }
    if (cassette.promoter.length === 0 && cassette.coding.length === 0) {
        return false;
    }
    return true;
}

export default ConstructCassetteEditor;
export {isValidCassette};