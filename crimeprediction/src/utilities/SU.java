package utilities;

public class SU {
	
	/*** Returns a String consisting of the toString representation of each object in the input separated by the specified separator. ***/
	public static String join (String separator,Object ... input) {
		
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < (input.length - 1); i ++) {
			b.append(input[i]).append(separator);
		}
		b.append(input[input.length - 1]);
		return b.toString();
	}

	public static void main(String[] args) {
		System.out.println(join(",",0.3f,1,"hello",'c',.7));
	}
	
	
}
