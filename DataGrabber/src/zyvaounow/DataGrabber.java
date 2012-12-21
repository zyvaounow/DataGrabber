package zyvaounow;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
//import java.sql.Connection;
import java.sql.DriverManager;
//import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.PreparedStatement;

public class DataGrabber {
	Connection conn;
	ArrayList<Lieu> lieux = new ArrayList<>();
	
	public DataGrabber() {
		// connect to the database
		try {
			Class.forName("com.mysql.jdbc.Driver");
			System.out.println("DRIVER OK ! ");
			String url = "jdbc:mysql://localhost/zyvaou";
			String user = "root";
			String passwd = "zyvaou";
			conn = (Connection) DriverManager.getConnection(url, user, passwd);
			System.out.println("Connection effective !");
		} catch (ClassNotFoundException e) {e.printStackTrace();
		} catch (SQLException e) {e.printStackTrace();
		}
		
		// extract places from web site
		readPlacesFromMamasound();
		
		// insert places into the database
		PreparedStatement stmnt = null;
		ResultSet rs = null;
		try {
			stmnt = (PreparedStatement) conn.prepareStatement(
					"INSERT INTO lieu(nom, location) values (?, PointFromText(?));"
					);
		} catch (SQLException e) { e.printStackTrace(); }
		
		try {
			for(Lieu lieu : lieux){
				//stmnt.setString(1, "tutu");
				//stmnt.setString(2, "POINT(" + 1.55 + " " + 2.3 + ")");
				stmnt.setString(1, lieu.nom);
				stmnt.setString(2, "POINT(" + lieu.location.getX() + " " + lieu.location.getY() + ")");
				stmnt.execute();
			}
		} catch (SQLException e) { e.printStackTrace(); }

	}
	

	public void readEventsFromMamasound(){
		String lieux_html = "bug";
		String base_url = "http://www.mamasound.fr";

		// get the html file
		try {
			lieux_html = readFile("http://www.mamasound.fr");
		} catch (IOException e) {
			e.printStackTrace();
		}
		// parse
		Pattern p = Pattern.compile("<li><a rel='nofollow' href='(.*?)'><a href=.(.*?).>(.*?)</a>");
		Matcher m = p.matcher(lieux_html);
		while(m.find()) {
			Lieu lieu = new Lieu();
			lieu.nom = m.group(3);
		}
	}
	
	public void readPlacesFromMamasound(){
		String lieux_html = "bug";
		String base_url = "http://www.mamasound.fr";

		// get the html file
		try {
			lieux_html = readFile("http://www.mamasound.fr/les-lieux/");
		} catch (IOException e) {
			e.printStackTrace();
		}
		// parse
		Pattern p = Pattern.compile("<li><a rel='nofollow' href='(.*?)'><a href=.(.*?).>(.*?)</a>");
		Pattern p2 = Pattern.compile("markerlist\":\"(.*?)\\{\\}1-default");
		Matcher m = p.matcher(lieux_html);
		while(m.find()) {
			Lieu lieu = new Lieu();
			lieu.nom = m.group(3);
			String lieu_url = base_url + m.group(2);
			String lieu_html = "empty";
			try {
				lieu_html = readFile(lieu_url);
				Matcher m2 = p2.matcher(lieu_html);
				while(m2.find()) {
					String[] ll = m2.group(1).split(",");
					lieu.location = new Point2D.Double(
							Double.valueOf((ll[0])),
							Double.valueOf((ll[1])));
				}
				
				lieux.add(lieu);
			} catch (IOException e) { e.printStackTrace(); }
			
	
		}
		
		for (Lieu l : lieux)
			System.out.println(
					"INSERT INTO lieu(nom, location) values (" +
					"'" + l.nom + "', " + "PointFromWKB(POINT(" +
							l.location.getX() + ", " +
							l.location.getY() + "));"
					);
	}
	
	public static String readFile(String fileName) throws UnsupportedEncodingException, IOException{
		// URL url = new URL("http://stackoverflow.com/questions/1381617");
		URL url = new URL(fileName);
		URLConnection con = url.openConnection();
		Pattern p = Pattern.compile("text/html;\\s+charset=([^\\s]+)\\s*");
		Matcher m = p.matcher(con.getContentType());
		/* If Content-Type doesn't match this pre-conception, choose default and 
		 * hope for the best. */
		String charset = m.matches() ? m.group(1) : "ISO-8859-1";
		Reader r = new InputStreamReader(con.getInputStream(), charset);
		StringBuilder buf = new StringBuilder();
		while (true) {
		  int ch = r.read();
		  if (ch < 0)
		    break;
		  buf.append((char) ch);
		}
		String str = buf.toString();
		return str;
	}
	
	public static void main(String[] argv){
		new DataGrabber();
	}
}

