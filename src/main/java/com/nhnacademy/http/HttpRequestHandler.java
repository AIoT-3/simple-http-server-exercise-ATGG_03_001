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

package com.nhnacademy.http;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

@Slf4j
/* TODO#6 Java에서 Thread는 implements Runnable or extends Thread를 이용해서 Thread를 만들 수 있습니다.
*  implements Runnable을 사용하여 구현 합니다.
*/
public class HttpRequestHandler implements Runnable {
    private final Socket client;

    private final static String CRLF = "\r\n";

    public HttpRequestHandler(Socket client) {
        //TODO#7 생성자를 초기화 합니다., cleint null or socket close 되었다면 적절히 Exception을 발생시킵니다.
        if (client == null || client.isClosed()) {
            throw new IllegalArgumentException("유효하지 않은 소켓입니다.");
        }

        this.client = client;
    }


    public void run() {
        //TODO#8 exercise-simple-http-server-step1을 참고 하여 구현 합니다.
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
             client){

            String request = getRequest(reader);

            log.debug("요청: {}", request);

            String responseBody = getResponseBody();
            String responseHeader = getResponseHeader(responseBody);

            log.debug("응답 헤더: {}", responseHeader);
            log.debug("응답 바디: {}", responseBody);

            writer.write(responseHeader);
            writer.write(responseBody);

            writer.flush();
        } catch (IOException e) {
            log.error("IOException 발생: {}", e.getMessage(), e);
        }
    }

    private String getRequest(BufferedReader reader) throws IOException {
        StringBuilder requestBuilder = new StringBuilder();

        while (true) {
            String line = reader.readLine();
            requestBuilder.append(line).append(System.lineSeparator());

            if (line == null || line.isEmpty()) {
                break;
            }
        }

        return requestBuilder.toString();
    }

    private String getResponseHeader(String responseBody) {
        StringBuilder responseHeaderBuilder = new StringBuilder();

        responseHeaderBuilder.append(String.format("HTTP/1.0 200 OK%s", CRLF));
        responseHeaderBuilder.append(String.format("Server: HTTP server/0.1%s", CRLF));
        responseHeaderBuilder.append(String.format("Content-type: text/html; charset=UTF-8%s", CRLF));
        responseHeaderBuilder.append(String.format("Connection: close%s", CRLF));

        int bodyLength = responseBody.getBytes(StandardCharsets.UTF_8).length;

        responseHeaderBuilder.append(String.format("Content-Length: %d%s%s", bodyLength, CRLF, CRLF));

        return responseHeaderBuilder.toString();
    }

    private String getResponseBody() {
        StringBuilder responseBodyBuilder = new StringBuilder();

        responseBodyBuilder.append("<html>");
        responseBodyBuilder.append("<body>");
        responseBodyBuilder.append("<h1>hello java!</h1>");
        responseBodyBuilder.append("</body>");
        responseBodyBuilder.append("</html>");

        return responseBodyBuilder.toString();
    }
}
