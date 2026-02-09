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
import com.nhnacademy.http.util.CounterUtils;
import com.nhnacademy.http.util.ResponseUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;

@Slf4j
public class IndexHttpService implements HttpService{

    @Override
    public void doGet(HttpRequest httpRequest, HttpResponse httpResponse) {

        //Body-설정
        String responseBody = null;

        try {
            //TODO#9 CounterUtils.increaseAndGet()를 이용해서 context에 있는 counter 값을 증가시키고, 반환되는 값을 index.html에 반영 합니다.
            //${count} <-- counter 값을 치환 합니다.
            responseBody = ResponseUtils.tryGetBodyFromFile(httpRequest.getRequestURI());

            long count = CounterUtils.increaseAndGet();
            responseBody = responseBody.replace("${count}", String.valueOf(count));

            String userId = httpRequest.getParameter("userId");

            if (userId != null && !userId.isEmpty()) {
                responseBody = responseBody.replace("</body>", String.format("<h1>%s님 회원가입 되었습니다</h1></body>", userId));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //Header-설정
        String charset = httpResponse.getCharacterEncoding();
        int bodyLength = responseBody.getBytes(Charset.forName(charset)).length;
        String responseHeader = ResponseUtils.createResponseHeader(200, charset, bodyLength);

        //PrintWriter 응답
        try(PrintWriter bufferedWriter = httpResponse.getWriter();){
            bufferedWriter.write(responseHeader);
            bufferedWriter.write(responseBody);
            bufferedWriter.flush();
            log.debug("body:{}",responseBody);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
