import java.util.PriorityQueue;

public class SuperUglyNumber {

	public int nthSuperUglyNumber(int n, int[] primes) {
		if(n == 1) return 1;
		PriorityQueue<Long> heap = new PriorityQueue<Long>();
		heap.add(1l);
		while(n > 1){
			long curr = heap.poll();
			for(int num : primes){
				if((long)num * curr < Integer.MAX_VALUE)
					heap.add(num * curr);
			}
			while(heap.peek() == curr) heap.poll();
			n--;
		}
		return heap.peek().intValue();
	}
	
	public static void main(String[] args) {
		SuperUglyNumber ugly = new SuperUglyNumber();
		System.out.println(ugly.nthSuperUglyNumber(30, new int[]{3,5,13,19,23,31,37,43,47,53}));
	}

}
