package funclang;

public class Printer {
	public void print(Value v) {
		System.out.println(v.tostring());
	}
	public void print(Exception e) {
		System.out.println(e.toString());
	}
}
