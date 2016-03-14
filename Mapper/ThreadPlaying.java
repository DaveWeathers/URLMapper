package Mapper;

import java.util.concurrent.Callable;
//Callable class used by Mapper to create timed threads for IP resolution
public class ThreadPlaying implements Callable{

	
	public String call() throws Exception {
		
		return  Mapper.threadCaller(); 
	}
	
}