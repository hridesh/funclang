package funclang;
import static funclang.AST.*;
import static funclang.Value.*;

import java.util.List;
import java.util.ArrayList;

import funclang.Env.*;

public class Evaluator implements Visitor<Value> {
	
	Printer.Formatter ts = new Printer.Formatter();

	Env initEnv = new EmptyEnv(); //New for definelang
	
	Value valueOf(Program p) {
		return (Value) p.accept(this, initEnv);
	}
	
	@Override
	public Value visit(AddExp e, Env env) {
		List<Exp> operands = e.all();
		int result = 0;
		for(Exp exp: operands) {
			Int intermediate = (Int) exp.accept(this, env); // Dynamic type-checking
			result += intermediate.v(); //Semantics of AddExp in terms of the target language.
		}
		return new Int(result);
	}

	@Override
	public Value visit(Const e, Env env) {
		return new Int(e.v());
	}

	@Override
	public Value visit(DivExp e, Env env) {
		List<Exp> operands = e.all();
		Int lVal = (Int) operands.get(0).accept(this, env);
		int result = lVal.v(); 
		for(int i=1; i<operands.size(); i++) {
			Int rVal = (Int) operands.get(i).accept(this, env);
			result = result / rVal.v();
		}
		return new Int(result);
	}

	@Override
	public Value visit(ErrorExp e, Env env) {
		return new Value.DynamicError("Encountered an error expression");
	}

	@Override
	public Value visit(MultExp e, Env env) {
		List<Exp> operands = e.all();
		int result = 1;
		for(Exp exp: operands) {
			Int intermediate = (Int) exp.accept(this, env); // Dynamic type-checking
			result *= intermediate.v(); //Semantics of MultExp.
		}
		return new Int(result);
	}

	@Override
	public Value visit(Program p, Env env) {
		for(DefineDecl d: p.decls())
			d.accept(this, initEnv);
		return (Value) p.e().accept(this, initEnv);
	}

	@Override
	public Value visit(SubExp e, Env env) {
		List<Exp> operands = e.all();
		Int lVal = (Int) operands.get(0).accept(this, env);
		int result = lVal.v();
		for(int i=1; i<operands.size(); i++) {
			Int rVal = (Int) operands.get(i).accept(this, env);
			result = result - rVal.v();
		}
		return new Int(result);
	}

	@Override
	public Value visit(VarExp e, Env env) {
		// Previously, all variables had value 42. New semantics.
		return env.get(e.name());
	}	

	@Override
	public Value visit(LetExp e, Env env) { // New for varlang.
		List<String> names = e.names();
		List<Exp> value_exps = e.value_exps();
		List<Value> values = new ArrayList<Value>(value_exps.size());
		
		for(Exp exp : value_exps) 
			values.add((Value)exp.accept(this, env));
		
		Env new_env = env;
		for (int index = 0; index < names.size(); index++)
			new_env = new ExtendEnv(new_env, names.get(index), values.get(index));

		return (Value) e.body().accept(this, new_env);		
	}	
	
	@Override
	public Value visit(DefineDecl e, Env env) { // New for definelang.
		String name = e.name();
		Exp value_exp = e.value_exp();
		Value value = (Value) value_exp.accept(this, env);
		initEnv = new ExtendEnv(initEnv, name, value);
		return new Value.Unit();		
	}	

	@Override
	public Value visit(LambdaExp e, Env env) { // New for funclang.
		return new Value.Fun(env, e.formals(), e.body());
	}
	
	@Override
	public Value visit(CallExp e, Env env) { // New for funclang.
		Object result = e.operator().accept(this, env);
		if(!(result instanceof Value.Fun))
			return new Value.DynamicError("Operator not a function in call " +  ts.visit(e, env));
		Value.Fun operator =  (Value.Fun) result; //Dynamic checking
		List<Exp> operands = e.operands();

		// Call-by-value semantics
		List<Value> actuals = new ArrayList<Value>(operands.size());
		for(Exp exp : operands) 
			actuals.add((Value)exp.accept(this, env));
		
		List<String> formals = operator.formals();
		if (formals.size()!=actuals.size())
			return new Value.DynamicError("Argument mismatch in call " + ts.visit(e, env));

		Env closure_env = operator.env();
		Env fun_env = appendEnv(closure_env, initEnv);
		for (int index = 0; index < formals.size(); index++)
			fun_env = new ExtendEnv(fun_env, formals.get(index), actuals.get(index));
		
		return (Value) operator.body().accept(this, fun_env);
	}
	
	/* Helper for CallExp */
	/***
	 * Create an env that has bindings from fst appended to bindings from snd.
	 * The order in which 
	 * @param fst
	 * @param snd
	 * @return
	 */
	private Env appendEnv(Env fst, Env snd){
		if(fst.isEmpty())
			return snd;
		ExtendEnv f = (ExtendEnv) fst;
		return new ExtendEnv(appendEnv(f.saved_env(),snd), f.var(), f.val());
	}
	/* End: helper for CallExp */
	
	@Override
	public Value visit(IfExp e, Env env) { // New for funclang.
		Object result = e.conditional().accept(this, env);
		if(!(result instanceof Value.Bool))
			return new Value.DynamicError("Condition not a boolean in expression " +  ts.visit(e, env));
		Value.Bool condition =  (Value.Bool) result; //Dynamic checking
		
		if(condition.v())
			return (Value) e.then_exp().accept(this, env);
		else return (Value) e.else_exp().accept(this, env);
	}

	@Override
	public Value visit(LessExp e, Env env) { // New for funclang.
		Value.Int first = (Value.Int) e.first_exp().accept(this, env);
		Value.Int second = (Value.Int) e.second_exp().accept(this, env);
		return new Value.Bool(first.v() < second.v());
	}
	
	@Override
	public Value visit(EqualExp e, Env env) { // New for funclang.
		Value.Int first = (Value.Int) e.first_exp().accept(this, env);
		Value.Int second = (Value.Int) e.second_exp().accept(this, env);
		return new Value.Bool(first.v() == second.v());
	}

	@Override
	public Value visit(GreaterExp e, Env env) { // New for funclang.
		Value.Int first = (Value.Int) e.first_exp().accept(this, env);
		Value.Int second = (Value.Int) e.second_exp().accept(this, env);
		return new Value.Bool(first.v() > second.v());
	}
	
}
