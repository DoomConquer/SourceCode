import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class EmployeeImportance {

	class Employee {
	    public int id;
	    public int importance;
	    public List<Integer> subordinates;
	}
	public int getImportance(List<Employee> employees, int id) {
		Map<Integer, Employee> map = new HashMap<Integer, Employee>();
		for(Employee em : employees)
			map.put(em.id, em);
		int sum = 0;
		Queue<Employee> queue = new LinkedList<Employee>();
		if(map.containsKey(id)){
			queue.add(map.get(id));
			while(!queue.isEmpty()){
				Employee em = queue.poll();
				sum += em.importance;
				if(em.subordinates.size() > 0){
					List<Integer> subordianates = em.subordinates;
					for(int subId : subordianates)
						queue.add(map.get(subId));
				}
			}
		}
		return sum;
	}
	
	public static void main(String[] args) {
	}

}
