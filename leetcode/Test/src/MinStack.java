import java.util.Stack;

class MinStack {

	private Stack<Integer> stack;
	private Stack<Integer> minStack;
    public MinStack() {
        stack = new Stack<>();
        minStack = new Stack<>();
    }
    
    public void push(int x) {
        if(minStack.isEmpty() || minStack.peek() >= x) minStack.push(x);
        stack.push(x);
    }
    
    public void pop() {
        if(!minStack.isEmpty() && (int)minStack.peek() == (int)stack.peek()) minStack.pop();
        stack.pop();
    }
    
    public int top() {
        return stack.peek();
    }
    
    public int getMin() {
        return minStack.peek();
    }
    
    public static void main(String[] args) {
		MinStack stack = new MinStack();
		stack.push(1);
	}
}