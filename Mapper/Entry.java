package Mapper;
public class Entry {
	private String url;
	private String ip;
	private String geocode;
	private int count;
	private String asn;
	private String location;
	/*
	 * @precondition unique url, resolved ip, and the total times that urls(or variations there of) was used.
	 * @postcondition creates an object that stores a string url, string ip, and int count
	 */
	public Entry(String Url, String Ip, int count) {
		this.url = Url;
		this.ip = Ip;
		this.count = count;
	}
	/*
	 * @precondition string in the form of "long, latitude, altitude"  
	 * a zero in the place of altitude attaches the location to the ground at that long/lat
	 * 
	 */
	public void addGeocode(String Geocode) {
		this.geocode = Geocode;
	}
	
	/*
	 * @precondition String asn resolved from database
	 * 
	 */
	public void addAsn(String asn){
		this.asn = asn;
	}
	/*
	 * @return string asn
	 */
	public String getAsn(){
		return this.asn;
	}
	/*
	 * @return string url
	 */
	public String getUrl() {
		return this.url;
	}
	/*
	 * @return string ip
	 */
	public String getIp() {
		return this.ip;
	}
	/*
	 * @return string geocode
	 */
	public String getGeocode() {
		return this.geocode;
	}
	/*
	 * @return string count
	 */
	public int getcount(){
		return this.count;
		
	}
	public String getLocation(){
		return this.location;
	}
	
	public void addLocation(String location){
		this.location = location;
	}
}
