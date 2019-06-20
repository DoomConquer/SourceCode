import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class MinimumHeightTrees {

	public List<Integer> findMinHeightTrees(int n, int[][] edges) {
		List<Integer> res = new ArrayList<>();
		if(n <= 0) return res;
		List<List<Integer>> path = new ArrayList<>();
		int[] degree = new int[n];
		for(int i = 0; i < n; i++)
			path.add(new ArrayList<>());
		for(int i = 0; i < edges.length; i++){
			path.get(edges[i][0]).add(edges[i][1]);
			path.get(edges[i][1]).add(edges[i][0]);
			degree[edges[i][0]]++;
			degree[edges[i][1]]++;
		}
		Queue<Integer> queue = new LinkedList<>();
		for(int i = 0; i < n; i++){
			if(degree[i] == 0){
				res.add(i);
				return res;
			}else if(degree[i] == 1){
				queue.offer(i);
			}
		}
		while(!queue.isEmpty()){
			int size = queue.size();
			res.clear();
			for(int i = 0; i < size; i++){
				int node = queue.poll();
				res.add(node);
				List<Integer> list = path.get(node);
				for(int j = 0; j < list.size(); j++){
					if(degree[list.get(j)] == 0) continue;
					if(degree[list.get(j)] == 2) queue.offer(list.get(j));
					degree[list.get(j)]--;
				}
			}
		}
		return res;
	}
	
	public static void main(String[] args) {
		MinimumHeightTrees tree = new MinimumHeightTrees();
		System.out.println(tree.findMinHeightTrees(1, new int[][]{}));
		System.out.println(tree.findMinHeightTrees(4, new int[][]{{1,0},{1,2},{1,3}}));
		System.out.println(tree.findMinHeightTrees(6, new int[][]{{0, 3}, {1, 3}, {2, 3}, {4, 3}, {5, 4}}));
	}

}
