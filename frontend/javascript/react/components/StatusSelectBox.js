import React from 'react';
import PropTypes from 'prop-types';

const StatusSelectBox = ({statuses, selectedId, onSelect}) => {
    return (
        <select className='form-control' onChange={(event) => onSelect(event.target.value)} value={selectedId}>
            <option value=''>Any</option>
            {statuses.map(status => (
                <option key={status.id} value={status.id}>
                    {status.name}
                </option>
            ))}
        </select>
    );
};

StatusSelectBox.propTypes = {
    statuses: PropTypes.array,
    selectedId: PropTypes.string,
    onSelect: PropTypes.func,
};

export default StatusSelectBox;
