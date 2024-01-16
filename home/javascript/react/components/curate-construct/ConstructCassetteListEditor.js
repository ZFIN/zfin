import React, {useState} from "react";
import PropTypes from 'prop-types';
import ConstructCassetteEditor, {isValidCassette} from "./ConstructCassetteEditor";
import ConstructCassetteView from "./ConstructCassetteView";

const ConstructCassetteListEditor = ({publicationId, onChange}) => {
    const defaultCassette = { promoter: [], coding: [] };
    const [cassettes, setCassettes] = useState([]);
    const [cassette, setCassette] = useState(null);
    const [isEditMode, setIsEditMode] = useState(false);

    const handleCassetteChange = (updatedCassette) => {
        console.log('DEBUG: handleCassetteChange', updatedCassette);
        setCassette(updatedCassette);
    }

    const handleAddCassette = (event) => {
        event.preventDefault();
        console.log('DEBUG: handleAddCassette', cassette);
        const newCassettes = [...cassettes, cassette];
        setCassettes(newCassettes);
        setCassette(null);
        setIsEditMode(false);
        if (onChange) {
            onChange(newCassettes);
        }
    }

    const showCassetteEditor = () => {
        return cassettes.length == 0 || isEditMode;
    }

    const handleRemoveCassette = (index) => {
        const newCassettes = [...cassettes];
        newCassettes.splice(index, 1);
        setCassettes(newCassettes);
    }

    const shouldDisableDoneButton = () => {
        return !isValidCassette(cassette);
    }

    return (
        <>
            <ul>
                {cassettes.map((cassette, index) => <li key={index}>
                    <ConstructCassetteView cassette={cassette}/> <a href='#' onClick={e => handleRemoveCassette(index)}><i className='fa fa-trash'/></a>
                </li>)}
            </ul>
            {(!showCassetteEditor() &&
                <a onClick={() => setIsEditMode(true)} title='Add' href='src#'>Add cassette</a>
            )}
            <br/>
            {showCassetteEditor() && <>
                <ConstructCassetteEditor publicationId={publicationId} onChange={handleCassetteChange}/>
                <input type='button' onClick={handleAddCassette} value='Done' disabled={shouldDisableDoneButton()}/>
            </>}
        </>
    );
};

ConstructCassetteListEditor.propTypes = {
    publicationId: PropTypes.string,
}

const cassetteHumanReadable = (cassette) => {
    if (!cassette) {
        return '';
    }
    const promoter = cassette.promoter.map(item => item.value + item.separator).join('');
    const coding = cassette.coding.map(item => item.value + item.separator).join('');
    return promoter + ':' + coding;
}

const cassetteHumanReadableList = (cassettes) => {
    return cassettes.map(cassetteHumanReadable).join(',');
}

export default ConstructCassetteListEditor;
export {cassetteHumanReadable, cassetteHumanReadableList};