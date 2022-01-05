import React from 'react';
import PropTypes from 'prop-types';
import {columnDefinitionType} from '../../utils/types';

const StatisticTable = ({
    supplementalData,
}) => {
    return (
        <div className='horizontal-scroll-container'>
            <table className='data-table table-fixed'>
                <thead>
                    <tr>
                        ${supplementalData.get('statistic').get('columns').map(column => column && ({column}))}

                    </tr>
                </thead>
                <tbody>
                    <tr>
                        <td className='text-center'>
                            Hi my friend
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>
    );
};

StatisticTable.propTypes = {
    columnHeaderFormat: PropTypes.func,
    columns: PropTypes.arrayOf(columnDefinitionType),
    data: PropTypes.array,
    noDataMessage: PropTypes.string,
    rowKey: PropTypes.oneOfType([PropTypes.string, PropTypes.func]),
    supplementalData: PropTypes.object,
    total: PropTypes.number,
};

export default StatisticTable;