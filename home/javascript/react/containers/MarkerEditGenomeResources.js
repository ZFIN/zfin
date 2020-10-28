import React, { useState } from 'react';
import PropTypes from 'prop-types';
import useFetch from '../hooks/useFetch';
import LoadingSpinner from '../components/LoadingSpinner';
import NoData from '../components/NoData';
import MarkerGenomeResourceEditModal from '../components/MarkerGenomeResourceEditModal';

const MarkerEditGenomeResources = ({markerId, group = 'other marker pages'}) => {
    const links = useFetch(`/action/marker/${markerId}/links?group=${group}`);
    const databases = useFetch(`/action/marker/link/databases?group=${group}`);
    const [modalLink, setModalLink] = useState(null);

    const handleEditClick = (e, link) => {
        e.preventDefault();
        setModalLink(link)
    };
    const handleAddClick = () => {
        setModalLink({
            accession: '',
            referenceDatabaseZdbID: '',
            references: [{ zdbID: '' }],
        });
    };

    const handleDelete = () => {
        links.setValue(links.value.filter(link => link.dblinkZdbID !== modalLink.dblinkZdbID));
    }

    const handleAdd = (newLink) => {
        links.setValue([
            ...links.value,
            newLink
        ]);
    }

    const handleEdit = (updated) => {
        const updatedIdx = links.value.findIndex(alias => alias.dblinkZdbID === modalLink.dblinkZdbID);
        links.setValue([
            ...links.value.slice(0, updatedIdx),
            updated,
            ...links.value.slice(updatedIdx + 1)
        ]);
    }

    if (links.pending || databases.pending) {
        return <LoadingSpinner />;
    }

    if (!links.value) {
        return null;
    }

    return (
        <>
            {links.value.length === 0 && <NoData placeholder='None' />}

            <ul className='list-unstyled'>
                {links.value.map(link => (
                    <li key={link.dblinkZdbID}>
                        <a href={link.link}>
                            {link.referenceDatabaseName}:{link.accession}
                        </a>
                        {' '}
                        {link.references && link.references.length && <span>({link.references.length})</span>}
                        <a className='show-on-hover px-1' href='#' onClick={e => handleEditClick(e, link)}>Edit</a>
                    </li>
                ))}
            </ul>

            <button type='button' className='btn btn-link px-0' onClick={handleAddClick}>Add</button>

            <MarkerGenomeResourceEditModal
                link={modalLink}
                onClose={() => setModalLink(null)}
                databaseOptions={databases.value}
                markerId={markerId}
                isEdit={modalLink && !!modalLink.dblinkZdbID}
                onAdd={handleAdd}
                onDelete={handleDelete}
                onEdit={handleEdit}
            />
        </>
    );
};

MarkerEditGenomeResources.propTypes = {
    markerId: PropTypes.string,
    group: PropTypes.string,
};

export default MarkerEditGenomeResources;
