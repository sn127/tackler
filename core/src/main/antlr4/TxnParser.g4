/*
 * Copyright 2016-2018 sn127.fi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
parser grammar TxnParser;

options {
    tokenVocab = TxnLexer;
    language = Java;
}

txns: blankline* txn (blankline+ txn)* blankline* EOF;

txn: date code? description? NL txn_meta? txn_comment* postings;

date: DATE
    | TS
    | TS_TZ
    ;

code: ' ' '(' code_value ')' ' '?;

code_value: ~(')'|NL)+;

description: ' ' text;

text: ~(NL)*?;

txn_meta: indent ';' ':' txn_meta_key ':' ' ' text NL;

// own uuid rule
txn_meta_key: UUID;


txn_comment: indent comment NL;

indent: (' '|'\t')+;

comment: ';' ' ' text;

postings: posting+ (posting|last_posting);

posting:  indent account sp amount opt_sp opt_unit? opt_comment? NL;

last_posting: indent account opt_sp opt_comment? NL;


opt_unit: sp unit opt_position?;

opt_comment: opt_sp comment;


opt_position: opt_opening_pos
    | opt_opening_pos  closing_pos
    | closing_pos
    ;

opt_opening_pos: sp '{' opt_sp amount sp unit opt_sp '}';

closing_pos: sp '@' sp amount sp unit;

account: ID (':' ID)*;

amount: NUMBER;

unit: ID;

sp: (' '|'\t')+;
opt_sp: (' '|'\t')*;

blankline: opt_sp NL;
