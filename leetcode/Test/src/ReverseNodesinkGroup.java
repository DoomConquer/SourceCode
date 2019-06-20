
public class ReverseNodesinkGroup {

	public ListNode reverseKGroup(ListNode head, int k) {
		if(k <= 1) return head;
		ListNode node = new ListNode(0);
		int len = 0;
		ListNode p = head;
		while(p != null){ len++; p = p.next; }
		if(len < k) return head;
		int times = len / k;
		p = head;
		ListNode q = node, last = node;
		while(times-- > 0){
			int step = k;
			last = p;
			while(step-- > 0){
				ListNode temp = q.next;
				q.next = p;
				p = p.next;
				q.next.next = temp;
			}
			q = last;
		}
		if(p != null) q.next = p;
		return node.next;
	}
	
	public static void main(String[] args) {
		ReverseNodesinkGroup reverse = new ReverseNodesinkGroup();
		ListNode head = new ListNode(6);
		ListNode l1 = new ListNode(9);
		ListNode l2 = new ListNode(4);
		head.next = l1;
		l1.next = l2;
		ListNode res = reverse.reverseKGroup(head, 1);
		while(res != null){
			System.out.print(res.val + "  ");
			res = res.next;
		}
	}

}
