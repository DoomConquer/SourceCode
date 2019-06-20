
/**
 * @author li_zhe
 * ����˼·O(n^2),һ��ɨ���˼·�����뵽,���leetcode
 * һ��ɨ��,���������¼����ֵ,������ɨ�赽С�����ֵʱ,��¼Ҫ��������ߵ�ֵ,��Ϊһֱ�����ɨ������ҵ�����ߵ�С�����ֵ��λ��
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
