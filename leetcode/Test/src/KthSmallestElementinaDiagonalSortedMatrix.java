import java.util.PriorityQueue;

/**
 * @author li_zhe
 * 题目: (非leetcode题目) 一个矩阵按对角线升序排列,每一条对角线上的元素无序,上一条对角线上的元素均小于当前对角线,找第k小的元素
 */
public class KthSmallestElementinaDiagonalSortedMatrix {

	public int kthSmallest(int[][] matrix, int k) {
		int n = matrix.length;
		int sum = 1, num = 1;
		PriorityQueue<Integer> heap = new PriorityQueue<Integer>();
		for(int i = 0; i < n<<2 - 1; i++){
			if(sum < k) { 
				if(i < n) num++;
				else num--;
				sum += num; 
				continue; 
			}
			int x = i < n ? i : n - 1;
			int y = i < n ? 0 : i - n + 1;
			while(x >= 0 && y < n){
				heap.add(matrix[x--][y++]);
			}
			break;
		}
		int i = num - sum + k - 1;
		while(i-- > 0){
			heap.poll();
		}
		return heap.poll();
	}
	
	public static void main(String[] args) {
		KthSmallestElementinaDiagonalSortedMatrix kth = new KthSmallestElementinaDiagonalSortedMatrix();
		System.out.println(kth.kthSmallest(new int[][]{{1,  5,  9}, {10, 11, 13}, {12, 14, 15}}, 3));
	}

}
