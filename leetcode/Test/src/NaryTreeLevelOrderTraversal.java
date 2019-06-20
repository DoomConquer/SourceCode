import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class NaryTreeLevelOrderTraversal {

	class Node {
	    public int val;
	    public List<Node> children;

	    public Node() {}

	    public Node(int _val,List<Node> _children) {
	        val = _val;
	        children = _children;
	    }
	};
    public List<List<Integer>> levelOrder(Node root) {
        if(root == null) return Collections.emptyList();
        List<List<Integer>> res = new ArrayList<>();
        Queue<Node> queue = new LinkedList<>();
        queue.offer(root);
        while(!queue.isEmpty()){
        	int size = queue.size();
        	List<Integer> list = new ArrayList<>();
        	while(size-- > 0){
        		Node node = queue.poll();
        		list.add(node.val);
        		if(node.children != null){
        			for(Node child : node.children) queue.offer(child);
        		}
        	}
        	res.add(list);
        }
        return res;
    }
    
	public static void main(String[] args) {
		NaryTreeLevelOrderTraversal naryTreeLevelOrderTraversal = new NaryTreeLevelOrderTraversal();
		Node node5 = naryTreeLevelOrderTraversal.new Node(6, null);
		Node node4 = naryTreeLevelOrderTraversal.new Node(5, null);
		Node node3 = naryTreeLevelOrderTraversal.new Node(4, null);
		Node node2 = naryTreeLevelOrderTraversal.new Node(2, null);
		Node node1 = naryTreeLevelOrderTraversal.new Node(3, Arrays.asList(node4, node5));
		Node root = naryTreeLevelOrderTraversal.new Node(1, Arrays.asList(node1, node2, node3));
		System.out.println(naryTreeLevelOrderTraversal.levelOrder(root));
	}

}
