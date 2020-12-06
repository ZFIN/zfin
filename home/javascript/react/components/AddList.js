import React from 'react';
import PropTypes from 'prop-types';


const AddList = ({ setModalItem, newItem,title}) => {


    const handleAddClick = () => {
        setModalItem(newItem, false);
    };

    return (
        <>




            <button type='button' className='btn btn-link px-0' onClick={handleAddClick}>Add {title}</button>
        </>
    );
};

AddList.propTypes = {

    newItem: PropTypes.any,
    setModalItem: PropTypes.func,
    title:PropTypes.string,
};

export default AddList;
