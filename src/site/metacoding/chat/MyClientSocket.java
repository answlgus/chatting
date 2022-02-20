package site.metacoding.chat;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class MyClientSocket {

    Socket socket;
    BufferedWriter writer;

    public MyClientSocket() {
        try {

            socket = new Socket("localhost", 1077); // (ip주소, 포트) 이때 연결
            writer = new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream())

            );

            writer.write("안녕\n");
            writer.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        new MyClientSocket();
    }
}
