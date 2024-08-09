import React, {useState, useEffect} from 'react';
import ConstructCassetteEditor, {isValidCassette} from './ConstructCassetteEditor';
import ConstructCassetteView from './ConstructCassetteView';
import {Cassette} from './ConstructTypes';
import {blankCassette, useCurateConstructEditContext} from './CurateConstructEditContext';

const ConstructCassetteListEditor = () => {
    const {state, setStateByProxy} = useCurateConstructEditContext();
    const [cassette, setCassette] = useState<Cassette>(null);

    const handleCassetteChange = (updatedCassette) => {
        setCassette(updatedCassette);
    }

    const handleAddCassetteClick = (e) => {
        e.preventDefault();
        setStateByProxy(proxy => {
            proxy.selectedConstruct.addCassetteMode = true;
            proxy.selectedConstruct.editCassetteMode = false;
        });
    }

    const handleAddCassette = (event) => {
        event.preventDefault();
        const newCassettes = [...state.selectedConstruct.cassettes];
        if (state.selectedConstruct.addCassetteMode) {
            newCassettes.push(cassette);
        } else if (state.selectedConstruct.editCassetteMode) {
            newCassettes[state.selectedConstruct.editCassetteIndex] = cassette;
        }
        setStateByProxy(proxy => {
            proxy.selectedConstruct.cassettes = newCassettes;
        });

        setCassette(null);
        setStateByProxy(proxy => {
            proxy.selectedConstruct.addCassetteMode = false;
            proxy.selectedConstruct.editCassetteMode = false;
            proxy.selectedConstruct.editCassetteIndex = null;
            proxy.stagedCassette = blankCassette();
        });
    }

    const handleRemoveCassette = (index) => {
        const newCassettes = [...state.selectedConstruct.cassettes];
        newCassettes.splice(index, 1);
        setStateByProxy(proxy => {proxy.selectedConstruct.cassettes = newCassettes;});
    }

    const handleEditCassetteClick = (e, index: number) => {
        e.preventDefault();
        handleEditCassette(index);
    }

    const handleMoveUpClick = (e, index: number) => {
        handleMoveClick(e, index, -1);
    }

    const handleMoveDownClick = (e, index: number) => {
        handleMoveClick(e, index, 1);
    }

    const handleMoveClick = (e, index: number, direction: number) => {
        e.preventDefault();
        const newCassettes = [...state.selectedConstruct.cassettes];
        if (index + direction >= 0 && index + direction < newCassettes.length) {
            const temp = newCassettes[index + direction];
            newCassettes[index + direction] = newCassettes[index];
            newCassettes[index] = temp;
            setStateByProxy(proxy => {proxy.selectedConstruct.cassettes = newCassettes;});
        }
    }

    const handleEditCassette = (index) => {
        setCassette(state.selectedConstruct.cassettes[index]);
        setStateByProxy(proxy => {
            proxy.selectedConstruct.addCassetteMode = false;
            proxy.selectedConstruct.editCassetteMode = true;
            proxy.selectedConstruct.editCassetteIndex = index;
        });
    }

    const handleCancelCassette = () => {
        setCassette(null);
        setStateByProxy(proxy => {
            proxy.selectedConstruct.addCassetteMode = false;
            proxy.selectedConstruct.editCassetteMode = false;
            proxy.selectedConstruct.editCassetteIndex = null;
            proxy.stagedCassette = blankCassette();
        });
    }

    const showCassetteEditor = () => {
        if (state.selectedConstruct.cassettes.length === 0) {
            return true;
        }
        return state.selectedConstruct.addCassetteMode || state.selectedConstruct.editCassetteMode;
    }

    const shouldDisableDoneButton = () => {
        return !isValidCassette(cassette);
    }

    useEffect(() => {
        if (state.selectedConstruct.cassettes.length === 0) {
            setStateByProxy(proxy => {proxy.selectedConstruct.addCassetteMode = true;});
        }
    }, [state.selectedConstruct]);

    return (
        <>
            {state.selectedConstruct.cassettes && state.selectedConstruct.cassettes.length > 0 && <b>Cassettes</b>}
            <ol>
                {state.selectedConstruct.cassettes.map((cassetteIterator, index) => <li key={index}>
                    <ConstructCassetteView cassette={cassetteIterator}/>{' '}
                    <a href='#' onClick={() => handleRemoveCassette(index)}><i className='fa fa-trash'/></a>{' '}
                    <a href='#' onClick={(e) => {handleEditCassetteClick(e, index);}}><i className='fa fa-edit'/></a>{' '}
                    {index > 0 && <a href='#' onClick={(e) => {handleMoveUpClick(e, index);}}><i className='fa fa-arrow-up'/></a>}{' '}
                    {index < state.selectedConstruct.cassettes.length - 1 &&
                        <a href='#' onClick={(e) => {handleMoveDownClick(e, index);}}><i className='fa fa-arrow-down'/></a>
                    }
                </li>)}
            </ol>
            {(!showCassetteEditor() &&
                <a onClick={(e) => {handleAddCassetteClick(e)}} title='Add' href='#'>Add cassette</a>
            )}
            {showCassetteEditor() && <>
                <ConstructCassetteEditor cassette={cassette} onChange={handleCassetteChange}/>
                <input style={{marginTop: '10px'}} type='button' onClick={handleAddCassette} value='Save Cassette' disabled={shouldDisableDoneButton()}/>
                <input style={{marginTop: '10px'}} type='button' onClick={handleCancelCassette} value='Cancel'/>
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
