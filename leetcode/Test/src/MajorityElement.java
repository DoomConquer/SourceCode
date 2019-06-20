
public class MajorityElement {

	public int majorityElement(int[] nums) {
		int num = nums[0];
		int count = 0;
		for(int i = 0; i < nums.length; i++){
			if(count == 0){
				count++;
				num = nums[i];
			}else if(num == nums[i]){
				count++;
			}else{
				count--;
			}
		}
		return num;
	}
	
	public static void main(String[] args) {
		MajorityElement majority = new MajorityElement();
		System.out.println(majority.majorityElement(new int[]{3,2,3}));
		System.out.println(majority.majorityElement(new int[]{2,2,1,1,1,2,2}));
	}

}
