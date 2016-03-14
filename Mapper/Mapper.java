package Mapper;

import java.awt.Button;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.SystemColor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class Mapper {
	private static int threadCount;
	private static double accuracy;
	private static String inFile;
	private static String outFile;
	public static BufferedReader read;
	public static String current;
	public static String line;
	public static ArrayList<String> list;
	public volatile static ArrayList<UrlCounter> cleanlist;
	volatile public static ArrayList<Entry> outputList = new ArrayList<Entry>();
	public static int listSize;
	private static int columns;
	static XSSFSheet sheet;
	static Iterator<Row> rowIterator;
	volatile static int count;
	public static ArrayList<String> templist = new ArrayList<String>();
	public static ArrayList<UrlCounter> templist2 = new ArrayList<UrlCounter>();
	public static String hostNotFoundError = "Host Not Found.";
	private static  JPanel contentPane;
	private static Panel panel;
	private static JTextPane txtpnInputFilePath;
	//private static JTextPane excel;
	//private static JTextPane txt;
	private static JTextField inputPath;
	private static Panel fileTypePanel;
	private static JLabel lblFileType;
	private static Panel okCancelPanel;
	private static Button button;
	private static Button button_1;
	private static Panel excelColumnPanel;
	private static JLabel lblIfExcelFile;
	private static JTextField textField;
	private static Panel inputPathPanel;
	private static JLabel lblOutputFilePath;
	private static JTextField txtOutputpath;
	private static Panel accuracyPanel;
	private static JComboBox accBox;
	private static Panel performancePanel;
	private static JComboBox comboBox;
	private static JLabel lblPerformance;
	private static JComboBox comboBox_1;
	private static Panel databasePanel1;
	private static Panel databasePanel2;
	private static JTextField databaseIP; 
	private static JTextField databaseName;
	private static JTextField databaseID;
	private static JTextField databasePassword;
	private static String type;
	static Connection c = null;
	private static String databaseIPS;
	private static String databaseNameS;
	private static String databaseIDS;
	private static String databasePasswordS;

	
	/*
	 * Executor() Uses callable implementation ThreadPlaying to 
	 * create timed threads that are used by threadCaller method to look up ips	 
	 * 
	 */
	
	public static void Executor() {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		
			Future<String> future = executor.submit(new ThreadPlaying());

			try {
				String temp = (future.get(150, TimeUnit.MILLISECONDS));
				templist.add(temp);
			} catch (TimeoutException e) {
				templist.add(hostNotFoundError);
			}

			catch (ExecutionException e1) {
				templist.add(hostNotFoundError);
			} catch (InterruptedException e2) {
				templist.add(hostNotFoundError);
			}
		
		executor.shutdownNow();
	}
	/*
	 * For each time callable implementation ThreadPlayng is called this thread 
	 * creates an attempt to access the url's ip by use of InetAddress.
	 * if the host is not found or the thread times out
	 *  a String "Host not found Error" is returned.
	 */
	public static String threadCaller(){
		try {
			UrlCounter current = cleanlist.get(0);
			cleanlist.remove(0);
			templist2.add(current);
			
			
			InetAddress add = InetAddress.getByName(current.getUrl());
			return add.getHostAddress();

		} catch (UnknownHostException e) {
			return hostNotFoundError;
		
		} catch (IOException e) {
			return "Error.";
		}
	}
	/*
	 * @Precondition String line1
	 * Adds the line1 to cleanList(arraylist) only if it is less than 35% similar to previous entries.
	 * This cuts down on the number of times the same ip will be returned for multiple urls.
	 */
	public static void compareAdd(String line1){
	
		Pattern dater = Pattern
				.compile(
						"^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
								"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
								"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
								"([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
		Matcher find = dater.matcher(line1);
		if(find.matches()){
			for(int i = 0; i < cleanlist.size(); i++){
				if(line1.equals(cleanlist.get(i))){
					cleanlist.get(i).incCount();
					return;}
			}
			UrlCounter n = new UrlCounter(line1);
			cleanlist.add(n);
		}
		else{
		for(int i = 0; i < cleanlist.size(); i++){
			if(LevenshteinDistance.similarity(cleanlist.get(i).getUrl(), line1) >= accuracy){
				cleanlist.get(i).incCount();
				return;}
			}
		UrlCounter n = new UrlCounter(line1);
		cleanlist.add(n);}
	}
	/*
	 * removes items from list and calls compare add until list(ArrayList) is empty 
	 */
	public static void listOrganizer(){
		while(!list.isEmpty()){
			String temp = list.get(0);
			list.remove(0);
			compareAdd(temp);
			}
		}
	/*
	 * 
	 * Uses Apache.poi.XSSF libraries to read in an xlsx file. 
	 * Each line is ready in by a thread and the url in the column(entered by user) is 
	 * saved into list(arraylist) until the document is empty.
	 */
	public static void readExcel(String filepath) throws IOException{
		
			FileInputStream file = new FileInputStream(new File(
					filepath));

			// Create Workbook instance holding reference to .xlsx file
			XSSFWorkbook workbook = new XSSFWorkbook(file);

			// Get first/desired sheet from the workbook
			sheet = workbook.getSheetAt(0);

			// Iterate through each rows one by one
			rowIterator = sheet.iterator();
			count = 0;

			line = "";
			while (rowIterator.hasNext()) {
				Row row = rowIterator.next();
				// For each row, iterate through all the columns
				Iterator<Cell> cellIterator = row.cellIterator();

				while (cellIterator.hasNext()) {
					count++;
					Cell cell = cellIterator.next();
					if (count == columns) {
						// Check the cell type and format accordingly
						switch (cell.getCellType()) {
						case Cell.CELL_TYPE_NUMERIC:
							line = (cell.getNumericCellValue() + " ");
							list.add(line);

							break;
						case Cell.CELL_TYPE_STRING:
							line = (cell.getStringCellValue());
							list.add(line);

							break;
						}
					}
				}
				count = 0;
			}
	}
	/*
	 * uses BufferedReader(FileReader) to read in a txt file.
	 * Each url must be on a new line.  
	 * Each url is added to list(ArrayList) 
	 */
	public static void readTxt(String filepath){
		try {
			read = new BufferedReader(new FileReader(filepath));
			line = read.readLine();
			while(!line.isEmpty()){
				list.add(line);
				line = read.readLine();
				
			}
			System.out.println(cleanlist.size());
		}
		catch(FileNotFoundException e){}
		catch(IOException e1){}
		catch(NullPointerException e2){}
		return;
	}
	/*
	 * simple method that just creates a new thread if the current activeCount() is less than
	 * the number of threads the program can safely run dependent upon performance (5, 10, or 15)
	 */
	public static void timedThreader(){
		while(!cleanlist.isEmpty()){ 
			if( Thread.activeCount() <= threadCount){
			Thread timed = new Thread(new ThreadRunner());
			timed.run();	
			}
		}
	}
	/*
	 * Creates a new Entry for each url/ip/count(number of times that or similar urls is used in the given input doc)
	 * this can be used later to access that url/ip/count when searching the database or creating the kml doc
	 */
	public static void entryCreator(){
		for(int i = 0; i < templist2.size(); i++)
			if(!(templist.get(i).equals(hostNotFoundError)|| templist.get(i).equals("Error."))){
				Entry newish = new Entry(templist2.get(i).getUrl(), templist.get(i), templist2.get(i).getCount());
				outputList.add(newish);
			}
	}
	/*
	 * Creates a KML with the information resolved from the previous methods.
	 * Reads in "kmlEntro.txt"  to create the beginning of the document
	 */
	
	public static void geoInfo(){
		ArrayList<Entry>  entries = new ArrayList();

		try {
			Class.forName("org.postgresql.Driver");
			String IP; IP = "jdbc:postgresql;//" ; IP += databaseIPS; IP += "/";
	
			c = DriverManager.getConnection(IP+ databaseNameS, databaseIDS, databasePasswordS);
			System.out.println("Opened database successfully");
			
			for (Entry e : outputList) {
				Location location = IPLocationMap.getLocation(e.getIp()); 
				String geoCode = location.latitude.toString() + ", " + location.longitude.toString() + ",0";
				String city = IPLocationMap.getCity(e.getIp());
				String asn = IPLocationMap.getASN(e.getIp());
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
	public static void createKML(){
		try {
			BufferedReader read = new BufferedReader(new FileReader("KMLentro.txt"));
			BufferedWriter write = new BufferedWriter(new FileWriter(outFile));
			String currentLine;
			currentLine = read.readLine();
			while(currentLine != null){
			write.write(currentLine);
			write.newLine();
			currentLine = read.readLine();}
			read.close();
			for (Iterator<Entry> iter = outputList.iterator(); iter.hasNext(); ) {
			    Entry element = iter.next();
			    write.write("<Placemark>" + " " + "<name>" + element.getUrl());
			    write.write("</name>" + "<description>");
			    write.write("Ip: " + element.getIp() + "\n" + "Asn:" +  element.getAsn() + "\n" + element.getcount() + " occurences of this url." 
			   + "\n" + element.getLocation() 		);
			    write.write("</description>" + " " +  "<Point>" + " " + "<coordinates>");
			    write.write(element.getGeocode() + "</coordinates>" + " " + "</Point>" + " " + "</Placemark>");
			    write.newLine();
			}
				write.write("\n" + "</Document>" + "\n" + "</kml>");
			
		
		
		write.close();
		} 	catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
	}
	/*
	 * Runs the successive methods in order of read document, clean list, attempt ip resolution, database implementation,
	 * and then kml building
	 */
	
	public static void implementMain(){
		if(type.equals("excel")){
			try {
				readExcel(inFile);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else readTxt(inFile);
		System.out.println(list.size());
		listOrganizer();
		System.out.println(cleanlist.size());
		timedThreader();
		System.out.println(templist.size());
		entryCreator();
		System.out.println(outputList.size());
		for(int i = 0; i < outputList.size(); i++)
		System.out.println(outputList.get(i).getIp());
		geoInfo();
		createKML();
		System.out.println("Complete.");
		System.exit(1);
	}
	/*
	 * @Precondition String[] args, Unused
	 * 
	 * main() creates a JFrame gui to allow users to easily define parameters of 
	 * Input file, Output file, whether input is txt or xlsx based, and the performance
	 * of the computers to create most possible threads without losing information.
	 */
	public static void main(String[] args) {
		long a = System.currentTimeMillis();
		list = new ArrayList<String>();
		cleanlist = new ArrayList<UrlCounter>();
		final JFrame dlg = new JFrame();
		final JPanel pnText = new JPanel();
		final JTextArea inputFile = new JTextArea(1, 20);
		final JTextArea kmlOut = new JTextArea(1, 20);
		final String[] typeOption = { "Excel(.xlsx)", "Text(.txt)"};
		final String[] performanceOption = {"High", "Medium", "Low"};
		final String[] accuracyOption = {"High(slow and messy output)", "Medium(recommended)", "Low(fast)"};
		dlg.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		dlg.setBounds(100, 100, 300, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		dlg.setContentPane(contentPane);
		contentPane.setLayout(new GridLayout(9, 2
				));
		
		
		performancePanel = new Panel();
		contentPane.add(performancePanel);
		accuracyPanel = new Panel();
		contentPane.add(accuracyPanel);
		fileTypePanel = new Panel();
		contentPane.add(fileTypePanel);	
		excelColumnPanel = new Panel();
		contentPane.add(excelColumnPanel);	
		panel = new Panel();
		contentPane.add(panel);		
		
		inputPathPanel = new Panel();
		contentPane.add(inputPathPanel);
		databasePanel1 = new Panel();
		databasePanel2 = new Panel();
		databaseIP = new JTextField(); databaseIP.setText("Database IP...");
		databaseName = new JTextField();  databaseName.setText("Database Name...");
		databaseID = new JTextField();  databaseID.setText("Database ID...");
		databasePassword = new JTextField();  databasePassword.setText("Database Password...");
		databasePanel1.add(databaseIP);
		databasePanel1.add(databaseName);
		databasePanel2.add(databaseID);
		databasePanel2.add(databasePassword);
		contentPane.add(databasePanel1);
		contentPane.add(databasePanel2);
		okCancelPanel = new Panel();
		contentPane.add(okCancelPanel);
		
		txtpnInputFilePath = new JTextPane();
		txtpnInputFilePath.setBackground(SystemColor.inactiveCaptionBorder);
		txtpnInputFilePath.setText("Input File Path:");
		panel.add(txtpnInputFilePath);
		
		inputPath = new JTextField();
		inputPath.setText("...");
		panel.add(inputPath);
		inputPath.setColumns(10);
		performancePanel.add(new JLabel("Performance:"));
		accuracyPanel.add(new JLabel("Accuracy"));
		accBox = new JComboBox(accuracyOption);
		accuracyPanel.add(accBox);
		comboBox = new JComboBox(performanceOption);
		performancePanel.add(comboBox);
		
		lblFileType = new JLabel("File Type:");
		fileTypePanel.add(lblFileType);
		
		
		comboBox_1 = new JComboBox(typeOption);
		fileTypePanel.add(comboBox_1);
		
		
		
		lblIfExcelFile = new JLabel("Excel Column number: ");
		excelColumnPanel.add(lblIfExcelFile);
		
		textField = new JTextField(2);
		excelColumnPanel.add(textField);
		
		lblOutputFilePath = new JLabel("Output File Path:");
		inputPathPanel.add(lblOutputFilePath);
	
		txtOutputpath = new JTextField();
		inputPathPanel.add(kmlOut);
		txtOutputpath.setText("...");
		txtOutputpath.setColumns(10);
		
		button = new Button("Ok");
		okCancelPanel.add(button);
		
		button_1 = new Button("Cancel");
		okCancelPanel.add(button_1);
		button.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				dlg.setVisible(false);
				inFile = inputPath.getText();
				databaseIPS = databaseIP.getText();
				databaseNameS = databaseName.getText();
				databaseIDS = databaseID.getText();
				databasePasswordS = databasePassword.getText();
				outFile = kmlOut.getText() + ".kml";
					
				if(comboBox.getSelectedItem().equals(performanceOption[0])){
					
					threadCount = 15;
				}
				else if(comboBox.getSelectedItem().equals(performanceOption[1])){
					
					threadCount = 10;
				}
				else threadCount = 5;
				if(comboBox_1.getSelectedIndex() == 0){
						columns = Integer.parseInt(textField.getText());
						type = "excel";
				}
if(accBox.getSelectedItem().equals(accuracyOption[0])){
					
					accuracy = 1;
				}
				else if(comboBox.getSelectedItem().equals(accuracyOption[1])){
					
					accuracy = .6;
				}
				else accuracy = .35;
				if(comboBox_1.getSelectedIndex() == 0){
						columns = Integer.parseInt(textField.getText());
						type = "excel";
				}
				
				else type = "text";
				
				implementMain();
				
			}
		});
		button_1.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {System.exit(0);;}
		});
				
		dlg.setVisible(true);
	
	}

	

}