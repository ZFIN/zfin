import React from 'react';
import PropTypes from 'prop-types';
import StatisticDataTable from '../components/data-table/StatisticDataTable';


const PublicationMarkerStatTable = ({type}) => {
    return (
        <>
            <StatisticDataTable
                dataUrl={`/action/api/publication/stats/${type}/histogram`}
                rowKey='efg'
            />
        </>
    );
};

PublicationMarkerStatTable.propTypes = {
    type: PropTypes.string,
};

export default PublicationMarkerStatTable;
