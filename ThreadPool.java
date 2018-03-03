
public interface ThreadPool<Job extends Runnable> {
	//执行一个Job，这个Job需要实现Runable
	public void execute(Job job);
	//关闭线程池
	public void shutdown();
	//增加工作者线程
	public void addWorkers(int num);
	//减少工作者线程
	public void removeWorker(int num);
	//得到正在等待执行的任务数量
	public int getJobSize();
}
