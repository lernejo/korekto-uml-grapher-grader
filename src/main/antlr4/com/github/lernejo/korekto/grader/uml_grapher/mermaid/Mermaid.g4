grammar Mermaid;

classDiagram
    : CLASS_DIAGRAM EOL
      (directionStmt EOL)?
      items
      EOF
    ;

directionStmt: DIRECTION direction;
direction: (LR | TD);

items
    : (item EOL?)*;

item
    : (classBlock | classLine | classLineField | classLineMethod | relation)
    ;

classBlock
    : CLASS className '{' .* '}'
    ;

classLine: CLASS className;
classLineField: className ':' visibility type name fieldClassifier?;
classLineMethod: className ':' visibility type name '(' parameterList ')' methodClassifier?;
parameterList: ( parameter ','? )+;
parameter: type name;
relation: className arrow className;
arrow: INHERITANCE | COMPOSITION | AGGREGATION | ASSOCIATION | SOLID_LINK | DEPENDENCY | REALIZATION | DASHED_LINK;

visibility: PUBLIC | PRIVATE | PROTECTED | PACKAGE_PROTECTED;
fieldClassifier: STATIC_CLASSIFIER;
methodClassifier: ABSTRACT_CLASSIFIER | STATIC_CLASSIFIER;
type: ID;
name: ID;
className: ID;

CLASS: 'class';
CLASS_DIAGRAM: 'classDiagram';
DIRECTION: 'direction';
LR: 'LR';
TD: 'TD';

PUBLIC: '+';
PRIVATE: '-';
PROTECTED: '#';
PACKAGE_PROTECTED: '~';

ABSTRACT_CLASSIFIER: '*';
STATIC_CLASSIFIER: '$';

INHERITANCE: '<|--';
COMPOSITION: '*--';
AGGREGATION: 'o--';
ASSOCIATION: '-->';
SOLID_LINK: '--';
DEPENDENCY: '..>';
REALIZATION: '..|>';
DASHED_LINK: '..';

ID
   : LETTER ( LETTER | DIGIT )*
   ;

EOL: ('\n' | '\r\n')+;

fragment DIGIT
   : [0-9]
   ;

fragment LETTER
   : [a-zA-Z\u0080-\u00FF_]
   ;
