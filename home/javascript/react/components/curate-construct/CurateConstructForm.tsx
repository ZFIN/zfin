import React, {useEffect, useState} from 'react';
import ConstructCassetteListEditor, {cassetteHumanReadableList} from './ConstructCassetteListEditor';
import {
    cassettesToSimplifiedCassettes,
    ConstructFormDTO,
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

/*
 * This component is used to create a new construct
 */
interface CurateConstructFormProps {
    submitButtonLabel: string;
    onCancel: () => void;
    onSubmit: (submissionObject) => Promise<void>;
}

const CurateConstructFormInner = ({submitButtonLabel, onCancel, onSubmit}: CurateConstructFormProps) => {

    const {state, setStateByProxy} = useCurateConstructEditContext();

    const [constructDisplayName, setConstructDisplayName] = useState('');
    const [saving, setSaving] = useState(false);

    useEffect(() => {
        const display = cassetteHumanReadableList(state.selectedConstruct.cassettes);
        setConstructDisplayName(state.selectedConstruct.chosenType + state.selectedConstruct.prefix + '(' + display + ')' );
    },[state]);

    const handleCassettesChanged = (cassettesChanged) => {
        setStateByProxy(proxy => {
            proxy.selectedConstruct.cassettes = cassettesChanged;
        });
    }

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
        const submissionObject : EditConstructFormDTO = {
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

    //eg. ZDB-TGCONSTRCT-220310-1
    useEffect(() => {
        const constructId = state.selectedConstructId;
        if (constructId) {
            // clearForm();
            fetch(`${calculatedDomain}/action/construct/json/${constructId}`)
                .then(response => response.json())
                .then(data => {
                    setStateByProxy(proxy => {
                        const blank = blankConstruct();
                        const fetchedCassettes = normalizeSimplifiedCassettes(data.cassettes);
                        const fullCassettes = simplifiedCassettesToCassettes(fetchedCassettes);
                        proxy.selectedConstruct = {
                            ...blank,
                            chosenType: data.typeAbbreviation,
                            prefix: data.prefix,
                            publicNote: data.publicNote,
                            curatorNote: data.curatorNote,
                            cassettes: fullCassettes,
                            editCassetteMode: false,
                            editCassetteIndex: null,
                            addCassetteMode: false
                        };
                    });
                })
                .then(() => {
                })
                .catch(error => {
                    console.error('Error fetching construct', error);
                });
        }
    }, [state.selectedConstructId]);

    return <>
        <div className='mb-3' style={{backgroundColor: '#eee'}}>
            <table>
                <thead/>
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
                        {/*Select dropdown associated with React const chosenType (Tg, Et, Gt, Pt)*/}
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
                    <td><CurateConstructSynonymEditor/>
                    </td>
                </tr>
                <tr>
                    <td><b>Sequence</b>:</td>
                    <td><CurateConstructSequenceEditor/></td>
                </tr>
                <tr>
                    <td><b>Public Note</b>:</td>
                    <td>
                        <CurateConstructPublicNoteEditor/>
                    </td>
                </tr>
                <tr>
                    <td><b>Curator Notes</b>:</td>
                    <td>
                        <CurateConstructNoteEditor/>
                    </td>
                </tr>
                </tbody>
            </table>
            <div className='mb-3'>
                <ConstructCassetteListEditor onChange={handleCassettesChanged}/>
            </div>
            <div className='mb-3'>
                <p>
                    <b>Display Name:</b>
                    <input name='constructDisplayName' disabled={true} type='text' value={constructDisplayName || ''}
                           size='150'/>
                </p>
            </div>
            <div className='mb-3'>
                <button type='button' className='mr-2' onClick={submitForm}
                        disabled={saving}>{submitButtonLabel}</button>
                <button type='button' onClick={clearForm} disabled={saving}>Cancel</button>
            </div>
        </div>
    </>;
}

const CurateConstructForm = ({publicationId, constructId, submitButtonLabel, onSubmit, onCancel}: CurateConstructFormProps) => {
    return <CurateConstructEditProvider publicationId={publicationId} selectedConstructId={constructId}>
        <CurateConstructFormInner submitButtonLabel={submitButtonLabel} onSubmit={onSubmit} onCancel={onCancel}/>
    </CurateConstructEditProvider>
}

export default CurateConstructForm;
