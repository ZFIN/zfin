import React, {useState} from "react";
import PropTypes from 'prop-types';
import ConstructRegulatoryCodingUnitList from "./ConstructRegulatoryCodingUnitList";

const ConstructCassetteEditor = ({publicationId, onChange}) => {
    const [state, setState] = useState({
        promoter: [],
        coding: []
    });

    const handleRegulatoryCodingUnitChange = (regulatoryCodingUnits, type) => {
        console.log('DEBUG: handleRegulatoryCodingUnitChange', regulatoryCodingUnits, type);

        //the last item should have its separator set to ''
        const transformedRegulatoryCodingUnits = regulatoryCodingUnits.map((item, index) => {
            if (index == regulatoryCodingUnits.length - 1) {
                return {...item, separator: ''};
            }
            return item;
        });

        const newState = {
            ...state,
            [type]: transformedRegulatoryCodingUnits
        }
        setState(newState);

        if (onChange) {
            onChange(newState);
        }
    }


    return <div>
        <b>Promoter</b>
        <ConstructRegulatoryCodingUnitList publicationId={publicationId} onChange={(items) => handleRegulatoryCodingUnitChange(items, 'promoter') }/>

        <b>Coding</b>
        <ConstructRegulatoryCodingUnitList publicationId={publicationId} onChange={(items) => handleRegulatoryCodingUnitChange(items, 'coding') }/>
    </div>;
}

ConstructCassetteEditor.propTypes = {
    publicationId: PropTypes.string,
    onChange: PropTypes.func,
}

const isValidCassette = (cassette) => {
    console.log("validating cassette", cassette);
    if (!cassette) {
        return false;
    }
    if (!cassette.promoter) {
        return false;
    }
    if (!cassette.coding) {
        return false;
    }
    if (cassette.promoter.length == 0) {
        return false;
    }
    if (cassette.coding.length == 0) {
        return false;
    }
    return true;
}

export default ConstructCassetteEditor;
export {isValidCassette};