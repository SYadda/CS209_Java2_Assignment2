package cn.edu.sustech.cs209.chatting.server;

import com.sun.net.httpserver.HttpServer;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.OutputStream;
import java.util.stream.Collectors;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;


public class Server {
    public static void main(String[] args) throws IOException{
        HttpServer httpServer = HttpServer.create(new InetSocketAddress(8080), 0);
        httpServer.createContext("/login", new loginHandler());
        httpServer.createContext("/send", new sendHandler());
        httpServer.createContext("/getMessage", new getMessageHandler());
        httpServer.setExecutor(Executors.newFixedThreadPool(10));
        httpServer.start();
        System.out.println("Starting server at " + DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()));
    }
}


class loginHandler implements HttpHandler {
    public final ArrayList<String> curr_login_users = new ArrayList<>();
    @Override
    public void handle(HttpExchange he) {
        try {
            String param = he.getRequestURI().getQuery();
            String response = "";
            if (param.startsWith("name=")) {
                String name = param.split("=")[1];
                boolean valid = true;
                for (String s: curr_login_users) {
                    if (name.equals(s)) {
                        valid = false;
                        break;
                    }
                }
                if (valid) {
                    response = "1";
                    curr_login_users.add(name);
                } else {
                    response = "0";
                }
            } else if (param.startsWith("getNameList&name=")) {
                String name = param.split("=")[1];
                response = curr_login_users.stream().filter(i -> ! i.equals(name)).collect(Collectors.joining(" "));
            }
            he.sendResponseHeaders(200, response.length());
            OutputStream out = he.getResponseBody();
            out.write(response.getBytes());
            out.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

class sendHandler implements HttpHandler {
    public static HashMap<String, ArrayList<Message>> all_Message = new HashMap<>(); //key : sendTo
    public void handle(HttpExchange he) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(he.getRequestBody())); // StandardCharsets.UTF_8
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            String[] messList = sb.toString().split("&");  //key : sendTo (messList[2])
            all_Message.computeIfAbsent(messList[2], key -> new ArrayList<>()).add(new Message(messList[0], messList[1], messList[2], messList[3]));
            he.sendResponseHeaders(200, "1".length());
            OutputStream out = he.getResponseBody();
            out.write("1".getBytes());
            out.close();
        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

class getMessageHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange he) {
        try {
            String[] param = he.getRequestURI().getQuery().split("&");
            String name = param[0];
            int count = Integer.parseInt(param[1]);

            ArrayList<Message> mess_List = sendHandler.all_Message.get(name); // fixme : list is null, count > size
            StringBuilder sb = new StringBuilder();

            for (Message message : mess_List) { // fixme : 可能乱序
                if (count > 0) {
                    count--;
                } else {
                    sb.append(message.getTimestamp());
                    sb.append("##");
                    sb.append(message.getSentBy());
                    sb.append("##");
                    sb.append(message.getSendTo());
                    sb.append("##");
                    sb.append(message.getData());
                    sb.append("&&");
                }
            }
            String response = sb.toString();

            he.sendResponseHeaders(200, response.length());
            OutputStream out = he.getResponseBody();
            out.write(response.getBytes());
            out.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

//class loginHandler implements HttpHandler {
//    @Override
//    public void handle(HttpExchange he) {
//        try {
//            String responseText = "Request Method: " + he.getRequestMethod() + "<br/>"
//                + "Request Parameter: " + getRequestParam(he) + "<br/>"
//                + "Request Header: <br/>" + getRequestHeader(he);
//
//            //生成html
//            String responseStr = "<html>" + "<body>" + responseText + "</body>" + "</html>";
//
//            //设置响应头，必须在sendResponseHeaders方法之前设置！
//            he.getResponseHeaders().add("Content-Type:", "text/html;charset=utf-8");
//
//            //设置响应码和响应体长度，必须在getResponseBody方法之前调用！
//            he.sendResponseHeaders(200, responseStr.length());
//
//            OutputStream out = he.getResponseBody();
//            out.write(responseStr.getBytes(StandardCharsets.UTF_8));
//            out.flush();
//            out.close();
//
//
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//    }
//
//    private String getRequestHeader(HttpExchange httpExchange) {
//        Headers headers = httpExchange.getRequestHeaders();
//        return headers.entrySet().stream()
//            .map((Map.Entry<String, List<String>> entry) -> entry.getKey() + ":" + entry.getValue().toString())
//            .collect(Collectors.joining("<br/>"));
//    }
//
//    private String getRequestParam(HttpExchange httpExchange) throws Exception {
//        String paramStr = "";
//
//        if (httpExchange.getRequestMethod().equals("GET")) {
//            //GET请求读queryString
//            paramStr = httpExchange.getRequestURI().getQuery();
//        } else {
//            //非GET请求读请求体
//            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpExchange.getRequestBody(), StandardCharsets.UTF_8));
//            StringBuilder requestBodyContent = new StringBuilder();
//            String line = null;
//            while ((line = bufferedReader.readLine()) != null) {
//                requestBodyContent.append(line);
//            }
//            paramStr = requestBodyContent.toString();
//        }
//
//        return paramStr;
//    }
//}

class Message {
    private String timestamp; // Long
    private String sentBy;
    private String sendTo;
    private String data;
    public Message(String timestamp, String sentBy, String sendTo, String data) {
        this.timestamp = timestamp;
        this.sentBy = sentBy;
        this.sendTo = sendTo;
        this.data = data;
    }
    public String getTimestamp() {
        return timestamp;
    }
    public String getSentBy() {
        return sentBy;
    }
    public String getSendTo() {
        return sendTo;
    }
    public String getData() {
        return data;
    }
}