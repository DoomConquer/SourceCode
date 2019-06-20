public class KClosestPointtoOrigin {

    public int[][] kClosest(int[][] points, int K) {
    	int n = points.length;
    	int[] index = new int[n];
    	for(int i = 0; i < n; i++) index[i] = i;
        count(points, index, new int[n], 0, n - 1, K);
        int[][] res = new int[K][2];
        for(int i = 0; i < K; i++){
        	res[i][0] = points[index[i]][0];
        	res[i][1] = points[index[i]][1];
        }
        return res;
    }
    private void count(int[][] points, int[] index, int[] temp, int left, int right, int k){
    	if(left >= right) return;
    	int mid = left + (right - left) / 2;
    	count(points, index, temp, left, mid, k);
    	count(points, index, temp, mid + 1, right, k);
    	
    	int i = left, j = mid + 1, m = 0;
    	while(i <= mid && j <= right && m < k){
    		if(powSum(points, index[i]) < powSum(points, index[j])) temp[m++] = index[i++];
    		else temp[m++] = index[j++];
    	}
    	while(i <= mid && m < k) temp[m++] = index[i++];
    	while(j <= right && m < k) temp[m++] = index[j++];
    	System.arraycopy(temp, 0, index, left, m);
    }
    private float powSum(int[][] points, int i){
    	return points[i][0] * points[i][0] + points[i][1] * points[i][1];
    }
    
	public static void main(String[] args) {
		KClosestPointtoOrigin kClosestPointtoOrigin = new KClosestPointtoOrigin();
		int[][] res = kClosestPointtoOrigin.kClosest(new int[][]{{1,3},{-2,2}}, 1);
		for(int i = 0; i < res.length; i++){
			System.out.print(res[i][0] + ", " + res[i][1] + "  ");
		}
		System.out.println();
		res = kClosestPointtoOrigin.kClosest(new int[][]{{3,3},{5,-1},{-2,4}}, 2);
		for(int i = 0; i < res.length; i++){
			System.out.print(res[i][0] + ", " + res[i][1] + "  ");
		}
	}

}
