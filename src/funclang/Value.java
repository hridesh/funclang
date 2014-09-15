package funclang;

import java.util.List;

import funclang.AST.Exp;

public interface Value {
	public String tostring();
	static class FunVal implements Value { //New in the funclang
		private Env _env;
		private List<String> _formals;
		private Exp _body;
		public FunVal(Env env, List<String> formals, Exp body) {
			_env = env;
			_formals = formals;
			_body = body;
		}
		public Env env() { return _env; }
		public List<String> formals() { return _formals; }
		public Exp body() { return _body; }
	    public String tostring() { 
			String result = "(lambda ( ";
			for(String formal : _formals) 
				result += formal + " ";
			result += ") ";
			result += _body.accept(new Printer.Formatter(), _env);
			return result + ")";
	    }
	}
	static class NumVal implements Value {
	    private double _val;
	    public NumVal(double v) { _val = v; } 
	    public double v() { return _val; }
	    public String tostring() { 
	    	int tmp = (int) _val;
	    	if(tmp == _val) return "" + tmp;
	    	return "" + _val; 
	    }
	}
	static class BoolVal implements Value {
		private boolean _val;
	    public BoolVal(boolean v) { _val = v; } 
	    public boolean v() { return _val; }
	    public String tostring() { if(_val) return "#t"; return "#f"; }
	}
	static class StringVal implements Value {
		private java.lang.String _val;
	    public StringVal(String v) { _val = v; } 
	    public String v() { return _val; }
	    public java.lang.String tostring() { return "" + _val; }
	}
	static class PairVal implements Value {
		protected Value _fst;
		protected Value _snd;
	    public PairVal(Value fst, Value snd) { _fst = fst; _snd = snd; } 
		public Value fst() { return _fst; }
		public Value snd() { return _snd; }
	    public java.lang.String tostring() { 
	    	return "(" + _fst.tostring() + " " + _snd.tostring() + ")"; 
	    }
	    protected java.lang.String tostringHelper() { 
	    	String result = "";
	    	if(_fst instanceof Value.PairVal && !(_fst instanceof Value.ExtendList))
	    		result += ((Value.PairVal) _fst).tostringHelper();
	    	else result += _fst.tostring();
	    	if(!(_snd instanceof Value.EmptyList)){
	    		result += " ";
	    		if(_snd instanceof Value.PairVal && !(_snd instanceof Value.ExtendList))
	    			result += ((Value.PairVal) _snd).tostringHelper();
	    		else result += _snd.tostring();
	    	}
	    	return result;
	    }
	}
	static interface ListVal extends Value {}
	static class EmptyList implements ListVal {
		public EmptyList() {}
	    public String tostring() { return "()"; }
	}
	static class ExtendList extends PairVal implements ListVal {
		public ExtendList(Value fst, Value snd) { super(fst, snd); }		
	    public String tostring() {
	    	String result = "(";
	    	if(_fst instanceof Value.PairVal && !(_fst instanceof Value.ExtendList))
	    		result += ((Value.PairVal) _fst).tostringHelper();
	    	else result += _fst.tostring();
	    	if(!(_snd instanceof Value.EmptyList)){
	    		result += " ";
	    		if(_snd instanceof Value.PairVal && !(_snd instanceof Value.ExtendList))
	    			result += ((Value.PairVal) _snd).tostringHelper();
	    		else result += _snd.tostring();
	    	}
	    	return result += ")";
	    }
	}
	static class UnitVal implements Value {
		public static final UnitVal v = new UnitVal();
	    public String tostring() { return ""; }
	}
	static class DynamicError implements Value { 
		private String message = "Unknown dynamic error.";
		public DynamicError() { }
		public DynamicError(String message) { this.message = message; }
	    public String tostring() { return "" + message; }
	}
}
