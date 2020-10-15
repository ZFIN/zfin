import React, { useState } from 'react';
import PropTypes from 'prop-types';
import MarkerAliasEditModal from './MarkerAliasEditModal';
import NoData from './NoData';

const MarkerAliases = ({markerId, aliases, setAliases}) => {

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
        setAliases(aliases.filter(alias => alias.zdbID !== modalAlias.zdbID));
    }

    const handleAdd = (newAlias) => {
        setAliases([
            ...aliases,
            newAlias
        ]);
    }

    const handleEdit = (updated) => {
        const updatedIdx = aliases.findIndex(alias => alias.zdbID === modalAlias.zdbID);
        setAliases([
            ...aliases.slice(0, updatedIdx),
            updated,
            ...aliases.slice(updatedIdx + 1)
        ]);
    }

    return (
        <>
            {aliases.length === 0 && <NoData placeholder='None' />}

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
    aliases: PropTypes.array,
    setAliases: PropTypes.func,
};

export default MarkerAliases;
