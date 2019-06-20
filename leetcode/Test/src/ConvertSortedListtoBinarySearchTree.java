
public class ConvertSortedListtoBinarySearchTree {

	public TreeNode sortedListToBST(ListNode head) {
		if(head == null) return null;
		int len = 0;
		ListNode node = head;
		while(node != null){
			len++;
			node = node.next;
		}
		int i = 0;
		int[] nums = new int[len];
		node = head;
		while(node != null){
			nums[i++] = node.val;
			node= node.next;
		}
		return construct(nums, 0, len - 1);
	}
	private TreeNode construct(int[] nums, int left, int right){
		if(left > right) return null;
		int mid = (left + right) >>> 1;
		TreeNode node = new TreeNode(nums[mid]);
		node.left = construct(nums, left, mid - 1);
		node.right = construct(nums, mid + 1, right);
		return node;
	}
	
	public static void main(String[] args) {
		ConvertSortedListtoBinarySearchTree convert = new ConvertSortedListtoBinarySearchTree();
		ListNode head = new ListNode(2);
		ListNode l1 = new ListNode(5);
		ListNode l2 = new ListNode(6);
		ListNode l3 = new ListNode(10);
		head.next = l1;
		l1.next = l2;
		l2.next = l3;
		convert.sortedListToBST(head);
	}

}
