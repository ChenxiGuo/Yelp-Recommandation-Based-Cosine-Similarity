package analyzer;

import java.util.Comparator;
//import java.awt.List;

import java.util.*;
import java.util.Map.Entry;



public class SortByValue implements Comparator<Map.Entry<String, Integer>>    
{    
    public int compare(Map.Entry<String, Integer> mp1, Map.Entry<String, Integer> mp2)     
    {    
        return mp2.getValue() - mp1.getValue();    
    }    
}    
