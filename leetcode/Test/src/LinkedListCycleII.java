public class LinkedListCycleII {

	public ListNode detectCycle(ListNode head) {
		ListNode p = head, q = head;
		boolean isExsit = false;
		while(p != null && p.next != null){
			q = q.next;
			p = p.next.next;
			if(q == p) { isExsit = true; break; }
		}
		if(isExsit){
			p = head;
			while(p != q){
				p = p.next;
				q = q.next;
			}
			return p;
		}
		return null;
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
		//l3.next = head;
		ListNode node = cycle.detectCycle(head);
		if(node != null)
			System.out.println(node.val);
	}

}
