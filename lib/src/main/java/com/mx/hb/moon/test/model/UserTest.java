package com.mx.hb.moon.test.model;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Kiven on 16/6/13.
 */
public class UserTest implements Comparable{
    private int age;
    private String name;

    public UserTest(String name, int age) {
        this.name = name;
        this.age = age;
    }



    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int compareTo(Object obj) {
        return this.age - ((UserTest)obj).age;
    }

    public static void main(String[] str) throws UnsupportedEncodingException, ParseException {
//        List<UserTest> lists = new ArrayList<>();
//        lists.add(new UserTest("张三",22));
//        lists.add(new UserTest("吴伟",10));
//        lists.add(new UserTest("李四",50));
//        lists.add(new UserTest("小普",9));
//        lists.add(new UserTest("小明",1));
//        Collections.sort(lists);
//        for(int i = 0;i < lists.size();i++){
//            System.out.println("第"+(i+1)+"位的是："+lists.get(i).getName()+"今年"+lists.get(i).getAge()+"岁");
//        }

//        System.out.println(checkEmail("kenvi"));

//        LinkedList<String> lList = new LinkedList<String>();
//        lList.add("1");
//        lList.add("2");
//        lList.add("3");
//        lList.add("4");
//        lList.add("5");
//        for(int i = 0;i < lList.size();i++){
//            System.out.println("元素是 : " + lList.get(i));
//        }
//
//        System.out.println("链表的第一个元素是 : " + lList.getFirst());
//        System.out.println("链表最后一个元素是 : " + lList.getLast());

//        List<Map<String,Object>> parents = new ArrayList<Map<String, Object>>();
//        for(int i = 0;i < 10;i++){
//            Map<String,Object> map = new HashMap<String, Object>();
//            map.put("type","支付宝");
//            map.put("money",10);
//            map.put("state","已到账");
//            for(int j = 0;j < i;j++){
//                Map<String,Object> map2 = new HashMap<String, Object>();
//                map2.put("stateName","提现成功");
//                map2.put("typeName","支付宝");
//                map2.put("num","a26fe37e8g"+(j));
//                map2.put("time",new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
//                map2.put("account","979903291@qq.com");
//                map2.put("accountName","朱亮亮");
//                map.put("list",map2);
//            }
//            parents.add(map);
//        }

//        String sr = "1465197632";
//        long date1=Long.parseLong(sr);
//        Date date = new Date(date1 * 1000);
//        SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        System.out.println(sd.format(date));

//        String uId = "3_o47cnv6vbbVF8ykFPEnXr_9dSZe4";
//        String loginType = uId.substring(0,1);
//        int type = Integer.parseInt(loginType);
//        if(type == 3){
//            System.out.print("type等于："+type);
//        } else {
//            System.out.print("type不等于："+type);
//        }
//        String userAccount = uId.substring(2,uId.length());
//        System.out.print("loginType："+loginType+"\nuserAccount："+userAccount);

//        String url = java.net.URLEncoder.encode("http://tabi621.gicp.net:8000/android/members_friend_add/?id=1","utf-8");
//        System.out.print("地址为："+url);

//        String msg = "更新说明：\n1、增强Root能力适配三星、华为等更多机型\n2、安全管理Root授权，覆盖Android5.0版本\n3、批量禁止自启，轻松管理软件自启行为";
//        System.out.print(msg);

//        boolean flag = true;
//        if (flag){
//            System.out.print("flag="+flag);
//        } else {
//            System.out.print("flag="+flag);
//        }
//        String value = "消消乐,快啵同城激情交友,约炮神器,哈撒坑";
//        String[] values = value.split(",");
//        for(int i = 0;i < values.length;i++){
//            System.out.println(values[i]);
//        }

//        String start = changeDate(1,"1473132411");
//        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        String last = simpleDateFormat.format(new Date());
//        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        Date startDate = dateFormat.parse(start);
//        Date lastDate = dateFormat.parse(last);
//        if(isSameData(startDate,lastDate)){
//            System.out.print("是同一天");
//        } else {
//            System.out.print("不是同一天");
//        }

//        System.out.print(pointhangeMoney(0.496));
//        for(int i = 0;i < 6;i++){
//
//
//            i++;
//        }

//        double money = Double.valueOf(10.49) -  10;
//        BigDecimal bigDecimal = new BigDecimal(money);
//        bigDecimal = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP);
//        money = bigDecimal.doubleValue();
//        System.out.print(money);

//        String num1 = "10.8元";
//        String num2 = "10元";
//        if(Double.valueOf(num1.replace("元","")) > Integer.parseInt(num2.replace("元",""))){
//            System.out.print("钱够了");
//        } else {
//            System.out.print("钱不够");
//        }

        String url = "http://www.ss.com/hh.html?name=a&name=a&type=c&type=c";
        url = url.substring(url.indexOf("?") + 1,url.length());
        System.out.print(url);
    }


