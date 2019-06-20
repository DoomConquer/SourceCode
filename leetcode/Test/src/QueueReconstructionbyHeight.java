import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * @author li_zhe
 * 思路参考leetcode
 * 贪心策略,一个height比之前小的people插入是不影响已经放好的序列
 */
public class QueueReconstructionbyHeight {

	public int[][] reconstructQueue(int[][] people) {
		if(people == null || people.length == 0) return new int[][]{};
		Arrays.sort(people, (o1, o2) -> {
			if(o1[0] == o2[0]) return o1[1] - o2[1];
			return o2[0] - o1[0];
		});
		List<int[]> list = new LinkedList<>();
		for(int[] p : people) list.add(p[1], p);
		return list.toArray(new int[people.length][2]);
	}
	
	private static void print(int[][] res){
		for(int i = 0; i < res.length; i++){
			for(int j = 0; j < res[i].length; j++)
				System.out.print(res[i][j] + " ");
			System.out.println();
		}
	}
	public static void main(String[] args) {
		QueueReconstructionbyHeight queue = new QueueReconstructionbyHeight();
		int[][] res = queue.reconstructQueue(new int[][]{
			{7,0}, {4,4}, {7,1}, {5,0}, {6,1}, {5,2}
		});
		print(res);
	}

}
