
public class LinkedListCycle {

	public boolean hasCycle(ListNode head) {
		ListNode p = head;
		while(p != null && p.next != null){
			head = head.next;
			p = p.next.next;
			if(head == p) return true;
		}
		return false;
	}
	
	public static void main(String[] args) {
		LinkedListCycle cycle = new LinkedListCycle();
		ListNode head = new ListNode(2);
		ListNode l1 = new ListNode(1);
		ListNode l2 = new ListNode(1);
		ListNode l3 = new ListNode(1);
		head.next = l1;
		l1.next = l2;
		l2.next = l3;
		l3.next = l2;
		System.out.println(cycle.hasCycle(head));
	}

}
