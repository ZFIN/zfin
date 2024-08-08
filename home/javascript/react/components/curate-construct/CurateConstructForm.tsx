import React, {useEffect, useState, useCallback} from 'react';
import ConstructCassetteListEditor, {cassetteHumanReadableList} from './ConstructCassetteListEditor';
import {
    cassettesToSimplifiedCassettes,
    EditConstructFormDTO,
    normalizeSimplifiedCassettes,
    simplifiedCassettesToCassettes,
    typeAbbreviationToType
} from './ConstructTypes';
import {backendBaseUrl} from './DomainInfo';
import {blankConstruct, CurateConstructEditProvider, useCurateConstructEditContext} from "./CurateConstructEditContext";
import CurateConstructSynonymEditor from "./CurateConstructSynonymEditor";
import CurateConstructSequenceEditor from "./CurateConstructSequenceEditor";
import CurateConstructNoteEditor from "./CurateConstructNoteEditor";
import CurateConstructPublicNoteEditor from "./CurateConstructPublicNoteEditor";

const calculatedDomain = backendBaseUrl();

interface CurateConstructFormProps {
    submitButtonLabel: string;
    onCancel: () => void;
    onSubmit: (submissionObject: EditConstructFormDTO) => Promise<void>;
}

const CurateConstructFormInner = ({submitButtonLabel, onCancel, onSubmit}: CurateConstructFormProps) => {

    const {state, setStateByProxy} = useCurateConstructEditContext();

    const [constructDisplayName, setConstructDisplayName] = useState('');
    const [saving, setSaving] = useState(false);
    const [initialState, setInitialState] = useState(null);
    const [isDirty, setIsDirty] = useState(false);

    useEffect(() => {
        const display = cassetteHumanReadableList(state.selectedConstruct.cassettes);
        setConstructDisplayName(state.selectedConstruct.chosenType + state.selectedConstruct.prefix + '(' + display + ')');
    }, [state]);

    const handleCassettesChanged = (cassettesChanged) => {
        setStateByProxy(proxy => {
            proxy.selectedConstruct.cassettes = cassettesChanged;
        });
        checkDirtyState();
    }


    const checkDirtyState = useCallback(() => {
        if (!initialState) return;
        const currentState = JSON.stringify(state.selectedConstruct);
        const isDataChanged = initialState !== currentState;

        setIsDirty(isDataChanged);
    }, [initialState, state.selectedConstruct]);

    const clearForm = () => {
        setStateByProxy(proxy => {
            proxy.selectedConstruct = blankConstruct();
            proxy.selectedConstructId = null;
        });
        if (onCancel) {
            onCancel();
        }
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
        setSaving(true);
        onSubmit(submissionObject).then(() => {
            setSaving(false);
            // clearForm();
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
                setInitialState(JSON.stringify(proxy.selectedConstruct));
            });
        } catch (error) {
            console.error('Error initializing data for construct ID', error);
        }
    };

    useEffect(() => {
        const constructId = state.selectedConstructId;
        if (constructId) {
            initializeDataForConstructID(constructId);
        }
    }, [state.selectedConstructId]);

    useEffect(() => {
        checkDirtyState();
    }, [checkDirtyState]);


    return (
        <>
            <div className='mb-3' style={{backgroundColor: '#eee'}}>
                <table>
                    <thead />
                    <tbody>
                    {state.selectedConstructId &&
                        <tr>
                            <td><b>Construct ID</b></td>
                            <td><a href={'/' + state.selectedConstructId} target='_blank'
                                   rel='noreferrer'>{state.selectedConstructId}</a></td>
                        </tr>
                    }
                    <tr>
                        <td><b>Construct Type</b></td>
                        <td>
                            <select value={state.selectedConstruct.chosenType || ''}
                                    onChange={e => setStateByProxy(proxy => {
                                        proxy.selectedConstruct.chosenType = e.target.value
                                    })}>
                                <option value='Tg'>Tg</option>
                                <option value='Et'>Et</option>
                                <option value='Gt'>Gt</option>
                                <option value='Pt'>Pt</option>
                            </select>
                            <label htmlFor='prefix'><b>Prefix:</b></label>
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
                        <input name='constructDisplayName' disabled={true} type='text' value={constructDisplayName || ''}
                               size='150' />
                    </p>
                </div>
                {/*<div className='mb-3'>*/}
                {/*    <span>{isDirty ? "Data Changed" : "No Changes"}</span>*/}
                {/*</div>*/}
                <div className='mb-3'>
                    <button type='button' className='mr-2' onClick={submitForm}
                            disabled={saving || !isDirty}>{submitButtonLabel}</button>
                    <button type='button' onClick={clearForm} disabled={saving}>Cancel</button>
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
