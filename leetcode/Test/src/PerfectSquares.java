import java.util.LinkedList;
import java.util.Queue;

public class PerfectSquares {

	public int numSquares(int n) {
		if(n <= 0) return 0;
		Queue<Integer> queue = new LinkedList<>();
		queue.add(n);
		int leatest = 0;
		while(!queue.isEmpty()){
			int size = queue.size();
			leatest++;
			for(int i = 0; i < size; i++){
				int num = queue.poll();
				for(int j = (int) Math.sqrt(num); j > 0; j--){
					int left = num - j * j;
					if(left == 0) return leatest;
					queue.add(left);
				}
			}
		}
		return leatest;
	}
	
	public static void main(String[] args) {
		PerfectSquares squares = new PerfectSquares();
		System.out.println(squares.numSquares(12));
	}

}
