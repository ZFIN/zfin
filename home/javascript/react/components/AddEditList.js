import React from 'react';
import PropTypes from 'prop-types';
import NoData from './NoData';

const AddEditList = ({items, setModalItem, itemKeyProp = 'zdbID', newItem, formatItem,title,maxLength=-1}) => {
    const isMaxLengthReached = () => {
        const hasMaxLength = (!!maxLength) && maxLength > 0;
        if (!hasMaxLength) {
            return false;
        }
        return items.length >= maxLength;
    };
    const handleEditClick = (e, item) => {
        e.preventDefault();
        setModalItem(item, true);
    };

    const handleAddClick = () => {
        setModalItem(newItem, false);
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

            {isMaxLengthReached()
                ? null
                : <button type='button' className='btn btn-link px-0' onClick={handleAddClick}>Add {title}</button>
            }

        </>
    );
};

AddEditList.propTypes = {
    items: PropTypes.array,
    itemKeyProp: PropTypes.string,
    newItem: PropTypes.any,
    setModalItem: PropTypes.func,
    formatItem: PropTypes.func,
    title:PropTypes.string,
    maxLength:PropTypes.number,
};

export default AddEditList;
