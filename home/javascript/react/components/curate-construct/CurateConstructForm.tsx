import React, {useState, useEffect} from 'react';
import ConstructCassetteListEditor, {cassetteHumanReadableList} from './ConstructCassetteListEditor';
import {
    cassettesToSimplifiedCassettes,
    ConstructFormDTO,
    normalizeSimplifiedCassettes,
    SimplifiedCassette,
    typeAbbreviationToType
} from './ConstructTypes';
import {backendBaseUrl} from './DomainInfo';
const calculatedDomain = backendBaseUrl();

/*
 * This component is used to create a new construct
 */
interface CurateConstructFormProps {
    publicationId: string;
    constructId: string;
    submitButtonLabel: string;
    onSubmit: (submissionObject: any) => Promise<void>;
}

const CurateConstructForm = ({publicationId, constructId, submitButtonLabel, onSubmit}: CurateConstructFormProps) => {

    const [chosenType, setChosenType] = useState('Tg');
    const [prefix, setPrefix] = useState('');
    const [synonym, setSynonym] = useState('');
    const [sequence, setSequence] = useState('');
    const [publicNote, setPublicNote] = useState('');
    const [curatorNote, setCuratorNote] = useState('');
    const [cassettes, setCassettes] = useState([]);
    const [initialCassettes, setInitialCassettes] = useState<SimplifiedCassette[]>([]);
    const [cassettesDisplay, setCassettesDisplay] = useState('');
    const [constructDisplayName, setConstructDisplayName] = useState('');
    const [saving, setSaving] = useState(false);
    const [resetFlag, setResetFlag] = useState<number>(0);

    const handleCassettesChanged = (cassettesChanged) => {
        setCassettes(cassettesChanged);
        setCassettesDisplay(cassetteHumanReadableList(cassettesChanged));
    }

    const clearForm = () => {
        setChosenType('Tg');
        setPrefix('');
        setSynonym('');
        setSequence('');
        setPublicNote('');
        setCuratorNote('');
        setCassettesDisplay('');
        setResetFlag(resetFlag + 1);
    }

    function submitForm() {
        const submissionObject : ConstructFormDTO = {
            constructNameObject: {
                type: typeAbbreviationToType(chosenType),
                prefix: prefix,
                cassettes: cassettesToSimplifiedCassettes(cassettes)
            },
            constructAlias: synonym,
            constructSequence: sequence,
            constructComments: publicNote,
            constructCuratorNote: curatorNote,
            pubZdbID: publicationId
        }
        setSaving(true);
        onSubmit(submissionObject).then(() => {
            setSaving(false);
            clearForm();
        });
    }

    function setInitialCassettesFromApiResult(normalizedCassettes) {
        setInitialCassettes(normalizeSimplifiedCassettes(normalizedCassettes));
    }

    //eg. ZDB-TGCONSTRCT-220310-1
    useEffect(() => {
        if (constructId) {
            clearForm();
            fetch(`${calculatedDomain}/action/construct/json/${constructId}`)
                .then(response => response.json())
                .then(data => {
                    console.log('construct data', data);
                    setChosenType(data.typeAbbreviation);
                    setPrefix(data.prefix);
                    // setSynonym(data.synonym);
                    // setSequence(data.sequence);
                    // setPublicNote(data.publicNote);
                    // setCuratorNote(data.curatorNote);
                    if (data.cassettes) {
                        setInitialCassettesFromApiResult(normalizeSimplifiedCassettes(data.cassettes));
                        setConstructDisplayName(data.displayName);
                    }
                });
        }
    }, [constructId]);

    useEffect(() => {
        setConstructDisplayName(chosenType + prefix + '(' + cassettesDisplay + ')');
    }, [chosenType, prefix, cassettesDisplay]);

    return <>
            <div className='mb-3' style={{backgroundColor: '#eee'}}>
                <table>
                    <thead/>
                    <tbody>
                    <tr>
                        <td><b>Construct Type</b></td>
                        <td>
                            {/*Select dropdown associated with React const chosenType (Tg, Et, Gt, Pt)*/}
                            <select value={chosenType} onChange={e => setChosenType(e.target.value)}>
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
                                value={prefix}
                                onChange={e => setPrefix(e.target.value)}
                                type='text'
                            />
                        </td>
                    </tr>
                    <tr>
                        <td><b>Synonym</b>:</td>
                        <td><input autoComplete='off' type='text' size='50' value={synonym} onChange={e => setSynonym(e.target.value)}/></td>
                    </tr>
                    <tr>
                        <td><b>Sequence</b>:</td>
                        <td><input autoComplete='off' type='text' size='50' value={sequence} onChange={e => setSequence(e.target.value)}/></td>
                    </tr>
                    <tr>
                        <td><b>Public Note</b>:</td>
                        <td>
                            <textarea rows='3' cols='50' value={publicNote} onChange={e => setPublicNote(e.target.value)}/>
                        </td>
                        <td><b>Curator Note</b>:</td>
                        <td>
                            <textarea rows='3' cols='50' value={curatorNote} onChange={e => setCuratorNote(e.target.value)}/>
                        </td>
                    </tr>
                    </tbody>
                </table>
                <div className='mb-3'>
                    <ConstructCassetteListEditor publicationId={publicationId} onChange={handleCassettesChanged} resetFlag={resetFlag} initialCassettes={initialCassettes}/>
                </div>
                <div className='mb-3'>
                    <p>
                        <b>Display Name:</b>
                        <input name='constructDisplayName' disabled='disabled' type='text' value={constructDisplayName} size='150'/>
                    </p>
                </div>
                <div className='mb-3'>
                    <button type='button' className='mr-2' onClick={submitForm} disabled={saving}>{submitButtonLabel}</button>
                    <button type='button' onClick={clearForm} disabled={saving}>Cancel</button>
                </div>
            </div>
    </>;
}

export default CurateConstructForm;
