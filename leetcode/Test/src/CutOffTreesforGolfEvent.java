import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * @author li_zhe
 * 之前题目理解不对,不是每次只能砍最小高度的树,是每次选择砍较小高度的树
 * (上述理解也是错误的，树也是可以被通过的)
 */
public class CutOffTreesforGolfEvent {

	class Tree{
		int height;
		int x;
		int y;
		public Tree(int x, int y, int height){
			this.x = x;
			this.y = y;
			this.height = height;
		}
	}
	public int cutOffTree(List<List<Integer>> forest) {
		if(forest == null || forest.size() == 0 || forest.get(0).size() == 0) return -1;
		Queue<Tree> heap = new PriorityQueue<>((o1, o2) -> {return o1.height - o2.height;});
		int n = forest.size();
		int m = forest.get(0).size();
		int[][] map = new int[n][m];
		for(int i = 0; i < n; i++){
			List<Integer> list = forest.get(i);
			for(int j = 0; j < list.size(); j++){
				map[i][j] = list.get(j);
				if(i == 0 && j == 0 && map[i][j] == 0) return -1;
				if(map[i][j] > 1) {
					Tree t = new Tree(i, j, map[i][j]);
					heap.offer(t);
				}
			}
		}
		Tree source = new Tree(0, 0, map[0][0]);
		Tree target = null;
		int step = 0;
		while(!heap.isEmpty()){
			target = heap.poll();
			int dist = distance(source, target, map);
			if(dist < 0)
				return -1;
			step += dist;
			source = target;
		}
		return step;
	}
	private int distance(Tree source, Tree target, int[][] map){
		if(source.x == target.x && source.y == target.y) return 0;
		Queue<int[]> queue = new LinkedList<>();
		queue.offer(new int[]{source.x, source.y});
		int width = map.length;
		int height = map[0].length;
		boolean[][] flag = new boolean[width][height];
		flag[source.x][source.y] = true;
		int[] dirX = new int[]{-1,1,0,0};
		int[] dirY = new int[]{0,0,-1,1};
		int step = 0;
		while(!queue.isEmpty()){
			step++;
			int size = queue.size();
			for(int i = 0; i < size; i++){
				int[] t = queue.poll();
				for(int j = 0; j < 4; j++){
					int xx = t[0] + dirX[j];
					int yy = t[1] + dirY[j];
					if(xx >= 0 && xx < width && yy >= 0 && yy < height){
						if(xx == target.x && yy == target.y){
							map[source.x][source.y] = 1;
							return step;
						}
						if(map[xx][yy] >= 1 && !flag[xx][yy]){
							int[] tt = new int[]{xx, yy};
							queue.offer(tt);
							flag[xx][yy] = true;
						}
					}
				}
			}
		}
		return -1;
	}
	
	public static void main(String[] args) {
		CutOffTreesforGolfEvent cutoff = new CutOffTreesforGolfEvent();
		List<List<Integer>> forest = new ArrayList<List<Integer>>();
		forest.add(Arrays.asList(new Integer[]{1,2,3}));
		forest.add(Arrays.asList(new Integer[]{0,0,4}));
		forest.add(Arrays.asList(new Integer[]{7,6,5}));
		System.out.println(cutoff.cutOffTree(forest));
		List<List<Integer>> forest1 = new ArrayList<List<Integer>>();
		forest1.add(Arrays.asList(new Integer[]{54581641,64080174,24346381,69107959}));
		forest1.add(Arrays.asList(new Integer[]{86374198,61363882,68783324,79706116}));
		forest1.add(Arrays.asList(new Integer[]{668150,92178815,89819108,94701471}));
		forest1.add(Arrays.asList(new Integer[]{83920491,22724204,46281641,47531096}));
		forest1.add(Arrays.asList(new Integer[]{89078499,18904913,25462145,60813308}));
		System.out.println(cutoff.cutOffTree(forest1));
		List<List<Integer>> forest2 = new ArrayList<List<Integer>>();
		forest2.add(Arrays.asList(new Integer[]{9,8,7}));
		forest2.add(Arrays.asList(new Integer[]{6,5,4}));
		forest2.add(Arrays.asList(new Integer[]{3,2,1}));
		System.out.println(cutoff.cutOffTree(forest2));
	}

}
