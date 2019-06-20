package toutiao;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

public class BinaryTreeZigzagLevelOrderTraversal {

	// 可以使用stack
    public List<List<Integer>> zigzagLevelOrder(TreeNode root) {
        Deque<TreeNode> queue = new LinkedList<>();
        Deque<TreeNode> temp = new LinkedList<>();
        List<List<Integer>> res = new ArrayList<>();
        if(root == null) return res;
        queue.addLast(root);
        int level = 0;
        while(!queue.isEmpty()){
        	level++;
        	List<Integer> list = new ArrayList<>();
        	while(!queue.isEmpty()){
        		if(level % 2 == 1){
        			TreeNode node = queue.pollLast();
        			list.add(node.val);
        			if(node.left != null) temp.addLast(node.left);
        			if(node.right != null) temp.addLast(node.right);
        		}else{
        			TreeNode node = queue.pollLast();
        			list.add(node.val);
        			if(node.right != null) temp.addLast(node.right);
        			if(node.left != null) temp.addLast(node.left);
        		}
        	}
        	Deque<TreeNode> deque = queue;
        	queue = temp;
        	temp = deque;
        	res.add(list);
        }
        return res;
    }
    
	public static void main(String[] args) {
		BinaryTreeZigzagLevelOrderTraversal tree = new BinaryTreeZigzagLevelOrderTraversal();
		TreeNode root = new TreeNode(0);
		TreeNode node1 = new TreeNode(1);
		TreeNode node2 = new TreeNode(2);
		TreeNode node3 = new TreeNode(3);
		TreeNode node4 = new TreeNode(4);
		TreeNode node5 = new TreeNode(5);
		TreeNode node6 = new TreeNode(6);
		root.left = node1;
		root.right = node2;
		node1.left = node3;
		node1.right = node4;
		node2.left = node5;
		node2.right = node6;
		System.out.println(tree.zigzagLevelOrder(root));
	}

}
