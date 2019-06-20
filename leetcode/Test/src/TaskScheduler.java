import java.util.Arrays;

public class TaskScheduler {

	public int leastInterval(char[] tasks, int n) {
		int[] count = new int[26];
		for(char ch : tasks){
			count[ch - 'A']++;
		}
		Arrays.sort(count);
		int num = count.length - 1;
		while(num >= 0)
			if(count[num] == count[count.length - 1]) num--;
			else break;
		return Math.max(tasks.length, (count[count.length - 1] - 1) * (n + 1) + count.length - 1 - num);
		
	}
	
	public static void main(String[] args) {
		TaskScheduler task = new TaskScheduler();
		System.out.println(task.leastInterval(new char[]{'A','A','A','B','B','B'}, 2));
	}

}
