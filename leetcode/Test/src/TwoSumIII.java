import java.util.ArrayList;
import java.util.List;

public class TwoSumIII {

	List<Integer> list = new ArrayList<Integer>();
	public boolean findTarget(TreeNode root, int k) {
		preOrder(root);
		Integer[] numbers = new Integer[list.size()]; 
		list.toArray(numbers);
		for(int left = 0, right = numbers.length - 1;left < right;){
			if(numbers[left] + numbers[right] == k){
				return true;
			}else if(numbers[left] + numbers[right] < k) left++;
			else right--;
		}
		return false;
	}
	private void preOrder(TreeNode root){
		if(root == null) return;
		preOrder(root.left);
		list.add(root.val);
		preOrder(root.right);
	}
	
	public static void main(String[] args) {
		TwoSumIII sum = new TwoSumIII();
		TreeNode root = new TreeNode(1);
		TreeNode node1 = new TreeNode(2);
		TreeNode node2 = new TreeNode(4);
		TreeNode node3 = new TreeNode(7);
		root.left = node1;
		root.right = node2;
		node2.right = node3;
		System.out.println(sum.findTarget(root, 8));
	}

}
