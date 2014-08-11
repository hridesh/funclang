package funclang;

import java.util.List;

/**
 * Representation of an environment, which maps variables to values.
 * 
 * @author hridesh
 *
 */
public interface Env {
	Value get (String search_var);
	void define (String saved_var, Value saved_val); //New for definelang

	@SuppressWarnings("serial")
	static public class LookupException extends RuntimeException {
		LookupException(String message){
			super(message);
		}
	}
	
	@SuppressWarnings("serial")
	static public class IllegalDefineException extends RuntimeException {
		IllegalDefineException(String message){
			super(message);
		}
	}

	static public class EmptyEnv implements Env {
		public Value get (String search_var) {
			throw new LookupException("No binding found for name: " + search_var);
		}
		public void define(String saved_var, Value saved_val) {
			throw new IllegalDefineException("Defining global variable " + saved_var + " not permitted in this language.");
		}
	}
	
	static public class ExtendEnv implements Env {
		private Env _saved_env; 
		private String _var; 
		private Value _val; 
		public ExtendEnv(Env saved_env, String var, Value val){
			_saved_env = saved_env;
			_var = var;
			_val = val;
		}
		public synchronized Value get (String search_var) {
			if (search_var.equals(_var))
				return _val;
			return _saved_env.get(search_var);
		}
		public synchronized void define(String saved_var, Value saved_val) {
			_saved_env.define(saved_var, saved_val);
		}
	}
	
	static public class GlobalEnv implements Env { // New for definelang
		private Env _saved_env; 
		private List<Binding> _bindings; 
		public GlobalEnv() {
			_saved_env = new EmptyEnv();
			_bindings = new java.util.ArrayList<Binding>();
		}
		public synchronized Value get (String search_var) {
			for(Binding binding : _bindings) {
				if (search_var.equals(binding._var))
					return binding._val;
			}
			return _saved_env.get(search_var);
		}
		public synchronized void define(String saved_var, Value saved_val) {
			_bindings.add( new Binding(saved_var, saved_val));
		}
		private class Binding {
			String _var; 
			Value _val;
			Binding(String var, Value val) {
				_var = var;
				_val = val; 
			}
		}
	}

}
