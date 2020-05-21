import React from 'react';
import PropTypes from 'prop-types';
import {resultResponseType} from '../../utils/types';

const List = ({data, rowFormat, rowKey}) => {
    return (
        <ul>
            {data.results.map(result => (
                <li key={rowKey(result)}>
                    {rowFormat(result)}
                </li>
            ))}
        </ul>
    )
};

List.propTypes = {
    data: resultResponseType,
    rowFormat: PropTypes.func,
    rowKey: PropTypes.func,
};

export default List