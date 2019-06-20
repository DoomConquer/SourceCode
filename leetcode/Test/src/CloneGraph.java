import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CloneGraph {

	private Map<Integer, UndirectedGraphNode> map = new HashMap<>();
	public UndirectedGraphNode cloneGraph(UndirectedGraphNode node) {
		if(node == null) return null;
		if(map.containsKey(node.label)) return map.get(node.label);
		UndirectedGraphNode clone = new UndirectedGraphNode(node.label);
		map.put(node.label, clone);
		if(node.neighbors != null){
			List<UndirectedGraphNode> neighbors = node.neighbors;
			for(UndirectedGraphNode neighbor : neighbors){
				clone.neighbors.add(cloneGraph(neighbor));
			}
		}
		return clone;
	}

}
class UndirectedGraphNode {
     int label;
     List<UndirectedGraphNode> neighbors;
     UndirectedGraphNode(int x) { label = x; neighbors = new ArrayList<UndirectedGraphNode>(); }
}