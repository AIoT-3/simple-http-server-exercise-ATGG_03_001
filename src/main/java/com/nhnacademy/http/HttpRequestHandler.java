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
import java.util.LinkedList;
import java.util.Queue;

@Slf4j
public class HttpRequestHandler implements Runnable {

    private final Queue<Socket> requestQueue;
    private final int MAX_QUEUE_SIZE = 10;

    private static final String CRLF = "\r\n";

    public HttpRequestHandler() {
        //TODO#1 requestQueue를 초기화 합니다. Java에서 Queue의 구현체인 LinkedList를 사용 합니다.
        requestQueue = new LinkedList<>();
    }

    public synchronized void addRequest(Socket client) {

        /* TODO#2 queueSize >= MAX_QUEUE_SIZE 대기 합니다.
            즉 queue에 데이터가 소비될 때 까지 client Socket을 Queue에 등록하는 작업을 대기 합니다.
        */
        if (requestQueue.size() >= MAX_QUEUE_SIZE) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        //TODO#3 requestQueue에 client를 추가 합니다.
        requestQueue.offer(client);

        //TODO#4 대기하고 있는 Thread를 깨웁니다.
        notifyAll();
    }

    public synchronized Socket getRequest() {

        //TODO#5 requestQueue가 비어 있다면 대기 합니다.
        if (requestQueue.isEmpty()) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        //TODO#6 대기하고 있는 Thread를 깨우고, requestQueue에서 client를 반환 합니다.
        Socket request = requestQueue.poll();

        notifyAll();

        return request;
    }

    @Override
    public void run() {

        while (!Thread.currentThread().isInterrupted()) {
            //TODO#7 getRequest()를 호출하여 client를 requestQueue로 부터 얻습니다., requestQueue가 비어있다면 대기 합니다.
            Socket client = getRequest();

            //TODO#8 다음과 같은 message가 응답되도록 구현 합니다. exercise-step2를 참고하세요
            //<html><body><h1>{threadA}:hello java</h1></body></html>
            //<html><body><h1>{threadB}:hello java</h1></body></html>

            try (client;
                 BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));) {

                String responseBody = getResponseBody();
                String responseHeader = getResponseHeader(responseBody);

                writer.write(responseHeader);
                writer.write(responseBody);

                writer.flush();

            } catch (IOException e) {
                log.error("IOException 발생: {}", e.getMessage(), e);
            }
        }
    }

    private String getResponseHeader(String responseBody) {
        StringBuilder responseHeaderBuilder = new StringBuilder();

        responseHeaderBuilder.append(String.format("HTTP/1.0 200 OK%s", CRLF));

        responseHeaderBuilder.append(String.format("Server: HTTP server/0.1%s", CRLF));

        responseHeaderBuilder.append(String.format("Content-type: text/html; charset=%s%s", "UTF-8", CRLF));

        responseHeaderBuilder.append(String.format("Connection: Closed%s", CRLF));

        responseHeaderBuilder.append(String.format("Content-Length: %d%s%s", responseBody.getBytes().length, CRLF, CRLF));

        return responseHeaderBuilder.toString();
    }

    private String getResponseBody() {
        return String.format("<html><body><h1>%s: hello java</h1></body></html>", Thread.currentThread().getName());
    }
}
