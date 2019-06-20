
public class ExcelSheetColumnNumber {

	public int titleToNumber(String s) {
		int res = 0;
		int base = 1;
		for(int i = s.length() - 1; i >= 0; i--){
			res += (s.charAt(i) - 'A' + 1) * base;
			base *= 26;
		}
		return res;
	}
	
	public static void main(String[] args) {
		ExcelSheetColumnNumber excel = new ExcelSheetColumnNumber();
		System.out.println(excel.titleToNumber("ZY"));
		System.out.println(excel.titleToNumber("ZZ"));
		System.out.println(excel.titleToNumber("A"));
		System.out.println(excel.titleToNumber("AB"));
		System.out.println(excel.titleToNumber("ABC"));
		System.out.println(excel.titleToNumber("AAA"));
	}

}
