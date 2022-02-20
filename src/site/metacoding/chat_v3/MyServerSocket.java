package site.metacoding.chat_v3;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Vector;

/**
 * JWP = 재원프로토콜
 * 1. 최초 메세지는 username
 * 2. 구분자 :
 * 3.ALL : 메세지
 * 4. CHAT : username : msg
 */

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

        String username; // 최초 메세지 담기 (전역으로 관리)

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

        // 메서드 생성
        // ALL:
        public void chatPublic(String msg) {

            try {
                System.out.println("전체채팅");
                for (고객전담스레드 t : 고객리스트) { // 왼쪽: 컬렉션 타입 , 오른쪽 : 컬렉션
                    // 본인이 쓴 문자 본인한테는 안뜨게
                    if (t != this) {
                        t.writer.write(t.socket.getLocalAddress() + username + ":" + msg + "\n");
                        t.writer.flush();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // CHAT: 문지현 : 안녕
        public void chatPrivate(String receiver, String mag) {

            try {
                for (고객전담스레드 t : 고객리스트) { // 왼쪽: 컬렉션 타입 , 오른쪽 : 컬렉션

                    // 본인이 쓴 문자 본인한테는 안뜨게
                    if (t.username.equals(receiver)) { // username 같은지 확인
                        t.writer.write(t.socket.getRemoteSocketAddress() + "[귓속말]" + username + ":" + mag + "\n");
                        t.writer.flush();
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 재원 프로토콜 검사기
        //
        public void jwp(String inputData) {

            // 1. 프로토콜 분리하기 (ALL: 안녕 or CHAT: 재원 : 안녕)
            String[] token = inputData.split(":");
            String protocol = token[0]; // 0번지

            if (protocol.equals("ALL")) {

                String msg = token[1]; // ALL : 메세지 에서 메세지는 1번지
                chatPublic(msg);

            } else if (protocol.equals("CHAT")) {
                String username = token[1];
                String msg = token[2];
                chatPrivate(username, msg);

            } else { // 프로토콜 통과 못함.
                System.out.println("프로토콜 없음");

            }

        }

        @Override
        public void run() { // 메인 스레드가 아닌 쪽에 while

            // 최초 메세지는 username

            try {
                username = reader.readLine();

                // username 받았을때
                isLogin = true;
            } catch (Exception e) {
                isLogin = false; // username 못받았을 때
                System.out.println("username을 받지 못했습니다");
            }

            while (isLogin) {

                try {
                    String inputData = reader.readLine();

                    jwp(inputData);

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
