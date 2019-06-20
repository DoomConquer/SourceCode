import java.util.HashMap;
import java.util.Map;

public class EncodeandDecodeTinyURL {

	private Map<String, String> tinyUrlMap = new HashMap<>();
	private Map<String, String> originUrlMap = new HashMap<>();
	private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
	private static final String TINY = "http://tinyurl.com/";
	
	private int[] index = new int[6];
	private String getNextLetter(){
		index[5] += 1;
		int carry = index[5] / 62;
		for(int i = 4; i >= 0 && carry > 0; i--){
			index[i] += carry;
			index[i + 1] = 0;
			carry = index[i] / 62;
		}
		if(carry > 0) return "";
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < 6; i++){
			sb.append(ALPHABET.charAt(index[i]));
		}
		return sb.toString();
	}
	
    // Encodes a URL to a shortened URL.
    public String encode(String longUrl) {
        if(tinyUrlMap.containsKey(longUrl)) return TINY + tinyUrlMap.get(longUrl);
        String letter = getNextLetter();
        while(originUrlMap.containsKey(letter)){ letter = getNextLetter(); }
        originUrlMap.put(letter, longUrl);
        tinyUrlMap.put(longUrl, letter);
        return TINY + letter;
    }

    // Decodes a shortened URL to its original URL.
    public String decode(String shortUrl) {
        if(shortUrl.startsWith(TINY) && originUrlMap.containsKey(shortUrl.substring(TINY.length())))
        	return originUrlMap.get(shortUrl.substring(TINY.length()));
        return "";
    }
    
	public static void main(String[] args) {
		EncodeandDecodeTinyURL encodeandDecodeTinyURL = new EncodeandDecodeTinyURL();
		System.out.println(encodeandDecodeTinyURL.encode("123"));
		System.out.println(encodeandDecodeTinyURL.encode("123"));
		System.out.println(encodeandDecodeTinyURL.encode("456"));
		System.out.println(encodeandDecodeTinyURL.decode("http://tinyurl.com/aaaaac"));
		System.out.println(encodeandDecodeTinyURL.decode("http://tinyurl.com/aaaaab"));
	}

}
