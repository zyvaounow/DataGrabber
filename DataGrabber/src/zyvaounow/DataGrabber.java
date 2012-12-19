package zyvaounow;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataGrabber {
	
	public DataGrabber() {
		readPlacesFromMamasound();
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
		//Pattern p = Pattern.compile("<li><a rel='nofollow' href='(.*?)'><a href=\"(.*?)/\">(.*?)</a>");
		Pattern p = Pattern.compile("<li><a rel='nofollow' href='(.*?)'><a href=.(.*?).>(.*?)</a>");
		Pattern p2 = Pattern.compile("markerlist\":\"(.*?)\\{\\}1-default");
		Matcher m = p.matcher(lieux_html);
		while(m.find()) {
			//System.out.println(m.group(0));
			String lieu = m.group(3);//.split("</a>")[0];
			String lieu_url = base_url + m.group(2);
System.out.print( lieu + " : " + lieu_url + " ");
			String lieu_html = "bug";
			try {
				lieu_html = readFile(lieu_url);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			Matcher m2 = p2.matcher(lieu_html);
			while(m2.find()) {
				String[] ll = m2.group(1).split(",");
System.out.println("( " + ll[0] + " ; " + ll[1] + " )");
			}
		}
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

