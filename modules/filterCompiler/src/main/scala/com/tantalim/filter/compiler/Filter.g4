grammar Filter;

start : phrase;

phrase : left=phrase andor=andOrs right=phrase             #AndPhrase
      | '(' phrase ')'                                     #ParenthesisPhrase
      | left=field comparator=comparators right=atom       #StatementPhrase
;

andOrs : AND | OR;

atom
 : field          #fieldAtom
 | basicAtom      #basicAtm
 ;

basicAtom
 : (INT | FLOAT)  #numberAtom
 | (TRUE | FALSE) #booleanAtom
 | STRING         #stringAtom
 | '(' (basicAtom) (',' basicAtom)* ')' #listAtom
 ;

field : FIELD;

date : 'NOW' ;

comparators : '='
      | '!='
      | 'Equals'
      | 'NotEquals'
      | 'In'
      | 'NotIn'
      | 'BeginsWith'
      | 'EndsWith'
      | 'Contains'
      | '>'
      | '>='
      | 'GreaterThan'
      | 'GreaterThanOrEqual'
      | '<'
      | '<='
      | 'LessThan'
      | 'LessThanOrEqual'
      | 'Before'
      | 'OnOrBefore'
      | 'After'
      | 'OnOrAfter'
;

TRUE : 'true';
FALSE : 'false';
AND : 'and' | 'AND';
OR : 'or' | 'OR';

FIELD
 : [a-zA-Z_] [a-zA-Z_0-9]*
 ;

INT
 : [0-9]+
 ;

FLOAT
 : [0-9]+ '.' [0-9]*
 | '.' [0-9]+
 ;

STRING
 : '"' (~["\r\n] | '""')* '"'
 | '\'' (~["\r\n] | '\'\'')* '\''
 ;

SPACE
 : [ \t\r\n] -> skip
 ;
