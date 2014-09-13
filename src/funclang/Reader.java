package funclang;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.FileReader;
import java.util.List;
import java.util.ArrayList;

import org.antlr.runtime.RecognitionException;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.LexerInterpreter;
import org.antlr.v4.runtime.ParserInterpreter;
import org.antlr.v4.runtime.tree.*;
import org.antlr.v4.tool.Grammar;
import org.antlr.v4.tool.LexerGrammar;

import funclang.AST.*;

public class Reader {
	
	private static String GRAMMAR_FILE = "build/funclang/FuncLang.g";
	//Following are ANTLR constants - Change them if you change the Grammar.
	//Convention: New rules are always added at the end of the file. 
	private static final String startRule = "program";
	private static final int 
		program = 0, definedecl = 1, exp = 2, varexp = 3, numexp = 4, strconst = 5,
		addexp = 6, subexp = 7, multexp = 8, divexp = 9,
		letexp = 10, // New expression for the varlang language.
		lambdaexp = 11, callexp = 12, // New expressions for this language.
		ifexp = 13, lessexp = 14, equalexp = 15, greaterexp = 16 // Other expressions for convenience.
		;

	private static final boolean DEBUG = false;
	
	Program read() throws IOException {
		String programText = readNextProgram();
		Program program = parse(programText);
		return program;
	}
	
	Program parse(String programText) {
		final LexerInterpreter lexEngine = lg.createLexerInterpreter(
				new ANTLRInputStream(programText));
		final CommonTokenStream tokens = new CommonTokenStream(lexEngine);
		final ParserInterpreter parser = g.createParserInterpreter(tokens);
		final ParseTree t = parser.parse(g.rules.get(startRule).index);
		if(DEBUG) 
			System.out.println("parse tree: " + t.toStringTree(parser));
		Program program = convertParseTreeToAST(parser, t);
		return program;
	}
	
	private Program convertParseTreeToAST(ParserInterpreter parser, ParseTree parseTree) {
		// We know that top-level parse tree node is a program, and for this 
		// language it contains an list of zero or more declarations followed by 
		// a single expression, so we first extract the declarations and then convert the 
		// enclosing expression's parse tree to the AST used by this interpreter.
		int numDecls = parseTree.getChildCount() - 1;		
		List<DefineDecl> definedecls = new ArrayList<DefineDecl>();
		TreeToExpConverter convertor = new TreeToExpConverter(parser);
		for(int i=0; i < numDecls ; i++) 
			definedecls.add((DefineDecl) parseTree.getChild(i).accept(convertor));
		Exp exp = parseTree.getChild(numDecls).accept(new TreeToExpConverter(parser));
		if(exp instanceof DefineDecl) {
			definedecls.add((DefineDecl) exp);
			return new Program(definedecls, new Const(0));
		} 
		return new Program(definedecls, exp);
	}
	
	private static final LexerGrammar lg = createLexicalGrammar();
	private static LexerGrammar createLexicalGrammar() {
		LexerGrammar lg = null;
		try {
			lg = new LexerGrammar(readFile(GRAMMAR_FILE));
		} catch (RecognitionException e) {
			System.out.println("ErrorExp in Lexical Specification\n" + e);
			System.exit(-1); // These are fatal errors
		}
		return lg;
	}

	private static final Grammar g = createGrammar();
	private static Grammar createGrammar() {
		Grammar g = null;
		try {
			g = new Grammar(readFile(GRAMMAR_FILE), Reader.lg);
		} catch (RecognitionException e) {
			System.out.println("Error in Grammar Specification\n" + e);
			System.exit(-1); // These are fatal errors
		}
		return g;
	}

	static String readFile(String fileName) {
		try {
			try (BufferedReader br = new BufferedReader(
					new FileReader(fileName))) {
				StringBuilder sb = new StringBuilder();
				String line = br.readLine();

				while (line != null) {
					sb.append(line);
					sb.append(System.lineSeparator());
					line = br.readLine();
				}
				return sb.toString();
			}
		} catch (IOException e) {
			System.out.println("Could not open file " + fileName);
			System.exit(-1); // These are fatal errors
		}
		return "";
	}

	public class ConversionException extends RuntimeException {
		private static final long serialVersionUID = -5663970743340723405L;
		public ConversionException(String message){
			super(message);
		}
	}
	/**
	 * This data adapter takes the parse tree provided by ANTLR and converts it
	 * to the abstract syntax tree representation used by the rest of this
	 * interpreter. This class needs to be adapted for each new abstract syntax
	 * tree node.
	 * 
	 * @author hridesh
	 * 
	 */
	class TreeToExpConverter implements ParseTreeVisitor<AST.Exp> {

