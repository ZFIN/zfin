import React, {useState} from 'react';
import PropTypes from 'prop-types';
import {useFetch, useRibbonState} from '../utils/effects';
import LoadingSpinner from '../components/LoadingSpinner';

import NoData from '../components/NoData';
import Ribbon, {getSelectedTermQueryParams} from '../components/Ribbon';
import GenericErrorMessage from '../components/GenericErrorMessage';
import DataTable, {DEFAULT_TABLE_STATE} from '../components/DataTable';
import AttributionLink from '../components/AttributionLink';
import StagePresentation from '../components/StagePresentation';

const GeneExpressionRibbon = ({geneId}) => {
    const [tableState, setTableState] = useState(DEFAULT_TABLE_STATE);
    const [selected, setSelected] = useRibbonState(() => setTableState(DEFAULT_TABLE_STATE));

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
            width: '250px',
        },
        {
            label: 'Stage Observed',
            content: row => (<StagePresentation stages={row.stageHistogram}/>),
            subHeader: 'cleavage blastula gastrula segmentation pharyngula hatching larva juvenile adult',
            width: '300',
        },
        {
            label: 'Publications',
            content: row => (<AttributionLink
                accession={null}
                url={`/action/marker/${row.term.oboID}`}
                publicationCount={row.numberOfPublications}
                publication={row.publication}
            />),
            width: '450px',
        },
    ];

    return (
        <div>
            <Ribbon
                subjects={data.value.subjects}
                categories={data.value.categories}
                itemClick={setSelected}
                selected={selected}
            />

            {selected &&
                <DataTable
                    url={`/action/api/marker/${geneId}/expression/ribbon-detail${getSelectedTermQueryParams(selected)}`}
                    columns={columns}
                    rowKey='rowKey'
                    tableState={tableState}
                    onTableStateChange={setTableState}
                />
            }
        </div>
    );
};

GeneExpressionRibbon.propTypes = {
    geneId: PropTypes.string,
};

export default GeneExpressionRibbon;