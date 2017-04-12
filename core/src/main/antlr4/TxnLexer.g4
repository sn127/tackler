lexer grammar TxnLexer;

UUID: 'uuid';

DATE: DIGIT DIGIT DIGIT DIGIT '-' DIGIT DIGIT '-' DIGIT DIGIT;
TS: DATE 'T' TIME;
TS_TZ: TS TZ;

ID: NameStartChar (NameChar|('-'))*;

NUMBER: INT | FLOAT;

fragment TIME: DIGIT DIGIT ':' DIGIT DIGIT ':' DIGIT DIGIT ('.' DIGIT+)?;

fragment TZ: 'Z' | (('+' | '-') DIGIT DIGIT ':' DIGIT DIGIT);

fragment INT: ('+' | '-')? DIGIT+;

fragment FLOAT: ('+' | '-')? DIGIT+ '.' DIGIT+;

fragment
NameChar
   : NameStartChar
   | '0'..'9'
   | '_'
   | '\u00B7'
   | '\u0300'..'\u036F'
   | '\u203F'..'\u2040'
   ;

fragment
NameStartChar
   : 'A'..'Z' | 'a'..'z'
   | '\u00C0'..'\u00D6'
   | '\u00D8'..'\u00F6'
   | '\u00F8'..'\u02FF'
   | '\u0370'..'\u037D'
   | '\u037F'..'\u1FFF'
   | '\u200C'..'\u200D'
   | '\u2070'..'\u218F'
   | '\u2C00'..'\u2FEF'
   | '\u3001'..'\uD7FF'
   | '\uF900'..'\uFDCF'
   | '\uFDF0'..'\uFFFD'
   ;

fragment DIGIT: [0-9];

L_BRACE: '(';
R_BRACE: ')';
SPACE: ' ';
TAB: '\t';
SEMICOLON: ';';
COLON: ':';
NL: '\r'? '\n';

ANYCHAR : . ;


