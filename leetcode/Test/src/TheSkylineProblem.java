import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

/**
 * @author li_zhe
 * 参考leetcode思路
 * heap O(n*logn)
 * 先将建筑顶点按x轴排序，将高放入堆中，扫描到建筑左顶点时，将高入堆，右顶点时将对应的高出堆，每次比较高度是否发生变化，如果变化就需要记录当前堆里最高点和当前点的x坐标
 * （可以简单理解成，用一根竖线扫描，遇到左顶点如果是最高点就记录下，遇到右顶点就找和第二高的交点）
 */
public class TheSkylineProblem {

	public List<int[]> getSkyline(int[][] buildings) {
		List<int[]> res = new ArrayList<>();
		if(buildings == null || buildings.length == 0) return res;
		List<int[]> height = new ArrayList<>();
		for(int[] b : buildings){
			height.add(new int[]{b[0], -b[2]});
			height.add(new int[]{b[1], b[2]});
		}
		Collections.sort(height,(a1, a2) -> { if(a1[0] != a2[0]) return a1[0] - a2[0]; else return a1[1] - a2[1];});
		Queue<Integer> heap = new PriorityQueue<>(Collections.reverseOrder());
		heap.offer(0);
		int preHeight = 0;
		for(int[] h : height){
			if(h[1] < 0){
				heap.offer(-h[1]);
			}else{
				heap.remove(h[1]);
			}
			int currHeight = heap.peek();
			if(preHeight != currHeight){
				res.add(new int[]{h[0], currHeight});
				preHeight = currHeight;
			}
		}
		return res;
	}
	
	// 线段树(另解法见https://blog.csdn.net/accepthjp/article/details/66477033)
	private static class Node{
	    int start, end;
	    Node left, right;
	    int height;
	    
	    public Node(int start, int end){
	        this.start = start;
	        this.end = end;
	    }
	}
	public List<int[]> getSkyline1(int[][] buildings) {
	    Set<Integer> endpointSet = new HashSet<Integer>();
	    for(int[] building : buildings){
	        endpointSet.add(building[0]);
	        endpointSet.add(building[1]);
	    }
	    
	    List<Integer> endpointList = new ArrayList<Integer>(endpointSet);
	    Collections.sort(endpointList);
	    
	    HashMap<Integer, Integer> endpointMap = new HashMap<Integer, Integer>();
	    for(int i = 0; i < endpointList.size(); i++){
	        endpointMap.put(endpointList.get(i), i);   
	    }
	    
	    Node root = buildNode(0, endpointList.size() - 1);
	    for(int[] building : buildings){
	        add(root, endpointMap.get(building[0]), endpointMap.get(building[1]), building[2]);
	    }
	    
	    List<int[]> result = new ArrayList<int[]>();
	    explore(result, endpointList, root);

	    if(endpointList.size() > 0){
	        result.add(new int[]{endpointList.get(endpointList.size() - 1), 0});
	    }
	    
	    return result;
	}
	private Node buildNode(int start, int end){
	    if(start > end){
	        return null;
	    }else{
	        Node result = new Node(start, end);
	        if(start + 1 < end){
	            int center = start + (end - start) / 2;
	            result.left = buildNode(start, center);
	            result.right = buildNode(center, end);
	        }
	        return result;
	    }
	}
	private void add(Node node, int start, int end, int height){
	    if(node == null || start >= node.end || end <= node.start || height < node.height){
	        return;
	    }else{
	        if(node.left == null && node.right == null){
	            node.height = Math.max(node.height, height);
	        }else{
	            add(node.left, start, end, height);
	            add(node.right, start, end, height);
	            node.height = Math.min(node.left.height, node.right.height);
	        }
	    }
	}
	private void explore(List<int[]> result, List<Integer> endpointList, Node node){
	    if(node == null){
	        return;
	    }else{
	        if(node.left == null && node.right == null && (result.size() == 0 || result.get(result.size() - 1)[1] != node.height)){
	            result.add(new int[]{endpointList.get(node.start), node.height});
	        }else{
	            explore(result, endpointList, node.left);
	            explore(result, endpointList, node.right);
	        }
	    }
	}
	
	public static void main(String[] args) {
		TheSkylineProblem skyline = new TheSkylineProblem();
		System.out.println(skyline.getSkyline(new int[][]{{2, 9, 10}, {3, 7, 15}, {5, 12, 12}, {15, 20, 10}, {19, 24, 8}}));
	}

}
