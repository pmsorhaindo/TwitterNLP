package eval.cv;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;

import util.FileSplitter;

public class CrossValidator {

	public CrossValidator(){
		FileSplitter splits = new FileSplitter();
		splits.readInAllTweetsSplit("/Volumes/LocalDataHD/ps324/Documents/workspace/TwitterNLP/bin/data/daily547.conll", 10, 5);
		File path = new File("/Volumes/LocalDataHD/ps324/Documents/workspace/TwitterNLP/bin/data/");
		trainModels(path,"daily547_split_");
		
	}
	
	private void trainModels(File path,String fileName) {

		
		String[] directories = path.list(new FilenameFilter() {
		  @Override
		  public boolean accept(File current, String name) {
		    return new File(current, name).isDirectory();
		  }
		});
		System.out.println(Arrays.toString(directories));
		
		for (int i = 0; i<directories.length; i++)
		{
			File f = new File(path,directories[i]);
			System.out.println("file "+i+": "+f.toString());
		}
		
	}

	public static void main(String[] args) throws IOException {
		CrossValidator cv = new CrossValidator();
	}
}
