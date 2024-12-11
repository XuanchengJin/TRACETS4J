package com.extract.service.cpgservice;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

@Component
public class CpgApi {
    private static final Logger logger = LoggerFactory.getLogger(CpgApi.class);
    
    private static final String CONTENT_TYPE_TEXT_JSON = "text/json";
    private static final CloseableHttpClient httpClient = HttpClients.createDefault();
    private static final RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(2000).setSocketTimeout(120000).build();

    /**
     * call cpg api
     */
    public String callCPGAPI(String url, Object param) {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("Context-Type", "application/json;charset=UTF-8");

        httpPost.setConfig(requestConfig);
        String parameter = JSON.toJSONString(param);
        StringEntity se = null;
        try {
            se = new StringEntity(parameter);
        } catch (UnsupportedEncodingException e) {
            logger.error("parameter error.", e);
        }
        se.setContentType(CONTENT_TYPE_TEXT_JSON);
        httpPost.setEntity(se);

        CloseableHttpResponse response;
        String result = null;
        try {
            response = httpClient.execute(httpPost);
            HttpEntity httpEntity = response.getEntity();
            JSONObject jsonObject = JSON.parseObject(EntityUtils.toString(httpEntity, "UTF-8"));
            JSONArray data = jsonObject.getJSONArray("data");
            result = data.get(0).toString();
        } catch (IOException e) {
            logger.error("api request error.", e);
        }
        return result;
    }
}
