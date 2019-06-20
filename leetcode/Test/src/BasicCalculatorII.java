import java.util.Stack;

public class BasicCalculatorII {

	public int calculate(String s) {
		Stack<Integer> nums = new Stack<>();
		Stack<Character> op = new Stack<>();
		char[] ss = s.toCharArray();
		int num = 0; boolean flag = false;
		for(int i = 0; i < ss.length; i++){
			while(i < ss.length && ss[i] == ' ') i++;
			while(i < ss.length && Character.isDigit(ss[i])){
				num = num * 10 + ss[i] - '0';
				i++;
				flag = true;
			}
			if(flag){
				nums.push(num);
				num = 0;
				flag = false;
			}
			
			while(i < ss.length && ss[i] == ' ') i++;
			if(i < ss.length){
				if(ss[i] == '*'){
					int num1 = nums.pop();
					int num2 = 0;
					i++;
					while(i < ss.length && ss[i] == ' ') i++;
					while(i < ss.length && Character.isDigit(ss[i])){
						num2 = num2 * 10 + ss[i] - '0';
						i++;
					}
					i--;
					nums.push(num1 * num2);
				}else if(ss[i] == '/'){
					int num1 = nums.pop();
					int num2 = 0;
					i++;
					while(i < ss.length && ss[i] == ' ') i++;
					while(i < ss.length && Character.isDigit(ss[i])){
						num2 = num2 * 10 + ss[i] - '0';
						i++;
					}
					i--;
					nums.push(num1 / num2);
				}else{
					op.push(ss[i]);
				}
			}
		}
		Stack<Integer> numss = new Stack<>();
		while(!nums.isEmpty()) numss.push(nums.pop());
		Stack<Character> opp = new Stack<>();
		while(!op.isEmpty()) opp.push(op.pop());
		while(!opp.isEmpty()){
			int num1 = numss.pop();
			int num2 = numss.pop();
			if(opp.pop() == '+'){
				numss.push(num1 + num2);
			}else{
				numss.push(num1 - num2);
			}
		}
		return numss.pop();
	}
	
	public static void main(String[] args) {
		BasicCalculatorII calc = new BasicCalculatorII();
		System.out.println(calc.calculate("2/2 *3 + 1"));
	}

}
