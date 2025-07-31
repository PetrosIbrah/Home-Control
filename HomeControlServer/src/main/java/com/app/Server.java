package com.app;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import org.json.JSONArray;
import org.json.JSONObject;


public class Server  {
    private static final int PORT = 41007;
    static volatile boolean running = true;
    public static List<String[]> devices = new ArrayList<>();

    public static void main(String[] args) {

        String filename = "Devices.json";

        PullJsonDevices(filename);
        Scanner scanner = new Scanner(System.in);
        String input;
        while (true) {
            System.out.print("Would you like to add more Device Entries?: ");
            input = scanner.nextLine();
            if (input.equalsIgnoreCase("Y") || input.equalsIgnoreCase("yes")) {
                System.out.print("Host Name: ");
                String newHost = scanner.nextLine();
                System.out.print("Ip Address: ");
                String newIp = scanner.nextLine();
                System.out.print("NetWork Name: ");
                String newNet = scanner.nextLine();
                devices.add(new String[]{newHost, newIp, newNet});
            } else {
                break;
            }
        }
        scanner.close();
        writeDevicesToJsonFile(devices, "Devices.json");
        System.out.println("=====================");

        String CurrentHost = " ";
        String CurrentNetWorkName = " ";

        String osName = System.getProperty("os.name");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            String ipAddress = InetAddress.getLocalHost().getHostAddress();

            CurrentHost = "HostName";
            CurrentNetWorkName = "NetworkName";
            for (String[] device : devices) {
                if (device[1].equals(ipAddress)) {
                    CurrentHost = device[0];
                    CurrentNetWorkName = device[2];
                }
            }

            final String FinalHost = CurrentHost;
            final String FinalNetWorkName = CurrentNetWorkName;

            System.out.println(FinalHost + " is listening on port " + PORT);

            while (running) {
                Socket clientSocket = serverSocket.accept();


                new Thread(() -> {
                    try (
                            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
                    ) {
                        String command = in.readLine();
                        if (!command.equals("Echo Silent")) {
                            System.out.println("Client connected");
                            System.out.println("Received: " + command);
                        }

                        switch (command) {
                            case "Echo":
                                out.println(FinalNetWorkName + " - " + osName);
                                break;
                            case "Restart":
                                out.println(FinalHost + " - Rebooting...");
                                try {
                                    ProcessBuilder Command;
                                    if (osName.equals("Windows 10") || osName.equals("Windows 11")){
                                        Command= new ProcessBuilder(
                                                "shutdown",
                                                "/r",
                                                "/f",
                                                "/t",
                                                "0"
                                        );
                                    } else {
                                        Command = new ProcessBuilder(
                                                "sudo",
                                                "shutdown",
                                                "-r",
                                                "now"
                                        );
                                    }
                                    Command.inheritIO();
                                    Command.redirectErrorStream(true);
                                    Command.start();
                                } catch (Exception e) {
                                    System.out.println("Error Restart " + e);
                                }
                                break;
                            case "Shutdown":
                                out.println(FinalHost + " - Shutting down...");
                                try {
                                    ProcessBuilder Command;
                                    if (osName.equals("Windows 10") || osName.equals("Windows 11")){
                                        Command = new ProcessBuilder(
                                                "shutdown",
                                                "/s",
                                                "/f",
                                                "/t",
                                                "0"
                                        );
                                    } else {
                                        Command = new ProcessBuilder(
                                                "sudo",
                                                "shutdown",
                                                "now"
                                        );
                                    }
                                    Command.inheritIO();
                                    Command.redirectErrorStream(true);
                                    Command.start();
                                    break;
                                } catch (Exception e) {
                                    System.out.println("Error Shutdown " + e);
                                }
                            case "Restore":
                                out.println(FinalNetWorkName + " - Restoring");

                                try {
                                    ProcessBuilder Command;
                                    if (osName.equals("Windows 10") || osName.equals("Windows 11")){
                                        Command = new ProcessBuilder(
                                                "shutdown",
                                                "/s",
                                                "/f",
                                                "/t",
                                                "0"
                                        );
                                    } else {
                                        Command = new ProcessBuilder(
                                                "sudo",
                                                "shutdown",
                                                "now"
                                        );
                                    }
                                    Command.inheritIO();
                                    Command.redirectErrorStream(true);
                                    Command.start();
                                    break;
                                } catch (Exception e) {
                                    System.out.println("Error Restart " + e);
                                }
                                break;
                            case "Echo Silent":
                                out.println(FinalNetWorkName + " - " + osName);
                                break;
                            default:
                                out.println(FinalNetWorkName + " - Unknown command");
                        }

                        clientSocket.close();
                    } catch (IOException e) {
                        System.out.println("Error handling client connection" + e);
                    }
                }).start();
            }
        } catch (IOException e) {
            System.out.println("Server socket error" + e);
        }

        throw new RuntimeException("Unexpected termination of server loop");
    }


    public static void writeDevicesToJsonFile(List<String[]> devices, String filename) {
        JSONArray jsonArray = new JSONArray();

        for (String[] device : devices) {
            JSONObject obj = new JSONObject();
            obj.put("Host", device[0]);
            obj.put("IP", device[1]);
            obj.put("Network", device[2]);
            jsonArray.put(obj);
        }

        try (FileWriter writer = new FileWriter(filename)) {
            writer.write(jsonArray.toString(2));
            System.out.println(filename + " created.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void PullJsonDevices(String filename) {
        File file = new File(filename);

        if (!file.exists()) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write("[]");
                System.out.println("Created empty JSON file: " + filename);
            } catch (IOException e) {
                System.out.println("Failed to create empty JSON file: " + e);
                return;
            }
        }

        String json = " ";
        try {
            json = new String(Files.readAllBytes(Paths.get(filename)));
        } catch (IOException e) {
            System.out.println("Error handling client connection" + e);
        }
        devices.clear();

        JSONArray jsonArray = new JSONArray(json);

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject obj = jsonArray.getJSONObject(i);

            String host = obj.optString("Host", "");
            String ip = obj.optString("IP", "");
            String network = obj.optString("Network", "");

            if (!host.isEmpty() && !ip.isEmpty() && !network.isEmpty()) {
                devices.add(new String[]{host, ip, network});
            }
        }
    }
}