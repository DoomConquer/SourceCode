package reflect;

public class Test {
	public static void main(String[] args) {
		ProxyFactory<DefaultMethodInterface> factory = new ProxyFactory<>(DefaultMethodInterface.class);
		DefaultMethodInterface method = factory.newInstance();
		//method.test();
		System.out.println(method.print());
//		method.hashCode();
//		System.out.println(method.toString());
	}
	
}
