package testDraw;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class TextParser {

	public ArrayList<String> readDoc(String filename) throws FileNotFoundException {
	        Scanner scanner = 
				new Scanner(new File(filename));
		    ArrayList<String> categories = new ArrayList<String>();
		    while (scanner.hasNextLine()) {
		    categories.add(scanner.nextLine());
		    }
		    scanner.close();
		    return categories;
	}
	
	
	
}

