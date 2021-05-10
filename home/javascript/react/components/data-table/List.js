import React from 'react';
import PropTypes from 'prop-types';
import NoData from '../NoData';

const List = ({items, rowFormat, rowKey, total}) => {
    if (total === 0) {
        return (
            <div className='text-center'>
                <NoData placeholder='No items match filter' />
            </div>
        );
    }

    return (
        <ul>
            {items.map(result => (
                <li key={rowKey(result)}>
                    {rowFormat(result)}
                </li>
            ))}
        </ul>
    )
};

List.propTypes = {
    items: PropTypes.array,
    rowFormat: PropTypes.func,
    rowKey: PropTypes.func,
    total: PropTypes.number,
};

export default List