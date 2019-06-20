import java.util.Stack;

public class SimplifyPath {

	// 该方法不能通过测试用例，/...期望的结果是/...
    public String simplifyPath1(String path) {
        Stack<String> stack = new Stack<>();
        int n = path.length();
        int index = 0;
        int start = -1;
        while(index < n){
        	if(path.charAt(index) == '/'){
        		if(start != -1 && start != index){
        			stack.push(path.substring(start, index));
        			start = -1;
        		}
        		index++;
	        	while(index < n && path.charAt(index) == '/') index++;
        	}else if(path.charAt(index) == '.'){
        		if(index + 1 < n && path.charAt(index + 1) == '.'){
    				if(!stack.isEmpty()) stack.pop(); 
        		}
        		index++;
        	}else{
        		if(start == -1) start = index;
        		if(index == n - 1 && start != -1 && start != index)
        			stack.push(path.substring(start));
        		index++;
        	}
        }
        StringBuilder sb = new StringBuilder();
        sb.append("/");
        while(!stack.isEmpty()){ sb.insert(1, stack.pop() + "/"); }
        if(sb.length() > 1) sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }
    
    public String simplifyPath(String path) {
        Stack<String> stack = new Stack<>();
        for(String str : path.split("/")){
        	if(str.equals("..") && !stack.isEmpty()) stack.pop();
        	else if(!str.equals("..") && !str.equals(".") && !str.equals("")) stack.push(str);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("/");
        while(!stack.isEmpty()) sb.insert(1, stack.pop() + "/");
        if(sb.length() > 1) sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }
    
	public static void main(String[] args) {
		SimplifyPath simplifyPath = new SimplifyPath();
		System.out.println(simplifyPath.simplifyPath("/..."));
		System.out.println(simplifyPath.simplifyPath("//"));
		System.out.println(simplifyPath.simplifyPath(".."));
		System.out.println(simplifyPath.simplifyPath("../."));
		System.out.println(simplifyPath.simplifyPath("../home"));
		System.out.println(simplifyPath.simplifyPath("."));
		System.out.println(simplifyPath.simplifyPath("//home"));
		System.out.println(simplifyPath.simplifyPath("home/"));
		System.out.println(simplifyPath.simplifyPath("home//../////"));
		System.out.println(simplifyPath.simplifyPath("/home/"));
		System.out.println(simplifyPath.simplifyPath("/../"));
		System.out.println(simplifyPath.simplifyPath("/home//foo/"));
		System.out.println(simplifyPath.simplifyPath("/a/./b/../../c/"));
		System.out.println(simplifyPath.simplifyPath("/a/../../b/../c//.//"));
		System.out.println(simplifyPath.simplifyPath("/a//b////c/d//././/.."));
	}

}
