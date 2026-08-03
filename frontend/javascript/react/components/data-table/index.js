import DataTable from './DataTable';
import DataList from './DataList';
import CollapseTable from './CollapseTable';

export const DEFAULT_TABLE_STATE = {
    limit: 10,
    page: 1,
    sortBy: null,
    filter: null,
};

export {
    DataList,
    CollapseTable,
};

export default DataTable;
