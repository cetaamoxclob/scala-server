grammar TantalimScript;

// Inspired by https://github.com/bkiers/Mu/blob/master/src/main/antlr4/mu/Mu.g4

start
 : block EOF
 ;

block
 : stat*
 ;

stat
 : print
 | forBlock
 ;

print
 : PRINT OPAR STRING CPAR
 ;

forBlock
 : FOR item=ID IN list=ID OBRACE block CBRACE
 ;



OR : 'or';
AND : 'and';
EQ : '==';
NEQ : '!=';
GT : '>';
LT : '<';
GTEQ : '>=';
LTEQ : '<=';
PLUS : '+';
MINUS : '-';
MULT : '*';
DIV : '/';
MOD : '%';
POW : '^';
NOT : '!';

ASSIGN : '=';
OPAR : '(';
CPAR : ')';
OBRACE : '{';
CBRACE : '}';

TRUE : 'true';
FALSE : 'false';
IF : 'if';
ELSE : 'else';
PRINT : 'print';
FOR : 'for';
IN : 'in';

ID
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
 ;
COMMENT
 : '#' ~[\r\n]* -> skip
 ;
SPACE
 : [ \t\r\n] -> skip
 ;
OTHER
 : .
 ;