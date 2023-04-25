package cn.edu.sustech.cs209.chatting.client;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.ArrayList;

public class Client {
  public static boolean login(String name) {
    try {
      URL url = new URL("http://localhost:8080/login?name=" + name);
      HttpURLConnection con = (HttpURLConnection) url.openConnection();
      con.setRequestMethod("GET");
      return con.getInputStream().read() == 49; // 49 means 1, 48 means 0 (in ascii)
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  public static String[] getNameList(String name) {
    try {
      URL url = new URL("http://localhost:8080/login?getNameList&name=" + name);
      HttpURLConnection con = (HttpURLConnection) url.openConnection();
      con.setRequestMethod("GET");
      return new BufferedReader(new InputStreamReader(con.getInputStream())).readLine().split(" ");
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  public static boolean sendPost(Long timestamp, String sentBy, String sendTo, String data) {
    try {
      URL url = new URL("http://localhost:8080/send");
      String postData = timestamp.toString() + "&" + sentBy + "&" + sendTo + "&" + data;
      HttpURLConnection con = (HttpURLConnection) url.openConnection();
      con.setRequestMethod("POST");

      con.setDoOutput(true);
      DataOutputStream dos = new DataOutputStream(con.getOutputStream());
      dos.writeBytes(postData);
      dos.flush();
      dos.close();
      return con.getInputStream().read() == 49; // 49 means 1, 48 means 0 (in ascii)
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  public static String getMessage(String name, int count) {
    try {
      URL url = new URL("http://localhost:8080/getMessage?" + name + "&" + count);
      HttpURLConnection con = (HttpURLConnection) url.openConnection();
      con.setRequestMethod("GET");

      BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream())); // StandardCharsets.UTF_8
      StringBuilder sb = new StringBuilder();
      String line;
      while ((line = br.readLine()) != null) {
        sb.append(line);
      }
      return sb.toString();
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }
}