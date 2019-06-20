import java.util.Random;

public class LinkedListRandomNode {

	public static void main(String[] args) {
		ListNode head = new ListNode(1);
		ListNode node1 = new ListNode(2);
		ListNode node2 = new ListNode(3);
		ListNode node3 = new ListNode(4);
		head.next = node1;
		node1.next = node2;
		node2.next = node3;
		Solution sol = new Solution(head);
		System.out.println(sol.getRandom());
		System.out.println(sol.getRandom());
		System.out.println(sol.getRandom());
		System.out.println(sol.getRandom());
		System.out.println(sol.getRandom());
		System.out.println(sol.getRandom());
		System.out.println(sol.getRandom());
	}

}

class Solution {

    /** @param head The linked list's head.
        Note that the head is guaranteed to be not null, so it contains at least one node. */
	private ListNode head = null;
	private Random ran = new Random();
    public Solution(ListNode head) {
        this.head = head;
    }
    
    /** Returns a random node's value. */
    public int getRandom() {
    	int res = head.val;
    	int i = 1;
    	ListNode node = head.next;
    	while(node != null){
    		i++;
    		if(ran.nextInt(i) == 0)
    			res = node.val;
    		node = node.next;
    	}
        return res;
    }
}