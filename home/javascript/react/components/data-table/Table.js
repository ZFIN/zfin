import React from 'react';
import {stringToFunction} from '../../utils';
import NoData from '../NoData';

const Table = ({columns, columnHeaderFormat, data, rowKey}) => {
    const {results, supplementalData, total} = data;
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
                    {results.map(row => (
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
                                <NoData placeholder='No rows match filters' />
                            </td>
                        </tr>
                    )}
                </tbody>
            </table>
        </div>
    );
};

export default Table;