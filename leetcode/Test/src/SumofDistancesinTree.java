import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author li_zhe
 * 参考leetcode解题思路
 */
public class SumofDistancesinTree {

	int[] subTreeSum;
	int[] subTreeNum;
	public int[] sumOfDistancesInTree(int N, int[][] edges) {
		List<Set<Integer>> nodes = new ArrayList<>();
		for(int i = 0; i < N; i++) nodes.add(new HashSet<>());
		for(int i = 0; i < edges.length; i++){
			nodes.get(edges[i][0]).add(edges[i][1]);
			nodes.get(edges[i][1]).add(edges[i][0]);
		}
		subTreeSum = new int[N];
		subTreeNum = new int[N];
		postOrder(nodes, new HashSet<>(), 0);
		preOrder(nodes, new HashSet<>(), 0, N);
		return subTreeSum;
	}
	private void preOrder(List<Set<Integer>> nodes, Set<Integer> visited, int curr, int N){
		visited.add(curr);
		for(int i : nodes.get(curr)){
			if(!visited.contains(i)){
				subTreeSum[i] = subTreeSum[curr] - subTreeNum[i] + N - subTreeNum[i];
				preOrder(nodes, visited, i, N);
			}
		}
	}
	private void postOrder(List<Set<Integer>> nodes, Set<Integer> visited, int curr){
		visited.add(curr);
		for(int i : nodes.get(curr)){
			if(!visited.contains(i)){
				postOrder(nodes, visited, i);
				subTreeNum[curr] += subTreeNum[i];
				subTreeSum[curr] += subTreeSum[i] + subTreeNum[i];
			}
		}
		subTreeNum[curr]++;
	}
	
	public static void main(String[] args) {
		SumofDistancesinTree sum = new SumofDistancesinTree();
		int[] res = sum.sumOfDistancesInTree(6, new int[][]{{0,1},{0,2},{2,3},{2,4},{2,5}});
		for(int num : res)
			System.out.print(num + "  ");
		System.out.println();
		res = sum.sumOfDistancesInTree(2, new int[][]{{1,0}});
		for(int num : res)
			System.out.print(num + "  ");
	}

}
