import React, {useEffect, useState} from 'react';
import ConstructCassetteListEditor, {cassetteHumanReadableList} from './ConstructCassetteListEditor';
import {
    cassettesToSimplifiedCassettes,
    EditConstructFormDTO,
    normalizeConstructCassette,
    normalizeConstructComponents,
    normalizeSimplifiedCassettes,
    simplifiedCassettesToCassettes,
    typeAbbreviationToType
} from './ConstructTypes';
import {backendBaseUrl} from './DomainInfo';
import {
    blankCassette,
    blankConstruct,
    CurateConstructEditProvider,
    useCurateConstructEditContext
} from './CurateConstructEditContext';
import CurateConstructSynonymEditor from './CurateConstructSynonymEditor';
import CurateConstructSequenceEditor from './CurateConstructSequenceEditor';
import CurateConstructNoteEditor from './CurateConstructNoteEditor';
import CurateConstructPublicNoteEditor from './CurateConstructPublicNoteEditor';

const calculatedDomain = backendBaseUrl();

interface CurateConstructFormProps {
    publicationId: string;
    submitButtonLabel: string;
    onCancel: () => void;
    onSubmit: (submissionObject: EditConstructFormDTO) => Promise<void>;
    constructId?: string;
}

interface CurateConstructFormInnerProps {
    submitButtonLabel: string;
    onCancel: () => void;
    onSubmit: (submissionObject: EditConstructFormDTO) => Promise<void>;
}

