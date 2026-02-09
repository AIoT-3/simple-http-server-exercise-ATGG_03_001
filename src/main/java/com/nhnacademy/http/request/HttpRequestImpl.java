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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class HttpRequestImpl implements HttpRequest {
    /* TODO#2 HttpRequest를 구현 합니다.
    *  test/java/com/nhnacademy/http/request/HttpRequestImplTest TestCode를 실행하고 검증 합니다.
    */
    private final Map<String, List<String>> headerMap = new HashMap<>();
    private final Map<String, String> parameterMap = new HashMap<>();
    private final Map<String, Object> attributeMap = new HashMap<>();

    private final Socket client;
    private String method;
    private String requestURI;

    public HttpRequestImpl(Socket client) {
        this.client = client;

        init();
    }

    private void init() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
            String requestLine = reader.readLine();

            parseRequestLine(requestLine);

            String headerLine;

            while ((headerLine = reader.readLine()) != null && !headerLine.isEmpty()) {
                parseHeader(headerLine);
            }
        } catch (IOException e) {
            log.warn("IOException 발생: {}", e.getMessage(), e);
        }
    }

    private void parseRequestLine(String requestLine) {
        String[] parts = requestLine.split(" ");

        method = parts[0].trim();

        String[] uriParts = parts[1].split("\\?");

        requestURI = uriParts[0].trim();

        if (uriParts.length < 2) {
            return;
        }

        String[] paramParts = uriParts[1].split("&");

        for (String parameter : paramParts) {
            String[] paramAndValue = parameter.split("=");
            String param = paramAndValue[0].trim();
            String value = paramAndValue[1].trim();
            parameterMap.put(param, value);
        }
    }

    private void parseHeader(String headerLine) {
        String[] headerAndValue = headerLine.split(":") ;
        String header = headerAndValue[0].trim();

        if (headerAndValue.length < 2) {
            return;
        }

        List<String> values = Arrays.stream(headerAndValue[1].split(";"))
                .map(String::trim)
                .toList();

        headerMap.put(header, values);
    }

    @Override
    public String getMethod() {
        return method;
    }

    @Override
    public String getParameter(String name) {
        return parameterMap.get(name);
    }

    @Override
    public Map<String, String> getParameterMap() {
        return parameterMap;
    }

    @Override
    public String getHeader(String name) {
        return String.join(";", headerMap.get(name));
    }

    @Override
    public void setAttribute(String name, Object o) {
        attributeMap.put(name, o);
    }

    @Override
    public Object getAttribute(String name) {
        return attributeMap.get(name);
    }

    @Override
    public String getRequestURI() {
        return requestURI;
    }
}
