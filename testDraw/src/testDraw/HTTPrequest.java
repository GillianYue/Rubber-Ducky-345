package testDraw;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class HTTPrequest {
	private String gglImgUrl = "https://www.google.com/search?tbm=isch&q=";

	/**
	 * take on average a minute and 15 seconds to fetch 
	 * a single ndjson file from the Google Cloud public 
	 * dataset bucket
	 * @param keyword
	 * @throws IOException
	 */
    public void googleCloudRequest(String keyword) throws IOException {
    	URL url = new URL
    	("http://storage.googleapis.com/"
    			+ "quickdraw_dataset/full/simplified/"
    			+ keyword +".ndjson");
    	ReadableByteChannel rbc = Channels.newChannel(url.openStream());
    	File file = new File(UsingProcessing.pathToLocal+
    			"ndjson/"+keyword+".ndjson");
    	if (!file.exists()) {
			file.createNewFile();
    	FileOutputStream fos = new FileOutputStream(file);
    	fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
    	fos.close();
        rbc.close();
        System.out.println(keyword+" ndjson file fetched");
    	}//if file exists no need to download again
    	else {
    	System.out.println(file.getName()+" already exists");	
    	}
    }
    
    
    /**
     * returns a list of urls of google image search result
     * @param keyword
     * @return
     * @throws IOException
     * @throws ParseException
     */
    public List<String> googleImageRequest(String keyword) throws IOException, ParseException{
    	
    	String userAgent = "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.116 Safari/537.36";
    	String url = (gglImgUrl+keyword);

    	List<String> resultUrls = new ArrayList<String>();


    	    Document doc = Jsoup.connect(url).userAgent(userAgent).referrer("https://www.google.com/").get();

    	    Elements elements = doc.select("div.rg_meta");

    	    JSONObject jsonObject;
    	    for (Element element : elements) {
    	        if (element.childNodeSize() > 0) {
    	            jsonObject = (JSONObject) new JSONParser().parse(element.childNode(0).toString());
    	            resultUrls.add((String) jsonObject.get("ou"));
    	        }
    	    }

    	   // System.out.println("number of results: " + resultUrls.size());

    	   return resultUrls;
    }
    
    
}
