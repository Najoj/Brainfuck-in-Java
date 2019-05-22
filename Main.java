package brainfuck;

public class Main {

	public static void main(String[] args) {
		String prog;
		prog = "++++++++++[>+++++++>++++++++++>+++>+<<<<-]>++.>+.+++++++..+++."
				+ ">++.<<+++++++++++++++.>.+++.------.--------.>+.>.";
		
		Brainfuck bf = new Brainfuck(prog);
		if(bf.compile()) {
			bf.run();
		}
	}

}
