
/**
 * @author li_zhe
 * 正常思路O(n^2),一遍扫描的思路不好想到,借鉴leetcode
 * 一遍扫描,从右往左记录最大的值,如果左边扫描到小于最大值时,记录要交换的左边的值,因为一直往左边扫描可以找到最左边的小于最大值的位置
 */
public class MaximumSwap {

	public int maximumSwap(int num) {
		String numstr = String.valueOf(num);
        int maxidx = -1; int maxdigit = -1;
        int leftidx = -1; int rightidx = -1;        
        char[] sch = numstr.toCharArray();
        for (int i = sch.length - 1; i >= 0; --i) {
            if (sch[i] > maxdigit) {
                maxdigit = sch[i];
                maxidx = i;
                continue;
            }
            if (sch[i] < maxdigit) {
                leftidx = i;
                rightidx = maxidx;
            }
        }
        if (leftidx == -1) return num;
        swap(sch, leftidx, rightidx);
        return Integer.valueOf(String.valueOf(sch));
	}
	private void swap(char[] sch, int i, int j){
		char temp = sch[i];
		sch[i] = sch[j];
		sch[j] = temp;
	}
	
	public static void main(String[] args) {
		MaximumSwap swap = new MaximumSwap();
		System.out.println(swap.maximumSwap(134567334));
		System.out.println(swap.maximumSwap(123));
		System.out.println(swap.maximumSwap(1));
	}

}
