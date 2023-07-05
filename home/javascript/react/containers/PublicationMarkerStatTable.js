import React, {useState} from 'react';
import PropTypes from 'prop-types';
import StatisticDataTable from '../components/data-table/StatisticDataTable';


const PublicationMarkerStatTable = ({type}) => {
    const [hasData, setHasData] = useState(false);

    function showData() {
        setHasData(true)
    }

    return (
        <>
            {!hasData && (
                <button onClick={showData}>
                    Show Stats
                </button>
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
