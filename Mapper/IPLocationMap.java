package Mapper;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;


public class IPLocationMap {
	
	static Connection c = null;
	
	public static void main(String args[]) {
		
		ArrayList<Entry>  entries = new ArrayList();

		try {
			Class.forName("org.postgresql.Driver");
			String database = "postgres";
			String username = "postgres";
			String password = "1234567890";
	
			c = DriverManager.getConnection("jdbc:postgresql://localhost:164.111.139.178/"+ database, username, password);
			System.out.println("Opened database successfully");
			
			for (Entry e : entries) {
				Location location = getLocation(e.getIp()); 
				String geoCode = location.latitude.toString() + " , " + location.longitude.toString();
				String city = getCity(e.getIp());
				String asn = getASN(e.getIp());
				e.addGeocode(geoCode);
				e.addAsn(asn);
				e.addLocation(city);
	c.close();
	
			}
       } catch ( Exception e ) {
    	   System.err.println( e.getClass().getName()+": "+ e.getMessage() );
    	   System.exit(0);
       }
		
	}
	
	public static String getCity(String IPAddress) throws UnknownHostException, SQLException {
		String city = new String();
		long longIPAddress = getIPLong(IPAddress); 
		
		Statement stmt = c.createStatement();
		ResultSet rs;
		rs = stmt.executeQuery("SELECT city_name FROM ip2location_db5 WHERE " + longIPAddress + " BETWEEN ip_from AND ip_to");
		while (rs.next()){
			city = rs.getString("city_name");
		}
		
		return city;
		
	}

	public static Location getLocation(String IPAddress) throws UnknownHostException, SQLException {
		Location location = new Location(0.0,0.0);
		long longIPAddress = getIPLong(IPAddress); 
		
		Statement stmt = c.createStatement();
		ResultSet rs;
		rs = stmt.executeQuery("SELECT latitude, longitude FROM ip2location_db5 WHERE " + longIPAddress + " BETWEEN ip_from AND ip_to");
		while (rs.next()){
			float latitude = rs.getFloat("latitude");
			float longitude = rs.getFloat("longitude");
			location = new Location(longitude, latitude);
		}
		
		return location;	
	}
	
	public static String getASN(String IPAddress) throws UnknownHostException, SQLException {
		String as = new String(); 
		long longIPAddress = getIPLong(IPAddress);
		
		Statement stmt = c.createStatement();
		ResultSet rs;
		rs = stmt.executeQuery("SELECT asn FROM ip2location_asn WHERE " + longIPAddress + " BETWEEN ip_from AND ip_to");
		while (rs.next()){
			as = rs.getString("asn");
		}
		
		return as;	
	}
		
	public static long getIPLong(String IPAddress) throws UnknownHostException {
		InetAddress inetAddr = InetAddress.getByName(IPAddress);		
	    byte[] address = inetAddr.getAddress();
		long longIPAddress =
			      ((address[0] & 0xFFl) << (3*8)) + 
			      ((address[1] & 0xFFl) << (2*8)) +
			      ((address[2] & 0xFFl) << (1*8)) +
			      (address[3] &  0xFFl);
		return longIPAddress;
	}
	
	
}
