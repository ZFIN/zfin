import React, {useEffect, useState} from 'react';
import ConstructRegulatoryCodingUnitList from './ConstructRegulatoryCodingUnitList';
import {Cassette, ConstructComponent} from './ConstructTypes';

interface ConstructCassetteEditorProps {
    onChange: (cassette: Cassette) => void;
    cassette?: Cassette;
}

const ConstructCassetteEditor = ({ onChange, cassette: initialCassette}: ConstructCassetteEditorProps) => {
    const blankCassette = {promoter: [], coding: []};
    const [cassetteForEdit, setCassetteForEdit] = useState<Cassette>(blankCassette);
    useEffect(() => {
        if (initialCassette) {
            setCassetteForEdit(initialCassette);
        }
    }, [initialCassette]);

    const handleRegulatoryCodingUnitChange = (constructComponents: ConstructComponent[], type) => {
        //the last item should have its separator set to ''
        const transformedConstructComponents = constructComponents.map((item, index) => {
            if (index === constructComponents.length - 1) {
                return {...item, separator: ''};
            }
            return item;
        });

        const newState = {
            ...cassetteForEdit,
            [type]: transformedConstructComponents
        }
        setCassetteForEdit(newState);

        if (onChange) {
            onChange(newState);
        }
    }

    return <div>
        <b>Promoter</b>
        <ConstructRegulatoryCodingUnitList
            onChange={(items) => handleRegulatoryCodingUnitChange(items, 'promoter') }
            type='promoter'
            cassette={cassetteForEdit}
        />

        <b>Coding</b>
        <ConstructRegulatoryCodingUnitList
            onChange={(items) => handleRegulatoryCodingUnitChange(items, 'coding') }
            type='coding'
            cassette={cassetteForEdit}
        />
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