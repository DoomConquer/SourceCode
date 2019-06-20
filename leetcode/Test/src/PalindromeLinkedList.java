
public class PalindromeLinkedList {

	public boolean isPalindrome(ListNode head) {
		ListNode node = head;
		if(head == null || head.next == null) return true;
		int len = 0;
		while(node != null){
			len++;
			node = node.next;
		}
		node = head;
		ListNode half = null;
		int num = 0;
		boolean flag = true;
		while(node != null){
			num++;
			if(num <= len / 2){
				ListNode next = node.next;
				node.next = half;
				half = node;
				node = next;
			}else{
				if(len % 2 == 1 && flag) {
					flag = false;
					node = node.next;
				}
				if(half.val == node.val){
					half = half.next;
					node = node.next;
				}else return false;
			}
		}
		return true;
	}
	
	public static void main(String[] args) {
		PalindromeLinkedList linkedList = new PalindromeLinkedList();
		ListNode head = new ListNode(1);
		ListNode node = new ListNode(2);
		ListNode node1 = new ListNode(2);
		ListNode node2 = new ListNode(1);
		head.next = node;
		node.next = node1;
		node1.next = node2;
		System.out.println(linkedList.isPalindrome(head));
	}

}

class ListNode {
    int val;
    ListNode next;
    ListNode(int x) { val = x; }
}
