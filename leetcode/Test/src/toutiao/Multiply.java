package toutiao;

public class Multiply {
	
    public String multiply(String num1, String num2) {
        int n1 = num1.length(), n2 = num2.length();
        int[] res = new int[n1 + n2];
        int carry = 0, carry1 = 0, base = 0;
        for(int i = num1.length() - 1; i >= 0; i--){
        	int f1 = num1.charAt(i) - '0';
        	int index = base;
        	for(int j = num2.length() - 1; j >= 0; j--){
        		int f2 = num2.charAt(j) - '0';
        		int value = (f1 * f2 + carry1) % 10;
        		carry1 = (f1 * f2 + carry1) / 10;
        		int newCarry = (res[index] + value + carry) / 10;
        		res[index] = (res[index] + value + carry) % 10;
        		carry = newCarry;
        		index++;
        	}
        	if(carry1 != 0){
        		int newCarry = (res[index] + carry1 + carry) / 10;
        		res[index] = (res[index] + carry1 + carry) % 10;
        		carry = newCarry;
        		carry1 = 0;
        		index++;
        	}
        	while(carry != 0){
        		int newCarry = (res[index] + carry) / 10;
        		res[index] = (res[index] + carry) % 10;
        		carry = newCarry; index++;
        	}
        	base++;
        }
        boolean first = true;
        StringBuilder sb = new StringBuilder();
        for(int i = n1 + n2 - 1; i >= 0; i--){
        	if(res[i] != 0) first = false;
        	if(first) continue;
        	sb.append(res[i]);
        }
        return sb.length() == 0 ? "0" : sb.toString();
    }
    
    public static void main(String[] args) {
    	Multiply multiply = new Multiply();
    	System.out.println(multiply.multiply("2222", "5"));
    	System.out.println(multiply.multiply("2", "3"));
    	System.out.println(multiply.multiply("1", "456"));
    	System.out.println(multiply.multiply("123", "456"));
    	System.out.println(multiply.multiply("999", "999"));
    	System.out.println(multiply.multiply("999", "1"));
    	System.out.println(multiply.multiply("999", "0"));
    	System.out.println(multiply.multiply("9999999999999", "2"));
    	System.out.println(multiply.multiply("999", "100000000000000000"));
	}
}
