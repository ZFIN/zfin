import React, {useState} from 'react';
import PropTypes from 'prop-types';
import {columnDefinitionType} from '../../utils/types';
import CheckboxListFilter from './CheckboxListFilter';
import TextBoxFilter from './TextBoxFilter';

const HeaderCell = ({column, filterValue, onFilterChange, supplementalData, selectionFilterDisplay}) => {
    const [filterOpen, setFilterOpen] = useState(false);
    const handleFilterChange = (newValue) => {
        if (!column.filterName) {
            return;
        }
        onFilterChange(column.filterName, newValue);
    }
    const filterText = 'enter keyword'
    const toggleFilter = () => setFilterOpen(prev => !prev);

    return (
        <>
            {column.label} {column.filterName && (
                <button className='btn text-muted bg-transparent border-0 p-0' onClick={toggleFilter} role='button'>
                    <i className='fas fa-filter'/>
                </button>
            )}
            {filterOpen && (
                column.filterOptions ?
                    <CheckboxListFilter options={column.filterOptions} value={filterValue} onChange={handleFilterChange} displayFunction={selectionFilterDisplay}/> :
                    column.filterOptionFromSupplementalData ?
                        <CheckboxListFilter
                            options={supplementalData[column.filterOptionFromSupplementalData]}
                            value={filterValue}
                            onChange={handleFilterChange}
                            displayFunction={selectionFilterDisplay}
                        /> :
                        <TextBoxFilter value={filterValue} onChange={handleFilterChange} placeholder={filterText}/>
            )
            }
        </>
    );
};

HeaderCell.propTypes = {
    column: columnDefinitionType,
    filterValue: PropTypes.string,
    supplementalData: PropTypes.object,
    onFilterChange: PropTypes.func,
    selectionFilterDisplay: PropTypes.func,
};

export default HeaderCell;
