package looptest;

import java.util.ArrayList;
import java.util.List;

public class LoopTester {

    private static final int TEST_LIST_SIZE = 10000;

    private static final int TEST_REPEAT = 1000;
    

    private static List getStringList(){
        List result = new ArrayList();
        for (int i=0;i<TEST_LIST_SIZE;i++){
            result.add(String.valueOf(i));
        }
        return result;
    }
    

    public static void main(String args[]){
        List testList = getStringList();
        getTime1(testList);
        getTime2(testList);
        getTime2(testList);
        getTime1(testList);        
        getTime1(testList);
        getTime2(testList);        
    }
    private static void getTime1(List list) {
        long time1 = System.currentTimeMillis();
        for (int i=0;i<TEST_REPEAT;i++) ListReader1.countOfIncluded(list, "9");
        long time2 = System.currentTimeMillis();
        System.out.println("test1:밖에 선언 " + (time2-time1));
    }
    
    private static void getTime2(List list) {
        long time1 = System.currentTimeMillis();
        for (int i=0;i<TEST_REPEAT;i++) {
            ListReader2.countOfIncluded(list, "9");
        }
        long time2 = System.currentTimeMillis();
        System.out.println("test2:안에 선언 " + (time2-time1));
    }    
    
    
}

