import React from 'react';
import PropTypes from 'prop-types';
import StatisticDataTable from '../components/data-table/StatisticDataTable';


const PublicationMarkerStatTable = () => {
    return (
        <>
            <StatisticDataTable
                dataUrl='/action/api/publication/stats/antibody/histogram'
                rowKey='efg'
            />
        </>
    );
};

PublicationMarkerStatTable.propTypes = {
    markerType: PropTypes.string,
};

export default PublicationMarkerStatTable;
