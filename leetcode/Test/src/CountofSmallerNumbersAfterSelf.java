import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author li_zhe
 * BST建树过程中统计(从后向前建树,这样建好的树中的信息不用再更新)
 */
public class CountofSmallerNumbersAfterSelf {

	// BST方法
//	class BSTNode{
//		int val;
//		int leftCount = 0, dup = 1;
//		BSTNode left, right;
//		public BSTNode(int val){
//			this.val = val;
//		}
//	}
//	public List<Integer> countSmaller(int[] nums) {
//        if(nums == null || nums.length == 0) return new ArrayList<>();
//        BSTNode root = null;
//        Integer[] count = new Integer[nums.length];
//        for(int i = nums.length - 1; i >= 0; i--){
//        	root = constructBST(root, nums[i], i, count, 0);
//        }
//        return Arrays.asList(count);
//    }
//	private BSTNode constructBST(BSTNode root, int val, int index, Integer[] count, int preLeftCount){
//		if(root == null){
//			root = new BSTNode(val);
//			count[index] = preLeftCount;
//		}else if(root.val > val){
//			root.leftCount++;
//			root.left = constructBST(root.left, val, index, count, preLeftCount);
//		}else if(root.val < val){
//			root.right = constructBST(root.right, val, index, count, preLeftCount + root.leftCount + root.dup);
//		}else{
//			root.dup++;
//			count[index] = preLeftCount + root.leftCount;
//		}
//		return root;
//	}
	
	// BIT(树状数组)方法
//	public List<Integer> countSmaller(int[] nums) {
//		List<Integer> res = new LinkedList<>();
//		if(nums == null || nums.length == 0) return res;
//		int[] sorted = nums.clone();
//		Arrays.sort(sorted);
//		Map<Integer, Integer> map = new HashMap<Integer, Integer>();
//		for(int i = 0; i < sorted.length; i++){
//			map.put(sorted[i], i + 1);
//		}
//		int[] bitTree = new int[nums.length + 1];
//		for(int i = nums.length - 1; i >= 0; i--){
//			res.add(0, getSum(bitTree, map.get(nums[i])));
//			update(bitTree, map.get(nums[i]) + 1, 1);
//		}
//		return res;
//	}
//	private int getSum(int[] bitTree, int index){
//		int sum = 0;
//		while(index > 0){
//			sum += bitTree[index];
//			index -= lowbit(index);
//		}
//		return sum;
//	}
//	private void update(int[] bitTree, int index, int val){
//		while(index < bitTree.length){
//			bitTree[index] += val;
//			index += lowbit(index);
//		}
//	}
//	private int lowbit(int index){
//		return index & -index;
//	}
	
	// ST(线段树)方法
	public List<Integer> countSmaller(int[] nums) {
		List<Integer> res = new LinkedList<>();
		if(nums == null || nums.length == 0) return res;
		int[] sorted = nums.clone();
		Arrays.sort(sorted);
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		for(int i = 0; i < sorted.length; i++){
			map.put(sorted[i], i + 1);
		}
		STNode root = buildTree(1, nums.length);
		for(int i = nums.length - 1; i >= 0; i--){
			res.add(0, query(root, 1, map.get(nums[i]) - 1));
			updateNode(root, map.get(nums[i]), 1);
		}
		return res;
	}
	class STNode{
		int start, end;
		int count;
		STNode left, right;
		public STNode(int start, int end){
			this.start = start;
			this.end = end;
		}
	}
	private STNode buildTree(int start, int end){
		STNode root = new STNode(start, end);
		if(start == end){
			return root;
		}
		int mid = (start + end) >>> 1;
		root.left = buildTree(start, mid);
		root.right = buildTree(mid + 1, end);
		return root;
	}
	private int updateNode(STNode root, int index, int val){
		if(index < root.start || index > root.end) return root.count;
		if(root.start == root.end && root.start == index){
			root.count += val;
			return root.count;
		}
		root.count = updateNode(root.right, index, val) + updateNode(root.left, index, val);
		return root.count;
	}
	private int query(STNode root, int start, int end){
		if(end < root.start || start > root.end) return 0;
		if(start <= root.start && end >= root.end) return root.count;
		return query(root.left, start, end) + query(root.right, start, end);
	}
	
	// 分治法
	public List<Integer> countSmaller2(int[] nums) {
		int[] index = new int[nums.length];
		int[] count = new int[nums.length];
		for(int i = 0; i < nums.length; i++) index[i] = i;
		count(nums, new int[nums.length], 0, nums.length - 1, index, count);
		List<Integer> list = new ArrayList<Integer>();
		for(int num : count){
			list.add(num);
		}
		return list;
	}
	private void count(int[] nums, int[] temp, int left, int right, int[] index, int[] count){
		if(left >= right) return;
		int mid = (left + right) / 2;
		count(nums, temp, left, mid, index, count);
		count(nums, temp, mid + 1, right, index, count);
		
		int k = right;
		int i = mid, j = right;
		while(i >= left && j > mid){
			if(nums[index[i]] > nums[index[j]]){
				count[index[i]] += j - mid;
				temp[k--] = index[i--];
			}else{
				temp[k--] = index[j--];
			}
		}
		while(i >= left) temp[k--] = index[i--];
		while(j > mid) temp[k--] = index[j--];
		System.arraycopy(temp, left, index, left, right - left + 1);
	}
	
	public static void main(String[] args) {
		CountofSmallerNumbersAfterSelf count = new CountofSmallerNumbersAfterSelf();
		List<Integer> res = count.countSmaller(new int[]{3,4,5,1});
		for(int num : res)
			System.out.print(num + "  ");
		System.out.println();
		res = count.countSmaller(new int[]{1,2,7,8,5});
		for(int num : res)
			System.out.print(num + "  ");
		System.out.println();
		res = count.countSmaller(new int[]{26,78,27,100,33,67,90,23,66,5,38,7,35,23,52,22,83,51,98,69,81,32,78,28,94,13,2,97,3,76,99,51,9,21,84,66,65,36,100,41});
		for(int num : res)
			System.out.print(num + "  ");
		System.out.println();
		res = count.countSmaller(new int[]{26,78,27,100,33,67,90,100,41});
		for(int num : res)
			System.out.print(num + "  ");
	}

}
