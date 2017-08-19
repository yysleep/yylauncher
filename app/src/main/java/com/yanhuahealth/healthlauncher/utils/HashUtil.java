/**
 * @file HashUtil.java
 * @author bluesea
 */
package com.yanhuahealth.healthlauncher.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 常用 hash 算法的工具类，提供 MD5 等 hash 算法
 * 
 * @author steven hu
 */
public class HashUtil {

	private static char HEX_DIGITS[] = {
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
	};
	
	/**
	 * MD5 的 16 或 32 位标志
	 */
	public static final int MD5_16 = 1;
	public static final int MD5_32 = 2;
	
	/**
	 * 指定返回全大写 or 全小写 字母
	 */
	public static final int MD5_LOWER_CASE = 1;
	public static final int MD5_UPPER_CASE = 2;
	
	/**
	 * 获得给定字符串的 16 或 32 位 MD5 hash 值
	 */
	public static String md5(String src, int tag, int alphacase) {

		if (null == src) {
			return null;
		}
		
		return md5(src.getBytes(), tag, alphacase);
	}

    /**
     * 获得给定字符串的 16 或 32 位 MD5 hash 值
     */
    public static String md5(byte[] src, int tag, int alphacase) {

        if (null == src) {
            return null;
        }

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(src);
            byte result[] = md.digest();
            char str[] = new char[16 * 2];
            int k = 0;
            for (int idx = 0; idx < 16; ++idx) {
                byte tmpByte = result[idx];
                str[k++] = HEX_DIGITS[(tmpByte >>> 4) & 0x0F];
                str[k++] = HEX_DIGITS[tmpByte & 0x0F];
            }

            String resultMD5 = new String(str);

            if (tag == MD5_16) {
                return resultMD5.substring(8, 24);
            }

            return resultMD5;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获得给定文件的 16 或 32 位 MD5 hash 值
     */
    public static String md5WithFile(String filePath, int tag, int alphacase) {

        if (null == filePath) {
            return null;
        }

        InputStream is = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            is = new FileInputStream(filePath);
            byte[] buffer = new byte[1024];
            int numRead = 0;
            do {
                numRead = is.read(buffer);
                if (numRead > 0) {
                    md.update(buffer, 0, numRead);
                }
            } while (numRead != -1);

            byte result[] = md.digest();
            char str[] = new char[16 * 2];
            int k = 0;
            for (int idx = 0; idx < 16; ++idx) {
                byte tmpByte = result[idx];
                str[k++] = HEX_DIGITS[(tmpByte >>> 4) & 0x0F];
                str[k++] = HEX_DIGITS[tmpByte & 0x0F];
            }

            String resultMD5 = new String(str);

            if (tag == MD5_16) {
                return resultMD5.substring(8, 24);
            }

            return resultMD5;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
