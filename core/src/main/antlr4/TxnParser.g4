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
