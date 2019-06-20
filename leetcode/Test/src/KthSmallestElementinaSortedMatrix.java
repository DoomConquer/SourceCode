import java.util.PriorityQueue;

public class KthSmallestElementinaSortedMatrix {

	class Pair{
		int i;
		int j;
		int val;
	}
	public int kthSmallest(int[][] matrix, int k) {
		int n = matrix.length;
		PriorityQueue<Pair> heap = new PriorityQueue<>((o1, o2)->{ return o1.val - o2.val;});
		for(int i = 0; i < n; i++){
			Pair pair = new Pair();
			pair.i = i;
			pair.j = 0;
			pair.val = matrix[i][0];
			heap.add(pair);
		}
		while(--k > 0){
			Pair pair = heap.poll();
			if(pair.j + 1 < n){
				Pair next = new Pair();
				next.i = pair.i;
				next.j = pair.j + 1;
				next.val = matrix[pair.i][pair.j + 1];
				heap.add(next);
			}
		}
		return heap.poll().val;
	}
	
	public static void main(String[] args) {
		KthSmallestElementinaSortedMatrix kth = new KthSmallestElementinaSortedMatrix();
		System.out.println(kth.kthSmallest(new int[][]{{1,5,9}, {10,11,13}, {12,13,15}}, 1));
	}

}
