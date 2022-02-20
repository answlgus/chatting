package site.metacoding.chat;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class MyServerSocket {

    // 변수만들기
    ServerSocket serverSocket; // 리스너 (연결 = 세션이 만들어짐) 연결되기를 기다리는 포트
    Socket socket; // 메세지 통신시 사용

    BufferedReader reader;

    public MyServerSocket() { // 메모리에 띄우기
        // 통신은 항상 예외처리가 필요함
        try {
            // 1.서버소켓 생성(리스너)
            // well known 포트 번호는 사용하면 안됨 (0 ~ 1023까지)
            serverSocket = new ServerSocket(1077); // 내부적에 while이 존재
            System.out.println("서버 소켓 생성");
            socket = serverSocket.accept(); // while 돌면서 대기 (클라이언트 접속까지 여기서 멈춤) 랜덤포트
            reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            String inputData = reader.readLine(); // string 으로 return
            System.out.println("받은 메시지 :" + inputData);
            System.out.println("클라이언트 연결");
        } catch (Exception e) {
            System.out.println("통신오류 발생: " + e.getMessage());
            // e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new MyServerSocket();
        System.out.println("메인종료");

    }
}
