package toutiao;

// dfs，可以用并查集求解
public class FriendCircle {

    public int findCircleNum(int[][] M) {
        int count = 0;
        int n = M.length;
        for(int i = 0; i < n; i++){
    		if(M[i][i] == 1){
    			find(M, i);
    			count++;
    		}
        }
        return count;
    }
    private void find(int[][] M , int i){
    	for(int j = 0; j < M.length; j++){
    		if(M[i][j] == 1){
    			M[i][j] = 0;
    			find(M, j);
    		}
    	}
    }
    
	public static void main(String[] args) {
		FriendCircle friendCircle = new FriendCircle();
		System.out.println(friendCircle.findCircleNum(new int[][]{{1,1,0},{1,1,0},{0,0,1}}));
		System.out.println(friendCircle.findCircleNum(new int[][]{{1,1,0},{1,1,1},{0,1,1}}));
		System.out.println(friendCircle.findCircleNum(new int[][]{{1,0,0,1},{0,1,1,0},{0,1,1,1},{1,0,1,1}}));
	}

}
