grammar Filter;

start : phrase ;

phrase : left=phrase andor=andOrs right=phrase #andPhrase
      | '(' phrase ')'                       #parenthesisPhrase
      | left=field comparator=comparators right=anyValue #operatorExpr
;

anyValue : field | list | simpleValue;

simpleValue : STRINGLITERAL | NUMBER | FLOAT | date;

andOrs : 'AND ' | 'OR';

field : LETTERS (LETTERS |NUMBER)*;

list : '(' LETTERS (LETTERS |NUMBER)* ')';

date : 'NOW'
      |
      | ;

comparators : '='
      | '!='
      | 'Equals'
      | 'NotEquals'
      | 'In'
      | 'NotIn'
      | 'BeginsWith'
      | 'EndsWith'
      | 'Contains'
      | 'GreaterThan'
      | 'GreaterThanOrEqual'
      | 'LessThan'
      | 'LessThanOrEqual'
      | 'Before'
      | 'OnOrBefore'
      | 'After'
      | 'OnOrAfter'
;

INT   : ('0'..'9')+ ;
LETTERS	:('a'..'z' |'A'..'Z' )+;
STRINGLITERAL : '"' ~["\r\n]* '"' | '\'' ~['\r\n]* '\'';
NUMBER : ('0'..'9')+ ( ('e' | 'E')  NUMBER)*;
FLOAT : ('0'..'9')* '.' ('0'..'9')+ ( ('e' | 'E') ('0'..'9')+)*;

WS    : [ \t\r\n]+ -> skip ;
