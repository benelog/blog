/**
 * Class Name 	 : ListReader1.java
 * Description : 
 * Copyright : Copyright 2005 BAI all right reseved.
 */
package looptest;

import java.util.List;

public class ListReader1 {
    
    static int countOfIncluded(List list, String str){
        int count = 0;
        String element;
        for (int i=0,n=list.size();i<n;i++){
            element = (String) list.get(i);
            if (element.indexOf(str)!= -1 ) count++;              
        }
        
        return count;
    }
}