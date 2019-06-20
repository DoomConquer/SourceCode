public class ReversePairs {

    public int reversePairs(int[] nums) {
    	return partion(nums, 0, nums.length - 1, new int[nums.length]);
    }
    private int partion(int[] nums, int left, int right, int[] temp){
    	if(left >= right) return 0;
    	int mid = (left + right) / 2;
    	
    	int count = partion(nums, left, mid, temp) + partion(nums, mid + 1, right, temp);
    	count += count(nums, left, right, mid);
    	
    	int index = 0;
    	int i = left, j = mid + 1;
    	while(i <= mid && j <= right){
    		if(nums[i] < nums[j]){
    			temp[index++] = nums[i++];
    		}else{
    			temp[index++] = nums[j++];
    		}
    	}
    	while(i <= mid) temp[index++] = nums[i++];
    	while(j <= right) temp[index++] = nums[j++];
    	System.arraycopy(temp, 0, nums, left, right - left + 1);
    	return count;
    }
    private int count(int[] nums, int left, int right, int mid){
    	int count = 0;
    	for(int i = left, j = mid + 1; i <= mid && j <= right;){
    		if(nums[i] > nums[j] * 2L){
    			count += mid - i + 1;
    			j++;
    		}else{
    			i++;
    		}
    	}
    	return count;
    }
    
	public static void main(String[] args) {
		ReversePairs reversePairs = new ReversePairs();
		System.out.println(reversePairs.reversePairs(new int[]{1,3,2,3,1}));
		System.out.println(reversePairs.reversePairs(new int[]{2,4,3,5,1}));
	}

}
