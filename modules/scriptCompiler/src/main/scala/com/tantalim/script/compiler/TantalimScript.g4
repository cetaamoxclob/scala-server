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
 | assignment
 | ifStat
 | returnStat
 ;

print
 : PRINT atom
 ;

assignment
 : ID ASSIGN atom #idAssignment
 | ID PERIOD ID ASSIGN atom #fieldAssignment
 ;

returnStat
 : RETURN expr
;

ifStat
 : IF conditionBlock (ELSE IF conditionBlock)* (ELSE statBlock)?
 ;

conditionBlock
 : expr statBlock
 ;

statBlock
 : OBRACE block CBRACE
 | stat
 ;

forBlock
 : FOR item=ID IN list=ID OBRACE block CBRACE
 ;

expr
 : OPAR expr CPAR                       #parExpr
 | MINUS expr                           #unaryMinusExpr
 | NOT expr                             #notExpr
 | expr op=(MULT | DIV | MOD) expr      #multiplicationExpr
 | expr op=(PLUS | MINUS) expr          #additiveExpr
 | expr op=(LTEQ | GTEQ | LT | GT) expr #relationalExpr
 | expr op=(EQ | NEQ) expr              #equalityExpr
 | expr AND expr                        #andExpr
 | expr OR expr                         #orExpr
 | atom                                 #atomExpr
 ;

atom
 : OPAR atom CPAR #parAtom
 | (INT | DOUBLE)  #numberAtom
 | (TRUE | FALSE) #booleanAtom
 | ID             #idAtom
 | STRING         #stringAtom
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
PERIOD : '.';

TRUE : 'true';
FALSE : 'false';
IF : 'if';
ELSE : 'else';
PRINT : 'print';
RETURN : 'return';
FOR : 'for';
IN : 'in';

ID
 : [a-zA-Z_] [a-zA-Z_0-9]*
 ;

INT
 : [0-9]+
 ;

DOUBLE
 : [0-9]+ PERIOD [0-9]+
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