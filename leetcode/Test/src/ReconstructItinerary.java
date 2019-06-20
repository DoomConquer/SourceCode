import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author li_zhe
 * tickets中可能有重复的航班
 */
public class ReconstructItinerary {

	public List<String> findItinerary(String[][] tickets) {
		List<String> res = new ArrayList<>();
		if(tickets == null || tickets.length == 0) return res;
		Map<String, List<String>> map = new HashMap<>();
		Map<String, Integer> visited = new HashMap<>();
		for(String[] ticket : tickets){
			List<String> list = map.getOrDefault(ticket[0], new ArrayList<>());
			list.add(ticket[1]);
			map.put(ticket[0], list);
			String path = ticket[0] + "-" + ticket[1];
			visited.put(path, visited.getOrDefault(path, 0) + 1);
		}
		if(!map.containsKey("JFK")) return res;
		for(Map.Entry<String, List<String>> entry : map.entrySet()){
			Collections.sort(entry.getValue());
		}
		res.add("JFK");
		find(map, res, new ArrayList<>(), visited, "JFK", tickets.length + 1);
		return res;
	}
	private  boolean find(Map<String, List<String>> map, List<String> res, List<String> temp, Map<String, Integer> visited, String departure, int len){
		if(temp.size() == len - 1){
			for(String s : temp)
				res.add(s);
			return true;
		}
		if(!map.containsKey(departure)) return false;
		List<String> list = map.get(departure);
		for(int i = 0; i < list.size(); i++){
			String arrival = list.get(i);
			String path = departure + "-" + arrival;
			if(visited.containsKey(path) && visited.get(path) > 0){
				temp.add(arrival);
				visited.put(path, visited.get(path) - 1);
				if(find(map, res, temp, visited, arrival, len)) return true;
				temp.remove(temp.size() - 1);
				visited.put(path, visited.get(path) + 1);
			}
		}
		return false;
	}
	
	public static void main(String[] args) {
		ReconstructItinerary reconstruct = new ReconstructItinerary();
		for(String s : reconstruct.findItinerary(new String[][]{{"JFK","SFO"},{"JFK","ATL"},{"SFO","ATL"},{"ATL","JFK"},{"ATL","SFO"}})){
			System.out.print(s + "  ");
		}
		System.out.println();
		for(String s : reconstruct.findItinerary(new String[][]{{"MUC", "LHR"}, {"JFK", "MUC"}, {"SFO", "SJC"}, {"LHR", "SFO"}})){
			System.out.print(s + "  ");
		}
		System.out.println();
		for(String s : reconstruct.findItinerary(new String[][]{{"JFK","KUL"},{"JFK","NRT"},{"NRT","JFK"}})){
			System.out.print(s + "  ");
		}
		System.out.println();
		for(String s : reconstruct.findItinerary(new String[][]{{"EZE","AXA"},{"TIA","ANU"},{"ANU","JFK"},{"JFK","ANU"},{"ANU","EZE"},{"TIA","ANU"},{"AXA","TIA"},{"TIA","JFK"},{"ANU","TIA"},{"JFK","TIA"}})){
			System.out.print(s + "  ");
		}
		System.out.println();
		for(String s : reconstruct.findItinerary(new String[][]{{"EZE","TIA"},{"EZE","HBA"},{"AXA","TIA"},{"JFK","AXA"},{"ANU","JFK"},{"ADL","ANU"},{"TIA","AUA"},{"ANU","AUA"},{"ADL","EZE"},{"ADL","EZE"},{"EZE","ADL"},{"AXA","EZE"},{"AUA","AXA"},{"JFK","AXA"},{"AXA","AUA"},{"AUA","ADL"},{"ANU","EZE"},{"TIA","ADL"},{"EZE","ANU"},{"AUA","ANU"}})){
			System.out.print(s + "  ");
		}
		System.out.println();
		for(String s : reconstruct.findItinerary(new String[][]{{"JFK","KUL"},{"KUL","ABC"},{"JFK","NRT"},{"NRT","JFK"}})){
			System.out.print(s + "  ");
		}
	}

}