		ParserInterpreter parser;

		TreeToExpConverter(ParserInterpreter parser) {
			this.parser = parser;
		}

		public AST.Exp visit(ParseTree tree) {
			System.out.println("visit: " + tree.toStringTree(parser));
			return null;
		}

		public AST.Exp visitChildren(RuleNode node) {			
			switch(node.getRuleContext().getRuleIndex()){
				case varexp: return convertVarExp(node);
				case numexp: return convertConst(node);
				case strconst: return convertStrConst(node);
				case addexp: return convertAddExp(node); 
				case subexp: return convertSubExp(node); 
				case multexp: return convertMultExp(node);
				case divexp: return convertDivExp(node);
				case letexp: return convertLetExp(node);
				case definedecl: return convertDefineDecl(node);
				case lambdaexp: return convertLambdaExp(node);
				case callexp: return convertCallExp(node);
				case ifexp: return convertIfExp(node);
				case lessexp: return convertLessExp(node);
				case equalexp: return convertEqualExp(node);
				case greaterexp: return convertGreaterExp(node);
				case exp: return visitChildrenHelper(node).get(0);
				case program: 
				default: 
					System.out.println("Conversion error (from parse tree to AST): found unknown/unhandled case " + parser.getRuleNames()[node.getRuleContext().getRuleIndex()]);
			}
			return null;
		}
		
		/**
		 *  Syntax: Identifier
		 */  
		private AST.VarExp convertVarExp(RuleNode node){
			if(node.getChildCount() > 1)
				throw new ConversionException("Conversion error: " + node.toStringTree(parser) + ", " + 
						"expected only Identifier, found " + node.getChildCount() +  " nodes.");
				
			String s = node.getChild(0).getText();
			return new AST.VarExp(s);
		}

		/**
		 *  Syntax: Number
		 */  
		private AST.Const convertConst(RuleNode node){
			String s = node.getChild(0).toStringTree(parser);
			try {
				int v = Integer.parseInt(s);
				return new AST.Const(v);
			} catch (NumberFormatException e) {}
			throw new ConversionException("Conversion error: " + node.toStringTree(parser) + ", " + 
					"expected Number, found " + node.getChild(0).toStringTree(parser));
		}
		
		/**
		 *  Syntax: Number
		 */  
		private AST.StrConst convertStrConst(RuleNode node){
			String s = node.getChild(0).toStringTree(parser);
			s = s.substring(1, s.length()-1); //Trim to remove quotes.
			return new AST.StrConst(s);
		}

		/**
		 *  Syntax: (+ exp* )
		 */  
		private AST.Exp convertAddExp(RuleNode node){
			int index = expect(node,0,"(", "+");
			List<AST.Exp> operands = expectOperands(node, index);
			return new AST.AddExp(operands);
		}
		
		/**
		 *  Syntax: (- exp* )
		 */  
		private AST.Exp convertSubExp(RuleNode node){
			int index = expect(node,0,"(", "-");
			List<AST.Exp> operands = expectOperands(node, index);
			return new AST.SubExp(operands);
		}

		/**
		 *  Syntax: (* exp* )
		 */  
		private AST.Exp convertMultExp(RuleNode node){
			int index = expect(node,0,"(", "*");
			List<AST.Exp> operands = expectOperands(node, index);
			return new AST.MultExp(operands);
		}

		/**
		 *  Syntax: (/ exp* )
		 */  
		private AST.Exp convertDivExp(RuleNode node){
			int index = expect(node,0,"(", "/");
			List<AST.Exp> operands = expectOperands(node, index);
			return new AST.DivExp(operands);
		}

		List<AST.Exp> expectOperands(RuleNode node, int startChildIndex) {
			int index = startChildIndex; 
			List<AST.Exp> operands = new ArrayList<AST.Exp>();	
			while (!match(node,index,")")) {
				AST.Exp operand = node.getChild(index++).accept(this);
				operands.add(operand);
			}
			expect(node,index++, ")");
			return operands;
		}
		
