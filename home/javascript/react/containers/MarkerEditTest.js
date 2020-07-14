import React, { useState } from 'react';
import Autocompletify from '../components/Autocompletify';

const MarkerEditTest = () => {
    const [ value, setValue ] = useState('');
    return (
        <>
            <div>Selected: {value}</div>
            <Autocompletify
                className='form-control'
                url='/action/quicksearch/autocomplete?q=%QUERY'
                value={value}
                onChange={e => setValue(e.target.value)}
            />
        </>
    );
};

export default MarkerEditTest;
