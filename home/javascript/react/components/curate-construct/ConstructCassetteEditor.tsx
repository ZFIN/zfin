import React, {useEffect, useState} from 'react';
import ConstructRegulatoryCodingUnitList from './ConstructRegulatoryCodingUnitList';
import {Cassette, ConstructComponent, normalizeConstructComponents} from './ConstructTypes';

interface ConstructCassetteEditorProps {
    onChange: (cassette: Cassette) => void;
    onSave: (cassette: Cassette) => void;
    onCancel: () => void;
    cassette?: Cassette;
}

const ConstructCassetteEditor = ({onChange, onSave, onCancel, cassette: initialCassette}: ConstructCassetteEditorProps) => {
    const blankCassette = () => {return {promoter: [], coding: []};};
    const [cassetteForEdit, setCassetteForEdit] = useState<Cassette>(blankCassette());
    const [rerenderKey, setRerenderKey] = useState<number>(0); // Key to force rerender

    useEffect(() => {
        if (initialCassette) {
            setCassetteForEdit(initialCassette);
        }
    }, [initialCassette]);

    const handleRegulatoryCodingUnitChange = (constructComponents: ConstructComponent[], type) => {
        //the last item should have its separator set to ''
        const transformedConstructComponents = normalizeConstructComponents(constructComponents);
        const newState = {
            ...cassetteForEdit,
            [type]: transformedConstructComponents
        }
        setCassetteForEdit(newState);

        if (onChange) {
            onChange(newState);
        }
    }

    const shouldDisableDoneButton = () => {
        return !isValidCassette(cassetteForEdit);
    }

    const shouldDisableCencelButton = () => {
        return isBlankCassette(cassetteForEdit);
    }

    const handleAddCassette = () => {
        onSave(cassetteForEdit);
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
        <input style={{marginTop: '10px'}} type='button' onClick={handleCancelCassette} value='Cancel' disabled={shouldDisableCencelButton()}/>
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

const isBlankCassette = (cassette) => {
    return cassette.promoter.length === 0 && cassette.coding.length === 0;
}

export default ConstructCassetteEditor;
export {isValidCassette};