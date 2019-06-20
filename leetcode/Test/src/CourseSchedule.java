import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class CourseSchedule {

	public boolean canFinish(int numCourses, int[][] prerequisites) {
		if(numCourses == 1 || prerequisites.length == 0) return true;
		int[] inDegree = new int[numCourses];
		for(int i = 0; i < prerequisites.length; i++){
			inDegree[prerequisites[i][1]]++;
		}
		Queue<Integer> queue = new LinkedList<>();
		Set<Integer> map = new HashSet<>();
		boolean flag = false;
		for(int i = 0; i < numCourses; i++){
			if(inDegree[i] == 0){
				queue.offer(i);
				map.add(i);
				flag = true;
			}
		}
		if(!flag) return false;
		while(!queue.isEmpty()){
			int size = queue.size();
			for(int i = 0; i < size; i++){
				int node = queue.poll();
				for(int j = 0; j < prerequisites.length; j++){
					if(prerequisites[j][0] == node){
						inDegree[prerequisites[j][1]]--;
					}
				}
				for(int j = 0; j < numCourses; j++){
					if(!map.contains(j) && inDegree[j] == 0){
						queue.offer(j);
						map.add(j);
					}
				}
			}
		}
		for(int j = 0; j < numCourses; j++)
			if(inDegree[j] != 0) return false;
		return true;
	}
	
	public static void main(String[] args) {
		CourseSchedule course = new CourseSchedule();
		System.out.println(course.canFinish(2, new int[][]{{1,0},{0,1}}));
		System.out.println(course.canFinish(2, new int[][]{{1,0}}));
		System.out.println(course.canFinish(3, new int[][]{{1,0}}));
		System.out.println(course.canFinish(3, new int[][]{{1,0},{2,0}}));
	}

}
