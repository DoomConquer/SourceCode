import java.util.Arrays;

public class AssignCookies {

	public int findContentChildren(int[] g, int[] s) {
		Arrays.sort(g);
		Arrays.sort(s);
		int i = 0;
		for(int index = 0; index < s.length && i < g.length; index++){
			if(s[index] >= g[i])
				i++;
		}
		return i;
	}
	
	public static void main(String[] args) {
		AssignCookies cookies = new AssignCookies();
		System.out.println(cookies.findContentChildren(new int[]{1,2}, new int[]{}));
	}

}
