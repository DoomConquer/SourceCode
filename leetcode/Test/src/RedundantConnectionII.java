/**
 * @author li_zhe
 * �ο�leetcode���⣬ԭ��򵥣��������
 * �����������壺һ���ڵ�������������ڵ㣬��ô����һ����һ���Ƕ���ģ����������������жϣ�
 * ��Ϊ����һ���ڵ����������ڵ�ʱ��û��union�����Ե��л�ʱ����Ҫȥ����һ���ᵼ��һ���ڵ�
 * ���������ڵ�������ߣ���Ϊֻ��һ������ıߣ���
 */
public class RedundantConnectionII {

	public int[] findRedundantDirectedConnection(int[][] edges) {
		if(edges == null || edges.length == 0) return new int[]{};
		unionFind(edges.length);
		int[] res1 = null, res2 = null;
		for(int[] edge : edges){
			int x = find(edge[0] - 1);
			int y = find(edge[1] - 1);
			if(x != y){
				if(y != edge[1] - 1) res1 = edge;
				else parent[y] = parent[x];
			}else{
				res2 = edge;
			}
		}
		if(res1 == null) return res2;
		if(res2 == null) return res1;
		for(int[] edge : edges)
			if(res1[1] == edge[1]) return edge;
		return null;
	}
	int[] parent;
	private void unionFind(int n){
		parent = new int[n];
		for(int i = 0; i < n; i++)
			parent[i] = i;
	}
	private int find(int x){
		while(x != parent[x]){
			x = parent[parent[x]];
		}
		return x;
	}
	
	public static void main(String[] args) {
		RedundantConnectionII redundant = new RedundantConnectionII();
		int[] res = redundant.findRedundantDirectedConnection(new int[][]{{1,2},{1,3},{2,3}});
		for(int num : res) System.out.print(num + " "); System.out.println();
		res = redundant.findRedundantDirectedConnection(new int[][]{{2,1},{3,1},{4,2},{1,4}});
		for(int num : res) System.out.print(num + " "); System.out.println();
	}

}
