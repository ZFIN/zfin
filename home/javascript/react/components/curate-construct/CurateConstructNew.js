import React, {useState} from 'react';
import PropTypes from 'prop-types';
import ConstructMarkerAutocomplete from "./ConstructMarkerAutocomplete";
import ConstructCassetteListEditor, {cassetteHumanReadableList} from "./ConstructCassetteListEditor";

const CurateConstructNew = ({publicationId, show= true}) => {

    const [display, setDisplay] = useState(show);
    const [chosenType, setChosenType] = useState('Tg');
    const [prefix, setPrefix] = useState('');
    const [synonym, setSynonym] = useState('');
    const [sequence, setSequence] = useState('');
    const [publicNote, setPublicNote] = useState('');
    const [curatorNote, setCuratorNote] = useState('');
    const [cassettes, setCassettes] = useState([]);
    const [cassettesDisplay, setCassettesDisplay] = useState('');

    const toggleDisplay = () => setDisplay(!display);

    const handleCassettesChanged = (cassettesChanged) => {
        console.log('DEBUG: CurateConstructNew.handleCassettesChanged() cassettes:', cassettesChanged);
        setCassettes(cassettesChanged);
        setCassettesDisplay(cassetteHumanReadableList(cassettesChanged));
    }

    return <>
        <div className='display-none'>DEBUG: CurateConstructNew for {publicationId}</div>
        <div className='mb-3'>
            <span className='bold'>CREATE NEW CONSTRUCT: </span>
            <a onClick={toggleDisplay} style={{textDecoration: 'underline'}}>{display ? 'Hide' : 'Show'}</a>
        </div>
        {display &&
        <div className='mb-3' style={{backgroundColor: '#eee'}}>
            <table>
                <thead></thead>
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
                <ConstructCassetteListEditor publicationId={publicationId} onChange={handleCassettesChanged}/>
            </div>
            <div className='mb-3'>
                <b>Construct Name</b><br/>
                {chosenType + prefix + '(' + cassettesDisplay + ')'}
            </div>
        </div>}
    </>;
}

CurateConstructNew.propTypes = {
    publicationId: PropTypes.string,
}

export default CurateConstructNew;