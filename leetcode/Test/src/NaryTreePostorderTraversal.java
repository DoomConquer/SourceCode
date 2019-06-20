import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NaryTreePostorderTraversal {

	class Node {
	    public int val;
	    public List<Node> children;

	    public Node(){}

	    public Node(int _val, List<Node> _children) {
	        val = _val;
	        children = _children;
	    }
	}
    public List<Integer> postorder(Node root) {
    	List<Integer> list = new ArrayList<>();
    	postorder(root, list);
    	return list;
    }
    private void postorder(Node root, List<Integer> list){
    	if(root == null) return;
    	if(root.children != null){
    		for(Node node : root.children) postorder(node, list);
    	}
    	list.add(root.val);
    }
    
	public static void main(String[] args) {
		NaryTreePostorderTraversal naryTreePostorderTraversal = new NaryTreePostorderTraversal();
		Node node5 = naryTreePostorderTraversal.new Node(6, null);
		Node node4 = naryTreePostorderTraversal.new Node(5, null);
		Node node3 = naryTreePostorderTraversal.new Node(4, null);
		Node node2 = naryTreePostorderTraversal.new Node(2, null);
		Node node1 = naryTreePostorderTraversal.new Node(3, Arrays.asList(node4, node5));
		Node root = naryTreePostorderTraversal.new Node(1, Arrays.asList(node1, node2, node3));
		System.out.println(naryTreePostorderTraversal.postorder(root));
	}

}
