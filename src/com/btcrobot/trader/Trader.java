package com.btcrobot.trader;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class Trader {
	//api�ӿڵ�ַ
	private final static String USER_INFO_URI = "https://www.okcoin.com/api/userinfo.do";
	//��Կ
	private final static String SECRET_KEY = "2FCF1D6E7478159ED589512C2D681FCB";
	//������ID
	private final static long PARTNER = 3600763;
	
	public void getUserinfo() throws ClientProtocolException, IOException, JSONException{
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpPost httpPost = new HttpPost("https://www.okcoin.com/api/userinfo.do");
		httpPost.addHeader("Content-Type","application/x-www-form-urlencoded");
		
		//����sign��
		
		//��������
				Map<String,String> sArray = new HashMap<String, String>();
				sArray.put("partner", Long.toString(PARTNER));
				
				//�Բ�������ǩ��
				String sign = buildMysign(sArray, SECRET_KEY);

		 
		 System.out.println(sign);

		
		List <NameValuePair> nvps = new ArrayList <NameValuePair>();
        nvps.add(new BasicNameValuePair("partner", Long.toString(PARTNER)));
        nvps.add(new BasicNameValuePair("sign", sign));
        httpPost.setEntity(new UrlEncodedFormEntity(nvps));
        CloseableHttpResponse response = httpclient.execute(httpPost);

        try {
            

            System.out.println(response.getStatusLine());
            HttpEntity entity = response.getEntity();
  
            InputStream in = entity.getContent();
            
            StringBuffer out = new StringBuffer(); 
            byte[] b = new byte[4096]; 
            for(int n; (n = in.read(b))!= -1;) { 
                    out.append(new String(b, 0, n,"utf-8")); 
            } 
            String result = out.toString(); 
            System.out.println(result);
            JSONObject json = new JSONObject(new JSONTokener(result));  
            System.out.println("code = " + json.getInt("code"));
            System.out.println("message = " + json.getString("msg"));
            System.out.println("time = " + json.getString("time"));
            EntityUtils.consume(entity);
        } finally {
            response.close();
        }
        
	}
	
	   /**
     * ����ǩ�����
     * @param sArray Ҫǩ��������
     * @return ǩ������ַ���
     */
    public static String buildMysign(Map<String, String> sArray,String secretKey) {
    	String mysign = "";
		try {
			String prestr = createLinkString(sArray); //����������Ԫ�أ����ա�����=����ֵ����ģʽ�á�&���ַ�ƴ�ӳ��ַ���
	        prestr = prestr + secretKey; //��ƴ�Ӻ���ַ������밲ȫУ����ֱ����������
	        mysign = getMD5String(prestr);
		} catch (Exception e) {
			e.printStackTrace();
		}
        return mysign;
    }
    /** 
     * ����������Ԫ�����򣬲����ա�����=����ֵ����ģʽ�á�&���ַ�ƴ�ӳ��ַ���
     * @param params ��Ҫ���򲢲����ַ�ƴ�ӵĲ�����
     * @return ƴ�Ӻ��ַ���
     */
    public static String createLinkString(Map<String, String> params) {

        List<String> keys = new ArrayList<String>(params.keySet());
        Collections.sort(keys);
        String prestr = "";
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            String value = params.get(key);
            if (i == keys.size() - 1) {//ƴ��ʱ�����������һ��&�ַ�
                prestr = prestr + key + "=" + value;
            } else {
                prestr = prestr + key + "=" + value + "&";
            }
        }
        return prestr;
    }
    /**
     * ����32λ��дMD5ֵ
     */
    private static final char HEX_DIGITS[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};  
	public static String getMD5String(String str) {
		try {
			if(str==null || str.trim().length() == 0){
				return "";
			}
			byte[] bytes	=	str.getBytes();
			MessageDigest messageDigest = MessageDigest.getInstance("MD5");
			messageDigest.update(bytes);
			bytes = messageDigest.digest();
			StringBuilder sb = new StringBuilder();  
	        for(int i = 0; i < bytes.length; i++){  
	            sb.append(HEX_DIGITS[(bytes[i] & 0xf0) >> 4] + "" + HEX_DIGITS[bytes[i] & 0xf]);  
	        }  
			return sb.toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return "";
	}



	public static void main(String[] args) {
		Trader trader = new Trader();
		try {
			trader.getUserinfo();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
