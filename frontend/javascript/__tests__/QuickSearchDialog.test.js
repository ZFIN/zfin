import { test } from 'node:test';
import assert from 'node:assert/strict';
import { findMatches } from '../react/containers/quickSearchFilter';

test('filter works', () => {
    assert.equal(findMatches(inputs, 'Cas9').length, 5);
    assert.equal(findMatches(inputs, 'zCas9').length, 2);
    assert.equal(findMatches(inputs, 'cerulean').length, 2);
    assert.equal(findMatches(inputs, 'crispr1').length, 9);
});

const inputs = [
    {
        'value': 'Tg(U6:CRISPR1-pgd,fli1:Cas9,myl7:EGFP)',
    },
    {
        'value': 'Tg(gfap:Cas9-2A-mCherry,rnu6-z2:CRISPR12-tp53)',
    },
    {
        'value': 'Tg(hsp70l:LOXP-DsRed-LOXP-Cas9,rnu6-32:CRISPR1-tyr)',
    },
    {
        'value': 'Tg(lyz:Cas9-CRISPR1-mfn2-CRISPR2-mfn2-CRISPR3-mfn2-CRISPR4-mfn2)',
    },
    {
        'value': 'Tg(lyz:Cas9-CRISPR1-opa1-CRISPR2-opa1)',
    },
    {
        'value': 'Tg(myl7:GFP,U6:CRISPR1-urod,gata1a:zCas9)',
    },
    {
        'value': 'Tg(myl7:GFP,U6:CRISPR1-urod,mylpfa:zCas9)',
    },
    {
        'value': 'Tg(rnu6-32:CRISPR1-tyr,cryaa:Cerulean)',
    },
    {
        'value': 'Tg(rnu6-32:CRISPR1-tyr,rnu6-32:CRISPR1-insra,rnu6-14:CRISPR2-insra,rnu6-7:CRISPR1-insrb,rnu6-279:CRISPR2-insrb,cryaa:Cerulean)',
    }
];
