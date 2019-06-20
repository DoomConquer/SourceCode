import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class OpentheLock {

	public int openLock(String[] deadends, String target) {
		if (target.isEmpty())
			return -1;
		Set<String> set = new HashSet<>();
		for (String s : deadends)
			set.add(s);
		if(set.contains("0000")) return -1;
		Queue<String> queue = new LinkedList<>();
		queue.offer("0000");
		set.add("0000");
		int res = -1;
		while(!queue.isEmpty()){
			res++;
			int size = queue.size();
			for(int i = 0; i < size; i++){
				String curr = queue.poll();
				if(target.equals(curr)) return res;
				char[] ch = curr.toCharArray();
				for(int k = 0; k < 4; k++){
					char temp = ch[k];
					ch[k] = (char)(ch[k] + 1) > '9' ? '0' : (char)(ch[k] + 1);
					String newLock = new String(ch);
					if(!set.contains(newLock)){
						queue.offer(newLock);
						set.add(newLock);
					}
					ch[k] = temp;
					temp = ch[k];
					ch[k] = (char)(ch[k] - 1) < '0' ? '9' : (char)(ch[k] - 1);
					newLock = new String(ch);
					if(!set.contains(newLock)){
						queue.offer(newLock);
						set.add(newLock);
					}
					ch[k] = temp;
				}
			}
		}
		return -1;
	}

	public static void main(String[] args) {
		OpentheLock lock = new OpentheLock();
		System.out.println(lock.openLock(new String[] { "0201", "0101", "0102", "1212", "2002" }, "0202"));
		System.out.println(lock.openLock(new String[] { "8888" }, "0009"));
		System.out.println(lock.openLock(new String[] { "8887","8889","8878","8898","8788","8988","7888","9888" }, "8888"));
		System.out.println(lock.openLock(new String[] { "0000" }, "8888"));
	}

}
