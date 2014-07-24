package eval.cv;

import java.io.IOException;

import util.FileSplitter;

public class CrossValidator {

	public CrossValidator(){
		FileSplitter splits = new FileSplitter();
		try {
			splits.split("daily547.conll", 4);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws IOException {
		CrossValidator cv = new CrossValidator();
	}
}
