import React, { useState } from 'react';
import PropTypes from 'prop-types';
import useFetch from '../hooks/useFetch';
import NoData from '../components/NoData';
import http from '../utils/http';
import LoadingSpinner from '../components/LoadingSpinner';
import useAddEditDeleteForm from '../hooks/useAddEditDeleteForm';
import AddEditDeleteModal from '../components/AddEditDeleteModal';
import FormGroup from '../components/form/FormGroup';
import SupplierInput from '../components/form/SupplierInput';

// this component works a little different that the typical Add/Edit/Delete list components because there
// is no concept of editing a supplier. Therefore the list presents a Delete link instead of an Edit link.
// that is handled within this component although if more components need the same pattern this could be
// integrated into the AddEditList component or spun off into its own component.

const MarkerEditSuppliers = ({ markerId }) => {
    const [error, setError] = useState('');
    const [deleting, setDeleting] = useState('');
    const [modalSupplier, setModalSupplier] = useState(null);

    const {
        value: suppliers,
        setValue: setSuppliers,
    } = useFetch(`/action/marker/${markerId}/suppliers`);

    const {
        values,
        modalProps
    } = useAddEditDeleteForm({
        addUrl: `/action/marker/${markerId}/suppliers`,
        onSuccess: () => setModalSupplier(null),
        items: suppliers,
        setItems: setSuppliers,
        defaultValues: modalSupplier,
    });

    const handleDeleteClick = async (event, supplier) => {
        event.preventDefault();
        setDeleting(supplier.zdbID);
        try {
            await http.delete(`/action/marker/${markerId}/suppliers/${supplier.zdbID}`)
            setSuppliers(suppliers.filter(other => other.zdbID !== supplier.zdbID));
        } catch (error) {
            setError('Could not delete supplier. Try again later.');
            throw error;
        }
        setDeleting('');
    }

    const handleAddClick = () => {
        setModalSupplier({
            name: '',
        })
    };

    if (!suppliers) {
        return null;
    }

    return (
        <>
            {suppliers.length === 0 && <NoData placeholder='None' />}

            <ul className='list-unstyled'>
                {suppliers.map(supplier => {
                    return (
                        <li key={supplier.zdbID}>
                            <a href={`/${supplier.zdbID}`}>{supplier.name}</a>
                            {deleting === supplier.zdbID ?
                                <LoadingSpinner /> :
                                <a className='show-on-hover px-2' href='#' onClick={e => handleDeleteClick(e, supplier)}>
                                    Delete
                                </a>
                            }
                        </li>
                    );
                })}
            </ul>

            {error && <div className='text-danger'>{error}</div>}

            <button type='button' className='btn btn-link px-0' onClick={handleAddClick}>Add</button>

            <AddEditDeleteModal {...modalProps} header='Supplier'>
                {values &&
                    <FormGroup
                        inputClassName='col-md-10'
                        label='Supplier'
                        id='supplier-name'
                        field='name'
                        tag={SupplierInput}
                    />
                }
            </AddEditDeleteModal>
        </>
    );
};

MarkerEditSuppliers.propTypes = {
    markerId: PropTypes.string,
};

export default MarkerEditSuppliers;
