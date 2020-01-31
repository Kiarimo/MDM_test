package com.abupdate.mdm.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/*
 * @date   : 2019/09/20
 * @author : LIRENQI
 * #eamil  : lirenqi@adups.com
 */
public class StringUtils {
    /**
     * 获取字串的字串类型的sha256值 <br>
     *
     * @param strSrc
     *            输入内容为字串
     *
     * @return 字串类型的sha256值，小写
     */
    public static String getStringSha256(String strSrc) {
        MessageDigest digest = null ;
        try {
            digest = MessageDigest. getInstance( "SHA-256");
        } catch (NoSuchAlgorithmException e1) {
            e1.printStackTrace();
        }
        digest.reset();
        byte [] data = digest.digest(strSrc.getBytes());

        return String.format( "%0" + (data.length * 2) + "X", new BigInteger(1, data)).toLowerCase();
    }

    /**
     * 获取byte[]类型的sha256值 <br>
     *
     * @param buff
     *            输入内容为byte[]
     *
     * @return byte[]类型的sha256值
     */
    public static byte[] getByteSha256(byte[] buff) {
        MessageDigest digest = null ;
        try {
            digest = MessageDigest. getInstance( "SHA-256");
        } catch (NoSuchAlgorithmException e1) {
            e1.printStackTrace();
        }
        digest.reset();
        byte [] data = digest.digest(buff);

        return data;//String.format( "%0" + (data.length * 2) + "X", new BigInteger(1, data)).toLowerCase();
    }

    /**
     * 拼接两个byte[] <br>
     *
     * @param data1
     *            输入内容为byte[]
     *
     * @param data2
     *            输入内容为byte[]
     *
     * @return 拼接后的byte[]
     */
    public static byte[] getMergeBytes(byte[] data1,byte[] data2) {
        byte[] data3 = new byte[data1.length+data2.length];
        System.arraycopy(data1,0,data3,0,data1.length);
        System.arraycopy(data2,0,data3,data1.length,data2.length);
        return data3;
    }

    /**
     * 比较两个byte[]是否一样 <br>
     *
     * @param data1
     *            输入内容为byte[]
     *
     * @param data2
     *            输入内容为byte[]
     *
     * @return true:相同，false:不相同
     */
    public static boolean byteCompare(byte[] data1, byte[] data2) {
        if (data1 == null && data2 == null) {
            return true;
        }

        if (data1 == null || data2 == null) {
            return false;
        }

        if (data1.length != data2.length) {
            return false;
        }

        if (data1 == data2) {
            return true;
        }

        boolean bEquals = true;
        for (int i = 0; i < data1.length; i++) {
            if (data1[i] != data2[i]) {
                bEquals = false;
                break;
            }
        }
        return bEquals;
    }

    /**
     * 获取文件的字串类型的sha256值 <br>
     *
     * @param filePath
     *            输入内容为文件的字串路径
     *
     * @return 字串类型的sha256值
     */
    public static String getFileSha256(String filePath) {

        File file= new File(filePath);
        if(!file.exists()){
            return null;
        }

        FileInputStream in = null;
        MessageDigest messagedigest;

        try {
            messagedigest = MessageDigest.getInstance("SHA-256");

            byte[] buffer = new byte[1024 * 100];
            int len = 0;

            in = new FileInputStream(file);
            while ((len = in.read(buffer)) >0) {
                //该对象通过使用 update（）方法处理数据
                messagedigest.update(buffer, 0, len);
            }

            return  new String(byte2hex(messagedigest.digest()));

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


        return  null;
    }


    /**
     * int转换为hex字串 <br>
     *
     * @param buffer
     *            输入内容为int
     *
     * @return 字串类型的hex值，不带0x
     */
    public static String int2hex(int buffer){
//        LogUtils.d(buffer);
        String temp = Integer.toHexString(buffer);

        if (temp.length()%2 !=0) {
            temp = "0" + temp;
        }

        return temp;
    }

    /**
     * hex字串转换为int <br>
     *
     * @param buffer
     *            输入内容为String
     *
     * @return int类型的值
     */
    public static int hex2int(String buffer){
        int value = Integer.parseInt(buffer, 16);
//        LogUtils.d(buffer + " -> " + value);
        return value;
    }

    /**
     * long转换为hex字串 <br>
     *
     * @param buffer
     *            输入内容为long
     *
     * @return 字串类型的hex值，不带0x
     */
    public static String long2hex(long buffer){
//        LogUtils.d(buffer);
        String temp = Long.toHexString(buffer);

        if (temp.length()%2 !=0) {
            temp = "0" + temp;
        }

        int count = temp.length()/2;
        if (count < 8) {
            for (int i=0;i < 8-count;i++) {
                temp = "00" + temp;
            }
        }

        return temp;
    }

    /*
     * Convert hex string to long.这里我们可以利用Long.parseLong(hex)来转换成16进制字符串。
     * @param hex string
     * @return src long
     */
    public static Long hex2long(String buffer){
        Long value = Long.parseLong(buffer, 16);
        return value;
    }

    /**
     * byte[]转换为hex字串 <br>
     *
     * @param buffer
     *            输入内容为byte[]
     *
     * @return 字串类型的hex值，不带0x
     */
    public static String byte2hex(byte[] buffer){
//        LogUtils.d(new String(buffer));
        String h = "";
        for(int i = 0; i < buffer.length; i++){
            String temp = Integer.toHexString(buffer[i] & 0xFF);
            if(temp.length() == 1){
                temp = "0" + temp;
            }
            h = h + temp;
        }
        return h.toUpperCase();
    }

    /**
     * Convert char to byte
     * @param c char
     * @return byte
     */
    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    /**
     * Convert hex string to byte[]
     * @param hexString the hex string
     * @return byte[]
     */
    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    /**
     * get string md5
     * @param string
     * @return String md5
     */
    public static String getMD5(String string) {
        byte[] encodeBytes = null;

        try {
            encodeBytes = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException neverHappened) {
            return null;
        }

        return byte2hex(encodeBytes);
    }

    private static final String HTTP_UTF_8 = "UTF-8";
    public static String encodeUrl(Map<String, String> parameters) {
        if (parameters == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String key : parameters.keySet()) {
            if (key.startsWith("_") || parameters.get(key) == null) {
                continue;
            }
            if (first) {
                first = false;
                sb.append("?");
            } else {
                sb.append("&");
            }
            try {
                sb.append(URLEncoder.encode(key, HTTP_UTF_8));
                sb.append("=");
                sb.append(URLEncoder.encode(String.valueOf(parameters.get(key)).trim(), HTTP_UTF_8));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    public static String mapToString(Map<String, String> map) {
        if (map == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (first) {
                first = false;
            } else {
                sb.append("&");
            }
            try {
                sb.append(URLEncoder.encode(entry.getKey(), HTTP_UTF_8));
                sb.append( "=" );
                sb.append(null==entry.getValue()?"":entry.getValue());
//                sb.append(null==entry.getValue()?"":URLEncoder.encode(entry.getValue(), HTTP_UTF_8));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        return sb.toString();
    }

    public static Map<String, String> stringToMap(String mapStr) {
        Map<String, String> map = new HashMap();
        StringTokenizer item;
        StringTokenizer entrys = new StringTokenizer(mapStr, "&");
        while(entrys.hasMoreTokens()){
            item = new StringTokenizer(entrys.nextToken(), "=");
            String key = item.nextToken();
            String value = item.hasMoreTokens()?item.nextToken():"";
            map.put(key, value);
        }
        return map;
    }
}