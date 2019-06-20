
/**
 * @author li_zhe
 * ²¢²é¼¯
 */
public class RedundantConnection {

	public int[] findRedundantConnection(int[][] edges) {
		if(edges == null || edges.length == 0) return new int[]{};
		unionFind();
		for(int[] edge : edges){
			int x = find(edge[0]);
			int y = find(edge[1]);
			if(x == y){
				return edge;
			}
			parent[x] = parent[y];
		}
		return null;
	}
	int[] parent = new int[1001];
	private void unionFind(){
		for(int i = 1; i < 1001; i++)
			parent[i] = i;
	}
	private int find(int x){
		if(parent[x] == x) return x;
		return find(parent[x]);
	}
	
	public static void main(String[] args) {
		RedundantConnection redundant = new RedundantConnection();
		int[] res = redundant.findRedundantConnection(new int[][]{{1,2}, {2,3}, {3,4}, {1,4}, {1,5}});
		System.out.println(res[0] + " " + res[1]);
		res = redundant.findRedundantConnection(new int[][]{{1,4},{3,4},{1,3},{1,2},{4,5}});
		System.out.println(res[0] + " " + res[1]);
		res = redundant.findRedundantConnection(new int[][]{{1,2},{1,3},{2,3}});
		System.out.println(res[0] + " " + res[1]);
	}

}
