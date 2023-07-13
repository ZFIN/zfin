import React, {useState} from 'react';
import PropTypes from 'prop-types';

const UberCell = ({value, onCardinalitySortChange}) => {

    const [sortField, setSortField] = useState(1);

    const handleCardSortChange = (newValue) => {
        onCardinalitySortChange(value.columnDefinition.filterName, newValue);
    }

    const toggleSortingFilter = () => {
        setSortField(prev => ((prev + 1) % 3));
        handleCardSortChange(sortField);
    }

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
                        <tr>
                            <td>M</td>
                            <td>{value.columnStat.multiplicity}
                                <button
                                    className='btn text-muted bg-transparent border-0 p-0'
                                    onClick={toggleSortingFilter}
                                    role='button'
                                >

                                    {sortField === 1 && (
                                        <i className='fas fa-sort'/>
                                    )
                                    }
                                    {sortField === 2 && (
                                        <i className='fas fa-sort-down'/>
                                    )
                                    }
                                    {sortField === 0 && (
                                        <i className='fas fa-sort-up'/>
                                    )
                                    }
                                </button>
                            </td>
                        </tr>
                        {value.columnDefinition.multiValued && (
                            <tr>
                                <td>C</td>
                                <td>{value.columnStat.cardinality.toLocaleString()} {' '}
                                    <button
                                        className='btn text-muted bg-transparent border-0 p-0'
                                        onClick={toggleSortingFilter}
                                        role='button'
                                    >

                                        {sortField === 1 && (
                                            <i className='fas fa-sort'/>
                                        )
                                        }
                                        {sortField === 2 && (
                                            <i className='fas fa-sort-down'/>
                                        )
                                        }
                                        {sortField === 0 && (
                                            <i className='fas fa-sort-up'/>
                                        )
                                        }
                                    </button>
                                </td>
                            </tr>
                        )
                        }
                        {value.columnDefinition.rowEntity && (
                            <tr>
                                <td>M</td>
                                <td>{value.columnStat.multiplicity}
                                    <button
                                        className='btn text-muted bg-transparent border-0 p-0'
                                        onClick={toggleSortingFilter}
                                        role='button'
                                    >

                                        {sortField === 1 && (
                                            <i className='fas fa-sort'/>
                                        )
                                        }
                                        {sortField === 2 && (
                                            <i className='fas fa-sort-down'/>
                                        )
                                        }
                                        {sortField === 0 && (
                                            <i className='fas fa-sort-up'/>
                                        )
                                        }
                                    </button>
                                </td>
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
    onCardinalitySortChange: PropTypes.func,
};

export default UberCell;
