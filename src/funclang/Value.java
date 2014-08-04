package funclang;

import java.util.List;

import funclang.AST.Exp;

public interface Value {
	public String tostring();
	static class Fun implements Value { //New in the funclang
		Env _env;
		List<String> _formals;
		Exp _body;
		public Fun(Env env, List<String> formals, Exp body) {
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
			result += _body.accept(new Printer.ExpToStringConverter(), _env);
			return result + ")";
	    }
	}
	static class Int implements Value {
	    int _val;
	    public Int(int v) { _val = v; } 
	    public int v() { return _val; }
	    public String tostring() { return "" + _val; }
	}
	static class Bool implements Value {
	    boolean _val;
	    public Bool(boolean v) { _val = v; } 
	    public boolean v() { return _val; }
	    public String tostring() { return "" + _val; }
	}
	static class Unit implements Value {
		public static final Unit v = new Unit();
	    public String tostring() { return "unit"; }
	}
	static class DynamicError implements Value { 
		String message = "Unknown dynamic error.";
		public DynamicError() { }
		public DynamicError(String message) { this.message = message; }
	    public String tostring() { return "" + message; }
	}
}
