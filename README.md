String-Match-Project
====================

A comparison of the implementations of three string matching algorithms in Java


Zack McGinnis
CS350
Winter 2014
Final Report - A Comparison of Three String Matching Algorithms


INTRODUCTION

In this report, our goal is to compare the performance of three popular string-matching algorithms.
The chosen algorithms for this study include a naïve, brute-force matching algorithm, the Knuth-Morris-Pratt algorithm, and the Boyer-Moore algorithm.  The purpose of this study is to, upon examining the results of our tests, determine which algorithm will be best suited a particular task string-matching task.  String matching in practice may require many different situations and contexts depending upon the task.  Because of this it will be helpful to know if for example, the Boyer-Moore algorithm may actually fare better than the Knuth-Morris-Pratt algorithm if we are searching for a pattern containing once character.  

Six different tests have been devised with the goal of locating performance discrepancies between the algorithms in areas such as: alphabet size, source/text size, and pattern size.  Each test will be ran 5 times in order to get a reasonable idea of the aggregate elapsed time value for each algorithm searching for each pattern in each test case.  The average of these 5 trials will be recorded and displayed in the results portion of this report.

Background information of the algorithms, notes of their implementation in stringMatch.java, as well as predictions regarding test performance, are included as follows:

Brute Force Algorithm
Upon scanning the text file, the scanning window is shifted exactly one character to the right, starting from the left of the pattern.  This process runs until a match is found, or the end of file is reached.  Because of this, the algorithm requires 2n expected character comparisons.  In the worst case, the efficiency of this algorithm is O((n-m+1)m), of which it is tightly bound to.  Because there is no pre-processing in the algorithm, the total running time is equal to its total matching time.

