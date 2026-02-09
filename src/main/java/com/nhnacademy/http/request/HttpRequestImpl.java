/*
 * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 * + Copyright 2024. NHN Academy Corp. All rights reserved.
 * + * While every precaution has been taken in the preparation of this resource,  assumes no
 * + responsibility for errors or omissions, or for damages resulting from the use of the information
 * + contained herein
 * + No part of this resource may be reproduced, stored in a retrieval system, or transmitted, in any
 * + form or by any means, electronic, mechanical, photocopying, recording, or otherwise, without the
 * + prior written permission.
 * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 */

package com.nhnacademy.http.request;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class HttpRequestImpl implements HttpRequest {

    private final Socket client;

    private final Map<String,Object> headerMap = new HashMap<>();
    private final Map<String,Object> attributeMap = new HashMap<>();
    private final static String KEY_HTTP_METHOD = "HTTP-METHOD";
    private final static String KEY_QUERY_PARAM_MAP = "HTTP-QUERY-PARAM-MAP";
    private final static String KEY_REQUEST_PATH="HTTP-REQUEST-PATH";
    private final static String HEADER_DELIMER=":";

    public HttpRequestImpl(Socket socket) {
        this.client = socket;
        initialize();
    }

    private void initialize() {

        try{
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(client.getInputStream()));

            String firstLine = bufferedReader.readLine();
            if (Objects.isNull(firstLine) || firstLine.isEmpty()) {
                return;
            }
            log.debug("firstLine:{}", firstLine);
            parseHttpRequestInfo(firstLine);

            String line;
            while ((line = bufferedReader.readLine()) != null && !line.isEmpty()) {
                log.debug("header line:{}", line);
                parseHeader(line);
            }

            //body 파싱
            String contentLengthStr = (String) headerMap.get("Content-Length");
            if (Objects.nonNull(contentLengthStr)) {
                int contentLength = Integer.parseInt(contentLengthStr);
                char[] buffer = new char[contentLength];
                int readCount = 0;
                while (readCount < contentLength) {
                    int count = bufferedReader.read(buffer, readCount, contentLength - readCount);
                    if (count == -1) break;
                    readCount += count;
                }
                String body = new String(buffer, 0, readCount);
                log.debug("body:{}", body);
                parseBody(body);
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

    }
    @Override
    public String getMethod() {
        return String.valueOf(headerMap.get(KEY_HTTP_METHOD));
    }
    @Override
    public String getParameter(String name) {
        return getParameterMap().get(name);
    }

    @Override
    public Map<String, String> getParameterMap() {
        Map<String, String> queryMap = (Map<String, String>) headerMap.get(KEY_QUERY_PARAM_MAP);
        if (Objects.isNull(queryMap)) {
            queryMap = new HashMap<>();
            headerMap.put(KEY_QUERY_PARAM_MAP, queryMap);
        }
        return queryMap;
    }

    @Override
    public String getHeader(String name) {
        return String.valueOf(headerMap.get(name));
    }
    @Override
    public void setAttribute(String name, Object o) {
        attributeMap.put(name,o);
    }
    @Override
    public Object getAttribute(String name) {
        return attributeMap.get(name);
    }
    @Override
    public String getRequestURI() {
        return String.valueOf(headerMap.get(KEY_REQUEST_PATH));
    }

    private void parseHeader(String s){
        int index = s.indexOf(HEADER_DELIMER);
        if (index == -1) return;

        String key = s.substring(0, index).trim();
        String value = s.substring(index + 1).trim();

        if(!key.isEmpty()) {
            headerMap.put(key, value);

            if (value.contains(";")) {
                String[] parts = value.split(";");
                for (int i = 1; i < parts.length; i++) {
                    String part = parts[i].trim();
                    if (part.contains("=")) {
                        String[] kv = part.split("=");
                        if (kv.length == 2) {
                            headerMap.put(kv[0].trim(), kv[1].trim());
                        }
                    }
                }
            }
        }
    }

    private void parseHttpRequestInfo(String s) {
        String arr[] = s.split(" ");
        //http method parse
        if (arr.length > 0) {
            headerMap.put(KEY_HTTP_METHOD, arr[0]);
        }
        //query parameter parse
        if (arr.length > 1) {
            Map<String, String> queryMap = new HashMap<>();
            String fullPath = arr[1];
            int questionIndex = fullPath.indexOf("?");
            String httpRequestPath;

            if(questionIndex >= 0){
                httpRequestPath = fullPath.substring(0, questionIndex);
                String queryString = fullPath.substring(questionIndex + 1);
                if (!queryString.isEmpty()) {
                    String qarr[] = queryString.split("&");
                    for (String q : qarr) {
                        String[] kv = q.split("=");
                        if (kv.length == 2) {
                            queryMap.put(URLDecoder.decode(kv[kv.length-2].trim(), StandardCharsets.UTF_8),
                                    URLDecoder.decode(kv[kv.length-1].trim(), StandardCharsets.UTF_8));
                        }
                    }
                }
            }else{
                httpRequestPath = fullPath;
            }

            //path 설정
            headerMap.put(KEY_REQUEST_PATH, httpRequestPath);

            //queryMap 설정
            headerMap.put(KEY_QUERY_PARAM_MAP, queryMap);
        }
    }

    private void parseBody(String body) {
        if (Objects.isNull(body) || body.isEmpty()) {
            return;
        }

        Map<String, String> queryMap = getParameterMap();

        String[] params = body.split("&");
        for (String param : params) {
            String[] keyValue = param.split("=");
            if (keyValue.length == 2) {
                String key = URLDecoder.decode(keyValue[0].trim(), StandardCharsets.UTF_8);
                String value = URLDecoder.decode(keyValue[1].trim(), StandardCharsets.UTF_8);
                queryMap.put(key, value);
            }
        }
    }
}