    /**
     * 将积分换成钱
     * @param money
     * @return
     *  直接删除多余的小数位，如2.35会变成2.3 setScale(1,BigDecimal.ROUND_DOWN)
    进位处理，2.35变成2.4  setScale(1,BigDecimal.ROUND_UP)
    四舍五入，2.35变成2.4  setScale(1,BigDecimal.ROUND_HALF_UP)
    四舍五入，2.35变成2.3，如果是5则向下舍setScaler(1,BigDecimal.ROUND_HALF_DOWN)
     */
    public static double pointhangeMoney(double money){
//        double money = (point / 100) * 0.55;
        BigDecimal bigDecimal = new BigDecimal(money);
        bigDecimal = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP);
        money = bigDecimal.doubleValue();
        return money;
    }

    /**
     * 获取时间的间隔
     * @param inputTime “yyyy-MM-dd HH:mm:ss”这样的格式
     * @return
     */
    public static String getInterval(String inputTime) {
        if (inputTime.length() != 19) {
            return inputTime;
        }
        String result = null;
        try {
            SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            ParsePosition pos = new ParsePosition(0);
            Date d1 = (Date) sd.parse(inputTime, pos);
            // 用现在距离1970年的时间间隔new
            // Date().getTime()减去以前的时间距离1970年的时间间隔d1.getTime()得出的就是以前的时间与现在时间的时间间隔
            long time = new Date().getTime() - d1.getTime();// 得出的时间间隔是毫秒
            if (time / 1000 <= 0) {
                // 如果时间间隔小于等于0秒则显示“刚刚”time/10得出的时间间隔的单位是秒
                result = "刚刚";
            } else if (time / 1000 < 60) {
                // 如果时间间隔小于60秒则显示多少秒前
                int se = (int) ((time % 60000) / 1000);
                result = se + "秒前";
            } else if (time / 60000 < 60) {
                // 如果时间间隔小于60分钟则显示多少分钟前
                int m = (int) ((time % 3600000) / 60000);// 得出的时间间隔的单位是分钟
                result = m + "分钟前";
            } else if (time / 3600000 < 24) {
                // 如果时间间隔小于24小时则显示多少小时前
                int h = (int) (time / 3600000);// 得出的时间间隔的单位是小时
                result = h + "小时前";
            } else if (time / 86400000 < 2) {
                // 如果时间间隔小于2天则显示昨天
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                result = sdf.format(d1.getTime());
                result = "昨天" + result;
            } else if (time / 86400000 < 3) {
                // 如果时间间隔小于3天则显示前天
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                result = sdf.format(d1.getTime());
                result = "前天" + result;
            } else if (time / 86400000 < 30) {
                // 如果时间间隔小于30天则显示多少天前
                SimpleDateFormat sdf = new SimpleDateFormat("MM月dd日 HH:mm");
                result = sdf.format(d1.getTime());
            } else if (time / 2592000000l < 12) {
                // 如果时间间隔小于12个月则显示多少月前
                SimpleDateFormat sdf = new SimpleDateFormat("MM月dd日");
                result = sdf.format(d1.getTime());
            } else {
                // 大于1年，显示年月日时间
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");
                result = sdf.format(d1.getTime());
            }
        } catch (Exception e) {
            return inputTime;
        }
        return result;
    }

    public static String changeDate(int type,String str){
        long date1 = Long.parseLong(str);
        Date date2 = null;
        SimpleDateFormat sd = null;
        if(type == 0){
            date2 = new Date(date1);
            sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        } else {
            date2 = new Date(date1 * 1000);
            sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }
        return sd.format(date2);
    }


    public static boolean isSameData(Date date1, Date date2){
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        boolean isSameYear = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);
        boolean isSameMonth = isSameYear && cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH);
        boolean isSameDate = isSameMonth && cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);
        return isSameDate;
    }


    /**
     * 验证邮箱
     * @param email
     * @return
     */
    public static boolean checkEmail(String alipayAccount){
        boolean flag = false;
//        try{
//            Pattern pattern = Pattern.compile("[0-9]*");
//            Matcher isNum = pattern.matcher(alipayAccount);
//            flag = isNum.matches();
//            if(flag){
//                if(alipayAccount.length() == 11){
//                    flag = true;
//                } else {
//                    flag = false;
//                }
//            } else {
//                String check = "^([a-z0-9A-Z]+[-|_|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
//                Pattern regex = Pattern.compile(check);
//                Matcher matcher = regex.matcher(alipayAccount);
//                flag = matcher.matches();
//            }
//        }catch(Exception e){
//            flag = false;
//        }
//        return flag;
        try{
                String check = "^(([\\u4e00-\\u9fa5]{2,8}))$";
                Pattern regex = Pattern.compile(check);
                Matcher matcher = regex.matcher(alipayAccount);
                flag = matcher.matches();
        }catch(Exception e){

        }
        return flag;
    }

    /**
     * 验证手机号码
     * @param mobiles
     * @return
     */
    public static boolean checkMobileNumber(String mobileNumber){
        boolean flag = false;
        try{
            Pattern regex = Pattern.compile("^(((13[0-9])|(15([0-3]|[5-9]))|(18[0,5-9]))\\d{8})|(0\\d{2}-\\d{8})|(0\\d{3}-\\d{7})$");
            Matcher matcher = regex.matcher(mobileNumber);
            flag = matcher.matches();
        }catch(Exception e){
            flag = false;
        }
        return flag;
    }

}
