import React from 'react';
import PropTypes from 'prop-types';

const CuratorSelectBox = ({curators, userId, selectedId, onSelect}) => {
    if (!curators || !curators.length) {
        return null;
    }
    let options = [...curators];
    const meIdx = options.findIndex(curator => curator.zdbID === userId);
    const me = options.splice(meIdx, 1)[0];
    me.name = 'Me';
    options = [
        me,
        {zdbID: '*', name: 'Anyone'},
        {zdbID: '-', name: '──────────', disabled: true},
        ...options
    ];
    return (
        <select className='form-control' onChange={(event) => onSelect(event.target.value)} value={selectedId}>
            {options.map(option => (
                <option disabled={option.disabled} key={option.zdbID} value={option.zdbID}>
                    {option.name}
                </option>
            ))}
        </select>
    );
};

CuratorSelectBox.propTypes = {
    curators: PropTypes.array,
    selectedId: PropTypes.string,
    userId: PropTypes.string,
    onSelect: PropTypes.func,
};

export default CuratorSelectBox;