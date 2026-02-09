package com.nhnacademy.http.service;

import com.nhnacademy.http.request.HttpRequest;
import com.nhnacademy.http.response.HttpResponse;
import com.nhnacademy.http.util.ResponseUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.PrintWriter;

@Slf4j
public class NotFoundHttpService implements HttpService{

    @Override
    public void doGet(HttpRequest httpRequest, HttpResponse httpResponse) {
        //Body-설정
        String responseBody = null;

        try {
            responseBody = ResponseUtils.tryGetBodyFromFile(ResponseUtils.DEFAULT_404);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //Header-설정
        String responseHeader = ResponseUtils.createResponseHeader(ResponseUtils.HttpStatus.NOT_FOUND.getCode(), "UTF-8",responseBody.length());

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