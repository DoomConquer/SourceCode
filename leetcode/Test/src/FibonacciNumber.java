public class FibonacciNumber {

    public int fib(int N) {
        int f0 = 0, f1 = 1;
        if(N == 0) return 0;
        if(N == 1) return 1;
        for(int i = 2; i <= N; i++){
        	int temp = f0 + f1;
        	f0 = f1;
        	f1 = temp;
        }
        return f1;
    }
    
	public static void main(String[] args) {
		FibonacciNumber fibonacciNumber = new FibonacciNumber();
		System.out.println(fibonacciNumber.fib(20));
		System.out.println(fibonacciNumber.fib(3));
		System.out.println(fibonacciNumber.fib(4));
	}

}
