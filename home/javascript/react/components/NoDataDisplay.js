import React from 'react';

const NoDataDisplay = ({data, noDataString}) => {
    if (data) {
        return <span>{data}</span>;
    } else {
        return (
            <span className='no-data-tag'>{noDataString}</span>
        );
    }
};

export default NoDataDisplay;
