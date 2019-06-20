
public class RomantoInteger {

	public int romanToInt(String s) {
		int num = 0;
		int[] dict = new int[100];
		dict['I'] = 1;
		dict['V'] = 5;
		dict['X'] = 10;
		dict['L'] = 50;
		dict['C'] = 100;
		dict['D'] = 500;
		dict['M'] = 1000;
		char[] ch = s.toCharArray();
		for(int i = 0; i < ch.length; i++){
			if(i < ch.length - 1 && (ch[i] == 'I' && (ch[i + 1] == 'V' || ch[i + 1] == 'X') 
					|| ch[i] == 'X' && (ch[i + 1] == 'L' || ch[i + 1] == 'C') 
					|| ch[i] == 'C' && (ch[i + 1] == 'D' || ch[i + 1] == 'M'))){
				num += dict[ch[i + 1]] - dict[ch[i]];
				i++;
			}else{
				num += dict[ch[i]];
			}
		}
		return num;
	}
	
	public static void main(String[] args) {
		RomantoInteger roman = new RomantoInteger();
		System.out.println(roman.romanToInt("MMMCCCXXXIII"));
	}

}
