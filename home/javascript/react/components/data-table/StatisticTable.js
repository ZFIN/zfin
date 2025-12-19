import React from 'react';
import PropTypes from 'prop-types';
import {columnDefinitionType, tableStateType} from '../../utils/types';
import HeaderCell from './HeaderCell';
import UberCell from './UberCell';
import EntityCell from './EntityCell';
import {stringToFunction} from '../../utils';

const StatisticTable = ({
    supplementalData,
    data,
    rowKey,
    tableState,
    handleFilterChange,
    handleCardinalitySortChange,
}) => {

    const columnGenerate = (label1, content1, filterName1) => (
        {
            label: label1,
            content: content1,
            filterName: filterName1,
        }
    );

    const columnGenerateOptions = (label1, content1, filterName1, options) => (
        {
            label: label1,
            content: content1,
            filterName: filterName1,
            filterOptions: Object.keys(options),
            //filterOptions: Object.entries(options).map((key,value)=> key+value),
        }
    )

    return (
        <div className='horizontal-scroll-container'>
            <table className='data-table table-fixed'>
                <colgroup>
                    <col className='bg-success'/>
                    <col className='bg-info'/>
                </colgroup>
                <thead>
                    <tr>
                        {Object.entries(supplementalData['statistic']['columns']).map(([key, value]) => key && (
                            <th className='uberCell' key={key}>
                                {( value.columnStat.histogram) && (
                                    <HeaderCell
                                        column={columnGenerateOptions(key, key, value.columnDefinition.filterName,value.columnStat.histogram)}
                                        selectionFilterDisplay={(keyName) => keyName + ' [' + value.columnStat.histogram[keyName] + ']'}
                                        filterValue={tableState.filter && tableState.filter[value.columnDefinition.filterName]}
                                        onFilterChange={handleFilterChange}
                                    />
                                )}
                                {!( value.columnStat.histogram) && (
                                    <HeaderCell
                                        column={columnGenerate(key, key, value.columnDefinition.filterName)}
                                        filterValue={tableState.filter && tableState.filter[value.columnDefinition.filterName]}
                                        onFilterChange={handleFilterChange}
                                    />
                                )}
                            </th>))
                        }
                    </tr>
                    <tr>
                        {Object.entries(supplementalData['statistic']['columns']).map(([key, value]) => key && (
                            <td key={key}><UberCell
                                key={key}
                                value={value}
                                onCardinalitySortChange={handleCardinalitySortChange}
                            />
                            </td>
                        ))
                        }
                    </tr>
                </thead>
                <tbody>
                    {data.map(row => (
                        <tr key={stringToFunction(rowKey)(row)}>
                            {Object.entries(row['columns']).map(([key, value]) => (
                                <td key={key}><EntityCell key={key} value={value}/></td>
                            ))
                            }
                        </tr>
                    ))}

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
    tableState: tableStateType,
    handleFilterChange: PropTypes.func,
    handleCardinalitySortChange: PropTypes.func,
};

export default StatisticTable;