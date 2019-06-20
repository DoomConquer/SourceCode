import java.util.Stack;

public class EvaluateReversePolishNotation {

	public int evalRPN(String[] tokens) {
		Stack<String> stack = new Stack<>();
		for(String s : tokens){
			switch(s){
				case "+":
					int num1 = Integer.parseInt(stack.pop());
					int num2 = Integer.parseInt(stack.pop());
					stack.push(String.valueOf(num1 + num2));
					break;
				case "-":
					num1 = Integer.parseInt(stack.pop());
					num2 = Integer.parseInt(stack.pop());
					stack.push(String.valueOf(num2 - num1));
					break;
				case "*":
					num1 = Integer.parseInt(stack.pop());
					num2 = Integer.parseInt(stack.pop());
					stack.push(String.valueOf(num1 * num2));
					break;
				case "/":
					num1 = Integer.parseInt(stack.pop());
					num2 = Integer.parseInt(stack.pop());
					stack.push(String.valueOf(num2 / num1));
					break;
				default:
					stack.push(s);
			}
		}
		return Integer.parseInt(stack.pop());
	}
	
	public static void main(String[] args) {
		EvaluateReversePolishNotation polish = new EvaluateReversePolishNotation();
		System.out.println(polish.evalRPN(new String[]{"2", "3", "*", "4", "-"}));
	}

}
