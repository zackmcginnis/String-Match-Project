/*
Zack McGinnis
CS350 Algorithms - Winter 2014
Final Project
String Matching
 
Note that patterns described within the txt file must be enclosed by "&" characters.
For example, in an examplePattern.txt file, if we want to search the source.txt file for the 
pattern: "thisstring", I would place "&thisstring&" (without quotation marks) within the examplePattern.txt file
*/
import java.io.*;		 
import java.util.*;		 

public class stringMatch{

	static File patternFile;
	static File sourceFile;
	static DataInputStream sourceInputStream;
	static File outputFile;
	static PrintWriter out = null;
	static final int NUM_BYTES = 25 * (int)Math.pow(2, 20);		 
	static byte[] b;
	static int numComparisons;
	static int numBytesRead;   
	final static boolean DEBUG = !true;
	final static boolean TIME = true;		 
	
	public static void main(String[] args) throws Exception
	{   
		double endTime, elapsedTime, startTime = System.currentTimeMillis();
		boolean[] results = new boolean[3];

		try {
			patternFile = new File(args[0]);
			sourceFile = new File(args[1]);
			outputFile = new File(args[2]);
			out = new PrintWriter(new FileWriter(outputFile));
			b = new byte[NUM_BYTES]; 

			Scanner sc = new Scanner(patternFile);  //scan pattern
			sc.useDelimiter("&(\n&)?(\n)?");

			while(sc.hasNext() ){	 
			
			 	int length = 0;
				String pattern = sc.next();   //next pattern
				
				if(DEBUG) System.out.println("pattern,   \"" + pattern + '"');
				results[length] = output(Algorithm.KMP, pattern); ++length;  // run each alg with pattern
				results[length] = output(Algorithm.BF, pattern); ++length;
				results[length] = output(Algorithm.BM, pattern); ++length;
				
				if(DEBUG) System.out.println();
				boolean result = true;                        //correct result for each algorithm tested
				for(int i = 0; result && i < length-1; ++i)   //collect and output results of algorithms
					result = result && (results[i] == results[i+1]); 
				assert (result):
					"Different results found for pattern: " + pattern;
			}
		}finally {
			out.close();
			endTime = System.currentTimeMillis();  //gather time info, and convert to secs
			elapsedTime = endTime - startTime;
			elapsedTime /= 1000;	 
			if(TIME) System.out.println("main() elapsedTime: " + elapsedTime + " sec");
		}
	}
	
	    //enum containing our three string matching algorithms
		public static enum Algorithm{
		KMP("KMP"), BF("BF"), BM("BM"); 

		String str;

		Algorithm(String s){
			str = s;
		}
	}
	

	//this method reads input and collects the bytes
	//a new input stream is created for each call
	//numbytes read will be reset to positive
	static boolean readBytes() throws Exception {
		b = new byte[NUM_BYTES];
		numBytesRead = sourceInputStream.read(b, 0, NUM_BYTES);	
		if (numBytesRead==-1)
			return false;
		return true;
	}

	//this method copies the array values into a new array
	//to be used when offset and array of bytes are present
	static boolean readBytes(int offset, byte[] c) throws Exception {
		b = new byte[offset + NUM_BYTES];  
		for (int i =0; i < offset; i++){
			b[i] = c[i];
		}
		numBytesRead = sourceInputStream.read(b, offset, NUM_BYTES) + offset;
		if (numBytesRead==-1) 	
			return false;
		return true;
	}	
	
	
	//method to return output of a pattern read into the algorithm implementations
	static boolean output(Algorithm alg, String pattern) throws Exception{
		numComparisons =0;
		boolean found = false;
		double end, elapsed, start = System.currentTimeMillis();
		sourceInputStream = new DataInputStream(new FileInputStream(sourceFile) );
		
		readBytes();
		if(pattern.length() == 0)		//if empty
			found = true;
		else if(sourceFile.length() < pattern.length())//if length is greater than source
			found = false;
		else if(alg == Algorithm.KMP )//run an algorithm
			found = KMP(pattern);
		else if(alg == Algorithm.BF)
			found = BF(pattern);
		else if(alg == Algorithm.BM)
			found = BM(pattern);
		System.out.println("numComparisons: " + numComparisons);
		String tResult = found ? "MATCH" : "FAIL";
		out.println(alg.str + " " + tResult + ": " + pattern);

		end = System.currentTimeMillis();  //time collection and conversion
		elapsed = (end - start);
		elapsed /= 1000;		
		if(TIME) System.out.println(alg.str + ", " + elapsed + ", sec");

		return found;
	}

//ALGORITHM IMPLEMENTATIONS
	