		/**
		 *  Syntax: (let ((name value_exp)* ) body_exp)
		 */  
		private AST.Exp convertLetExp(RuleNode node){
			int index = expect(node,0,"(", "let", "(");
			List<String> names = new ArrayList<String>();	
			List<AST.Exp> value_exps = new ArrayList<AST.Exp>();	
			while (match(node,index,"(")) {
				index++;
				String name = expectString(node, index++, 1)[0];
				names.add(name);
				AST.Exp value_exp = expectExp(node, index++, 1)[0];
				value_exps.add(value_exp);
				index = expect(node,index, ")");
			}
			expect(node,index++, ")");
			AST.Exp body = node.getChild(index++).accept(this);
			expect(node,index++, ")");
			return new AST.LetExp(names,value_exps,body);
		}

		/**
		 *  Syntax: (define name value_exp)
		 */  
		private AST.Exp convertDefineDecl(RuleNode node){
			int index = expect(node,0,"(", "define");
			String name = expectString(node, index++, 1)[0];
			AST.Exp value_exp = expectExp(node, index++, 1)[0];
			expect(node,index++, ")");
			return new AST.DefineDecl(name,value_exp);
		}
		
		/**
		 *  Syntax: ( lambda ( Identifier+ ) body_exp )
		 */
		private AST.Exp convertLambdaExp(RuleNode node){
			int index = expect(node,0,"(", "lambda", "(");
			List<String> formals = new ArrayList<String>();	
			while (!match(node,index,")")) {
				String formal = expectString(node, index++, 1)[0];
				formals.add(formal);
			}
			expect(node,index++, ")");
			AST.Exp body_exp = node.getChild(index++).accept(this);
			expect(node,index++, ")");
			return new AST.LambdaExp(formals, body_exp);
		}
		
		/**
		 *  Syntax: ( operator_exp operand_exp* )
		 */
		private AST.Exp convertCallExp(RuleNode node){
			int index = expect(node,0,"(");
			AST.Exp operator = node.getChild(index++).accept(this);
			List<AST.Exp> operands = new ArrayList<AST.Exp>();	
			while (!match(node,index,")")) {
				AST.Exp operand = node.getChild(index++).accept(this);
				operands.add(operand);
			}
			expect(node,index++, ")");
			return new AST.CallExp(operator, operands);
		}

		/**
		 *  Syntax: ( if conditional_exp then_exp else_exp )
		 */
		private AST.Exp convertIfExp(RuleNode node){
			int index = expect(node,0,"(", "if");
			AST.Exp conditional = node.getChild(index++).accept(this);
			AST.Exp then_exp = node.getChild(index++).accept(this);
			AST.Exp else_exp = node.getChild(index++).accept(this);
			expect(node,index++, ")");
			return new AST.IfExp(conditional, then_exp, else_exp);
		}
		
		/**
		 *  Syntax: ( < first_exp second_exp )
		 */
		private AST.Exp convertLessExp(RuleNode node){
			int index = expect(node,0,"(","<");
			AST.Exp first_exp = node.getChild(index++).accept(this);
			AST.Exp second_exp = node.getChild(index++).accept(this);
			expect(node,index++, ")");
			return new AST.LessExp(first_exp, second_exp);
		}
		
		/**
		 *  Syntax: ( == first_exp second_exp )
		 */
		private AST.Exp convertEqualExp(RuleNode node){
			int index = expect(node,0,"(","=");
			AST.Exp first_exp = node.getChild(index++).accept(this);
			AST.Exp second_exp = node.getChild(index++).accept(this);
			expect(node,index++, ")");
			return new AST.EqualExp(first_exp, second_exp);
		}

		/**
		 *  Syntax: ( > first_exp second_exp )
		 */
		private AST.Exp convertGreaterExp(RuleNode node){
			int index = expect(node,0,"(",">");
			AST.Exp first_exp = node.getChild(index++).accept(this);
			AST.Exp second_exp = node.getChild(index++).accept(this);
			expect(node,index++, ")");
			return new AST.GreaterExp(first_exp, second_exp);
		}

		public AST.Exp visitTerminal(TerminalNode node) {
			String s = node.toStringTree(parser);
			if (isConcreteSyntaxToken(s))
				return null;

			try {
				int v = Integer.parseInt(s);
				return new AST.Const(v);
			} catch (NumberFormatException e) {
			}
			// Error case - generally means a new Token is added in the grammar
			// and not handled in
			// the filterTokens method above, or a new value type is added.
			System.out.println("visitTerminal: Illegal terminal " + s);
			return new AST.ErrorExp();
		}

