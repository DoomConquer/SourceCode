public class MiddleoftheLinkedList {

	public ListNode middleNode(ListNode head) {
		ListNode slow = head, fast = head;
        while (fast != null && fast.next != null) {
            slow = slow.next;
            fast = fast.next.next;
        }
        return slow;
    }
	
	public static void main(String[] args) {
		MiddleoftheLinkedList middleoftheLinkedList = new MiddleoftheLinkedList();
		ListNode head = new ListNode(0);
		ListNode node1 = new ListNode(1);
		ListNode node2 = new ListNode(2);
		ListNode node3 = new ListNode(3);
		ListNode node4 = new ListNode(4);
		head.next = node1;
		node1.next = node2;
		node2.next = node3;
		node3.next = node4;
		ListNode res = middleoftheLinkedList.middleNode(head);
		System.out.println(res != null ? res.val : "");
	}
}
