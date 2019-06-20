// 参考leetcode思路
public class CountofRangeSum {

    public int countRangeSum(int[] nums, int lower, int upper) {
        long[] sum = new long[nums.length + 1]; // 注意类型，求和可能超出int范围
        for(int i = 1; i <= nums.length; i++){
        	sum[i] = nums[i - 1] + sum[i - 1];
        }
        return count(sum, new long[sum.length], 0, nums.length, lower, upper);
    }
    private int count(long[] sum, long[] temp, int left, int right, int lower, int upper){
    	if(left >= right) return 0;
    	int mid = left + (right - left) / 2;
    	int count = count(sum, temp, left, mid, lower, upper) + count(sum, temp, mid + 1, right, lower, upper);
    	
    	int i = mid + 1, j = mid + 1;
    	for(int k = left; k <= mid; k++){
    		while(i <= right && sum[i] - sum[k] < lower) i++;
    		while(j <= right && sum[j] - sum[k] <= upper) j++;
    		count += j - i;
    	}
    	
    	i = left; j = mid + 1;
    	int index = 0;
    	while(i <= mid && j <= right){
    		if(sum[i] < sum[j]) temp[index++] = sum[i++];
    		else temp[index++] = sum[j++];
    	}
    	while(i <= mid) temp[index++] = sum[i++];
    	while(j <= right) temp[index++] = sum[j++];
    	System.arraycopy(temp, 0, sum, left, right - left + 1);
    	return count;
    }
    
	public static void main(String[] args) {
		CountofRangeSum CountofRangeSum = new CountofRangeSum();
		System.out.println(CountofRangeSum.countRangeSum(new int[]{-2,5,-1}, -2, 2));
		System.out.println(CountofRangeSum.countRangeSum(new int[]{2147483647,-2147483648,-1,0}, -1, 0));
		System.out.println(CountofRangeSum.countRangeSum(new int[]{-2147483647,0,-2147483647,2147483647}, -564, 3864));
	}

}
