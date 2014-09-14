package funclang;

import java.util.List;

import funclang.AST.Exp;

public interface Value {
	public String tostring();
	static class Fun implements Value { //New in the funclang
		private Env _env;
		private List<String> _formals;
		private Exp _body;
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
			result += _body.accept(new Printer.Formatter(), _env);
			return result + ")";
	    }
	}
	static class Num implements Value {
	    private double _val;
	    public Num(double v) { _val = v; } 
	    public double v() { return _val; }
	    public String tostring() { 
	    	int tmp = (int) _val;
	    	if(tmp == _val) return "" + tmp;
	    	return "" + _val; 
	    }
	}
	static class Bool implements Value {
		private boolean _val;
	    public Bool(boolean v) { _val = v; } 
	    public boolean v() { return _val; }
	    public String tostring() { if(_val) return "#t"; return "#f"; }
	}
	static class Str implements Value {
		private java.lang.String _val;
	    public Str(String v) { _val = v; } 
	    public String v() { return _val; }
	    public java.lang.String tostring() { return "" + _val; }
	}
	static class Unit implements Value {
		public static final Unit v = new Unit();
	    public String tostring() { return "unit"; }
	}
	static class DynamicError implements Value { 
		private String message = "Unknown dynamic error.";
		public DynamicError() { }
		public DynamicError(String message) { this.message = message; }
	    public String tostring() { return "" + message; }
	}
}
