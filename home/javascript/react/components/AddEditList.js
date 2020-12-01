import React from 'react';
import PropTypes from 'prop-types';
import NoData from './NoData';

const AddEditList = ({items, setModalItem, itemKeyProp = 'zdbID', newItem, formatItem,title}) => {
    const handleEditClick = (e, item) => {
        e.preventDefault();
        setModalItem(item);
    };

    const handleAddClick = () => {
        setModalItem(newItem);
    };

    return (
        <>
            {items.length === 0 && <NoData placeholder='None' />}

            <ul className='list-unstyled'>
                {items.map(item => {
                    const editLink = (
                        <a className='show-on-hover px-1' href='#' onClick={e => handleEditClick(e, item)}>
                            Edit
                        </a>
                    );
                    return (
                        <li key={item[itemKeyProp]}>
                            {formatItem(item, editLink)}
                        </li>
                    );
                })}
            </ul>

            <button type='button' className='btn btn-link px-0' onClick={handleAddClick}>Add {title}</button>
        </>
    );
};

AddEditList.propTypes = {
    items: PropTypes.array,
    itemKeyProp: PropTypes.string,
    newItem: PropTypes.object,
    setModalItem: PropTypes.func,
    formatItem: PropTypes.func,
    title:PropTypes.string,
};

export default AddEditList;
