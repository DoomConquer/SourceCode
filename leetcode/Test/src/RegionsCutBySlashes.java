public class RegionsCutBySlashes {

    public int regionsBySlashes(String[] grid) {
    	int len = grid.length;
        int[][] m = new int[len * 3][len * 3];
        for(int i = 0; i < len; i++){
        	for(int j = 0; j < len; j++){
        		if(grid[i].charAt(j) == '/') m[i * 3][j * 3 + 2] = m[i * 3 + 1][j * 3 + 1] = m[i * 3 + 2][j * 3] = 1;
        		else if(grid[i].charAt(j) == '\\') m[i * 3][j * 3] = m[i * 3 + 1][j * 3 + 1] = m[i * 3 + 2][j * 3 + 2] = 1;
        	}
        }
        int count = 0;
        len = len * 3;
        for(int i = 0; i < len; i++){
        	for(int j = 0; j < len; j++){
        		if(m[i][j] != 1){
        			find(m, i, j, len); 
        			count++;
        		}
        	}
        }
        return count;
    }
    private void find(int[][] m, int i, int j, int len){
    	if(i < 0 || j < 0 || i >= len || j >= len || m[i][j] == 1) return;
    	m[i][j] = 1;
    	find(m, i + 1, j, len);
    	find(m, i, j + 1, len);
    	find(m, i - 1, j, len);
    	find(m, i, j - 1, len);
    }
    
	public static void main(String[] args) {
		RegionsCutBySlashes regionsCutBySlashes = new RegionsCutBySlashes();
		System.out.println(regionsCutBySlashes.regionsBySlashes(new String[]{"\\/", "/\\"}));
	}

}
