import java.util.concurrent.CompletableFuture;

public class CompletableFutureTest {
	public static void main(String[] args) throws Exception {
		 CompletableFuture<String> cf = CompletableFuture.completedFuture("message");
		 System.out.println(cf.getNow(null));
		 
		 CompletableFuture<Void> cf1 = CompletableFuture.runAsync(() -> {
		        System.out.println("cf1.isDaemon: " + Thread.currentThread().isDaemon());
		    });
		 cf1.join();
		 System.out.println("cf1.isDone: " + cf1.isDone());
		 System.out.println(cf1.getNow(null));
		 
		 StringBuilder result = new StringBuilder();
	     CompletableFuture.completedFuture("thenAccept message")
	            .thenAccept(s -> result.append(s));
	     System.out.println("result: " + result.toString()); 
	     throw new Exception();
	}
}
