import React from 'react';
import { arrayOf, shape, string } from 'prop-types';

const SupplierList = ({suppliers}) => {
    if (!suppliers) {
        return null;
    }

    return suppliers.map((supplier) => (
        <div className='mb-1' key={supplier.organization.zdbID}>
            <a href={supplier.organization.zdbID}>{supplier.organization.name}</a> (
            <a href={supplier.orderURL}>order this</a>)
        </div>
    ));
};

SupplierList.propTypes = {
    suppliers: arrayOf(shape({
        organization: shape({
            zdbID: string,
            name: string,
        }),
        orderURL: string,
    }))
}

export default SupplierList;
