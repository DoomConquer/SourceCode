
public class RotateList {

	public ListNode rotateRight(ListNode head, int k) {
		if(head == null) return head;
		int len = 0;
		ListNode p = head;
		while(p != null){
			p = p.next;
			len++;
		}
		if(k >= len) k %= len;
		p = head;
		ListNode q = head;
		if(k == 0) return head;
		while(q.next != null){
			q = q.next;
			if(k <= 0) { p = p.next; }
			k--;
		}
		q.next = head;
		head = p.next;
		p.next = null;
		return head;
	}
	
	public static void main(String[] args) {
		RotateList rotate = new RotateList();
		ListNode head = new ListNode(1);
		ListNode node1 = new ListNode(2);
		ListNode node2 = new ListNode(3);
		ListNode node3 = new ListNode(4);
		head.next = node1;
		node1.next = node2;
		node2.next = node3;
		ListNode node = rotate.rotateRight(head, 5);
		while(node != null){
			System.out.print(node.val + "  ");
			node = node.next;
		}
	}

}
