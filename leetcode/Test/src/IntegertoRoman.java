
public class IntegertoRoman {

	public String intToRoman(int num) {
		int[] dict = new int[]{1000,900,500,400,100,90,50,40,10,9,5,4,1};
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < dict.length && num > 0;){
			if(num >= dict[i]){
				sb.append(convert(dict[i]));
				num -= dict[i];
			}else{
				i++;
			}
		}
		return sb.toString();
	}
	private String convert(int n){
		switch(n){
			case 1:
				return "I";
			case 4:
				return "IV";
			case 5:
				return "V";
			case 9:
				return "IX";
			case 10:
				return "X";
			case 40:
				return "XL";
			case 50:
				return "L";
			case 90:
				return "XC";
			case 100:
				return "C";
			case 400:
				return "CD";
			case 500:
				return "D";
			case 900:
				return "CM";
			case 1000:
				return "M";
		}
		return "";
	}
	
	public static void main(String[] args) {
		IntegertoRoman roman = new IntegertoRoman();
		System.out.println(roman.intToRoman(18));
	}

}
