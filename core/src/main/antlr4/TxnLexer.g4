/*
 * Copyright 2016-2018 SN127.fi
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
lexer grammar TxnLexer;

UUID: 'uuid';

DATE: DIGIT DIGIT DIGIT DIGIT '-' DIGIT DIGIT '-' DIGIT DIGIT;
TS: DATE 'T' TIME;
TS_TZ: TS TZ;

ID: NameStartChar (NameChar)*;

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
   | '-'
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
L_CURLY: '{';
R_CURLY: '}';
AT: '@';
SPACE: ' ';
TAB: '\t';
SEMICOLON: ';';
COLON: ':';
NL: '\r'? '\n';

ANYCHAR : . ;


