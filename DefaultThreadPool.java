import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class DefaultThreadPool<Job extends Runnable> implements ThreadPool<Job> {
	// 线程池最大限制数
	private static final int MAX_WORKER_NUMBERS = 10;
	// 线程池默认的数量
	private static final int DEFAULT_WORKER_NUMBERS = 5;
	// 线程池最小的数量
	private static final int MIN_WORKER_NUMBERS = 1;
	// 这是一个任务列表，将会向里面插入任务
	private final LinkedList<Job> jobs = new LinkedList<Job>();
	// 工作者列表 工作者负责消费任务
	private final List<Worker> workers = Collections.synchronizedList(new ArrayList<Worker>());
	// 工作者线程的数量
	private int workerNum = DEFAULT_WORKER_NUMBERS;
	// 线程编号生成
	private AtomicLong threadNum = new AtomicLong();
	
	public DefaultThreadPool() {
		initializeWokers(DEFAULT_WORKER_NUMBERS);
	}
	
	public DefaultThreadPool(int num) {
		workerNum = num>MAX_WORKER_NUMBERS?MAX_WORKER_NUMBERS:num<MIN_WORKER_NUMBERS?MIN_WORKER_NUMBERS:num;
		initializeWokers(workerNum);
	}
	
	private void initializeWokers(int num) {
		for(int i=0;i<num;i++){
			Worker worker = new Worker();
			workers.add(worker);
			Thread thread = new Thread(worker,"ThreadPool-Worker-"+threadNum.incrementAndGet());
			thread.start();
		}
	}

	@Override
	public void execute(Job job) {
		if(job!=null){
			synchronized (jobs) {
				jobs.addLast(job);
				jobs.notify();
			}		
		}
	}

	@Override
	public void shutdown() {
		for (Worker worker : workers) {
			worker.shutdown();
		}
	}

	@Override
	public void addWorkers(int num) {
		//新增的worker数量不能超过最大值
		if(workerNum+num>MAX_WORKER_NUMBERS){
			num = MAX_WORKER_NUMBERS - workerNum;
		}
		synchronized (workers) {
			initializeWokers(num);
			workerNum += num;
		}
	}

	@Override
	public void removeWorker(int num) {
		//要删除的数量大于当前线程池已有的数量
		if(num >= workerNum){
			throw new IllegalArgumentException("beyond workNum");
		}
		//按照给定的数量停止Worker
		synchronized (workers) {
			int count = 0;
			while(count < num){
				Worker worker = workers.get(count);
				if(workers.remove(worker)){
					worker.shutdown();
					count++;
				}
			}
			workerNum -=num;
		}
	}

	@Override
	public int getJobSize() {
		return jobs.size();
	}
	
	//工作者线程 负责消费任务
	class Worker implements Runnable{
		//是否工作
		private volatile boolean running = true; 
		
		@Override
		public void run() {
			while(running){
				Job job = null;
				synchronized (jobs) {
					//如果任务队列是空的  就等待
					while(jobs.isEmpty()){
						if(running == false){
							return;
						}
						try{
							jobs.wait(1000);
						}catch(InterruptedException ex){
							//感知到外部对WorkerThread的中断，由于是wait方法被中断 所以中断状态将被清除
							Thread.currentThread().interrupt();//重新设置中断状态
							return;
						}
						
					}
					job = jobs.removeFirst();
				}
				
				if(job!=null){
					//当前线程驱动任务执行
					job.run();
				}
				
			}
		}

		public void shutdown() {
			running = false;
		}
		
	}
}
