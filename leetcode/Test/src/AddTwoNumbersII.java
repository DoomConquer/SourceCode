import java.util.Stack;

public class AddTwoNumbersII {

	public ListNode addTwoNumbers(ListNode l1, ListNode l2) {
		Stack<Integer> stack1 = new Stack<>();
		Stack<Integer> stack2 = new Stack<>();
		while(l1 != null){
			stack1.push(l1.val);
			l1 = l1.next;
		}
		while(l2 != null){
			stack2.push(l2.val);
			l2 = l2.next;
		}
		int carry = 0;
		ListNode l3 = new ListNode(0);
		while(!stack1.isEmpty() || !stack2.isEmpty()){
			int sum = (stack1.isEmpty() ? 0 : stack1.pop()) + (stack2.isEmpty() ? 0 : stack2.pop()) + carry;
			carry = sum / 10;
			ListNode node = new ListNode(sum % 10);
			ListNode temp = l3.next;
			l3.next = node;
			l3.next.next = temp;
		}
		if(carry > 0){
			ListNode node = new ListNode(carry);
			ListNode temp = l3.next;
			l3.next = node;
			l3.next.next = temp;
		}
		return l3.next;
	}
	
	public static void main(String[] args) {
		AddTwoNumbersII add = new AddTwoNumbersII();
		ListNode l1 = new ListNode(6);
		ListNode l2 = new ListNode(9);
		ListNode l3 = new ListNode(4);
		l2.next = l3;
		ListNode res = add.addTwoNumbers(l1, l2);
		while(res != null){
			System.out.print(res.val + "  ");
			res = res.next;
		}
	}

}
