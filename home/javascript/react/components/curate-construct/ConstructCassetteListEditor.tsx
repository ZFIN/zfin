import React, {useEffect} from 'react';
import ConstructCassetteEditor from './ConstructCassetteEditor';
import ConstructCassetteView from './ConstructCassetteView';
import {blankCassette, useCurateConstructEditContext} from './CurateConstructEditContext';
import {Cassette, ConstructComponent} from './ConstructTypes';

const ConstructCassetteListEditor = () => {
    const {state, setStateByProxy} = useCurateConstructEditContext();

    const setCassette = (cassette: Cassette) => {
        setStateByProxy(proxy => {
            proxy.stagedCassette = cassette;
        });
    }

    const handleAddCassetteClick = (e: React.MouseEvent) => {
        e.preventDefault();
        setStateByProxy(proxy => {
            proxy.selectedConstruct.addCassetteMode = true;
            proxy.selectedConstruct.editCassetteMode = false;
        });
    }

    const handleAddCassette = () => {
        const newCassettes = [...state.selectedConstruct.cassettes];
        if (state.selectedConstruct.addCassetteMode) {
            newCassettes.push({...state.stagedCassette});
        } else if (state.selectedConstruct.editCassetteMode) {
            newCassettes[state.selectedConstruct.editCassetteIndex] = {...state.stagedCassette};
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

    const handleRemoveCassette = (index: number) => {
        const newCassettes = [...state.selectedConstruct.cassettes];
        newCassettes.splice(index, 1);
        setStateByProxy(proxy => {proxy.selectedConstruct.cassettes = newCassettes;});
    }

    const handleEditCassetteClick = (e: React.MouseEvent, index: number) => {
        e.preventDefault();
        handleEditCassette(index);
    }

    const handleMoveUpClick = (e: React.MouseEvent, index: number) => {
        handleMoveClick(e, index, -1);
    }

    const handleMoveDownClick = (e: React.MouseEvent, index: number) => {
        handleMoveClick(e, index, 1);
    }

    const handleMoveClick = (e: React.MouseEvent, index: number, direction: number) => {
        e.preventDefault();
        const newCassettes = [...state.selectedConstruct.cassettes];
        if (index + direction >= 0 && index + direction < newCassettes.length) {
            const temp = newCassettes[index + direction];
            newCassettes[index + direction] = newCassettes[index];
            newCassettes[index] = temp;
            setStateByProxy(proxy => {proxy.selectedConstruct.cassettes = newCassettes;});
        }
    }

    const handleEditCassette = (index: number) => {
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

    const showCassetteEditorAddMode = () => {
        if (state.selectedConstruct.cassettes.length === 0) {
            return true;
        }
        return state.selectedConstruct.addCassetteMode;
    }
    const showCassetteEditorEditMode = () => {
        return state.selectedConstruct.editCassetteMode;
    }

    const showCassetteEditor = () => {
        return showCassetteEditorAddMode() || showCassetteEditorEditMode();
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
                    <a href='#' onClick={(e) => {e.preventDefault(); handleRemoveCassette(index)}}><i className='fa fa-trash'/></a>{' '}
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
            {showCassetteEditorEditMode() && <>
                <ConstructCassetteEditor onSave={handleAddCassette} onCancel={handleCancelCassette}/>
            </>}
            {showCassetteEditorAddMode() && <>
                <ConstructCassetteEditor onSave={handleAddCassette} onCancel={handleCancelCassette}/>
            </>}
        </>
    );
};

const cassetteHumanReadable = (cassette: Cassette) => {
    if (!cassette) {
        return '';
    }
    const promoter = cassette.promoter.map((item: ConstructComponent) => item.value + item.separator).join('');
    const coding = cassette.coding.map((item: ConstructComponent) => item.value + item.separator).join('');
    if (promoter.length === 0) {
        return coding;
    }
    if (coding.length === 0) {
        return promoter;
    }
    return promoter + ':' + coding;
}

const cassetteHumanReadableList = (cassettes: Cassette[]) => {
    return cassettes.map(cassetteHumanReadable).join(',');
}

export default ConstructCassetteListEditor;
export {cassetteHumanReadable, cassetteHumanReadableList};
