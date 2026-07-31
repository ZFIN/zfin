import React from 'react';
import { arrayOf, shape, string } from 'prop-types';
import Link from './ExternalLinkMaybe';

const SupplierList = ({suppliers}) => {
    if (!suppliers) {
        return null;
    }

    return suppliers.map((supplier) => (
        <div className='mb-1' key={supplier.organization.zdbID}>
            <a href={supplier.organization.zdbID}>{supplier.organization.name}</a> (
            <Link href={supplier.orderURL}>order this</Link>)
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
