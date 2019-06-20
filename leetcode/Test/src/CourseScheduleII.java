import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class CourseScheduleII {

	public int[] findOrder(int numCourses, int[][] prerequisites) {
		int[] res = new int[numCourses];
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
		if(!flag) return new int[]{};
		int count = 0;
		while(!queue.isEmpty()){
			int size = queue.size();
			for(int i = 0; i < size; i++){
				int node = queue.poll();
				count++;
				res[numCourses - count] = node;
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
			if(inDegree[j] != 0) return new int[]{};
		return res;
	}
	
	public static void main(String[] args) {
		CourseScheduleII course = new CourseScheduleII();
		int[] res = course.findOrder(2, new int[][]{});
		//int[] res = course.findOrder(2, new int[][]{{0,1},{1,0}});
		for(int num : res)
			System.out.print(num + "  ");
	}

}
