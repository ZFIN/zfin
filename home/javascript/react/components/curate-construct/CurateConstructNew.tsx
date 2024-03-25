import React, {useState} from 'react';
import ConstructCassetteListEditor, {cassetteHumanReadableList} from './ConstructCassetteListEditor';
import {cassettesToSimplifiedCassettes, typeAbbreviationToType} from './ConstructTypes';

/*
 * This component is used to create a new construct
 */

interface CurateConstructNewProps {
    publicationId: string;
    show: boolean;
}

//TODO: This is a hack to get the domain for developing locally.  It should be removed when this is deployed to production.
let calculatedDomain = window.location.origin;
if (calculatedDomain.indexOf('localhost') > -1) {
    calculatedDomain = 'https://cell-mac.zfin.org';
}

const CurateConstructNew = ({publicationId, show= true}: CurateConstructNewProps) => {

    const [display, setDisplay] = useState(show);
    const [chosenType, setChosenType] = useState('Tg');
    const [prefix, setPrefix] = useState('');
    const [synonym, setSynonym] = useState('');
    const [sequence, setSequence] = useState('');
    const [publicNote, setPublicNote] = useState('');
    const [curatorNote, setCuratorNote] = useState('');
    const [cassettes, setCassettes] = useState([]);
    const [cassettesDisplay, setCassettesDisplay] = useState('');
    const [saving, setSaving] = useState(false);
    const [showError, setShowError] = useState<boolean>(false);
    const [showSuccess, setShowSuccess] = useState<string>('');
    const [resetFlag, setResetFlag] = useState<number>(0);

    const toggleDisplay = () => setDisplay(!display);

    const handleCassettesChanged = (cassettesChanged) => {
        setCassettes(cassettesChanged);
        setCassettesDisplay(cassetteHumanReadableList(cassettesChanged));
    }

    const submitForm = async () => {
        const submissionObject = {
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

        try {
            //post with fetch to `/action/construct/create`
            const result = await fetch(`${calculatedDomain}/action/construct/create`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(submissionObject),
            });
            const body = await result.text();
            clearForm();
            setShowSuccess(body);
        } catch (error) {
            console.error('Error fetching data:', error);
            setShowError(true);
            setShowSuccess('');
        } finally {
            setSaving(false);
        }
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
        setShowError(false);
        setShowSuccess('');
    }

    return <>
        <div className='mb-3'>
            <span className='bold'>CREATE NEW CONSTRUCT: </span>
            <a onClick={toggleDisplay} style={{textDecoration: 'underline'}}>{display ? 'Hide' : 'Show'}</a>
        </div>
        {display &&
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
                <ConstructCassetteListEditor publicationId={publicationId} onChange={handleCassettesChanged} resetFlag={resetFlag}/>
            </div>
            <div className='mb-3'>
                <p>
                    <b>Display Name:</b>
                    <input name='constructDisplayName' disabled='disabled' type='text' value={chosenType + prefix + '(' + cassettesDisplay + ')'} size='150'/>
                </p>
            </div>
            <div className='mb-3'>
                <button type='button' className='mr-2' onClick={submitForm} disabled={saving}>Create</button>
                <button type='button' onClick={clearForm} disabled={saving}>Cancel</button>
            </div>
            <div className='mb-3'>
                {showError && <div className='alert alert-danger' role='alert'>Error creating construct</div>}
                {showSuccess !== '' && <div className='alert alert-success' role='alert' dangerouslySetInnerHTML={{__html: showSuccess}}/>}
            </div>
        </div>}
    </>;
}

export default CurateConstructNew;