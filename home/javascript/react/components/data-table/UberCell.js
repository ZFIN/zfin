import React from 'react';
import PropTypes from 'prop-types';

const UberCell = ({value}) => {

    return (
        <>
            {value.columnDefinition.superEntity && (
                <>
                    {value.columnStat.totalNumber.toLocaleString()}
                </>
            )}
            {!value.columnDefinition.superEntity && (
                <table className='table borderless'>
                    <tbody>
                        <tr>
                            <td>T [D]</td>
                            <td>{value.columnStat.totalNumber.toLocaleString()} [{value.columnStat.totalDistinctNumber.toLocaleString()}]</td>
                        </tr>
                        {value.columnDefinition.multiValued && (
                            <tr>
                                <td>C</td>
                                <td>{value.columnStat.cardinality.toLocaleString()}</td>
                            </tr>
                        )
                        }
                        {value.columnDefinition.rowEntity && (
                            <tr>
                                <td>M</td>
                                <td>{value.columnStat.multiplicity}</td>
                            </tr>
                        )}
                    </tbody>
                </table>
            )}
        </>
    );
};

UberCell.propTypes = {
    key: PropTypes.string,
    value: PropTypes.object,
};

export default UberCell;
