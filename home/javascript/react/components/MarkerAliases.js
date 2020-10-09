import React, { useState } from 'react';
import PropTypes from 'prop-types';
import useMutableFetch from '../hooks/useMutableFetch';
import MarkerAliasEditModal from './MarkerAliasEditModal';

const MarkerAliases = ({markerId}) => {
    const {
        value: aliases,
        setValue,
    } = useMutableFetch(`/action/marker/${markerId}/aliases`, []);
    const [modalAlias, setModalAlias] = useState(null);

    const handleEditClick = (event, alias) => {
        event.preventDefault();
        setModalAlias(alias)
    }

    const handleAddClick = () => {
        setModalAlias({
            alias: '',
            references: [{ zdbID: '' }],
        });
    }

    const handleDelete = () => {
        setValue(aliases.filter(alias => alias.zdbID !== modalAlias.zdbID));
    }

    const handleAdd = (newAlias) => {
        setValue([
            ...aliases,
            newAlias
        ]);
    }

    const handleEdit = (updated) => {
        const updatedIdx = aliases.findIndex(alias => alias.zdbID === modalAlias.zdbID);
        setValue([
            ...aliases.slice(0, updatedIdx),
            updated,
            ...aliases.slice(updatedIdx + 1)
        ]);
    }

    return (
        <>
            <ul className='list-unstyled'>
                {aliases.map(alias => (
                    <li key={alias.zdbID}>
                        {alias.alias} {alias.references.length > 0 && <>({alias.references.length})</>}
                        <a className='show-on-hover px-1' href='#' onClick={e => handleEditClick(e, alias)}>Edit</a>
                    </li>
                ))}
            </ul>

            <button type='button' className='btn btn-link px-0' onClick={handleAddClick}>Add</button>

            <MarkerAliasEditModal
                alias={modalAlias}
                markerId={markerId}
                onAdd={handleAdd}
                onClose={() => setModalAlias(null)}
                onDelete={handleDelete}
                onEdit={handleEdit}
            />
        </>
    )
};

MarkerAliases.propTypes = {
    markerId: PropTypes.string,
};

export default MarkerAliases;
