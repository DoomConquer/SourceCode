
public class ReverseLinkedList {

	public ListNode reverseList(ListNode head) {
		ListNode node = new ListNode(0);
		ListNode p = node, temp = null;
		while(head != null){
			temp = p.next;
			p.next = head;
			head = head.next;
			p.next.next = temp;
		}
		return node.next;
	}
	
	public static void main(String[] args) {
		ReverseLinkedList reverse = new ReverseLinkedList();
		ListNode head = new ListNode(0);
		ListNode l1 = new ListNode(1);
		ListNode l2 = new ListNode(3);
		ListNode l3 = new ListNode(4);
		head.next = l1;
		l1.next = l2;
		l2.next = l3;
		ListNode res = reverse.reverseList(head);
		while(res != null){
			System.out.print(res.val + "  ");
			res = res.next;
		}
	}

}
