package brainfuck;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class Brainfuck {
	private static final int LENGTH = 2*2048;

	private char tape[] = new char[LENGTH];
	private Integer tapePosition;
	private Integer reachedTapePosition;
	private char program[];

	private Stack<Integer> programPosition = new Stack<>();
	private Stack<Integer> openingBracketPosition = new Stack<>();
	private Map<Integer, Integer> loopJump = new HashMap<>();

	public Brainfuck() {
	}

	public Brainfuck(String prog) {
		program = prog.toCharArray();
	}

	public boolean compile(String program) {
		return compile(program.toCharArray());
	}
	private boolean compile(char[] program) {
		this.program = program;
		return parse_and_compile();
	}
	
	public boolean compile() {
		if(program == null || program.length == 0) {
			System.err.println("No program to compile.");
			return false;
		}
		return parse_and_compile();
	}
	public boolean recompile() {
		return compile();
	}
	/* Fake run to parse and simplify the program. */
	private boolean parse_and_compile() {
		int leftRightShift = 0;
		int leftRightLoop = 0;

		Stack<Integer> programPosition = new Stack<>();

		for(int pPosition = 0; pPosition < program.length; pPosition++) {
			switch(program[pPosition]) {
			case '+': /* Safe chars */
			case '-':
			case '.':
			case ',':
				break;
			case '>':
				leftRightShift++;
				if(leftRightLoop >= LENGTH) {
					System.err.println("Shift out of bounds");
					printErrorPosition(pPosition);
					return false;
				}
				break;
			case '<':
				leftRightShift--;
				if(leftRightShift < 0) {
					System.err.println("Shift out of bounds");
					printErrorPosition(pPosition);
					return false;
				}
				break;
			case ']':
				if(openingBracketPosition.isEmpty()) {
					System.err.println("Ending bracket ] not matching opening bracket [.");
					printErrorPosition(pPosition);
					return false;
				}
				openingBracketPosition.pop();
				int opening = programPosition.pop();
				int ending = pPosition;
				loopJump.put(opening, ending);
				loopJump.put(ending, opening);

				leftRightLoop--;
				if(leftRightLoop < 0) {
					System.err.println("Ending bracket ] preceeding opening bracket [.");
					printErrorPosition(pPosition);
					return false;
				}
				break;
			case '[':
				leftRightLoop++;
				openingBracketPosition.push(pPosition);
				programPosition.push(pPosition);

				if(pPosition == 0) {	// Fast forward
					leftRightLoop--; // Don't bother
					int openBrackets = 0;
					for(;pPosition < program.length; pPosition++) {
						
						if(openBrackets == 1 && program[pPosition] == ']') {
							openBrackets--;
							loopJump.put(openBrackets, pPosition);
							loopJump.put(pPosition, openBrackets);
							break;
						}
						
						if(program[pPosition] == ']') {
							openBrackets--;
						} else if(program[pPosition] == '[') {
							openBrackets++;
						}
					}
				}

				break;

			default: /* Remove comments. */
				break;
			}
		}

		if(leftRightLoop > 0) {
			System.err.println("leftRightLoop = " + leftRightLoop);
			System.err.println("Unclosed bracket [.");
			printErrorPosition(openingBracketPosition.peek());
		}

		/* This would be false if we got many unclosed brackets */
		return leftRightLoop == 0;
	}

	/* Print the error message. */
	private void printErrorPosition(int errPos) {
		final int margin = 10;

		int from = (errPos - margin > 0 ? errPos - margin : 0);
		int to = (errPos+margin < program.length ? errPos+margin : program.length);

		for(int pos = from; pos < to; pos++) {
			System.err.print(program[pos] == '\n' ? ' ' : program[pos]);
		}

		System.err.println("");
		System.err.flush();

		for(int pos = 0; pos < errPos-from; pos++) {
			System.err.print(' ');
		}
		System.err.println('^');
	}

	public void run() {
		tapePosition = 0;
		reachedTapePosition = 0;
		programPosition.push(0);
		printArray();
		while(programPosition.peek() < program.length) {
			step();
		}
	}

	private void step() {
		Integer pPosition = programPosition.pop();

		switch(program[pPosition]) {
		case '+': /* All right chars */
			tape[tapePosition]++;
			break;
		case '-':
			tape[tapePosition]--;
			break;
		case ',':
			try {
				tape[tapePosition] = (char)System.in.read();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		case '.':
			System.out.print(tape[tapePosition]);
			break;
		case '>':
			tapePosition++;
			reachedTapePosition = reachedTapePosition < tapePosition ? tapePosition : reachedTapePosition;
			break;
		case '<':
			tapePosition--;
			break;
		case '[':
			// Jump ahead if this tape position is 0
			if(tape[tapePosition] == 0) {
				pPosition = (Integer) loopJump.get(pPosition);
			}
			break;
		case ']':
			// Go to previous position. -1, because we need to stand at [ char 
			pPosition = (Integer) loopJump.get(pPosition) - 1;
			break;
		default:
			/* Comments. */
		}
		printArray();

		programPosition.push(pPosition+1);
	}

	public void printArray() {
		int zeros = 0;
		final int limit = 2;
		System.out.print("\n[");
		for(int i = 0; i < reachedTapePosition; i++) {
			if(i == tapePosition) {
				System.out.print("*");
			}
			if((int)tape[i] != 0) {
				if(isChar(tape[i])) {
					System.out.print('\''+(char)tape[i]+'\'');
				} else {
					System.out.print((int)tape[i]);
				}
				System.out.print(", ");
				zeros = 0;
			} else if(zeros < limit) {
				System.out.print('0');
				System.out.print(", ");
				zeros++;
			} else if(zeros == limit) {
				zeros++;
				System.out.print("...");
				System.out.print(", ");
			}
		}
		if(reachedTapePosition == tapePosition) {
			System.out.print("*");
		}
		int i = reachedTapePosition;
		if(isChar(tape[i])) {
			System.out.print('\''+(char)tape[i]+'\'');
		} else {
			System.out.print((int)tape[i]);
		}
		System.out.println("]\n");
	}

	private boolean isChar(char c) {
		return '*' < c && c < '~';
	}
}
