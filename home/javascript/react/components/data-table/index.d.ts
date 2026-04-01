import React from 'react';

// eslint-disable-next-line @typescript-eslint/no-explicit-any
interface ColumnDefinition<T = any> {
    label: string;
    content?: (row: T, supplementalData?: Record<string, unknown>) => React.ReactNode;
    width?: string;
    filterName?: string;
    hidden?: boolean;
    hideIfAllNull?: boolean;
    accessor?: string;
}

interface DownloadOption {
    format: string;
    label: string;
}

interface SortOption {
    value: string;
    label: string;
}

interface TableState {
    limit?: number;
    page?: number;
    sortBy?: string | null;
    filter?: Record<string, string> | null;
}

interface DataTableProps {
    columns: ColumnDefinition[];
    dataUrl: string;
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    rowKey: string | ((row: any) => string);
    downloadOptions?: DownloadOption[];
    onDataLoaded?: (data: unknown) => void;
    onDataLoadedCount?: (count: number) => void;
    pagination?: boolean;
    setTableState?: (state: TableState) => void;
    sortOptions?: SortOption[];
    tableState?: TableState;
    tableFixed?: boolean;
}

declare const DataTable: React.FC<DataTableProps>;

export default DataTable;

export declare const DEFAULT_TABLE_STATE: TableState;

interface DataListProps {
    columns: ColumnDefinition[];
    dataUrl: string;
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    rowKey: string | ((row: any) => string);
    downloadOptions?: DownloadOption[];
    onDataLoaded?: (data: unknown) => void;
    onDataLoadedCount?: (count: number) => void;
    pagination?: boolean;
    setTableState?: (state: TableState) => void;
    sortOptions?: SortOption[];
    tableState?: TableState;
}

export declare const DataList: React.FC<DataListProps>;

interface CollapseTableProps {
    columns: ColumnDefinition[];
    dataUrl: string;
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    rowKey: string | ((row: any) => string);
    [key: string]: unknown;
}

export declare const CollapseTable: React.FC<CollapseTableProps>;
