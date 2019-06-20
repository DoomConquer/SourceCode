public class FriendCircles {

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
		FriendCircles friend = new FriendCircles();
		System.out.println(friend.findCircleNum(new int[][]{{1,1,0},{1,1,0},{0,0,1}}));
		System.out.println(friend.findCircleNum(new int[][]{{1,0,0,1},{0,1,1,0},{0,1,1,1},{1,0,1,1}}));
	}

}
