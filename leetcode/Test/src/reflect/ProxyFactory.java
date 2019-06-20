package reflect;

import java.lang.reflect.Proxy;

public class ProxyFactory<T> {
	private Class<?> mapperInterface;
	public ProxyFactory(Class<T> mapperInterface){
		this.mapperInterface = mapperInterface;
	}
	
	@SuppressWarnings("unchecked")
	public T newInstance(){
		return (T) Proxy.newProxyInstance(mapperInterface.getClassLoader(), new Class[]{mapperInterface}, new ProxyHandler());
	}
}
