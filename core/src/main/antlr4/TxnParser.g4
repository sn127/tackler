/*
 * Copyright 2016-2017 Jani Averbach
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

posting:  indent account (' '|'\t')+ amount (' '|'\t')* comment? NL;

last_posting: indent account (' '|'\t')* comment? NL;

account: ID (':' ID)*;

amount: NUMBER;

blankline: (' '|'\t')* NL;
