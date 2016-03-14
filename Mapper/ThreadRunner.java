package Mapper;


public class ThreadRunner implements Runnable{
	
	//Runnable class used by Mapper to create threads for document reading.


	public void run() {
		Mapper.Executor();
		
	}
	
}