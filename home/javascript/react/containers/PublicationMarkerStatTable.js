import React, {useState} from 'react';
import PropTypes from 'prop-types';
import StatisticDataTable from '../components/data-table/StatisticDataTable';


const PublicationMarkerStatTable = ({type}) => {
    const [hasData, setHasData] = useState(false);

    return (
        <>
            {!hasData && (
                <span onClick={setHasData(true)}>Show Data</span>
            )}

            {hasData && (
                <StatisticDataTable
                    dataUrl={`/action/api/publication/stats/${type}/histogram`}
                    rowKey='efg'
                />
            )}
        </>
    );
};

PublicationMarkerStatTable.propTypes = {
    type: PropTypes.string,
};

export default PublicationMarkerStatTable;
