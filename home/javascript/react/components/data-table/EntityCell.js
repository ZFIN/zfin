import React from 'react';
import PropTypes from 'prop-types';

const EntityCell = ({value}) => {

    return (
        <>
            {value.columnDefinition.superEntity && (
                <a
                    className='text-break'
                    href={`/${value.columnStat.value}`}
                    dangerouslySetInnerHTML={{__html: value.columnStat.value}}
                />
            )}
            {!value.columnDefinition.superEntity && value.columnDefinition.rowEntity && (
                <table className='table borderless'>
                    <tbody>
                        <tr>
                            <td>T</td>
                            <td>{value.columnStat.totalNumber.toLocaleString()}</td>
                        </tr>
                    </tbody>
                </table>
            )}
            {!value.columnDefinition.superEntity && !value.columnDefinition.rowEntity && (
                <table className='table borderless'>
                    <tbody>
                        <tr>
                            <td>T [D]</td>
                            <td>{value.columnStat.totalNumber.toLocaleString()} [{value.columnStat.totalDistinctNumber.toLocaleString()}]</td>
                        </tr>
                        {value.columnDefinition.multiValued && (
                            <tr>
                                <td>C</td>
                                <td>{value.columnStat.cardinality}</td>
                            </tr>
                        )}
                    </tbody>
                </table>
            )}
        </>
    );
};

EntityCell.propTypes = {
    key: PropTypes.string,
    value: PropTypes.object,
};

export default EntityCell;
