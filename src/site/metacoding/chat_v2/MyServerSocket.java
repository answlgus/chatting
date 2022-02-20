package site.metacoding.chat_v2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Vector;

public class MyServerSocket {

    // 서버에 필요한 것
    // 리스너 (연결 받기) -> 메인 스레드
    ServerSocket serverSocket;
    List<고객전담스레드> 고객리스트; // heap에 뜬 것 보관

    // Socket socket; (바이트 스트림 선) - > 지역변수로 만들기 (계속 연결 받아야 하기 때문)
    // 메세지 받아서 보내기(클라이언트 수에 따라)

    public MyServerSocket() {

        try {
            serverSocket = new ServerSocket(2000);
            고객리스트 = new Vector<>(); // 동기화가 처리된 ArrayList (동시접근이 있으면 동기화 시켜주는게 좋다)

            // 계속 연결받아야 하기 때문 (메인스레드)
            while (true) {
                Socket socket = serverSocket.accept(); // accept가 연결 요청 받음 , 여기서 기다림
                System.out.println("클라이언트 연결"); // 확인

                // 고객전담스레드 만들기 (garbage collection 피하기 위해)
                고객전담스레드 t = new 고객전담스레드(socket); // socket 넘기기
                고객리스트.add(t); // socket을 가진 t 를 리스트에 담기
                System.out.println("고객리스트 크기 : " + 고객리스트.size()); // 크기 = 몇명이 연결되었는지
                new Thread(t).start();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // 내부 클래스 생성 (socket 담기)
    class 고객전담스레드 implements Runnable {

        Socket socket;

        BufferedReader reader;
        BufferedWriter writer;

        boolean isLogin;

        public 고객전담스레드(Socket socket) {

            this.socket = socket;

            try {
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        @Override
        public void run() { // 메인 스레드가 아닌 쪽에 while

            while (isLogin) {

                try {
                    String inputData = reader.readLine();
                    System.out.println("from 클라이언트: " + inputData);

                    // 메세지 받았기 때문에 List<고객전담스레드> 고객리스트에 담기
                    // 모든 클라이언트에 메세지 전송(for문 돌려서)
                    // for : 컬렉션의 크기 만큼만 반복
                    for (고객전담스레드 t : 고객리스트) { // 왼쪽: 컬렉션 타입 , 오른쪽 : 컬렉션
                        t.writer.write(inputData + "\n");
                        t.writer.flush();

                    }

                } catch (Exception e) {

                    try {
                        System.out.println("통신실패: " + e.getMessage());
                        isLogin = false; // while 종료 - > run 종료
                        고객리스트.remove(this); // 스레드 자체를 날렸기 때문에 garbage collection 대상

                        // 언젠가는 garbage collection 됨(조금이라도 더 빨리 버리기 위해)
                        reader.close();
                        writer.close();
                        socket.close();
                    } catch (Exception e1) {
                        System.out.println("연결해제 프로세스 실패" + e1.getMessage());

                    }
                }

            }

        }

    }

    public static void main(String[] args) {
        new MyServerSocket();
    }
}
