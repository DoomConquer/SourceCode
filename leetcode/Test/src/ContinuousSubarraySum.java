import java.util.HashSet;

// ˼·��Դleetcode��math : (a + b) % k = (a % k + b % k) % k���������a % k == 0����ôb % k�ͻ������
public class ContinuousSubarraySum {

    public boolean checkSubarraySum(int[] nums, int k) {
        HashSet<Integer> set = new HashSet<>();
        int sum = 0, pre = 0;
        for(int num : nums){
        	sum += num;
        	int mod = (k == 0 ? sum : sum % k);
        	if(set.contains(mod)) return true;
        	set.add(pre);
        	pre = mod;
        }
        return false;
    }
    
	public static void main(String[] args) {
		ContinuousSubarraySum continuousSubarraySum = new ContinuousSubarraySum();
		System.out.println(continuousSubarraySum.checkSubarraySum(new int[]{23, 2, 4, 6, 7}, 6));
		System.out.println(continuousSubarraySum.checkSubarraySum(new int[]{23, 2, 6, 4, 7}, 6));
		System.out.println(continuousSubarraySum.checkSubarraySum(new int[]{23, 2}, 6));
	}

}
