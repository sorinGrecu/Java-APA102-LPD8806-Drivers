package com.sorin.grecu.ledstrip.ledStrip;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Sorin on 7/17/2016.
 */
@Slf4j
public class NetworkUtils {

    private static String hostname;

    /**
     * Since java will return all network addresses, we need to filter for the local one
     */
    public static Optional<String> getLocalIpAddress(String regex) {
        try {
            Enumeration e = NetworkInterface.getNetworkInterfaces();
            while (e.hasMoreElements()) {
                NetworkInterface n = (NetworkInterface) e.nextElement();
                Enumeration ee = n.getInetAddresses();
                while (ee.hasMoreElements()) {
                    InetAddress i = (InetAddress) ee.nextElement();
                    String address = i.getHostAddress().trim();
                    if (inputMatchesRegex(address, regex)) {
                        return Optional.of(address);
                    }
                }
            }
        } catch (SocketException e1) {
            log.error("Failed to get local IP address: {}", e1.getMessage());
        }
        return Optional.empty();
    }

    public static String getLocalHostname() {
        if (StringUtils.isNotBlank(hostname)) {
            return hostname;
        }

        try {
            hostname = executeSystemCommand("hostname");
        } catch (IOException e) {
            e.printStackTrace();
        }

        log.info("Fetched hostname: {}", hostname.toLowerCase());
        return hostname;
    }

    public static boolean isMachineRpi() {
        String os = System.getProperty("os.name");
        if (os.toLowerCase().contains("windows") || os.toLowerCase().contains("mac")) {
            return false;
        }
        return true;
    }

    private static boolean inputMatchesRegex(String input, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        return matcher.find();
    }

    public static String executeSystemCommand(String command) throws IOException {
        Runtime runtime = Runtime.getRuntime();
        Process process;
        process = runtime.exec(command);
        InputStream is = process.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        String line;
        while ((line = br.readLine()) != null) {
            return line;
        }
        return null;
    }
}
