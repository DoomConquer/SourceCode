package toutiao;
import java.util.Stack;

public class SimplifyPath {
    
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