		public AST.Exp visitErrorNode(ErrorNode node) {
			System.out.println("visitErrorNode: " + node.toStringTree(parser));
			return new AST.ErrorExp();
		}

		private List<AST.Exp> visitChildrenHelper(RuleNode node) {
			int childCount = node.getChildCount();
			List<AST.Exp> results = new ArrayList<AST.Exp>(); 
			if(DEBUG) System.out.println("visitChildren(RuleNode node), node = "
					+ node.toStringTree(parser) + ", #children = "
					+ childCount);
			for (int i = 0; i < childCount; i++) {
				AST.Exp result = node.getChild(i).accept(this);
				if(result!=null) results.add(result);
			}
			return results;
		}
				
		/**
		 * This method filters out those Tokens that are part of the concrete
		 * syntax and thus are not represented in the abstract syntax.
		 * 
		 * @param s - string representation of the token
		 * @return true if the token is part of concrete syntax.
		 */
		private boolean isConcreteSyntaxToken(String s) {
			if (s.equals("(") || s.equals(")") || s.equals("+")
					|| s.equals("-") || s.equals("*") || s.equals("/"))
				return true;
			return false;
		}
		
		/**
		 * Expect nth, n+1th, ..., n+mth children of node to be expressions
		 * @param node - node to be examined.
		 * @param n - index of first child.
		 * @param numTokens - expected number of expressions.
		 * @return an array of n + numTokens expressions, if expectations is met. Otherwise, throws ConversionException.
		 */
		protected AST.Exp[] expectExp(RuleNode node, int n, int numTokens){
			AST.Exp results[] = new AST.Exp[numTokens];
			for(int i = 0; i< numTokens; i++) {
				AST.Exp value = node.getChild(n+i).accept(this);
				if(value == null || value instanceof AST.ErrorExp)
					throw new ConversionException("Conversion error: " + node.toStringTree(parser) + ", " + 
							"expected Exp, found " + node.getChild(n+i).toStringTree(parser));
				results[i] = value;
			}
			return results;
		}

		/**
		 * Expect nth, n+1th, ..., n+mth children of node to be strings
		 * @param node - node to be examined.
		 * @param n - index of first child.
		 * @param numTokens - expected number of strings.
		 * @return an array of n + numTokens strings, if expectations is met. Otherwise, throws ConversionException.
		 */
		protected String[] expectString(RuleNode node, int n, int numTokens){
			String results[] = new String[numTokens];
			for(int i = 0; i< numTokens; i++, n++) {
				String value = node.getChild(n).toString();
				if(value == null || node.getChild(n).getChildCount()!=0)
					throw new ConversionException("Conversion error: " + node.toStringTree(parser) + ", " + 
							"expected Exp, found " + node.getChild(n).toStringTree(parser));
				results[i] = value;
			}
			return results;
		}

		/**
		 * Expect nth, n+1th, ... children of node to match the token 
		 * @param node - node to be examined.
		 * @param n - index of child.
		 * @param tokens - expected strings.
		 * @return n + tokens.length, if expectations is met. Otherwise, throws ConversionException.
		 */
		protected int expect(RuleNode node, int n, String ... tokens){
			int numTokens = tokens.length;
			for(int i = 0; i< numTokens; i++) {
				if (!node.getChild(n+i).toStringTree(parser).equals(tokens[i])) 
					throw new ConversionException("Conversion error: " + node.toStringTree(parser) + ", " + 
							"expected " + tokens[i] + ", found " + node.getChild(n+i).toStringTree(parser));
			}
			return n+numTokens;
		}
		
		/**
		 * Test if nth, n+1th, ... children of node match the token 
		 * @param node - node to be examined.
		 * @param n - index of child.
		 * @param tokens - expected strings.
		 * @return true, if test is met. False, otherwise.
		 */
		protected boolean match(RuleNode node, int n, String ... tokens){
			int numTokens = tokens.length;
			for(int i = 0; i< numTokens; i++) {
				if (!node.getChild(n+i).toStringTree(parser).equals(tokens[i])) 
					return false;
			}
			return true;
		}
	}
	
	private String readNextProgram() throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.print("$ ");
		String programText = br.readLine();
		return runFile(programText);
	}
	
	private String runFile(String programText) throws IOException {
		if(programText.startsWith("run ")){
			programText = readFile("build/funclang/" + programText.substring(4));
		}
		return programText; 
	}
}
