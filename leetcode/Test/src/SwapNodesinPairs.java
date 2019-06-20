
public class SwapNodesinPairs {

	public ListNode swapPairs(ListNode head) {
		if(head == null || head.next == null) return head;
		ListNode node = new ListNode(0);
		ListNode pre = node, first = head, second = head.next;
		while(second != null){
			pre.next = second;
			first.next = second.next;
			second.next = first;
			pre = first;
			first = first.next;
			if(first != null) second = first.next;
			else second = null;
		}
		return node.next;
	}
	
	public static void main(String[] args) {
		SwapNodesinPairs swap = new SwapNodesinPairs();
		ListNode head = new ListNode(1);
		ListNode node1 = new ListNode(2);
		ListNode node2 = new ListNode(3);
		ListNode node3 = new ListNode(4);
		ListNode node4 = new ListNode(5);
		head.next = node1;
		node1.next = node2;
		node2.next = node3;
		node3.next = node4;
		ListNode res = swap.swapPairs(head);
		while(res != null){
			System.out.print(res.val + "  ");
			res = res.next;
		}
	}

}
