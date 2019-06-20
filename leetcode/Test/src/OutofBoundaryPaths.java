public class OutofBoundaryPaths {

	public int findPaths(int m, int n, int N, int i, int j) {
		long[][][] path = new long[m][n][N + 1];
		for(int in = 0; in < m; in++)
			for(int jn = 0; jn < n; jn++)
				for(int kn = 0; kn <= N; kn++)
					path[in][jn][kn] = -1;
		int res= (int)find(m, n, N, i, j, path);
		return res;
	}
	private long find(int m, int n, int N, int i, int j, long[][][] path){
		if(i < 0 || i >= m || j < 0 || j >= n) return 1;
		if(N == 0) return 0;
		if(path[i][j][N] != -1) return path[i][j][N];
		path[i][j][N] = 0;
		long left = find(m, n, N - 1, i - 1, j, path) % (1000000000 + 7);
		long right = find(m, n, N - 1, i + 1, j, path) % (1000000000 + 7);
		long up = find(m, n, N - 1, i, j - 1, path) % (1000000000 + 7);
		long down = find(m, n, N - 1, i, j + 1, path) % (1000000000 + 7);
		path[i][j][N] = (left + right + up + down) % (1000000000 + 7);
		return path[i][j][N];
	}
	
	public static void main(String[] args) {
		OutofBoundaryPaths boundary = new OutofBoundaryPaths();
		System.out.println(boundary.findPaths(50, 50, 50, 25, 25));
		System.out.println(boundary.findPaths(2, 2, 2, 0, 0));
		System.out.println(boundary.findPaths(8, 7, 16, 1, 5));
		System.out.println(boundary.findPaths(8, 7, 50, 1, 5));
	}

}
