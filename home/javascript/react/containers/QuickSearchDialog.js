import React, {useEffect, useState} from 'react';
import PropTypes from 'prop-types';

const QuickSearchDialog = ({baseUrlWithoutPage, queryString, eventBus}) => {

    const [page, setPage] = useState(1);
    const [pageSize, setPageSize] = useState(10);
    const [filter, setFilter] = useState('');
    const [field, setField] = useState(null);
    const [facetValues, setFacetValues] = useState(null);

    const resetDefaults = () => {
        setPage(1);
        setPageSize(10);
        setFilter('');
    }

    //one time set up to add event listener
    useEffect(() => {
        window.zfinEventBus = eventBus;

        eventBus.subscribe('facet-modal-open', (field) => {
            setField(field);
            resetDefaults();
            fetchFacetValues(field);
        });
    }, []);

    const fetchFacetValues = (field) => {
        if (!field) {
            return;
        }

        let url = '/action/quicksearch/facet-autocomplete?' + queryString + '&field=' + field + '&term=&limit=-1&sort=index';

        fetch(url)
            .then(response => response.json())
            .then(fetchedData => {
                setFacetValues(fetchedData);
            });
    }

    const updateFilter = (event) => {
        setFilter(event.target.value);
    }

    const handlePageSizeChange = (e) => {
        const newPageSize = e.target.value;
        const newLastPageNumber = lastPageNumberByPageSize(newPageSize);
        setPageSize(newPageSize);
        if (page > newLastPageNumber) {
            setPage(newLastPageNumber);
        }
    }

    //important!  the input to a filter is the entire list, not one record at a time
    const filteredValues = () => {
        let input = facetValues;
        let query = filter;
        let split_on = ['-', ':', ',', '(', ')', '.', ';']; //in addition to spaces
        let output = [];
        if (input === undefined) {
            return;
        }
        if (query !== undefined && query !== '') {
            let query_value_string = query.toLowerCase();
            //todo: if we run into performance trouble, do this once when the list is loaded rather than for each keystroke
            split_on.forEach(function(split_char) {
                query_value_string = query_value_string.replace(split_char,' ');
            });
            let query_list = query_value_string.split(' ');
            input.forEach(function(facetValue) {
                let value_string = facetValue.value.toLowerCase();
                //split on commas and colons
                split_on.forEach(function(split_char) {
                    value_string = value_string.replace(split_char,' ');
                });
                let value_list = value_string.split(' ');
                let query_hit_map = [];
                query_list.forEach(function(query_component) {
                    query_hit_map[query_component] = false;
                });

                for (let j = 0 ; j < value_list.length ; j++) {
                    for (let i = 0 ; i < query_list.length ; i++) {
                        if (value_list[j].indexOf(query_list[i]) === 0) {
                            query_hit_map[query_list[i]] = true;
                        }
                    }
                }

                let addToOutput = true;
                query_list.forEach(function(query_component) {
                    if (query_hit_map[query_component] === false) {
                        addToOutput = false;
                    }
                });

                if (addToOutput) {
                    output.push(facetValue)
                }
            });
        } else { //if there was no query, return everything...
            return input;
        }
        return output;
    }


    const paginatedValues = () => {
        return filteredValues().slice((page - 1) * pageSize, page * pageSize);
    }

    const lastPageNumber = () => {
        return Math.ceil(filteredValues().length / pageSize);
    }

    const lastPageNumberByPageSize = (byPageSize) => {
        return Math.ceil(filteredValues().length / byPageSize);
    }

    const nextPage = () => {
        if (page < lastPageNumber()) {
            setPage(page + 1);
        }
    }

    const prevPage = () => {
        if (page > 1) {
            setPage(page - 1);
        }
    }

    if (!facetValues) {
        return <div>Loading...</div>;
    }

    return <>
        <div>
            <div>
                Filter: <input value={filter} onChange={updateFilter}/>
            </div>

            <ul className='list-unstyled modal-body-scrolling' style={{padding: '10px'}}>
                {paginatedValues().map((row) =>
                    <li key={row.value} style={{clear: 'both'}} className='selectable-facet-value facet-value'>
                        {' '}<a className='facet-include' href={baseUrlWithoutPage + 'fq=' + field + ':%22' + row.name + '%22'}>
                            <i className='include-exclude-icon fa fa-plus-circle'/>
                        </a>{' '}
                        <a className='facet-exclude' href={baseUrlWithoutPage + 'fq=-' + field + ':%22' + row.name + '%22'}>
                            <i className='include-exclude-icon fa fa-minus-circle'/>
                        </a>{' '}
                        {' '}
                        <a href={baseUrlWithoutPage + 'fq=' + field + ':%22' + row.name + '%22'} dangerouslySetInnerHTML={{__html: row.value}}/>
                        {' '}
                        {' '}<span style={{'paddingLeft': '1em'}} className='float-right'>({row.count})</span>{' '}
                    </li>
                )}
            </ul>

            <div style={{'marginTop': '.5em'}}>
                <button style={{'marginLeft': '3em'}} className='btn btn-outline-secondary' onClick={prevPage}>
                    <i className='fas fa-chevron-left'/>
                </button>
                {' '}{page}/{lastPageNumber()}{' '}
                <button className='btn btn-outline-secondary' onClick={nextPage}>
                    <i className='fas fa-chevron-right'/>
                </button>

                <div className='float-right'>
                    Show:{' '}
                    <select
                        value={pageSize}
                        onChange={handlePageSizeChange}
                        style={{'width': '4em', 'position': 'relative', 'top': '.3em'}}
                    >
                        <option value='10'>10</option>
                        <option value='100'>100</option>
                        <option value='9999999999'>All</option>
                    </select>
                </div>
            </div>
        </div>
    </>;
};

QuickSearchDialog.propTypes = {
    baseUrlWithoutPage: PropTypes.string,
    queryString: PropTypes.string,
    eventBus: PropTypes.object,
}

export default QuickSearchDialog;
