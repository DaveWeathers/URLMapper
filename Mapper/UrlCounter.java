package Mapper;
/*UrlCounter.class 
 * Keeps an object of type URL that is used by Mapper to store urls, and the number of times that url was used.
 * 
 */
public class UrlCounter {
	private String url;
	private int count;
	
	/*
	 * @precondition a string url that was unique when run through the Levenshtein Distance.
	 * @postcondition an object that stores a url and a count that is initialized to 1
	 */
	public UrlCounter(String url){
	this.url = url;
	this.count = 1;
	}
	/*
	 * @return url of the target object.
	 */
	public String getUrl(){
		return this.url;
	}
	/*
	 * @return count of the target object.
	 */
	public int getCount(){
		return this.count;
	}
	/*
	 * Increments value of target object's count variable.
	 */
	
	public void incCount(){
		this.count++;
	}
	
}