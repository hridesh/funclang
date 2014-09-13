package funclang;

import java.util.List;

import funclang.AST.Exp;

public class Printer {
	public void print(Value v) {
		System.out.println(v.tostring());
	}
	public void print(Exception e) {
		System.out.println(e.toString());
	}
	
	public static class Formatter implements AST.Visitor<String> {
		
		public String visit(AST.AddExp e, Env env) {
			String result = "(+ ";
			for(AST.Exp exp : e.all()) 
				result += exp.accept(this, env) + " ";
			return result + ")";
		}
		
		public String visit(AST.Const e, Env env) {
			return "" + e.v();
		}
		
		public String visit(AST.DivExp e, Env env) {
			String result = "(/ ";
			for(AST.Exp exp : e.all()) 
				result += exp.accept(this, env) + " ";
			return result + ")";
		}
		
		public String visit(AST.ErrorExp e, Env env) {
			return e.toString();
		}
		
		public String visit(AST.MultExp e, Env env) {
			String result = "(* ";
			for(AST.Exp exp : e.all()) 
				result += exp.accept(this, env) + " ";
			return result + ")";
		}
		
		public String visit(AST.Program p, Env env) {
			return "" + p.e().accept(this, env);
		}
		
		public String visit(AST.SubExp e, Env env) {
			String result = "(- ";
			for(AST.Exp exp : e.all()) 
				result += exp.accept(this, env) + " ";
			return result + ")";
		}
		
		public String visit(AST.VarExp e, Env env) {
			return "" + e.name();
		}
		
		public String visit(AST.LetExp e, Env env) {
			String result = "(let (";
			List<String> names = e.names();
			List<Exp> value_exps = e.value_exps();
			int num_decls = names.size();
			for (int i = 0; i < num_decls ; i++) {
				result += " (";
				result += names.get(i) + " ";
				result += value_exps.get(i).accept(this, env) + ")";
			}
			result += ") ";
			result += e.body().accept(this, env) + " ";
			return result + ")";
		}
		
		public String visit(AST.DefineDecl d, Env env) {
			String result = "(define ";
			result += d.name() + " ";
			result += d.value_exp().accept(this, env);
			return result + ")";
		}
		
		public String visit(AST.LambdaExp e, Env env) {
			String result = "(lambda ( ";
			for(String formal : e.formals()) 
				result += formal + " ";
			result += ") ";
			result += e.body().accept(this, env);
			return result + ")";
		}
		
		public String visit(AST.CallExp e, Env env) {
			String result = "(";
			result += e.operator().accept(this, env) + " ";
			for(AST.Exp exp : e.operands())
				result += exp.accept(this, env) + " ";
			return result + ")";
		}
		
		public String visit(AST.IfExp e, Env env) {
			String result = "(if ";
			result += e.conditional().accept(this, env) + " ";
			result += e.then_exp().accept(this, env) + " ";
			result += e.then_exp().accept(this, env);
			return result + ")";
		}
		
		public String visit(AST.LessExp e, Env env) {
			String result = "(< ";
			result += e.first_exp().accept(this, env) + " ";
			result += e.second_exp().accept(this, env);
			return result + ")";
		}

		public String visit(AST.EqualExp e, Env env) {
			String result = "(= ";
			result += e.first_exp().accept(this, env) + " ";
			result += e.second_exp().accept(this, env);
			return result + ")";
		}
		
		public String visit(AST.GreaterExp e, Env env) {
			String result = "(> ";
			result += e.first_exp().accept(this, env) + " ";
			result += e.second_exp().accept(this, env);
			return result + ")";
		}
	}
}
