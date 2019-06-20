
/**
 * @author li_zhe
 * ˼·��Դ֮ǰ����һ����,O(n)ʱ��,O(1)�ռ�
 * ����ԭ�����¼Ӧ������ȷλ�õ���,�����ͳ������鳤�ȵ�������Ҫ����
 */
public class FirstMissingPositive {

	public int firstMissingPositive(int[] nums) {
		if(nums == null || nums.length == 0) return 1;
		int index = 0, len = nums.length;
		while(index < len){
			if(nums[index] > len || nums[index] <= 0 || nums[index] == index + 1){ index++; continue; }
			int next = nums[index];
			while(next > 0 && next <= len && nums[next - 1] != next){
				int temp = nums[next - 1];
				nums[next - 1] = next;
				next = temp;
			}
			index++;
		}
		for(int i = 0; i < len; i++) if(nums[i] !=  i + 1) return i + 1;
		return len + 1;
	}
	
	public static void main(String[] args) {
		FirstMissingPositive first = new FirstMissingPositive();
		System.out.println(first.firstMissingPositive(new int[]{1,1,1,2}));
		System.out.println(first.firstMissingPositive(new int[]{3,4,-1,1}));
	}

}
