import React, { useState } from 'react';
import PropTypes from 'prop-types';
import {useFetch} from '../utils/effects';
import LoadingSpinner from '../components/LoadingSpinner';

import DataTable, {DEFAULT_TABLE_STATE} from '../components/DataTable';
import NoData from '../components/NoData';
import Ribbon from '../components/Ribbon';

const GeneOntologyRibbon = ({geneId}) => {
    const [selected, setSelected] = useState(null);
    const [tableState, setTableState] = useState(DEFAULT_TABLE_STATE);

    const data = useFetch(`/action/api/marker/${geneId}/go/ribbon-summary`);

    if (data.rejected) {
        return <span className='text-danger'>Something went wrong fetching data. Try again later or <a href={`mailto:${process.env.ZFIN_ADMIN}`}>contact us</a>.</span>;
    }

    if (data.pending) {
        return <LoadingSpinner />;
    }

    if (!data.value) {
        return null;
    }

    if (data.value.subjects[0].nb_annotations === 0) {
        return <NoData />
    }

    const columns = [
        {
            label: 'Ontology',
            content: ({ontology}) => ontology,
            width: '60px',
            hidden: selected && selected.group.type !== 'GlobalAll',
        },
        {
            label: 'Qualifier',
            content: ({qualifier}) => qualifier,
            width: '60px',
        },
        {
            label: 'Term',
            content: ({term}) => <a href={`/${term.oboID}`}>{term.termName}</a>,
            width: '200px',
        },
        {
            label: 'Annotation Extension',
            content: ({annotationExtensions}) => annotationExtensions.map(ax => <div key={ax} dangerouslySetInnerHTML={{__html: ax}} />),
            width: '100px',
        },
        {
            label: 'Evidence',
            content: ({evidenceCode}) => (
                <a href={`http://www.geneontology.org/GO.evidence.shtml#${evidenceCode.code.toLowerCase()}`}>
                    {evidenceCode.code}
                </a>
            ),
            width: '50px',
        },
        {
            label: 'With/From',
            content: ({inferenceLinks}) => inferenceLinks.map(il => <div key={il} dangerouslySetInnerHTML={{__html: il}} />),
            width: '120px',
        },
        {
            label: 'Publications',
            content: ({publications}) => publications.map(pub => (
                <div key={pub.zdbID}><a href={'/' + pub.zdbID} dangerouslySetInnerHTML={{__html: pub.shortAuthorList}} /></div>
            )),
            width: '120px',
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
                    url={`/action/api/marker/${geneId}/go${termQuery}`}
                    columns={columns}
                    rowKey='rowKey'
                    tableState={tableState}
                    onTableStateChange={setTableState}
                />
            }
        </div>
    );
};

GeneOntologyRibbon.propTypes = {
    geneId: PropTypes.string,
};

export default GeneOntologyRibbon;
