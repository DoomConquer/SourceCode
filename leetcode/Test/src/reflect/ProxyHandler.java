package reflect;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class ProxyHandler implements InvocationHandler{

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) {
		try {
	      if (Object.class.equals(method.getDeclaringClass())) { // 如果是Object的方法，hashcode、equals、toString等
	    	  return method.invoke(this, args);
	      } else if (method.isDefault()) { // 如果是接口的默认方法
	        return invokeDefaultMethod(proxy, method, args);
	      }else{
	    	  System.out.println(method.getName());
	      }
	    } catch (Throwable t) {
	      t.printStackTrace();
	    }
		return null;
	}

	@Override
	public String toString(){
		return "tostring";
	}
	public static String toString1(){
		return "tostring1";
	}
	
	private Object invokeDefaultMethod(Object proxy, Method method, Object[] args)
		       {
//		    final Constructor<MethodHandles.Lookup> constructor = MethodHandles.Lookup.class
//		        .getDeclaredConstructor(Class.class, int.class);
//		    if (!constructor.isAccessible()) {
//		      constructor.setAccessible(true);
//		    }
//		    final Class<?> declaringClass = method.getDeclaringClass();
//		    return constructor
//		        .newInstance(declaringClass,
//		            MethodHandles.Lookup.PRIVATE | MethodHandles.Lookup.PROTECTED
//		                | MethodHandles.Lookup.PACKAGE | MethodHandles.Lookup.PUBLIC)
//		        .unreflectSpecial(method, declaringClass).bindTo(proxy).invokeWithArguments(args);
		
//			MethodType methodType = MethodType.methodType(method.getReturnType());
//	        try {
//	            Constructor<MethodHandles.Lookup> constructor = 
//	                MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, int.class);
//	            constructor.setAccessible(true);
//	            MethodHandles.Lookup instance = constructor.newInstance(DefaultMethodInterface.class, -1);
//	            MethodHandle methodHandle = 
//	                instance.findSpecial(DefaultMethodInterface.class, method.getName(), methodType, DefaultMethodInterface.class);
//	            return methodHandle.bindTo(proxy).invokeWithArguments(args);
//	        } catch (Exception e) {
//	            e.printStackTrace();
//	        } catch (Throwable throwable) {
//	            throwable.printStackTrace();
//	        }
//	        return null;
	        
		
//			MethodType methodType = MethodType.methodType(method.getReturnType());
//	        try {
//	            Constructor<MethodHandles.Lookup> constructor = 
//	                MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, int.class);
//	            constructor.setAccessible(true);
//	            MethodHandles.Lookup instance = constructor.newInstance(DefaultMethodInterface.class, -1);
//	            MethodHandle methodHandle = 
//	                instance.findSpecial(DefaultMethodInterface.class, method.getName(), methodType, DefaultMethodInterface.class);
//	            return methodHandle.bindTo(new TestImpl(1)).invokeWithArguments(args); // 与TestImpl(1)没关系，调用接口中默认方法
//	        } catch (Exception e) {
//	            e.printStackTrace();
//	        } catch (Throwable throwable) {
//	            throwable.printStackTrace();
//	        }
//	        return null;
		
	        
	        MethodHandles.Lookup lookup = MethodHandles.lookup(); // 返回当前caller类的lookup
	        MethodHandle methodHandle;
			try {
				//methodHandle = lookup.findStatic(ProxyHandler.class, "toString1", MethodType.methodType(String.class));
				//methodHandle = lookup.findVirtual(String.class, "substring", MethodType.methodType(String.class, int.class, int.class));
				methodHandle = lookup.findVirtual(DefaultMethodInterface.class, "print", MethodType.methodType(String.class));
				//methodHandle = lookup.findVirtual(DefaultMethodInterface.class, "test", MethodType.methodType(void.class));
				//methodHandle = lookup.unreflectSpecial(method, method.getDeclaringClass());
				return methodHandle.bindTo(new TestImpl(1)).invoke();
				
//				try {
//					return MethodHandles.lookup()
//					  .in(method.getDeclaringClass())
//					  .unreflectSpecial(method, method.getDeclaringClass())
//					  .bindTo(proxy)
//					  .invokeWithArguments(args);
//				} catch (Throwable e) {
//					e.printStackTrace();
//				}
			} catch (Throwable e) {
				e.printStackTrace();
			}
	        return null;
		  }
}
class TestImpl implements DefaultMethodInterface {
	private int i = 10;
	public TestImpl(int i){
		this.i = i;
	}
	
	@Override
	public String print(){
		System.out.println("testimpl hello");
		return String.valueOf(i);
	}

	@Override
	public void test() {
		System.out.println("testImpl");
	}
	
}
