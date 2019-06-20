
public class StringCompression {

	public int compress(char[] chars) {
		int count = 1;
		int len = 0;
		int j = 0;
		for(int i = 0; i < chars.length; i++){
			if(i + 1 < chars.length && chars[i] == chars[i + 1]){
				count++; continue;
			}
			chars[j++] = chars[i];
			if(count == 1){
				len += 1;
			}else{
				int numCount = numCount(count);
				len += numCount + 1;
				int k = j + numCount - 1;
				while(count > 0){
					chars[j + numCount - 1] = (char) ('0' + (count % 10));
					count /= 10;
					numCount--;
				}
				j = k; j++;
				count = 1;
			}
		}
		return len;
	}
	private int numCount(int x){
		int count = 0;
		while(x > 0){
			count++;
			x /= 10;
		}
		return count;
	}
	
	public static void main(String[] args) {
		StringCompression compression = new StringCompression();
//		System.out.println(compression.compress(new char[]{'a', 'a', 'b','b','b','b','b','b','b','b','b','b','b','b'}));
		System.out.println(compression.compress(new char[]{'a', 'a'}));
//		System.out.println(compression.compress(new char[]{'a'}));
//		System.out.println(compression.compress(new char[]{'a', 'b', 'b', 'a'}));
//		System.out.println(compression.compress(new char[]{'z', 'z', 'j'}));
//		System.out.println(compression.compress(new char[]{'a','a','a','b','b','a','a'}));
	}

}
