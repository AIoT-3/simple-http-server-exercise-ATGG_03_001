package com.nhnacademy.http.service;

import com.nhnacademy.http.request.HttpRequest;
import com.nhnacademy.http.response.HttpResponse;
import com.nhnacademy.http.util.ResponseUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;

@Slf4j
public class RegisterHttpService implements HttpService {
    @Override
    public void doGet(HttpRequest httpRequest, HttpResponse httpResponse) {
        String responseBody = null;
        try {
            responseBody = ResponseUtils.tryGetBodyFromFile(httpRequest.getRequestURI());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String charset = httpResponse.getCharacterEncoding();
        int bodyLength = responseBody.getBytes(Charset.forName(charset)).length;

        String responseHeader = ResponseUtils.createResponseHeader(200, charset, bodyLength);

        try (PrintWriter bufferedWriter = httpResponse.getWriter()) {
            bufferedWriter.write(responseHeader);
            bufferedWriter.write(responseBody);
            bufferedWriter.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void doPost(HttpRequest httpRequest, HttpResponse httpResponse) {
        String id = httpRequest.getParameter("userId");

        String charset = httpResponse.getCharacterEncoding();
        String location = String.format("http://localhost:8080/index.html?userId=%s", id);

        String responseHeader = ResponseUtils.createRedirectHeader(charset, location);

        try (PrintWriter bufferedWriter = httpResponse.getWriter()) {
            bufferedWriter.write(responseHeader);
            bufferedWriter.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