The implementation of this algorithm within stringMatch.java is standard and isn't too intricate.  A StringBuilder is constructed to append char strings in the source file, and for loops are used to test the string against the pattern to be searched.  The innermost for loop returns true if every character of the string matches the pattern, and will break from the loop if a mismatch is found.
A snippet of these operations is provided:
for(int j = 0; j < P_LEN; ++j){ 
      ++numComparisons;       
      if(align.charAt(j) != pattern.charAt(j) )
            break;		 
      if(j == P_LEN-1 ){		 
	return true;





A visual example of the brute force algorithm is as follows:
...lkjndogescv...      TEXT
   dog                      PATTERN
    dog                     PATTERN (after one shift)
      …                    (shift)(shift)(shift)
         dog               (match)

It is expected that this algorithm will perform the worst out of the three algorithms tested for all of the included test cases (based mostly on it's time efficiency class).


Knuth-Morris-Pratt Algorithm
This string matching algorithm enables the use of pre-processing to match a string.  It begins like the brute-force method in that scanning of the pattern is initiated from left to right, however,  when a mismatch occurs, the pattern is shifted to the right such that the scanning is restarted at or after the point where the mismatch occurred in the input.  The preprocessing of the pattern is done within the compStr method found within the stringMatch.java file.  This method tells the algorithm how far to shift the pattern in the text.
The time efficiency of this algorithm is O(m)+O(m+n), which gives us an idea of how it will compare to brute-force searching.

The implementation of this algorithm within stringMatch.java is similar to that of the brute-force implementation in that StringBuilder is used to perform operations on the source text.  However, this algorithm is different in that it makes use of the preprocessing method compStr.  This method is used to calculate the shift of the string examining the source file.  
A snippet of these operations is provided at the innermost level of the KMP algorithm implementation:
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

For example, when a mismatch occurs, the compStr method returns an array to the KMP algorithm which signals the correct number of bytes to shift.  The method returns true if the string and pattern match every character.


As a visual example, consider searching for the string “card” in a source text
...lncarcardxwv...      TEXT
      card                    PATTERN
           card               PATTERN (after shift)
By using the compStr preprocessing method, we don't need to resume scanning beginning over at the next character “a” after our previous beginning character (first c character in text) failed to match a string.  This is because we are able to reset the align variable to match the next matchable character in the text.  When compStr returns the shift array, we know to shift to the right 3 places at the next incidence of “c” since it has already been read.

It is expected that this algorithm should outperform the brute-force algorithm for most tests, but not the Boyer-Moore algorithm, as Boyer-Moore contains additional pre-processing methods which, hypothetically, will increase performance.


Boyer-Moore Algorithm 
Unlike the previous two algorithms, Boyer-Moore compares the pattern with the text beginning at the right end of the pattern, scanning right to left. When a  mismatch occurs, the pattern is shifted according to the value of the pre-processing methods (goodSuffix, compStr, and badChar).
The bad character shift can be implemented when the first mismatch is found at position y (when scanning the pattern right to left) and T(x) is the character that mismatches P(y), then shift the pattern right by max(1, y - R(T(x)) .
The good suffix shift can be implemented when a situation is encountered such that we find t (suffix) is the longest suffix of P (pattern) that matches T (text) in the current position.  Then, P can be shifted so that the previous occurrence of t in P matches T.  
Similar to the Knuth-Morris-Pratt algorithm, this algorithm has time efficiency O(m+|E|)+O(n).

The implementation of this algorithm within stringMatch.java utilizes two additional pre-processing methods to match a string.  The badCharacter method is utilized when a mismatch occurs.  For example, if the mismatched letter appears in the pattern, the character of the mismatch is aligned to the last occurrence of that character in the initial part of the pattern.  Otherwise, the alignment is shifted to one position before the pattern if the mismatched character isn't located in the scanned portion of the pattern.  Although useful, this method does not always ensure progress will be made in regards to matching the pattern.  
A snippet of these operations is provided:
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

Examine the following example:
...xykakcbq...     TEXT
   ....uakcbq          PATTERN
 ..uakcbq            PATTERN (after shift)
Notice that the pattern shifted to the left after mismatching the k from the text with the u from the pattern.  This is one case where the bad character shift may not be as useful as other methods in terms of progress when scanning certain patterns.
The method returns an array value which shifts the align variable according to these two cases.  

Also utilized in the Boyer-Moore algorithm is the goodSuffix method.   Given a mismatch, this method aligns the scanned portion of the text with the rightmost occurrence of that character string in the pattern that is preceded by a different character than the previously matched suffix.
A snippet of these operations is provided:
   static int[] goodSuffix(String pattern, int[] f) {
	char[] p = pattern.toCharArray();
	int initVal= p.length - f[p.length];
	int[] s = new int[p.length + 1];
	int cLength = 0;
	int mix = 0;
		
	for (int i = 0 ; i< s.length; ++i) {  
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

For example:
....abcdabcexyzfxyz...     TEXT 
       ...zabcfxyzfxyz          PATTERN
              ...zabcfxyzfxyz      PATTERN (after shift)
In this example, the good suffix method would lead to a shift of four positions, since the matched part m = xyzfxyz occurs in the pattern four places left of the occurrence of it's suffix and is preceded by
 a different character there (z instead of f) than in the suffix position.  Note that the good suffix method always shifts to the right to ensure progress is being made.
This method will also return an array to the BM method which tells it how far to shift the align variable. 

It is expected that this algorithm will outperform the other two on most tests.  However, it will be interesting to see how a smaller patterns and larger alphabets are affected by the algorithms pre-processing methods.


METHODS
To test the three aforementioned algorithms, tests need to be constructed which take into account the different variables which may affect algorithm performance.  All tests were conducted on a PC running Windows 8 with a 64-bit Intel processor.  The constructed test cases are as follows:


Test Case 1: Small English Text
In this test, the patterns “test”, “thisisastring”, and “shouldnotfindthisstring” are searched for 
in a source file containing aproximately 2,048 bytes.  The first two strings are randomly placed within the source file and should be matched by all algorithms, while “shouldnotfindthisstring” should obviously not be matched, as it will be used to examine the worst case for each algorithm.
Note that the source file of this test contains 26 distinct lower case roman alphabet characters.


Test Case 2: Normal English Text
In this test, the patterns “test”, “thisisastring”, and “shouldnotfindthisstring” are searched for 
in a source file containing aproximately 1,115,062 bytes.  The first two strings are randomly placed within the source file and should be matched by all algorithms, while “shouldnotfindthisstring” should obviously not be matched, as it will be used to examine the worst case for each algorithm.
Note that the source file of this test contains 26 distinct lower case roman alphabet characters.

Test Case 3: Large English Text
In this test, the patterns “test”, “thisisastring”, and “shouldnotfindthisstring” are searched for 
in a source file containing aproximately 13,380,439 bytes.  The first two strings are randomly placed within the source file and should be matched by all algorithms, while “shouldnotfindthisstring” should obviously not be matched, as it will be used to examine the worst case for each algorithm.
Note that the source file of this test contains 26 distinct lower case roman alphabet characters.

Test Case 4: Large Alphabet
In this test, the source file contains occurrences of all ASCII printable characters.  This includes all symbols, numbers, upper and lower case letters.  The source file contains 1857 bytes of data.  The patterns to be searched are “55five555”, “MY*^*hat”, which both should be matched by all algorithms, and “MYKEYS”, which should not be matched by any algorithm.


Test Case 5: Small Alphabet
In this test, the source file is a list of DNA sequences consisting of only the characters “A”, “C”, “G”, and “T”.  The patterns to be searched are “TTTTT”, which should be matched by all algorithms, and “GGGGG”, which should not be matched by any algorithm.  The source file has 2129 bytes.


Test Case 6: One Character Pattern
In this test, the patterns “x” and “y” are searched for in a source file containing aproximately 2,048 bytes.  The first string “x” should be matched by all algorithms, while “y” should not be matched, as it will be used to examine the worst case for each algorithm.
Note that the source file of this test contains 26 distinct lower case roman alphabet characters.


RESULTS
Note that for each of the test results, the depicted chart shows a mean average of elapsed time over 5 trial runs for each algorithm searching for each pattern described in the methods section.  Also note that the number of comparisons will not vary among the 5 trial runs of each test case, as neither the source file nor the pattern file will change between trial runs.   


Test Case 1: Small English Text



Test Case 2: Normal English Text









Test Case 3: Large English Text



Test Case 4: Large Alphabet















Test Case 5: Small Alphabet



Test Case 6: One Character Pattern


CONCLUSION

The results of Test Case 1 are somewhat interesting.  While it was expected that the brute-force algorithm (BF) would perform worst in terms of elapsed time as well as total number of comparisons, I did not expect the Knuth-Morris-Pratt algorithm (KMP) to outperform the Boyer-Moore algorithm (BM) in the timings.  Granted, the difference in two of the three string searches was minimal (<.02), the BM algorithm recorded substantially less comparisons compared to the KMP algorithm.  This is a puzzling result, and I can only attribute it to the overuse of the badChar method of the BM algorithm as it could be hindering progress by matching to the left more than matching to the right.

The results of Test Case 2 were very interesting.  The KMP algorithm performed the worst in terms of elapsed time, while BM was best in terms of time and comparisons (as expected).  Since the source file is relatively small, I can attribute the poor performance of KMP partially to a lack of sufficient trial runs.

The results of Test Case 3 are similar to that of Test Case 2.  While the source size is significantly larger, there still seems to be some performance issues with the KMP algorithm.  Again, BM performed the best in terms of both speed and comparisons, and KMP recorded the worst elapsed time.  It is interesting to note that KMP recorded less comparisons than BF, despite having a slower elapsed time.  

The results of Test Case 4 are more reassuring.  With a larger testing alphabet, we see that both BM and KMP perform similarly in terms of elapsed time (<.01 differences), though BM still records substantially less comparisons (worst case 1866 to 331).

The results of Test Case 5 are somewhat interesting.  With a smaller alphabet like that of DNA sequences, the algorithms for the most part performed pretty similarly in terms of elapsed time.  The BM even recorded the slowest times, which was surprising.  

The results of Test Case 6 were somewhat expected.  This test (checking a for a one character pattern) produced nearly identical numbers for all algorithms in terms of elapsed time, as well as the total number of comparisons.  By only searching for a one character pattern, the benefits of the pre-processing capabilities found in KMP and BM algorithms are nullified to an extent.

After considering the results of all six test cases, the BM algorithm was the top performer overall.  Although, the gap between it and the other algorithms seems to be lessened when the search pattern is very small (one char), or when the source alphabet is very small (DNA sequences).  As the pattern, source file, or alphabets increase in size, the BF algorithm will be left in the proverbial dust as the BM algorithm will perform best, followed by the KMP algorithm.  This is clearly due to the pre-processing methods which BM and KMP employ in their algorithmic implementations.  Reflecting back to the time efficiency of each algorithm, the results (for the most part) support our predictions.  As expected, the BF algorithm, having time complexity O((n-m+1)m), routinely performed the worst, while the KMP and BM algorithms, having time complexity O(m)+O(m+n) and O(m+|E|)+O(n) respectively, performed better (especially as alphabet size grew larger).

Though the results are informative, improvements to testing protocols and the algorithms themselves can be made.  Additionally, it was concerning to have strange results regarding the KMP algorithm in the first three tests.  Perhaps with more trial runs, better, more accurate results may have been produced.  The addition of a partner or group may have also allowed for smoother algorithm implementation, as well as other ideas which the author did not employ.

This study taught me that while an algorithm may be predicted to perform better than another algorithm based on their time efficiency classes, this may change at times based on input variables.  More concisely, if we limit the size of the input (and alphabet), we can (sometimes) limit the performance of a supposedly faster algorithm.  
