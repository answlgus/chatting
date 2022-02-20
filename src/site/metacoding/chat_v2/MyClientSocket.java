package site.metacoding.chat_v2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Scanner;

public class MyClientSocket {

    Socket socket;

    // 키보드로 부터 받아서 쓸 스레드
    BufferedWriter writer;
    Scanner sc;

    // 읽을 스레드
    BufferedReader reader;

    public MyClientSocket() {
        try {

            // 1. 소캣 만들기
            socket = new Socket("127.0.0.1", 2000);

            // 2. 버퍼
            sc = new Scanner(System.in);
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // 3읽기전용 스레드
            new Thread(new 읽기전담스레드()).start();

            // 메인스레드 만들기(쓰기전용)
            while (true) {
                String keyboardInputData = sc.nextLine();
                writer.write(keyboardInputData + "\n"); // 버퍼에 담기
                writer.flush(); // 버퍼에 담긴 것 stream 으로 흘려보내기 (flush = 통신의 시작)
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 3내부클래스 만들기 (장점: 외부 클래스의 전역변수를 다 사용할 수 있다.)
    class 읽기전담스레드 implements Runnable {

        @Override
        public void run() {
            try {
                while (true) {

                    String inputData = reader.readLine();
                    System.out.println("받은메시지: " + inputData);

                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    public static void main(String[] args) {
        new MyClientSocket();
    }
}
