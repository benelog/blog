/**
 * Class Name 	 : LoopTester2.java
 * Description : 
 * Copyright : Copyright 2005 BAI all right reseved.
 */
package looptest;

import java.util.List;

public class ListReader2 {
    
    static int countOfIncluded(List list, String str){
        int count = 0;
        
        for (int i=0,n=list.size();i<n;i++){
            String element = (String) list.get(i);
            if (element.indexOf(str)!= -1 ) count++;              
        }
        
        return count;
    }    
}