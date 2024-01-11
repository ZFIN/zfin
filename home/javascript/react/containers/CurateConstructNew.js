import React, {useState} from 'react';
import PropTypes from 'prop-types';

const CurateConstructNew = ({publicationId}) => {

    const [display, setDisplay] = useState(false);
    const [chosenType, setChosenType] = useState('Tg');
    const [prefix, setPrefix] = useState('');
    const [synonym, setSynonym] = useState('');
    const [sequence, setSequence] = useState('');
    const [publicNote, setPublicNote] = useState('');
    const [curatorNote, setCuratorNote] = useState('');

    const toggleDisplay = () => setDisplay(!display);
    return <>
        <div className='display-none'>DEBUG: CurateConstructNew for {publicationId}</div>
        <div className='mb-3'>
            <span className='bold'>CREATE NEW CONSTRUCT: </span>
            <a onClick={toggleDisplay} style={{textDecoration: 'underline'}}>{display ? 'Hide' : 'Show'}</a>
        </div>
        {display &&
        <div className='mb-3' style={{backgroundColor: '#eee'}}>
            <table>
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
            </table>
            <div className='mt-3 cassette-handler'>
                <div id='cassette1' className='clonable'>
                    <div className='cassette-inner'>
                        <span className='tab'>
                            <a className='cloneMe' title='Add' href='#'>Add cassette</a>
                            <a className='deleteMe' title='Delete' href='#'>Remove cassette </a>
                        </span>
                        <b>Promoter</b>
                        <div id='promoterCassette1' className='1'>
                            <input id='cassette1Promoter1' className='cassette1Promoter' size='10'/>
                            <button id='addPromoter1' className='1'>+</button>
                            <button id='delPromoter1' className='1'>-</button>
                        </div>
                        <b>Coding</b>
                        <div id='codingCassette1' className='1'>
                            <input id='cassette1Coding1' className='cassette1Coding' size='10'/>
                            <button id='addCoding1' className='1'>+</button>
                            <button id='delCoding1' className='1'>-</button>
                        </div>
                    </div>
                </div>
            </div>
        </div>}
    </>;
}

CurateConstructNew.propTypes = {
    publicationId: PropTypes.string,
}

export default CurateConstructNew;