	//Knuth Morris Pratt
	public static boolean KMP(String pattern) throws Exception{
		final int P_LEN = pattern.length();
		int[] a = compStr(pattern);
		StringBuilder align = new StringBuilder();
		char aChar = 0;
		assert(numBytesRead >= P_LEN) : "This pattern is too large";

		int x =0;
		for (int i = 0; i < numBytesRead; ++i) {
			if (b[i]==0)
				break;

			aChar = (char) b[i];
			align.append(aChar);
			if (x == P_LEN-1 && align.charAt(x) == pattern.charAt(x) ){
				return true;
			}

			if (align.charAt(x) == pattern.charAt(x) ) {
				x++;
				numComparisons++;
			}
			else if (x == 0) {
				numComparisons++;
				align = new StringBuilder();
			}
			else if (x > 0) {
				numComparisons++;
				x = a[align.length()-1];
				align.deleteCharAt(0);
			}

			if ((i == numBytesRead-1) && readBytes() )
				i =- 1;   
		}
		return false;
	}	
	
	//Brute Force algorithm
	public static boolean BF(String pattern) throws Exception{
		final int P_LEN = pattern.length();
		StringBuilder align = new StringBuilder("$");  
		char aChar = 0;
		assert(numBytesRead >= P_LEN) : "Pattern is too big for text";
		for(int i = 0; align.length() < P_LEN; ++i) //initialize and build string
			align.append((char)b[i]);		          
		assert(b.length > 0): "the array b is empty";
		for(int i = P_LEN-1; i < numBytesRead; ++i){ //delete and append string of chars
			assert(aChar <= 127):
				"This text contains these ascii values larger than 127: "+(int)aChar;
			aChar = (char)b[i]; 
			align.deleteCharAt(0);
			align.append(aChar);		 		 
			for(int j = 0; j < P_LEN; ++j){ //compare our string against the pattern, if no match, break loop, 
				++numComparisons;           //and don't read the rest of string.  if all chars match, return true
				if(align.charAt(j) != pattern.charAt(j) )
					break;		 
				if(j == P_LEN-1 ){		 
					return true;
				}
			}

			if ((i==numBytesRead-1) && readBytes() ) //reset i in outer for loop
				i=-1;
		}

		return false;
	}

	//Boyer-Moore
	//pre processing steps are found in badChar, compcores, and goodsuffix methods
	static boolean BM(String pattern) throws Exception{
		char[] p = pattern.toCharArray();
		int[] f = compStr(pattern);	
		int[] s = goodSuffix (pattern, f);
		int[] t = badChar(pattern);
		int l2 = 0;
		int l1=0;
        int j = p.length;
		
		while (l2<=(numBytesRead-p.length)) {
			j = p.length;
			while (j>0 && p[j-1] == b[l2+j-1]) {
				numComparisons++;
				j--;
			}
			if (j==0)
				return true;
			else {
				numComparisons++;
				l1=l2;
				l2 += Math.max(j-1-t[(int)b[l2+j-1]], s[p.length-j]);
			}

			if (l2==numBytesRead && readBytes()) { //this portion of the code is sort of a backguard to prevent
				l2=0;                              //the reading in of more bytes than we want
				j= p.length;
			}
			else if (l2 >(numBytesRead-p.length) && l2<numBytesRead) {
				int offset = numBytesRead-l2;
				byte[] c = new byte[offset];
				for (int i =0; i < offset; ++i) {
					c[i] = b[l2];
					l2++;
				}
				if (readBytes(offset, c) ) {   
					l2=0;
					j = p.length;
				}
			}
		}
		return false;
	}	
	
    //pre-processing method used in BM and KMP
	//takes in pattern, returns shift array
	public static int[] compStr(String pattern) {
		int m = pattern.length();
		char[] p = new char[m +1];
		int[] f = new int[m+1];

		for (int i = 0; i < pattern.length(); ++i) {
			p[i+1] = pattern.charAt(i);
		}
		f[0] = 0;
		f[1] = 0;

		for (int j = 2; j<=m; ++j) {
			int k = f[j-1];
			while (k>0 && p[j] != p[k+1]) {
				k = f[k];
			}
			if (k==0 && p[j] != p[k+1])
				f[j] =0;
			else
				f[j] = k +1;

		}
		return f;
	}


	//Bad character method
	//takes in pattern, returns array of shifted pattern
	//with bad char shift applied (BM)
	static int[] badChar(String pattern) {
		char[] p = pattern.toCharArray();
		int[] t = new int[1024];
		for (int i=0; i < t.length; ++i) {
			t[i]=-1;
		}	

		for (int i =0; i < p.length; ++i) {
			t[(int)p[i]] =  i;
		}

		return t;
	}

	//Good suffix method (BM)
	//reads in pattern, and array
	//returns array with good suffix rule applied
	static int[] goodSuffix(String pattern, int[] f) {
		char[] p = pattern.toCharArray();
		int initVal= p.length - f[p.length];
		int[] s = new int[p.length + 1];
		int cLength = 0;
		int mix = 0;
		
		for (int i = 0 ; i< s.length; ++i) { //initialize
			s[i] = initVal;
		}
		s[0] =1; 

		for (int i = 1; i<s.length; ++i) {
			cLength = f[i];
			mix = i-cLength;  
			if (mix<s[cLength])
				s[cLength] = mix;
		}
		return s;
	}


}