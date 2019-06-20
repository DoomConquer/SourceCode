
public class ExcelSheetColumnTitle {

	public String convertToTitle(int n) {
		StringBuilder sb = new StringBuilder();
		while(n > 0){
			int base = n % 26 == 0 ? 26 : n % 26;
			if(base == 26) n--;
			sb.append((char)('A' - 1 + base));
			n /= 26;
		}
		return sb.reverse().toString();
	}
	
	public static void main(String[] args) {
		ExcelSheetColumnTitle excel = new ExcelSheetColumnTitle();
		System.out.println(excel.convertToTitle(100));
		System.out.println(excel.convertToTitle(1));
		System.out.println(excel.convertToTitle(731));
		System.out.println(excel.convertToTitle(701));
		System.out.println(excel.convertToTitle(28));
		System.out.println(excel.convertToTitle(52));
	}

}
