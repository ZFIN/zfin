import * as React from 'react';
import type { StatusPayload } from './statusPayload';

/**
 * The submission page's current server-computed status payload, for editors
 * that are mounted by a list renderer rather than handed a JsonForms config
 * of their own: the inline mutation editor and the gene / lesion / assay /
 * phenotype editors inside it (ZFIN-10543). They look up their own entity's
 * field and section maps here, so the badges in edit mode match the ones the
 * read-only cards showed.
 *
 * Null outside a page that provides it; editors then render no badges.
 */
export const StatusPayloadContext = React.createContext<StatusPayload | null>(null);
