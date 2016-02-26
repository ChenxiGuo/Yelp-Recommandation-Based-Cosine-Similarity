
import java.util.Map.Entry;
import java.math.*;
import java.util.*;
public class Test {
    public int majorityElement(int[] nums) {
        int l=nums.length;
        int times = l%2==1?(l-1)/2:l/2;
        int a=0;
        HashMap<Integer,Integer> numsMap= new HashMap<Integer,Integer>();
        for(int num:nums){
            if(!numsMap.containsKey(num)){
                numsMap.put(num,1);
            }
            else{
                int newValue = (int)numsMap.get(num)+1;
                numsMap.remove(num);
                numsMap.put(num,newValue);
            }
        }
        
        for(Map.Entry entry: numsMap.entrySet()){
            if((int)entry.getValue() > times)
                a = (int)entry.getValue();
        }
        return a;
    }
}