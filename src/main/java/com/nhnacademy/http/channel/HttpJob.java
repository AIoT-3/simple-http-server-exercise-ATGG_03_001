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

package com.nhnacademy.http.channel;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Slf4j
public class HttpJob implements Executable {
    private final Socket client;
    private static final String CRLF="\r\n";

    public HttpJob(Socket client) {
        if(Objects.isNull(client)){
            throw new IllegalArgumentException("client Socket is null");
        }
        this.client = client;
    }

    public Socket getClient() {
        return client;
    }

    @Override
    public void execute(){

        //TODO#23 HttpJob는 execute() method를 구현 합니다. step2~3 참고하여 구현합니다.
        //<html><body><h1>thread-0:hello java</h1></body>
        //<html><body><h1>thread-1:hello java</h1></body>
        //<html><body><h1>thread-2:hello java</h1></body>
        //....

        try (client;
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()))) {
            String responseBody = getResponseBody();
            String responseHeader = getResponseHeader(responseBody);

            writer.write(responseHeader);
            writer.write(responseBody);

            writer.flush();
        } catch (IOException e) {
            log.warn("IOException 발생: {}", e.getMessage(), e);
        }
    }

    private String getResponseHeader(String responseBody) {

        StringBuilder responseHeader = new StringBuilder();

        responseHeader.append(String.format("HTTP/1.0 200 OK%s",CRLF));

        responseHeader.append(String.format("Server: HTTP server/0.1%s",CRLF));

        responseHeader.append(String.format("Content-type: text/html; charset=%s%s","UTF-8",CRLF));

        responseHeader.append(String.format("Connection: close%s",CRLF));

        int bodyLength = responseBody.getBytes(StandardCharsets.UTF_8).length;

        responseHeader.append(String.format("Content-Length:%d%s%s",bodyLength, CRLF,CRLF));

        return responseHeader.toString();
    }

    private String getResponseBody() {
        return String.format("<html><body><h1>%s:hello java</h1></body>", Thread.currentThread().getName());
    }
}
