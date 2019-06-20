
public class Base7 {

	public String convertToBase7(int num) {
		int res = 0;
		int base[] = new int[]{1, 7, 49, 343, 2401, 16807, 117649, 823543, 5764801, 40353607, 282475249, 1977326743};
		for(int i = base.length - 1; i >= 0; i--){
			res = res * 10 + num / base[i];
			num %= base[i];
		}
		return String.valueOf(res);
	}
	
	public static void main(String[] args) {
		Base7 base = new Base7();
		System.out.println(base.convertToBase7(100));
		System.out.println(base.convertToBase7(-7));
		System.out.println(base.convertToBase7(7));
		System.out.println(base.convertToBase7(5));
	}

}
