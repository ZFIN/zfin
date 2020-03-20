import React, {useState} from 'react';
import PropTypes from 'prop-types';
import {useFetch} from '../utils/effects';
import LoadingSpinner from '../components/LoadingSpinner';

import NoData from '../components/NoData';
import Ribbon from '../components/Ribbon';
import GenericErrorMessage from '../components/GenericErrorMessage';
import DataTable, {DEFAULT_TABLE_STATE} from '../components/DataTable';
import AttributionLink from '../components/AttributionLink';

const GeneExpressionRibbon = ({geneId}) => {

        const [selected, setSelected] = useState(null);
        const [tableState, setTableState] = useState(DEFAULT_TABLE_STATE);

        const data = useFetch(`/action/api/marker/${geneId}/expression/ribbon-summary`);

        if (data.rejected) {
            return <GenericErrorMessage/>;
        }

        if (data.pending) {
            return <LoadingSpinner/>;
        }

        if (!data.value) {
            return null;
        }

        if (data.value.subjects[0].nb_annotations === 0) {
            return <NoData/>
        }

        const columns = [
            {
                label: 'Expression Location',
                content: row => <a href={`/action/marker/citation-list/${row.term.oboID}`}>{row.term.name}</a>,
                width: '60px',
            },
            {
                label: 'Stage Observed',
                content: '',
                width: '60px',
            },
            {
                label: 'Publications',
                content: row => (<AttributionLink
                    url={`/action/marker/${row.term.oboID}`}
                    publicationCount={row.numberOfPublications}
                    publication={row.publication}
                />)
                ,
                width:
                    '120px',
            },
        ];

        const handleItemClick = (subject, group) => {
            if (selected && selected.group.id === group.id && selected.group.type === group.type) {
                setSelected(null);
            } else {
                setTableState(DEFAULT_TABLE_STATE);
                setSelected({subject, group});
            }
        };

        let termQuery = '';
        if (selected) {
            if (selected.group.type !== 'GlobalAll') {
                termQuery += `?termId=${selected.group.id}`;
            }
            if (selected.group.type === 'Other') {
                termQuery += '&isOther=true';
            }
        }

        return (
            <div>
                <Ribbon
                    subjects={data.value.subjects}
                    categories={data.value.categories}
                    itemClick={handleItemClick}
                    selected={selected}
                />

                {selected &&
                <DataTable
                    url={`/action/api/marker/${geneId}/expression/ribbon-detail${termQuery}`}
                    columns={columns}
                    rowKey='rowKey'
                    tableState={tableState}
                    onTableStateChange={setTableState}
                />
                }
            </div>
        );
    }
;

GeneExpressionRibbon.propTypes = {
    geneId: PropTypes.string,
};

export default GeneExpressionRibbon;