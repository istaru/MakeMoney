package com.mx.hb.moon.test.main;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.TreeSet;

public class DeleteRepeated {
    private String str;
    private TreeSet<String> noReapted;//带有String类型的TreeSet泛型

    public DeleteRepeated() {
        Scanner in = new Scanner(System.in);
        System.out.println("输入一个字符串:");
        str = in.nextLine();
        noReapted = new TreeSet();
    }

    //清楚重复的数据
    public void removeRepeated() {
        for (int i = 0; i < str.length(); i++) {
            noReapted.add("" + str.charAt(i));
            //str.charAt(i)返回的是char型  所以先加一个""空格 , 转换成String型
            //TreeSet泛型能保证重复的不加入 , 而且有序
        }

        str = "";

        for (String index : noReapted) {
            str += index;
        }
        //输出
        System.out.println(str);
    }

    public static String removerepeatedchar(String s) {
        if (s == null)
            return s;
        StringBuilder sb = new StringBuilder();
        int i = 0, len = s.length();
        while (i < len) {
            char c = s.charAt(i);
            sb.append(c);
            i++;
            while (i < len && s.charAt(i) == c) {
                i++;
            }
        }
        return sb.toString();
    }

    public static void main(String[] args) {
//        String url = "http://www.ss.com/hh.html?name=a&name=a&type=c&type=c&age=13&age=13";
//        String urlStr = url.substring(0,url.indexOf("?")+1);
//        url = url.substring(url.indexOf("?") + 1,url.length());
//        String[] urls = url.split("&");
//        List<String> lists = new ArrayList<>();
//        for(int i = 0;i < urls.length;i++){
//            lists.add(urls[i]);
//        }
//        List list = new ArrayList(new HashSet(lists));
//        String u = "";
//        for(Object i:list){
//            u += i+"&";
//        }
//        urlStr = (urlStr+u);
//        System.out.println(urlStr.substring(0,urlStr.length() - 1));

//        DeleteRepeated dr = new DeleteRepeated();
//        dr.removeRepeated();

        String str[] = {"http://192.168.1.211:5222/html/index.html",
                "http://192.168.1.211:5222/html/9_9.html",
                "http://192.168.1.211:5222/html/share.html",
                "http://192.168.1.211:5222/html/my.html"};
        Random random = new Random();
        int s = random.nextInt(4)+1;
        String sb = str[s-1].substring(str[s-1].lastIndexOf("/")+1);
//        String sb = str[s-1].substring(str[s-1].lastIndexOf("/")+1);
        System.out.println("最后的值"+sb);
//        if(str[s-1].contains("index.html") ||
//                str[s-1].contains("9_9.html") ||
//                str[s-1].contains("share.html") ||
//                str[s-1].Ccontains("my.html")){
//            System.out.print("包含");
//        } else {
//            System.out.print("不包含");
//        }
    }
}