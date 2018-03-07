package com.mx.hb.moon.test.model;

import com.alibaba.fastjson.JSON;

import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by Moon on 2016/4/5.
 */
public class AES {

    private static List<DemoBean> mDataList = new ArrayList<DemoBean>();
    /**
     * 获取10位随机数
     * @return
     */
    public static long get10Random() {
        Random random = new Random();
        String result = "";
        for (int i = 0; i < 10; i++) {
            result += (random.nextInt(10));
        }
        String resultFirst = result.substring(0, 1);
        String resultSecond = result.substring(1, result.length());
        if (resultFirst.contains("0")) {
            resultFirst = "1";
            result = resultFirst + resultSecond;
        }
        long poor = (Long.parseLong(result.trim()));
        return poor;
    }

    /**
     * 将生成的10随机数减2
     * @param random
     * @return
     */
    public static String longMinusNum(String random){
        long poor = (Long.parseLong(random.trim())) - 2;
        return poor+"";
    }

    /**
     * 16位小md5加密
     *
     * @return md5(value) or ""
     */
    public static String md5(String val) {
        String result = "";
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(val.getBytes());
            byte b[] = md.digest();
            int i;
            StringBuffer buf = new StringBuffer("");
            for (int offset = 0; offset < b.length; offset++) {
                i = b[offset];
                if (i < 0)
                    i += 256;
                if (i < 16)
                    buf.append("0");
                buf.append(Integer.toHexString(i));
            }
            result = buf.substring(8,8+16);
//            System.out.println("MD5(" + val + ",32) = " + result);
//            System.out.println("MD5(" + val + ",16) = " + buf.toString().substring(8, 24));
        } catch (Exception e) {
            System.out.println(e);
        }
        return result;
    }

    /**
     * 加密
     *
     * @param content key
     * @return
     */
    public static String encrypt(String content,String key) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            int blockSize = cipher.getBlockSize();

            byte[] dataBytes = content.getBytes();
            int plaintextLength = dataBytes.length;
            if (plaintextLength % blockSize != 0) {
                plaintextLength = plaintextLength + (blockSize - (plaintextLength % blockSize));
            }
            byte[] plaintext = new byte[plaintextLength];
            System.arraycopy(dataBytes, 0, plaintext, 0, dataBytes.length);
            SecretKeySpec keyspec = new SecretKeySpec(key.getBytes(), "AES");
            IvParameterSpec ivspec = new IvParameterSpec(key.getBytes());
            cipher.init(Cipher.ENCRYPT_MODE, keyspec, ivspec);
            byte[] encrypted = cipher.doFinal(plaintext);
            return MyBase64.encode(encrypted);
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        return null;
    }

    /**
     * 解密
     *
     * @param data
     * @return
     */
    public static String decrypt(String data, String key) {
        try {
            String text = MyBase64.decode(data.getBytes());
            byte[] by = convertTobyte(text);
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            SecretKeySpec keyspec = new SecretKeySpec(key.getBytes(), "AES");
            IvParameterSpec ivspec = new IvParameterSpec(key.getBytes());
            cipher.init(Cipher.DECRYPT_MODE, keyspec, ivspec);
            byte[] original = cipher.doFinal(by);
            String originalString = new String(original);
            return originalString.trim();
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        return null;
    }

    /**
     * @param data
     * @return
     */
    private static byte[] convertTobyte(String data) {
        int maxLength = data.length();
        byte[] by = new byte[maxLength];
        char[] chars = data.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            by[i] = (byte) chars[i];
        }
        return by;
    }

    public static void main(String[] str){
//        String data = "{\"msys\":\"1\"}";
//        long secret = AES.get10Random();
//        String content = AES.encrypt(data,md5(longMinusNum(secret+"")));
//        System.out.println("key:"+secret);
//        System.out.println("加密数据:"+content);

//        String content = "dxAcsX8JudBtCTKXeeuMDA==";
//        String secret = "4253351003";
//        String result = decrypt(content,md5(longMinusNum(secret+"")));
//        System.out.print("解密数据:"+result);

//        String msg = "{\"user_name\":\"朱亮亮\",\"user_id\":\"jNS6PugAE6\",\"sfuid\":0,\"user_headImgUrl\":\"http://graph.facebook.com/107642933039094/picture?type=small\"}";
//        msg = msg.substring(0,msg.length()-1)+","+"\"need_refresh\""+":"+0+"}";
//        System.out.print("拼出来的数据为:"+msg);
//        System.out.println("原始值："+System.currentTimeMillis());
//        System.out.print("拼接值："+date());

        String url = "data:image/jpg;base64,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";

        System.out.print("数据："+ url.split(",")[1]);
    }

    private static List<DemoBean> getData() {
        for (int i = 0; i < 3; i++) {
            DemoBean bean = new DemoBean();
            bean.setTimeend("2014-12-12 12:45:60");
            bean.setTime("2016-02-02");
            bean.setPaytype("微信支付");
            bean.setMoney("0.02");
            bean.setOuttradeno("P2013123123156");
            mDataList.add(bean);

        }

        for (int i = 0; i < 3; i++) {
            DemoBean bean = new DemoBean();
            bean.setTimeend("2016-02-06 12:45:60");
            bean.setTime("2016-02-06");
            bean.setPaytype("支付宝支付");
            bean.setMoney("0.04");
            bean.setOuttradeno("P2013123123156");
            mDataList.add(0, bean);
        }
        for (int i = 0; i < 3; i++) {
            DemoBean bean = new DemoBean();
            bean.setTimeend("2016-02-06 12:45:60");
            bean.setTime("2016-02-06");
            bean.setPaytype("唐人支付");
            bean.setMoney("0.04");
            bean.setOuttradeno("P2013123123156");
            mDataList.add(0, bean);
        }
        for (int i = 0; i < 3; i++) {
            DemoBean bean = new DemoBean();
            bean.setTimeend("2016-02-08 12:45:60");
            bean.setTime("2016-02-08");
            bean.setPaytype("QQ支付");
            bean.setMoney("0.04");
            bean.setOuttradeno("P2013123123156");
            mDataList.add(0, bean);
        }
        return mDataList;
    }

    private static long date() {
//        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        String date = "";
//        try {
//            Date d1 = df.parse("2006-11-26 12:00:00");
//            Date d2 = df.parse("2006-11-26 11:50:00");
//            long diff = d1.getTime() - d2.getTime();//这样得到的差值是微秒级别
//
//            long days = diff / (1000 * 60 * 60 * 24);
//            long hours = (diff - days * (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
//            long minutes = (diff - days * (1000 * 60 * 60 * 24) - hours * (1000 * 60 * 60)) / (1000 * 60);
//            date = days + "天" + hours + "小时" + minutes + "分";
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        String current = (System.currentTimeMillis())+"";
        current = current.substring(0,10);
        long xzTime = Long.parseLong(current);
        System.out.println("第一个值："+xzTime);
        long hqtime = 1448183952;
        System.out.println("第二个值："+hqtime);
        System.out.println("一减二的差值："+(xzTime - hqtime));
        long s = (xzTime - hqtime) / 60;
        return s;
    }

}
