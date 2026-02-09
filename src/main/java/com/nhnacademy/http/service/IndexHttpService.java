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

package com.nhnacademy.http.service;

import com.nhnacademy.http.request.HttpRequest;
import com.nhnacademy.http.response.HttpResponse;
import com.nhnacademy.http.util.ResponseUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;

@Slf4j
public class IndexHttpService implements HttpService{
    /*TODO#2 /index.html을  처리하는 HttpService 입니다.
        - doGet()method를 구현 합니다.
    */

    @Override
    public void doGet(HttpRequest httpRequest, HttpResponse httpResponse) {

        //Body-설정
        String responseBody = null;
        try {
            responseBody = ResponseUtils.tryGetBodyFromFile("/index.html");
        } catch (IOException e) {
            log.warn("html 파일 읽기 실패: {}", e.getMessage(), e);
            return;
        }

        //Header-설정
        String charset = httpResponse.getCharacterEncoding();
        int bodyLength = responseBody.getBytes(Charset.forName(charset)).length;
        String responseHeader = ResponseUtils.createResponseHeader(200, charset, bodyLength);


        //PrintWriter 응답
        try (PrintWriter bufferedWriter = httpResponse.getWriter()) {
            bufferedWriter.write(responseHeader);
            bufferedWriter.write(responseBody);

            bufferedWriter.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
