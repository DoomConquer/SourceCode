
public class MaximumProductofWordLengths {

	public int maxProduct(String[] words) {
		if(words == null || words.length == 0) return 0;
		int len = words.length;
		int[] bits = new int[len];
		for(int i = 0; i < len; i++){
			String word = words[i];
			for(char ch : word.toCharArray()){
				bits[i] |= (1 << ch - 'a');
			}
		}
		int product = 0;
		for(int i = 0; i < len; i++){
			for(int j = i; j < len; j++){
				if((bits[i] & bits[j]) == 0){
					int currProduct = words[i].length() * words[j].length();
					if(product < currProduct) product = currProduct;
				}
			}
		}
		return product;
	}
	
	public static void main(String[] args) {
		MaximumProductofWordLengths max= new MaximumProductofWordLengths();
		System.out.println(max.maxProduct(new String[]{"abcw", "baz", "foo", "bar", "xtfn", "abcdef"}));
		System.out.println(max.maxProduct(new String[]{"a", "ab", "abc", "d", "cd", "bcd", "abcd"}));
		System.out.println(max.maxProduct(new String[]{"a", "aa", "aaa", "aaaa"}));
	}

}
