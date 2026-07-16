import React, {useState} from 'react';
import PropTypes from 'prop-types';
import StatisticDataTable from '../components/data-table/StatisticDataTable';


const MarkerStatisticTable = ({type, show = false}) => {
    const [hasData, setHasData] = useState(false);

    function showData() {
        setHasData(true)
    }

    if(show) {
        showData();
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
                    dataUrl={`/action/api/marker/stats/${type}/histogram`}
                    rowKey='efg'
                />
            )}
        </>
    );
};

MarkerStatisticTable.propTypes = {
    type: PropTypes.string,
    show: PropTypes.bool,
};

export default MarkerStatisticTable;
