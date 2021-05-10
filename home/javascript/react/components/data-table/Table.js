import React from 'react';
import PropTypes from 'prop-types';
import {stringToFunction} from '../../utils';
import NoData from '../NoData';
import {columnDefinitionType} from '../../utils/types';

const Table = ({
    columnHeaderFormat,
    columns,
    data,
    noDataMessage = 'No rows match filters',
    rowKey,
    supplementalData,
    total,
}) => {
    return (
        <div className='horizontal-scroll-container'>
            <table className='data-table table-fixed'>
                <thead>
                    <tr>
                        {columns.map(column => !column.hidden && (
                            <th key={column.key || column.label} style={{width: column.width, textAlign: column.align}}>
                                {columnHeaderFormat ?
                                    columnHeaderFormat(column) :
                                    column.label
                                }
                            </th>
                        ))}
                    </tr>
                </thead>
                <tbody>
                    {data.map(row => (
                        <tr key={stringToFunction(rowKey)(row)}>
                            {columns.map(column => {
                                if (column.hidden) {
                                    return null;
                                }
                                const valueGetter = stringToFunction(column.content);
                                const value = valueGetter(row, supplementalData);
                                return (
                                    <td key={column.key || column.label} style={{textAlign: column.align}}>
                                        {value}
                                    </td>
                                );
                            })}
                        </tr>
                    ))}

                    {total === 0 && (
                        <tr>
                            <td className='text-center' colSpan={columns.length}>
                                <NoData placeholder={noDataMessage} />
                            </td>
                        </tr>
                    )}
                </tbody>
            </table>
        </div>
    );
};

Table.propTypes = {
    columnHeaderFormat: PropTypes.func,
    columns: PropTypes.arrayOf(columnDefinitionType),
    data: PropTypes.array,
    noDataMessage: PropTypes.string,
    rowKey: PropTypes.oneOfType([PropTypes.string, PropTypes.func]),
    supplementalData: PropTypes.object,
    total: PropTypes.number,
};

export default Table;