
/**
 * @author li_zhe
 * �������ⲻ��������⣺��ǰΪ����һ�����ֶ�һ��Ľ������ͬ���ֺϲ�һ��
 */
public class CountandSay {

	public String countAndSay(int n) {
		StringBuilder sb1 = new StringBuilder("1");
		StringBuilder sb2 = new StringBuilder();
		n--;
		while(n-- > 0){
			int count = 1;
			for(int i = 0; i < sb1.length(); i++){
				if(i + 1 < sb1.length() && sb1.charAt(i) == sb1.charAt(i + 1)){
					count++; continue;
				}
				sb2.append(String.valueOf(count));
				sb2.append(sb1.charAt(i));
				count = 1;
			}
			sb1 = sb2;
			sb2 = new StringBuilder();
		}
		return sb1.toString();
	}
	
	public static void main(String[] args) {
		CountandSay count = new CountandSay();
		System.out.println(count.countAndSay(1));
		System.out.println(count.countAndSay(2));
		System.out.println(count.countAndSay(3));
		System.out.println(count.countAndSay(5));
		System.out.println(count.countAndSay(10));
	}

}
