package toutiao;

public class LinkedListCycleII {

    public ListNode detectCycle(ListNode head) {
        ListNode slow = head, fast = head;
        while(fast != null && fast.next != null){
        	slow = slow.next;
        	fast = fast.next.next;
        	if(slow == fast) break;
        }
        if(fast == null || fast.next == null) return null;
        slow = head;
        while(slow != fast){
        	slow = slow.next;
        	fast = fast.next;
        }
        return slow;
    }
    
	public static void main(String[] args) {
		LinkedListCycleII cycle = new LinkedListCycleII();
		ListNode head = new ListNode(2);
		ListNode l1 = new ListNode(3);
		ListNode l2 = new ListNode(5);
		ListNode l3 = new ListNode(1);
		head.next = l1;
		l1.next = l2;
		l2.next = l3;
		l3.next = l2;
		ListNode node = cycle.detectCycle(head);
		if(node != null)
			System.out.println(node.val);
	}

}
