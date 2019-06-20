import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

/**
 * @author li_zhe
 * 题目理解不对,不是每次只能砍最小高度的树,是每次选择砍较小高度的树
 * (该理解是树不能通过，每次可以砍当前树周围所有能砍的树中height最小的)
 */
public class CutOffTreesforGolfEvent_Wrong {
	
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
		Set<String> visited = new HashSet<>();
		int n = forest.size();
		int m = forest.get(0).size();
		int trees = 0;
		int[][] map = new int[n][m];
		for(int i = 0; i < n; i++){
			List<Integer> list = forest.get(i);
			for(int j = 0; j < list.size(); j++){
				map[i][j] = list.get(j);
				if(map[i][j] > 1) trees++;
			}
		}
		if(map[0][0] == 0) return -1;
		if(map[0][0] == 1) trees++;
		Tree tree = new Tree(0, 0, map[0][0]);
		visited.add(0 + "-" + 0);
		int step = 0;
		Tree source = tree;
		distance(source, null, map, heap, visited);
		while(!heap.isEmpty()){
			Tree target= heap.poll();
			int dist = distance(source, target, map, heap, visited);
			if(dist < 0) return -1;
			step += dist;
			source = target;
			distance(source, null, map, heap, visited);
		}
		if(visited.size() == trees) return step;
		return -1;
	}
	private int distance(Tree source, Tree target, int[][] map, Queue<Tree> heap, Set<String> visited){
		if(target != null && source.x == target.x && source.y == target.y) return 0;
		Queue<Tree> queue = new LinkedList<>();
		Set<String> set = new HashSet<>();
		int width = map.length;
		int height = map[0].length;
		int[] dirX = new int[]{-1,1,0,0};
		int[] dirY = new int[]{0,0,-1,1};
		queue.offer(source);
		set.add(source.x + "-" + source.y);
		int step = 0;
		while(!queue.isEmpty()){
			step++;
			int size = queue.size();
			for(int i = 0; i < size; i++){
				Tree t = queue.poll();
				for(int j = 0; j < 4; j++){
					int xx = t.x + dirX[j];
					int yy = t.y + dirY[j];
					if(xx >= 0 && xx < width && yy >= 0 && yy < height){
						if(target != null && xx == target.x && yy == target.y){
							map[source.x][source.y] = 1;
							return step;
						}
						if(map[xx][yy] > 1 && !visited.contains(xx + "-" + yy)){
							Tree tt = new Tree(xx, yy, map[xx][yy]);
							heap.offer(tt);
							visited.add(xx + "-" + yy);
						}else if(map[xx][yy] == 1 && !set.contains(xx + "-" + yy)){
							Tree tt = new Tree(xx, yy, map[xx][yy]);
							queue.offer(tt);
							set.add(xx + "-" + yy);
						}
					}
				}
			}
		}
		return -1;
	}
	
	public static void main(String[] args) {
		CutOffTreesforGolfEvent_Wrong cutoff = new CutOffTreesforGolfEvent_Wrong();
		List<List<Integer>> forest = new ArrayList<List<Integer>>();
		forest.add(Arrays.asList(new Integer[]{2,3,4}));
		forest.add(Arrays.asList(new Integer[]{0,0,5}));
		forest.add(Arrays.asList(new Integer[]{8,7,6}));
		System.out.println(cutoff.cutOffTree(forest));
		List<List<Integer>> forest2 = new ArrayList<List<Integer>>();
		forest2.add(Arrays.asList(new Integer[]{9,8,7}));
		forest2.add(Arrays.asList(new Integer[]{6,5,4}));
		forest2.add(Arrays.asList(new Integer[]{3,2,1}));
		System.out.println(cutoff.cutOffTree(forest2));
		List<List<Integer>> forest1 = new ArrayList<List<Integer>>();
		forest1.add(Arrays.asList(new Integer[]{54581641,64080174,24346381,69107959}));
		forest1.add(Arrays.asList(new Integer[]{86374198,61363882,68783324,79706116}));
		forest1.add(Arrays.asList(new Integer[]{668150,92178815,89819108,94701471}));
		forest1.add(Arrays.asList(new Integer[]{83920491,22724204,46281641,47531096}));
		forest1.add(Arrays.asList(new Integer[]{89078499,18904913,25462145,60813308}));
		System.out.println(cutoff.cutOffTree(forest1));
	}
}
