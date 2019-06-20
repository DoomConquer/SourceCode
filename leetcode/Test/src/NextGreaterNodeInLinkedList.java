import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

// Ë¼Â·²Î¿¼leetcode
public class NextGreaterNodeInLinkedList {

    public int[] nextLargerNodes(ListNode head) {
       Stack<Integer> stack = new Stack<>();
       List<Integer> nums = new ArrayList<>();
       while(head != null){
    	   nums.add(head.val);
    	   head = head.next;
       }
       int[] res = new int[nums.size()];
       for(int i = 0; i < nums.size(); i++){
    	   while(!stack.isEmpty() && nums.get(i) > nums.get(stack.peek())){
    		   res[stack.pop()] = nums.get(i);
    	   }
    	   stack.push(i);
       }
       return res;
    }
    
	public static void main(String[] args) {
		NextGreaterNodeInLinkedList nextGreaterNodeInLinkedList = new NextGreaterNodeInLinkedList();
		ListNode head = new ListNode(2);
		ListNode node1 = new ListNode(7);
		ListNode node2 = new ListNode(4);
		ListNode node3 = new ListNode(3);
		ListNode node4 = new ListNode(5);
		head.next = node1;
		node1.next = node2;
		node2.next = node3;
		node3.next = node4;
		int[] res = nextGreaterNodeInLinkedList.nextLargerNodes(head);
		for(int num : res) System.out.print(num + " "); System.out.println();
		
		head = new ListNode(1);
		node1 = new ListNode(7);
		node2 = new ListNode(5);
		node3 = new ListNode(1);
		node4 = new ListNode(9);
		ListNode node5 = new ListNode(2);
		ListNode node6 = new ListNode(5);
		ListNode node7 = new ListNode(1);
		head.next = node1;
		node1.next = node2;
		node2.next = node3;
		node3.next = node4;
		node4.next = node5;
		node5.next = node6;
		node6.next = node7;
		res = nextGreaterNodeInLinkedList.nextLargerNodes(head);
		for(int num : res) System.out.print(num + " "); System.out.println();
		
		head = new ListNode(5);
		node1 = new ListNode(5);
		node2 = new ListNode(6);
		head.next = node1;
		node1.next = node2;
		res = nextGreaterNodeInLinkedList.nextLargerNodes(head);
		for(int num : res) System.out.print(num + " "); System.out.println();
	}

}
