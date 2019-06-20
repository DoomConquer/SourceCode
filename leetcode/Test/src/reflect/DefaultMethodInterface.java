package reflect;

public interface DefaultMethodInterface {
	default String print(){
		System.out.println("hello");
		return "hello";
	}
	
	void test();
}
