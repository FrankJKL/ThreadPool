import java.util.concurrent.atomic.AtomicInteger;

public class ThreadPoolTest {

	public static void main(String[] args) {
		ThreadPool threadPool = new DefaultThreadPool();
		threadPool.execute(new TestJob());
		threadPool.execute(new TestJob());
		threadPool.shutdown();
	}

}
class TestJob implements Runnable{
	private static AtomicInteger i = new AtomicInteger(0);
	private int number;
	
	public TestJob() {
		number = i.incrementAndGet();
	}
	
	@Override
	public void run() {
		System.out.println("Job"+number+" has been finished");
	}
	
}
