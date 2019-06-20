import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

// ²Î¿¼leetcodeË¼Â·
public class WordLadderII {

    public List<List<String>> findLadders(String beginWord, String endWord, List<String> wordList) {
        List<List<String>> res = new ArrayList<List<String>>();
        Set<String> set = new HashSet<>(wordList);
        set.add(beginWord);
        Map<String, List<String>> next = new HashMap<>();
        for(String word : set) next.put(word, new ArrayList<>());
        Map<String, Integer> level = new HashMap<>();
        level.put(beginWord, 0);
        
        Queue<String> queue = new LinkedList<>();
        queue.offer(beginWord);
        while(!queue.isEmpty()){
        	boolean found = false;
        	int size = queue.size();
        	while(size-- > 0){
	        	String word = queue.poll();
	        	char[] sch = word.toCharArray();
	        	for(int i = 0; i < sch.length; i++){
	        		for(char ch = 'a'; ch <= 'z'; ch++){
	        			if(ch != sch[i]){
	        				char old = sch[i];
	        				sch[i] = ch;
	        				String newWord = new String(sch);
	        				if(set.contains(newWord)){
	        					next.getOrDefault(word, new ArrayList<>()).add(newWord);
	        					if(!level.containsKey(newWord)){
	        						level.put(newWord, level.get(word) + 1);
	        						if(endWord.equals(newWord)) found = true;
	        						else queue.offer(newWord);
	        					}
	        				}
	        				sch[i] = old;
	        			}
	        		}
	        	}
	        	if(found) break;
        	}
        }
        findPath(beginWord, endWord, set, next, level, res, new ArrayList<String>());
        return res;
    }
    private void findPath(String currWord, String endWord, Set<String> set, Map<String, List<String>> next, Map<String, Integer> level, List<List<String>> res, List<String> list){
    	list.add(currWord);
    	if(currWord.equals(endWord)){
    		res.add(new ArrayList<>(list));
    	}else{
	    	for(String word : next.get(currWord)){
	    		if(level.get(word) == level.get(currWord) + 1){
	    			findPath(word, endWord, set, next, level, res, list);
	    		}
	    	}
    	}
    	list.remove(list.size() - 1);
    }
    
	public static void main(String[] args) {
		WordLadderII wordLadderII = new WordLadderII();
		for(List<String> list : wordLadderII.findLadders("hit", "cog", Arrays.asList(new String[]{"hot","dot","dog","lot","log","cog"}))) 
			System.out.println(list);
		for(List<String> list : wordLadderII.findLadders("hit", "cog", Arrays.asList(new String[]{"hot","dot","dog","lot","log"}))) 
			System.out.println(list);
		for(List<String> list : wordLadderII.findLadders("cet", "ism", Arrays.asList(new String[]{"kid","tag","pup","ail","tun","woo","erg","luz","brr","gay","sip","kay","per","val","mes","ohs","now","boa","cet","pal","bar","die","war","hay","eco","pub","lob","rue","fry","lit","rex","jan","cot","bid","ali","pay","col","gum","ger","row","won","dan","rum","fad","tut","sag","yip","sui","ark","has","zip","fez","own","ump","dis","ads","max","jaw","out","btu","ana","gap","cry","led","abe","box","ore","pig","fie","toy","fat","cal","lie","noh","sew","ono","tam","flu","mgm","ply","awe","pry","tit","tie","yet","too","tax","jim","san","pan","map","ski","ova","wed","non","wac","nut","why","bye","lye","oct","old","fin","feb","chi","sap","owl","log","tod","dot","bow","fob","for","joe","ivy","fan","age","fax","hip","jib","mel","hus","sob","ifs","tab","ara","dab","jag","jar","arm","lot","tom","sax","tex","yum","pei","wen","wry","ire","irk","far","mew","wit","doe","gas","rte","ian","pot","ask","wag","hag","amy","nag","ron","soy","gin","don","tug","fay","vic","boo","nam","ave","buy","sop","but","orb","fen","paw","his","sub","bob","yea","oft","inn","rod","yam","pew","web","hod","hun","gyp","wei","wis","rob","gad","pie","mon","dog","bib","rub","ere","dig","era","cat","fox","bee","mod","day","apr","vie","nev","jam","pam","new","aye","ani","and","ibm","yap","can","pyx","tar","kin","fog","hum","pip","cup","dye","lyx","jog","nun","par","wan","fey","bus","oak","bad","ats","set","qom","vat","eat","pus","rev","axe","ion","six","ila","lao","mom","mas","pro","few","opt","poe","art","ash","oar","cap","lop","may","shy","rid","bat","sum","rim","fee","bmw","sky","maj","hue","thy","ava","rap","den","fla","auk","cox","ibo","hey","saw","vim","sec","ltd","you","its","tat","dew","eva","tog","ram","let","see","zit","maw","nix","ate","gig","rep","owe","ind","hog","eve","sam","zoo","any","dow","cod","bed","vet","ham","sis","hex","via","fir","nod","mao","aug","mum","hoe","bah","hal","keg","hew","zed","tow","gog","ass","dem","who","bet","gos","son","ear","spy","kit","boy","due","sen","oaf","mix","hep","fur","ada","bin","nil","mia","ewe","hit","fix","sad","rib","eye","hop","haw","wax","mid","tad","ken","wad","rye","pap","bog","gut","ito","woe","our","ado","sin","mad","ray","hon","roy","dip","hen","iva","lug","asp","hui","yak","bay","poi","yep","bun","try","lad","elm","nat","wyo","gym","dug","toe","dee","wig","sly","rip","geo","cog","pas","zen","odd","nan","lay","pod","fit","hem","joy","bum","rio","yon","dec","leg","put","sue","dim","pet","yaw","nub","bit","bur","sid","sun","oil","red","doc","moe","caw","eel","dix","cub","end","gem","off","yew","hug","pop","tub","sgt","lid","pun","ton","sol","din","yup","jab","pea","bug","gag","mil","jig","hub","low","did","tin","get","gte","sox","lei","mig","fig","lon","use","ban","flo","nov","jut","bag","mir","sty","lap","two","ins","con","ant","net","tux","ode","stu","mug","cad","nap","gun","fop","tot","sow","sal","sic","ted","wot","del","imp","cob","way","ann","tan","mci","job","wet","ism","err","him","all","pad","hah","hie","aim","ike","jed","ego","mac","baa","min","com","ill","was","cab","ago","ina","big","ilk","gal","tap","duh","ola","ran","lab","top","gob","hot","ora","tia","kip","han","met","hut","she","sac","fed","goo","tee","ell","not","act","gil","rut","ala","ape","rig","cid","god","duo","lin","aid","gel","awl","lag","elf","liz","ref","aha","fib","oho","tho","her","nor","ace","adz","fun","ned","coo","win","tao","coy","van","man","pit","guy","foe","hid","mai","sup","jay","hob","mow","jot","are","pol","arc","lax","aft","alb","len","air","pug","pox","vow","got","meg","zoe","amp","ale","bud","gee","pin","dun","pat","ten","mob"}))) 
			System.out.println(list);
	}

}