const CurateConstructFormInner = ({submitButtonLabel, onCancel, onSubmit}: CurateConstructFormInnerProps) => {

    const {state, setStateByProxy} = useCurateConstructEditContext();

    const [constructDisplayName, setConstructDisplayName] = useState('');
    const [saving, setSaving] = useState(false);
    const [initialState, setInitialState] = useState(null);

    useEffect(() => {
        const display = cassetteHumanReadableList(cassettesWithStagedCassette());
        setConstructDisplayName(state.selectedConstruct.chosenType + state.selectedConstruct.prefix + '(' + display + ')');
    }, [state]);

    const isStagedCassetteBlank = () => {
        return state.stagedCassette.coding.length === 0 && state.stagedCassette.promoter.length === 0;
    }

    const cassettesWithStagedCassette = () => {
        //is there a staged cassette?
        if (isStagedCassetteBlank()) {
            return state.selectedConstruct.cassettes;
        }

        if (state.selectedConstruct.addCassetteMode) {
            //change trailing separator to '' for the last items of staged cassette
            const stagedCassette = {...state.stagedCassette};
            stagedCassette.coding = normalizeConstructComponents(stagedCassette.coding);
            stagedCassette.promoter = normalizeConstructComponents(stagedCassette.promoter);

            return [...state.selectedConstruct.cassettes, stagedCassette];
        } else if (state.selectedConstruct.editCassetteMode) {
            //ignore the staged cassette if we are editing a cassette
            return state.selectedConstruct.cassettes;
        } else {
            return state.selectedConstruct.cassettes;
        }
    }

    const handleCassettesChanged = (cassettesChanged) => {
        setStateByProxy(proxy => {
            proxy.selectedConstruct.cassettes = cassettesChanged;
        });
    }

    const isDirty = (stateObject) => {
        return captureStateForDirtyCheck(stateObject) !== initialState;
    }

    const captureStateForDirtyCheck = (stateObject) => {
        return JSON.stringify(
            { selectedConstruct: stateObject.selectedConstruct,
                stagedSynonym: stateObject.stagedSynonym,
                stagedSequence: stateObject.stagedSequence,
                stagedNote: stateObject.stagedNote}
        );
    }

    const handleCancelButton = () => {
        clearForm();
        if (onCancel) {
            onCancel();
        }
    }

    const clearForm = () => {
        setStateByProxy(proxy => {
            proxy.selectedConstruct = blankConstruct();
            proxy.selectedConstructId = null;
        });
    }

    function submitForm() {
        const submissionObject: EditConstructFormDTO = {
            constructName: {
                type: typeAbbreviationToType(state.selectedConstruct.chosenType),
                prefix: state.selectedConstruct.prefix,
                cassettes: cassettesToSimplifiedCassettes(state.selectedConstruct.cassettes)
            },
            synonyms: state.selectedConstruct.synonyms,
            sequences: state.selectedConstruct.sequences,
            notes: state.selectedConstruct.notes,
            publicNote: state.selectedConstruct.publicNote,
            publicationZdbID: state.publicationId
        }

        //add the staged text values if they have not been added to the construct yet
        if (state.stagedSynonym) {
            submissionObject.synonyms = [...submissionObject.synonyms, {label: state.stagedSynonym, zdbID: null}];
            setStateByProxy(proxy => {proxy.selectedConstruct.synonyms.push({label: state.stagedSynonym, zdbID: null});});
            setStateByProxy(proxy => {proxy.stagedSynonym = '';});
        }

        if (state.stagedSequence) {
            submissionObject.sequences = [...submissionObject.sequences, {label: state.stagedSequence, zdbID: null}];
            setStateByProxy(proxy => {proxy.selectedConstruct.sequences.push({label: state.stagedSequence, zdbID: null});});
            setStateByProxy(proxy => {proxy.stagedSequence = '';});
        }

        if (state.stagedNote) {
            submissionObject.notes = [...submissionObject.notes, {label: state.stagedNote, zdbID: null}];
            setStateByProxy(proxy => {proxy.selectedConstruct.notes.push({label: state.stagedNote, zdbID: null});});
            setStateByProxy(proxy => {proxy.stagedNote = '';});
        }

        if (!isStagedCassetteBlank()) {
            //if the last promoter has a separator of '-', change it to ''
            const modifiedStagedCassette = normalizeConstructCassette(state.stagedCassette);

            submissionObject.constructName.cassettes = cassettesToSimplifiedCassettes([...state.selectedConstruct.cassettes, modifiedStagedCassette]);
            setStateByProxy(proxy => {proxy.selectedConstruct.cassettes.push(state.stagedCassette);});
            setStateByProxy(proxy => {proxy.stagedCassette = blankCassette();});
        }

        setSaving(true);
        onSubmit(submissionObject).then(() => {
            setSaving(false);
        }).catch(() => {
            setSaving(false);
        });
    }

    const initializeDataForConstructID = async (constructId) => {
        try {
            const response = await fetch(`${calculatedDomain}/action/construct/json/${constructId}`);
            const constructNameData = await response.json();
            const constructDetailsResponse = await fetch(`${calculatedDomain}/action/construct/construct-do-update/${constructId}`);
            const constructDetailsArray = await constructDetailsResponse.json();

            setStateByProxy(proxy => {
                // Update state with construct name data
                const blank = blankConstruct();
                const fetchedCassettes = normalizeSimplifiedCassettes(constructNameData.cassettes);
                const fullCassettes = simplifiedCassettesToCassettes(fetchedCassettes);
                proxy.selectedConstruct = {
                    ...blank,
                    chosenType: constructNameData.typeAbbreviation,
                    prefix: constructNameData.prefix,
                    publicNote: constructNameData.publicNote,
                    curatorNote: constructNameData.curatorNote,
                    cassettes: fullCassettes,
                    editCassetteMode: false,
                    editCassetteIndex: null,
                    addCassetteMode: false
                };

                // Update state with extended construct data
                if (constructDetailsArray && constructDetailsArray.length > 0) {
                    const constructData = constructDetailsArray[0];

                    // Initialize synonyms
                    proxy.selectedConstruct.synonyms = constructData.constructAliases.map(syn => ({
                        label: syn.alias,
                        zdbID: syn.aliasZdbID
                    }));

                    // Initialize public note
                    proxy.selectedConstruct.publicNote = constructData.constructComments || '';

                    // Initialize sequences
                    proxy.selectedConstruct.sequences = constructData.constructSequences.map(seq => ({
                        label: seq.view,
                        zdbID: seq.zdbID
                    }));

                    // Initialize notes
                    proxy.selectedConstruct.notes = constructData.constructCuratorNotes.map(note => ({
                        label: note.noteData,
                        zdbID: note.zdbID
                    }));
                }

                // Set the initial state for dirty checking
                setInitialState(captureStateForDirtyCheck(proxy));
            });
        } catch (error) {
            console.error('Error initializing data for construct ID', error);
        }
    };

    const initializeDataForNewConstruct = () => {
        setStateByProxy(proxy => {
            proxy.selectedConstruct = blankConstruct();
            proxy.selectedConstruct.addCassetteMode = true;
        });
        setInitialState(captureStateForDirtyCheck(state));
    }

    useEffect(() => {
        const constructId = state.selectedConstructId;
        if (constructId) {
            initializeDataForConstructID(constructId);
        } else {
            initializeDataForNewConstruct();
        }
    }, [state.selectedConstructId]);

    return (
        <>
            <div className='mb-3' style={{backgroundColor: '#eee'}}>
                <table>
                    <thead />
                    <tbody>
                        {state.selectedConstructId &&
                            <tr>
                                <td><b>Construct ID</b></td>
                                <td>
                                    <a
                                        href={backendBaseUrl() + '/' + state.selectedConstructId}
                                        target='_blank'
                                        rel='noreferrer'
                                    >{state.selectedConstructId}</a>
                                </td>
                            </tr>
                        }
                        <tr>
                            <td><b>Construct Type</b></td>
                            <td>
                                <select
                                    value={state.selectedConstruct.chosenType || ''}
                                    onChange={e => setStateByProxy(proxy => {
                                        proxy.selectedConstruct.chosenType = e.target.value
                                    })}
                                >
                                    <option value='Tg'>Tg</option>
                                    <option value='Et'>Et</option>
                                    <option value='Gt'>Gt</option>
                                    <option value='Pt'>Pt</option>
                                </select>{' '}
                                <label htmlFor='prefix'><b>Prefix:</b></label>{' '}
                                <input
                                    id='prefix'
                                    size='15'
                                    className='prefix'
                                    name='prefix'
                                    value={state.selectedConstruct.prefix || ''}
                                    onChange={e => setStateByProxy(proxy => {
                                        proxy.selectedConstruct.prefix = e.target.value
                                    })}
                                    type='text'
                                />
                            </td>
                        </tr>
                        <tr>
                            <td><b>Synonym</b>:</td>
                            <td><CurateConstructSynonymEditor /></td>
                        </tr>
                        <tr>
                            <td><b>Sequence</b>:</td>
                            <td><CurateConstructSequenceEditor /></td>
                        </tr>
                        <tr>
                            <td><b>Public Note</b>:</td>
                            <td><CurateConstructPublicNoteEditor /></td>
                        </tr>
                        <tr>
                            <td><b>Curator Notes</b>:</td>
                            <td><CurateConstructNoteEditor /></td>
                        </tr>
                    </tbody>
                </table>
                <div className='mb-3'>
                    <ConstructCassetteListEditor onChange={handleCassettesChanged} />
                </div>
                <div className='mb-3'>
                    <p>
                        <b>Display Name:</b>
                        <input
                            name='constructDisplayName'
                            disabled={true}
                            type='text'
                            value={constructDisplayName || ''}
                            size='150'
                        />
                    </p>
                </div>
                <div className='mb-3'>
                    <button
                        type='button'
                        className='mr-2'
                        onClick={submitForm}
                        disabled={saving || !isDirty(state)}
                    >{submitButtonLabel}
                    </button>
                    <button type='button' onClick={handleCancelButton} disabled={saving}>Cancel</button>
                </div>
            </div>
        </>
    );
}

const CurateConstructForm = ({publicationId, constructId, submitButtonLabel, onSubmit, onCancel}: CurateConstructFormProps) => {
    return (
        <CurateConstructEditProvider publicationId={publicationId} selectedConstructId={constructId}>
            <CurateConstructFormInner submitButtonLabel={submitButtonLabel} onSubmit={onSubmit} onCancel={onCancel} />
        </CurateConstructEditProvider>
    )
}

export default CurateConstructForm;
