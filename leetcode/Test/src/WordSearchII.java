import java.util.ArrayList;
import java.util.List;

public class WordSearchII {

	public List<String> findWords1(char[][] board, String[] words) {
		List<String> res = new ArrayList<>();
		for(String s : words){
			if(findWord(board, s) && !res.contains(s)) res.add(s);
		}
		return res;
	}
	private boolean findWord(char[][] board, String word){
		for(int i = 0; i < board.length; i++){
			for(int j = 0; j < board[i].length; j++){
				if(find(board, word, i, j, 0)) return true;
			}
		}
		return false;
	}
	private boolean find(char[][] board, String word, int i, int j, int n){
		if(n == word.length()){
			return true;
		}
		if(i >= 0 && i < board.length && j >= 0 && j < board[i].length && board[i][j] == word.charAt(n)){
			board[i][j] = '*';
			boolean res = find(board, word, i - 1, j, n + 1) 
				|| find(board, word, i + 1, j, n + 1)
				|| find(board, word, i, j - 1, n + 1)
				|| find(board, word, i, j + 1, n + 1);
			board[i][j] = word.charAt(n);
			return res;
		}
		return false;
	}
	
	// TrieÊ÷
	public List<String> findWords(char[][] board, String[] words) {
		TrieNode root = buildTrie(words);
		List<String> res = new ArrayList<>();
		int width = board.length;
		int height = board[0].length;
		for(int i = 0; i < width; i++){
			for(int j = 0; j < height; j++){
				findWords(board, i, j, width, height, root, res);
			}
		}
		return res;
	}
	private void findWords(char[][] board, int i, int j, int width, int height, TrieNode node, List<String> res){
		char ch = board[i][j];
		if(ch == '*' || node.children[ch - 'a'] == null) return;
		node = node.children[ch - 'a'];
		if(node.word != null){
			res.add(node.word);
			node.word = null;
		}
		board[i][j] = '*';
		if(i > 0) findWords(board, i - 1, j, width, height, node, res);
		if(j > 0) findWords(board, i, j - 1, width, height, node, res);
		if(i + 1 < width) findWords(board, i + 1, j, width, height, node, res);
		if(j + 1 < height) findWords(board, i, j + 1, width, height, node, res);
		board[i][j] = ch;
	}
	class TrieNode{
		String word;
		TrieNode[] children = new TrieNode[26];
	}
	private TrieNode buildTrie(String[] words){
		TrieNode root = new TrieNode();
		for(String word : words){
			TrieNode temp = root;
			for(char ch : word.toCharArray()){
				if(temp.children[ch - 'a'] == null){
					TrieNode node = new TrieNode();
					temp.children[ch - 'a'] = node;
				}
				temp = temp.children[ch - 'a'];
			}
			temp.word = word;
		}
		return root;
	}
	
	public static void main(String[] args) {
		WordSearchII word = new WordSearchII();
		System.out.println(word.findWords(new char[][]{
		  {'o','a','a','n'},
		  {'e','t','a','e'},
		  {'i','h','k','r'},
		  {'i','f','l','v'}
		}, new String[]{"oath","pea","eat","rain"}));
	}

}
