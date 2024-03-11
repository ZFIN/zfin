import React, {useEffect, useState} from 'react';
import ConstructCassetteEditor, {isValidCassette} from './ConstructCassetteEditor';
import ConstructCassetteView from './ConstructCassetteView';
import {Cassette} from './ConstructTypes';

interface ConstructCassetteListEditorProps {
    publicationId: string;
    onChange?: (cassettes: Cassette[]) => void;
    resetFlag?: number;
}

const ConstructCassetteListEditor = ({publicationId, onChange, resetFlag}: ConstructCassetteListEditorProps) => {
    const [cassettes, setCassettes] = useState<Cassette[]>([]);
    const [cassette, setCassette] = useState<Cassette>(null);
    const [isEditMode, setIsEditMode] = useState(false);

    const handleCassetteChange = (updatedCassette) => {
        setCassette(updatedCassette);
        const eventPayload = [...cassettes, updatedCassette];
        notifyParentOfChange(eventPayload);
    }

    const handleAddCassette = (event) => {
        event.preventDefault();
        const newCassettes = [...cassettes, cassette];
        setCassettes(newCassettes);
        setCassette(null);
        setIsEditMode(false);
        notifyParentOfChange(newCassettes);
    }

    const handleRemoveCassette = (index) => {
        const newCassettes = [...cassettes];
        newCassettes.splice(index, 1);
        setCassettes(newCassettes);
        notifyParentOfChange(newCassettes);
    }

    const notifyParentOfChange = (cassettes) => {
        if (onChange) {
            onChange(cassettes);
        }
    }

    const showCassetteEditor = () => {
        return cassettes.length === 0 || isEditMode;
    }

    const shouldDisableDoneButton = () => {
        return !isValidCassette(cassette);
    }

    const resetState = () => {
        setCassettes([]);
        setCassette(null);
        setIsEditMode(false);
    }

    useEffect(() => {
        resetState();
    }, [resetFlag]);

    return (
        <>
            {cassettes && cassettes.length > 0 && <b>Cassettes</b>}
            <ol>
                {cassettes.map((cassette, index) => <li key={index}>
                    <ConstructCassetteView cassette={cassette}/> <a href='#' onClick={() => handleRemoveCassette(index)}><i className='fa fa-trash'/></a>
                </li>)}
            </ol>
            {(!showCassetteEditor() &&
                <a onClick={(e) => {e.preventDefault(); setIsEditMode(true);}} title='Add' href='#'>Add cassette</a>
            )}
            {showCassetteEditor() && <>
                <ConstructCassetteEditor publicationId={publicationId} onChange={handleCassetteChange} resetFlag={resetFlag}/>
                <input style={{marginTop: '10px'}} type='button' onClick={handleAddCassette} value='Save Cassette' disabled={shouldDisableDoneButton()}/>
            </>}
        </>
    );
};

const cassetteHumanReadable = (cassette) => {
    if (!cassette) {
        return '';
    }
    const promoter = cassette.promoter.map(item => item.value + item.separator).join('');
    const coding = cassette.coding.map(item => item.value + item.separator).join('');
    if (promoter.length === 0) {
        return coding;
    }
    if (coding.length === 0) {
        return promoter;
    }
    return promoter + ':' + coding;
}

const cassetteHumanReadableList = (cassettes) => {
    return cassettes.map(cassetteHumanReadable).join(',');
}

export default ConstructCassetteListEditor;
export {cassetteHumanReadable, cassetteHumanReadableList};