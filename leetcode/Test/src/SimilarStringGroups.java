/**
 * @author li_zhe
 * ²¢²é¼¯
 */
public class SimilarStringGroups {

	public int numSimilarGroups(String[] A) {
		if(A == null || A.length == 0) return 0;
		unionFind();
		count = A.length;
		for(int i = 0; i < A.length; i++){
			for(int j = i + 1; j < A.length; j++){
				if(isSimilar(A[i], A[j])){
					union(i, j);
				}
			}
		}
		return count;
	}
	private boolean isSimilar(String s1, String s2){
		char[] ch1 = s1.toCharArray();
		char[] ch2 = s2.toCharArray();
		int diff = 0;
		for(int i = 0; i < ch1.length; i++){
			if(ch1[i] != ch2[i]) diff++;
			if(diff > 2) return false;
		}
		return diff == 2;
	}
	int UF_LEN = 2000;
	int count = 0;
	int[] parent = new int[UF_LEN];
	private void unionFind(){
		for(int i = 0; i < UF_LEN; i++)
			parent[i] = i;
	}
	private int find(int x){
		if(x == parent[x]) return x;
		return find(parent[x]);
	}
	private void union(int x, int y){
		int xx = find(x);
		int yy = find(y);
		if(xx == yy) return;
		count--;
		parent[yy] = xx;
	}
	
	public static void main(String[] args) {
		SimilarStringGroups similar = new SimilarStringGroups();
		System.out.println(similar.numSimilarGroups(new String[]{"tars","rats","arts","star"}));
		System.out.println(similar.numSimilarGroups(new String[]{"ajdidocuyh","djdyaohuic","ddjyhuicoa","djdhaoyuic","ddjoiuycha","ddhoiuycja","ajdydocuih","ddjiouycha","ajdydohuic","ddjyouicha"}));
	}

}
