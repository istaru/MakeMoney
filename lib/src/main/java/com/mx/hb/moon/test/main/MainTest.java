package com.mx.hb.moon.test.main;

import com.mx.hb.moon.test.model.UserTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainTest implements Comparator<UserTest> {
//    @Override
//    public int compare(Object obj1, Object obj2) {
//        return toInt(obj1) - toInt(obj2);
//    }

    @Override
    public int compare(UserTest obj1, UserTest obj2) {
        return obj1.getName().compareTo(obj2.getName());
    }

    private int toInt(Object obj){
        String str = obj + "";
        str = str.replaceAll("a","1");
        str = str.replaceAll("d","4");
        str = str.replaceAll("c","3");
        str = str.replaceAll("b","2");
        str = str.replaceAll("e","5");
        return Integer.parseInt(str);
    }

    public static void main(String[] args) {
//        String[] str = new String[]{"a","d","c","b","e"};
//        Arrays.sort(str, new MainTest());
//        for(int i = 0;i < str.length;i++){
//            System.out.println("排序："+str[i]);
//        }

        List<UserTest> lists = new ArrayList<>();
        lists.add(new UserTest("张三",22));
        lists.add(new UserTest("李四",10));
        lists.add(new UserTest("王五",50));
        lists.add(new UserTest("马六",9));
        lists.add(new UserTest("艾森富",8));
        lists.add(new UserTest("秦熙",16));
        lists.add(new UserTest("刘聪",40));
//        lists.add(new UserTest("Mark",30));
        Collections.sort(lists,new MainTest());
        for(int i = 0;i < lists.size();i++){
            System.out.println("第"+(i+1)+"位的是："+lists.get(i).getName()+"今年"+lists.get(i).getAge()+"岁");
        }
    }
}
