import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class BusRoutes {

	public int numBusesToDestination(int[][] routes, int S, int T) {
		if(S == T) return 0;
		if(routes == null || routes.length == 0) return -1;
		Map<Integer, Set<Integer>> stationMap = new HashMap<>();
		Map<Integer, List<Integer>> busMap = new HashMap<>();
		int n = routes.length;
		for(int i = 0; i < n; i++){
			for(int j = 0; j < routes[i].length; j++){
				Set<Integer> tempSet = stationMap.getOrDefault(routes[i][j], new HashSet<>());
				tempSet.add(i);
				stationMap.put(routes[i][j], tempSet);
				List<Integer> tempList = busMap.getOrDefault(i, new ArrayList<>());
				tempList.add(routes[i][j]);
				busMap.put(i, tempList);
			}
		}
		Queue<Integer> queue = new LinkedList<>();
		Set<Integer> set = new HashSet<>();
		queue.offer(S);
		set.add(S);
		int count = 0;
		while(!queue.isEmpty()){
			count++;
			int size = queue.size();
			for(int i = 0; i < size; i++){
				int curr = queue.poll();
				Set<Integer> bus = stationMap.get(curr);
				if(bus != null){
					Iterator<Integer> iter = bus.iterator();
					while(iter.hasNext()){
						int iBus = iter.next();
						for(int k = 0; k < routes[iBus].length; k++){
							if(!set.contains(routes[iBus][k])){
								if(busMap.get(iBus).contains(T)) return count;
								queue.offer(routes[iBus][k]);
								set.add(routes[iBus][k]);
							}
						}
					}
				}
			}
		}
		return -1;
	}
	
	public static void main(String[] args) {
		BusRoutes bus = new BusRoutes();
		System.out.println(bus.numBusesToDestination(new int[][]{{1,2,7},{3,6,7}}, 1, 6));
		System.out.println(bus.numBusesToDestination(new int[][]{{1,7},{3,5},{5,5}}, 5, 5));
		System.out.println(bus.numBusesToDestination(new int[][]{{2},{2,8}}, 8, 2));
	}

}
