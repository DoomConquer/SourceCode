import java.util.ArrayList;
import java.util.List;

public class FindEventualSafeStates {

	public List<Integer> eventualSafeNodes(int[][] graph) {
		List<Integer> res = new ArrayList<>();
		if(graph == null || graph.length == 0) return res;
		int[] visited = new int[graph.length];
		int[] safe = new int[graph.length];
		for(int i = 0; i < graph.length; i++){
			if(!find(graph, visited, safe, i)) res.add(i);
		}
		return res;
	}
	private boolean find(int[][] graph, int[] visited, int[] safe, int node){
		if(visited[node] == 1) return true;
		if(safe[node] == 1) return false;
		visited[node] = 1;
		for(int i = 0; i < graph[node].length; i++){
			if(find(graph, visited, safe, graph[node][i])) return true;
		}
		visited[node] = 0;
		safe[node] = 1;
		return false;
	}
	
	public static void main(String[] args) {
		FindEventualSafeStates find = new FindEventualSafeStates();
		System.out.println(find.eventualSafeNodes(new int[][]{{1,2},{2,3},{5},{0},{5},{},{}}));
		System.out.println(find.eventualSafeNodes(new int[][]{{0},{2,3,4},{3,4},{0,4},{}}));
		System.out.println(find.eventualSafeNodes(new int[][]{{159},{129,192,259,274,328},{334},{146,290,300,374,490},{99,203},{24},{11,242,385},{89,212,359},{376,427},{184,392},{170,231},{56,114,394,402},{},{28,68,193,236,255,321},{},{328},{127,248,276,459},{},{},{},{376},{131,163,210,221,268,318},{119,293,308},{87,93,204,218,269},{119,132,286,316,471,475},{90,97,233,340},{347},{281,413,480},{298},{59,268},{305},{160,247,372,393,475},{72,262,322,401,422},{464},{167,183,202,289,303,385},{},{497},{129,166,240,242},{46,112,367,466},{141,148,172,293,344,492},{198,499},{83,104,217,233,296,450},{195,256},{32,68,289,461,478,490},{32,241,298,402,453,482},{127,152,154,256,351,366},{27,56,178,285,367,435,460},{122,178,286,418,468},{211,287,329,333},{108,193,237,476,495},{},{5,60,65,128,468},{148,385},{119,219},{210,462,496},{113,132,215,366,421,494},{47,111,113,126,225,450,491},{328,465},{76,251,307,396},{86,163,287},{98,126,219,229,436},{91,222,262,345,386,462},{64,66,252,318,437},{454},{117,176,181,417,421,487},{},{},{81,437},{116,403},{147,218,261,420},{363,388,481},{},{91,165,249,287,393,406},{231,356,462},{391},{97,169,242,454},{109,189,431},{121,354,389,436},{88,95},{247,379,403,460},{133,184,343,467},{154,289,434},{137,236,303,372},{135,205,281,419,490},{124,299,432},{146,331,370,373,494},{251},{},{112,131,274,313,426,468},{118,351,378,395,434},{119,401,447},{},{179,224,231,341,427},{102,174,284,416,440,465},{324,338,481},{231,348},{199,314},{235,343},{352},{159,313,406},{408,415},{122},{161,291,363},{},{170,173,346},{280,282,379,432,454},{239,248,345,452,495},{},{},{135,310},{295,345,350,379},{},{333,414,462},{263,349},{304,324,404},{},{},{335,435},{},{174,441},{184,272},{212,250,278,454},{156,167,171,223},{286,353},{241,260,337,355,491},{144,152,292,459,481},{132},{193,269,356},{134,327},{},{182,351,385,406},{13,155,180,278,300,380,402},{166,255,404,413,444},{173,196,355,423,492},{156,161,306,326,490},{},{235,287,343,466},{202,453,485},{241,270,397},{157,404,418,430,474},{226,228},{258},{21,300,356,398,420,485},{164,261,446},{151,178,210,248,326,375},{},{},{230,249,304,307},{},{214,329,373,443},{244,304,377,441,467,492},{439},{170,377},{195,198,232,425,433,499},{136,165,234,315,328},{193,236,459},{194,231,250,374},{218,229,245,267,291,430},{171,291,328,333,484},{320},{318,332,354},{176,189,294,408,426,434},{380,390,415,464},{188,226,357},{190,336,341,390,448,453},{},{187,208},{241,283,314,441,457,478},{},{170,174,437,483},{186},{228,372,449,455,491,497},{232,260,443,481},{490},{213,242,311,367,393,490},{194,245,294},{291,415},{},{197,425,438,476},{209,288,404,426},{419},{207,242,264,269,386,475},{244,407},{207},{228,267,306},{289,322,329,376},{214,264,289,397,447,493},{208,234,334,339,413,476},{284,294,329,393},{},{331,388},{337,362},{339,368,399,479},{},{389,413,459},{196,267,268,338,399,472},{307,324},{206,403,415},{240,300,306},{},{377,425},{},{377,445},{257,365,451},{},{232,384,463,489,496},{410},{256,293,311,356,380,412},{},{},{452},{224,284,386,433,437},{32,225,267,291,299},{},{151},{268,304,355,408,444,476},{},{337},{265,440,467,469},{225,249,356},{228,235},{344,385,394,499},{22,224,275,312,320,452},{235,253,262,274,464,496},{246,312,360},{363,370,444,472},{232,449,452,466,476,487},{342,373},{359,379,411,421,463,491},{248,384,404,441,487},{},{243,276,352,446,454,485},{388},{255,261,292,473,474},{294,321,333,456,495},{},{254,364,367,437},{335,365,460},{},{267,395,437,479},{264,389,416,457},{307,342,389,427,471,489},{359,363,390},{285,327,388,408,441},{},{338,356,369,436,486},{261,297,307,393,403,477},{},{486},{263,319,482,483,485,491},{492},{259,310,443},{41,317,490},{298,304,403,484},{427},{272,382},{314,357,475},{276,302,333,397,408,444},{286,340,401,429,446},{324,325,362,388,410,442},{337,425,433,434},{324,336,351,359},{427,439},{287,320,449},{285,387,397,425},{280,314,348,373,374},{371,382,486},{314,331,419,430},{288,345,426,465},{145,487},{286,329,360,388},{366,416,435,470},{312,339,417},{322,455},{279,376,454,490},{383,414,423,463,489},{323,330,378,385},{342,402,442,457,484},{348},{303,387,441,446},{299},{334,346,424,490},{393,467},{432,440,496},{294,298,448,459},{},{332,350,392,412,457,460},{359,389,392,426,429,483},{328,359,380,487},{180,302},{425,447},{},{329},{494},{297,330,364,377,386,479},{316,351,415,447,451,485},{383,459},{302,331,363,364,401,438},{333},{372,382,426,453,459,463},{378,433,479,497},{349},{316,391,400,442,487},{338,454},{368,496},{315,422,441,463,482},{363,390,447,455,465},{397,491},{340,391,392,444,469,472},{318,323,335,371,385,474},{361,388,426,482},{},{327,340},{329},{},{334,375,377,395,400,477},{357},{379,443,471},{326,344,400,491},{320,323,391,421,474},{393},{401,411,465},{442,456},{},{332,382,472},{391,417,452,482,495,497},{340,382,427,429,442},{449},{347,348,359,390,434,456},{406},{},{352,421,427,457},{339,353,404,451,452},{},{},{355,399},{364,390,412,416},{354,398,413},{373},{353,382,441,470},{382,413},{386,428,443,487},{343,413,415,423,474},{416,430,489,491},{407,415,441,489,494},{352,379,392,412},{348,372,396,468},{406},{425},{352,362,385,458,463,491},{374,408,426,428},{375,376,414,461},{384,399,447},{362,405,420,478},{363,376,404},{381,434},{435},{374,378,381,437,441,458},{370,408,451,461,481},{},{371,466},{498},{391,410,436,452,481},{367,425},{400,441},{423,478},{392,431,458,491},{378,425},{371,409},{430,442,464},{378,401,425,442,497,499},{199,372,387,390,450,467,476},{},{395,491,494},{419,487},{383,393,474,480},{401,490},{418,429,467,468,469,488},{386,397,461,472,481},{387,453},{},{389,442,477,479,482},{394,419,436},{396},{390,435,445},{429,456},{441},{391,402,426,445,469},{428},{464,467,472,488,492},{391,471,482},{419,471,481,482},{},{},{416,449,462,464,468,475},{462,483},{418,430,465},{},{75,400,402,473,477,490},{443,447,470,491,498},{441,448,449,495},{428,437,460,478},{438,457,481,484},{414,433,451},{412,419,454,462},{440,481},{423,478},{},{481},{418,429,442,449,471,473},{435,446,448,471,498},{416,431,442,446,453},{497},{},{436,447},{465,478},{424,443,472,498},{455},{75,430,444,449,463,470,482},{437,447,486},{422},{},{436,465},{435,436,489},{432,479,494},{426,429,445,452,479,485},{},{456,474},{433,458,461,474,477,479},{446},{},{451,479,489,497},{435,438,446,475,485},{439},{462,470,489},{446,449,462},{444,457,485},{442,466},{466},{493},{463,478,480,491},{447,451,478,479,496},{451,455,458},{455},{457,499},{461,471},{453,476,498},{483,484},{461,472,475,487,498},{451,483},{177,452,478,480,497},{456,493},{},{455,471},{},{},{},{},{465,468,478,495},{470,474},{},{476,485,497},{465,470,471,491,492,496},{478,487,490,496},{482,498},{472,478,491,492},{472,478,485,487,495,498},{482,485,488,497,498},{473,475,477,480,481,496},{474,477,485,496},{479,491,492,493},{475,480,490,492},{486,487,488,492,496,498},{474,477,485},{490,492,498},{478,480,481,482,498},{480},{490,491,493},{485,486,493},{},{481,487,489,498,499},{483,484,485,487,497},{485,491,496,498},{490,491,494},{486,490,493,497,499},{487,488,490,494,499},{},{},{490,493,494,498},{492,494,496,499},{491,493,494,495,496,497},{498},{497},{497,498,499},{495,496,497,498,499},{496,497,498,499},{497,498,499},{498,499},{499},{}}));
	}

}
