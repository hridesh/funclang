grammar FuncLang;
 
 // Grammar of this Programming Language
 //  - grammar rules start with lowercase
 program : 
		(definedecl)* (exp)? //Zero or more define declarations followed by an optional expression.
		;

 definedecl  :               //New for definelang 
 		'(' Define 
 			Identifier
 			exp
 			')' 
 		;

 exp : 
		varexp 
		| numexp 
		| strconst
        | addexp 
        | subexp 
        | multexp 
        | divexp
        | letexp
        | lambdaexp //New for funclang
        | callexp //New for funclang
        | ifexp //New for funclang
        | lessexp //New for funclang
        | equalexp //New for funclang
        | greaterexp //New for funclang
        ;
 
 varexp  : 
 		Identifier
 		;
 
 numexp :
 		Number 
 		;

 strconst :
 		StrLiteral
 		;
  
 addexp :
 		'(' '+'
 		    exp 
 		    (exp)+ 
 		    ')' 
 		;
 
 subexp :  
 		'(' '-' 
 		    exp 
 		    (exp)+ 
 		    ')' 
 		;

 multexp : 
 		'(' '*' 
 		    exp 
 		    (exp)+ 
 		    ')' 
 		;
 
 divexp  : 
 		'(' '/' 
 		    exp 
 		    (exp)+ 
 		    ')' 
 		;

 letexp  :
 		'(' Let 
 			'(' ( '(' Identifier exp ')' )+  ')'
 			exp 
 			')' 
 		;

 lambdaexp :
 		'(' Lambda 
 			'(' Identifier* ')'
 			exp 
 			')' 
 		;

 callexp :
 		'(' exp 
 			exp* 
 			')' 
 		;

 ifexp :
 		'(' If 
 		    exp 
 			exp 
 			exp 
 			')' 
 		;

 lessexp :
 		'(' Less 
 		    exp 
 			exp 
 			')' 
 		;

 equalexp :
 		'(' Equal 
 		    exp 
 			exp 
 			')' 
 		;

 greaterexp :
 		'(' Greater 
 		    exp 
 			exp 
 			')' 
 		;

// Keywords

 Let : 'let' ;
 Define : 'define' ;
 Lambda : 'lambda' ;
 If : 'if' ; 
 Less : '<' ;
 Equal : '=' ;
 Greater : '>' ;
 
 // Lexical Specification of this Programming Language
 //  - lexical specification rules start with uppercase

 Identifier :   Letter LetterOrDigit*;
 	
 Number : 
	DIGIT 
	| (DIGIT_NOT_ZERO DIGIT+); 

// Identifier :   Letter LetterOrDigit*;

 Letter :   [a-zA-Z$_]
	|   ~[\u0000-\u00FF\uD800-\uDBFF] 
		{Character.isJavaIdentifierStart(_input.LA(-1))}?
	|   [\uD800-\uDBFF] [\uDC00-\uDFFF] 
		{Character.isJavaIdentifierStart(Character.toCodePoint((char)_input.LA(-2), (char)_input.LA(-1)))}? ;

 LetterOrDigit: [a-zA-Z0-9$_]
	|   ~[\u0000-\u00FF\uD800-\uDBFF] 
		{Character.isJavaIdentifierPart(_input.LA(-1))}?
	|    [\uD800-\uDBFF] [\uDC00-\uDFFF] 
		{Character.isJavaIdentifierPart(Character.toCodePoint((char)_input.LA(-2), (char)_input.LA(-1)))}?;

 fragment DIGIT: ('0'..'9');
 fragment DIGIT_NOT_ZERO: ('1'..'9');

 fragment ESCQUOTE : '\\"';
 StrLiteral :   '"' ( ESCQUOTE | ~('\n'|'\r') )*? '"';

 AT : '@';
 ELLIPSIS : '...';
 WS  :  [ \t\r\n\u000C]+ -> skip;
 Comment :   '/*' .*? '*/' -> skip;
 Line_Comment :   '//' ~[\r\n]* -> skip;