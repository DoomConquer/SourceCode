package toutiao;

import java.util.Stack;

public class MinStack {

	private Stack<Integer> stack = null;
	private Stack<Integer> min = null;
	public MinStack() {
        stack = new Stack<>();
        min = new Stack<>();
    }
    
    public void push(int x) {
        stack.push(x);
        if(min.isEmpty() || min.peek() >= x)min.push(x);
    }
    
    public void pop() {
        int value = stack.pop();
        if(!min.isEmpty() && min.peek() == value) min.pop();
    }
    
    public int top() {
        return stack.isEmpty() ? 0 : stack.peek();
    }
    
    public int getMin() {
        return min.isEmpty() ? 0 : min.peek();
    }
    
	public static void main(String[] args) {
		MinStack minStack = new MinStack();
		minStack.push(-2);
		minStack.push(0);
		minStack.push(-3);
		minStack.push(-3);
		System.out.println(minStack.getMin());
		System.out.println(minStack.top());
		minStack.pop();
		System.out.println(minStack.top());
		System.out.println(minStack.getMin());
	}

}
