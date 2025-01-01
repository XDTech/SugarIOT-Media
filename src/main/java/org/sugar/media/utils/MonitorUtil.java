package org.sugar.media.utils;

import com.sun.management.OperatingSystemMXBean;
import org.springframework.util.ClassUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.lang.management.ManagementFactory;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.Formatter;
import java.util.StringTokenizer;

public class MonitorUtil {

    private static final int BYTE_TO_KB = 1024;

    private static final int BYTE_TO_MB = 1024 * 1024;

    private static final int BYTE_TO_GB = 1024 * 1024 * 1024;

    private static final String CMD = "cat /proc/net/dev";

    private static final String WINCMD = "netstat -e";

    /**
     * 获取操作系统名称
     *
     * @return
     */
    public static String getOsName() {
        String osName = System.getProperty("os.name");
        return osName;
    }

    /**
     * 获取系统cpu负载(百分比不带百分号)
     *
     * @return
     */
    public static double getSystemCpuLoad() {
        OperatingSystemMXBean osmxb = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        double systemCpuLoad = osmxb.getSystemCpuLoad() * 100;
        return formatNumber(systemCpuLoad);
    }

    /**
     * 获取物理内存负载(百分比不带百分号)
     *
     * @return
     */
    public static double getMemoryLoad() {
        OperatingSystemMXBean osmxb = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        double totalMemorySize = osmxb.getTotalPhysicalMemorySize() / BYTE_TO_MB;
        double usedMemory = (osmxb.getTotalPhysicalMemorySize() - osmxb.getFreePhysicalMemorySize()) / BYTE_TO_MB;
        double memorySizeLoad = usedMemory / totalMemorySize * 100;
        return formatNumber(memorySizeLoad);
    }

    /**
     * 获取总的物理内存（单位:M）
     *
     * @return
     */
    public static long getTotalMemorySize() {
        OperatingSystemMXBean osmxb = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        long totalMemorySize = osmxb.getTotalPhysicalMemorySize() / BYTE_TO_MB;
        return totalMemorySize;
    }

    /**
     * 获取剩余的物理内存（单位:M）
     *
     * @return
     */
    public static long getFreePhysicalMemorySize() {
        OperatingSystemMXBean osmxb = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        long freePhysicalMemorySize = osmxb.getFreePhysicalMemorySize() / BYTE_TO_MB;
        return freePhysicalMemorySize;
    }

    /**
     * 获取已使用的物理内存（单位:M）
     *
     * @return
     */
    public static long getUsedMemory() {
        OperatingSystemMXBean osmxb = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        long usedMemory = (osmxb.getTotalPhysicalMemorySize() - osmxb.getFreePhysicalMemorySize()) / BYTE_TO_MB;
        return usedMemory;
    }

    /**
     * JVM内存负载(百分比不带百分号)
     *
     * @return
     */
    public static double getJvmMemoryLoad() {
        Runtime rt = Runtime.getRuntime();
        long jvmTotal = rt.totalMemory();
        long jvmFree = rt.freeMemory();
        long jvmUse = jvmTotal - jvmFree;
        double jvmMemoryLoad = (double) jvmUse / jvmTotal * 100;
        return formatNumber(jvmMemoryLoad);
    }

    /**
     * 获取jvm线程负载(百分比不带百分号)
     *
     * @return
     */
    public static double getProcessCpuLoad() {
        OperatingSystemMXBean osmxb = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        double ProcessCpuLoad = osmxb.getProcessCpuLoad() * 100;
        return formatNumber(ProcessCpuLoad);
    }

    /**
     * JVM内存的空闲空间（单位:M）
     *
     * @return
     */
    public static long getJvmFreeMemory() {
        Runtime rt = Runtime.getRuntime();
        long jvmFree = rt.freeMemory() / BYTE_TO_MB;
        return jvmFree;
    }

    /**
     * JVM内存已用的空间（单位:M）
     *
     * @return
     */
    public static long getJvmUseMemory() {
        Runtime rt = Runtime.getRuntime();
        long jvmTotal = rt.totalMemory() / BYTE_TO_MB;
        long jvmFree = rt.freeMemory() / BYTE_TO_MB;
        long jvmUse = jvmTotal - jvmFree;
        return jvmUse;
    }

    /**
     * JVM总内存空间（单位:M）
     *
     * @return
     */
    public static long getJvmTotalMemory() {
        Runtime rt = Runtime.getRuntime();
        long jvmTotal = rt.totalMemory() / BYTE_TO_MB;
        return jvmTotal;
    }

    /**
     * JVM最大能够申请的内存（单位:M）
     *
     * @return
     */
    public static long getJvmMaxMemory() {
        Runtime rt = Runtime.getRuntime();
        long jvmMax = rt.maxMemory() / BYTE_TO_MB;
        return jvmMax;
    }

    /**
     * 获取磁盘已使用大小（单位：G）
     *
     * @return
     */
    public static long getTotalDisk() {
        File[] roots = File.listRoots();// 获取磁盘分区列表
        long total = 0;
        for (File file : roots) {
            total += file.getTotalSpace();
        }
        return total / BYTE_TO_GB;
    }

    /**
     * 获取磁盘已使用大小（单位：G）
     *
     * @return
     */
    public static long getUsedDisk() {
        File[] roots = File.listRoots();// 获取磁盘分区列表
        long free = 0;
        long total = 0;
        for (File file : roots) {
            free += file.getFreeSpace();
            total += file.getTotalSpace();
        }
        long used = (total - free) / BYTE_TO_GB;
        return used;
    }

    /**
     * 获取磁盘未使用大小（单位：G）
     *
     * @return
     */
    public static long getFreeSpace() {
        File[] roots = File.listRoots();// 获取磁盘分区列表
        long free = 0;
        for (File file : roots) {
            free += file.getFreeSpace();
        }
        return free / BYTE_TO_GB;
    }

    /**
     * 获取磁盘负载（单位：G）
     *
     * @return
     */
    public static double getDiskLoad() {
        File[] roots = File.listRoots();// 获取磁盘分区列表
        long freeTotal = 0;
        long total = 0;
        for (File file : roots) {
            freeTotal += file.getFreeSpace();
            total += file.getTotalSpace();
        }
        long useTotal = total - freeTotal;
        double spaceLoad = (double) useTotal / total * 100;
        return formatNumber(spaceLoad);
    }

    /**
     * 获取下载速度
     *
     * @return
     */
    public static String getDownloadSpeed(long sleepTime) {
        if (getOsName().toLowerCase().startsWith("windows")) { // 判断操作系统类型是否为：windows
            return getDownloadSpeedForWindows(sleepTime);
        } else {
            return getLinuxDownloadSpeed(sleepTime);
        }
    }

    /**
     * 获取上传速度
     *
     * @return
     */
    public static String getUploadSpeed(long sleepTime) {
        if (getOsName().toLowerCase().startsWith("windows")) { // 判断操作系统类型是否为：windows
            return getUploadSpeedForWindows(sleepTime);
        } else {
            return getLinuxUploadSpeed(sleepTime);
        }
    }

    /**
     * 获取Windows环境下网口的下行速率
     *
     * @return
     */
    public static String getDownloadSpeedForWindows(long sleepTime) {
        Process pro1;
        Process pro2;
        Runtime r = Runtime.getRuntime();
        BufferedReader input;
        String downloadSpeed = "";
        try {
            pro1 = r.exec(WINCMD);
            input = new BufferedReader(new InputStreamReader(pro1.getInputStream()));
            String result1[] = readInLine(input);
            Thread.sleep(sleepTime * 1000);
            pro2 = r.exec(WINCMD);
            input = new BufferedReader(new InputStreamReader(pro2.getInputStream()));
            String result2[] = readInLine(input);
            downloadSpeed = Double.toString(formatNumber((double) (Long.parseLong(result2[0]) - Long.parseLong(result1[0])) / BYTE_TO_KB / (sleepTime))); // 平均到每秒的上行速率(KB/s)
            pro1.destroy();
            pro2.destroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return downloadSpeed;
    }

    /**
     * 获取Windows环境下网口的上行速率
     *
     * @return
     */
    public static String getUploadSpeedForWindows(long sleepTime) {
        Process pro1;
        Process pro2;
        Runtime r = Runtime.getRuntime();
        BufferedReader input;
        String uploadSpeed = "";
        try {
            pro1 = r.exec(WINCMD);
            input = new BufferedReader(new InputStreamReader(pro1.getInputStream()));
            String result1[] = readInLine(input);
            Thread.sleep(sleepTime * 1000);
            pro2 = r.exec(WINCMD);
            input = new BufferedReader(new InputStreamReader(pro2.getInputStream()));
            String result2[] = readInLine(input);
            uploadSpeed = Double.toString(formatNumber((double) (Long.parseLong(result2[1]) - Long.parseLong(result1[1])) / BYTE_TO_KB / (sleepTime))); // 平均到每秒的下行速率(KB/s)
            pro1.destroy();
            pro2.destroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return uploadSpeed;
    }

    /**
     * 获取Linux环境下网口的下行速率
     *
     * @return
     */
    public static String getLinuxDownloadSpeed(long time) {
        String info1 = null;
        String info2 = null;
        try {
            info1 = MonitorUtil.runCommand(CMD);
            Thread.sleep(time * 1000);
            info2 = MonitorUtil.runCommand(CMD);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String[] data1 = info1.split("\n");
        String[] data2 = info2.split("\n");
        long receiveBytes1 = 0;
        long receiveBytes2 = 0;

        long receiveBytes = 0;
        for (int i = 2; i < data1.length; i++) {
            if (data1[i].trim().startsWith("eth0")) {
                String[] numdata1 = data1[i].trim().split(" +");
                String[] numdata2 = data2[i].trim().split(" +");
                receiveBytes1 = Long.parseLong(numdata1[1]);
                receiveBytes2 = Long.parseLong(numdata2[1]);
                receiveBytes = receiveBytes2 - receiveBytes1;
            }
        }
        return Long.toString(receiveBytes / time / BYTE_TO_KB);
    }

    /**
     * 获取Linux环境下网口的上行速率
     *
     * @return
     */
    public static String getLinuxUploadSpeed(long time) {
        String info1 = null;
        String info2 = null;
        try {
            info1 = MonitorUtil.runCommand(CMD);
            Thread.sleep(time * 1000);
            info2 = MonitorUtil.runCommand(CMD);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String[] data1 = info1.split("\n");
        String[] data2 = info2.split("\n");
        long transmitBytes1 = 0;
        long transmitBytes2 = 0;

        long transmitBytes = 0;
        for (int i = 2; i < data1.length; i++) {
            if (data1[i].trim().startsWith("eth0")) {
                String[] numdata1 = data1[i].trim().split(" +");
                String[] numdata2 = data2[i].trim().split(" +");
                transmitBytes1 = Long.parseLong(numdata1[8]);
                transmitBytes2 = Long.parseLong(numdata1[8]);
                transmitBytes = transmitBytes2 - transmitBytes1;
            }
        }
        return Long.toString(transmitBytes / time / BYTE_TO_KB);
    }

    /**
     * springboot项目用java -jar运行的jar包运行目录
     *
     * @return
     */
    public static String getJarPath() {
        String jarPath = ClassUtils.getDefaultClassLoader().getResource("").getPath();
        String[] sarry = jarPath.split("/");
        String path = "";
        for (int i = 1; i < sarry.length - 5; i++) {
            path += "/" + sarry[i];
        }
        return path;
    }

    /**
     * 获取本地IP
     *
     * @return
     */
    public static String getLocalIp() {
        /*InetAddress addr = null;
        try {
            addr = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return addr.getHostAddress();*/
        return getRouterIp();
    }

    /**
     * 获取路由分配的IP
     *
     * @return
     */
    @SuppressWarnings("unchecked")
    public static String getRouterIp() {
        String SERVER_IP = null;
        try {
            Enumeration allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            InetAddress ip = null;
            while (allNetInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
                Enumeration addresses = netInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    ip = (InetAddress) addresses.nextElement();
                    if (ip != null && ip instanceof Inet4Address) {
                        if (!"127.0.0.1".equals(ip.getHostAddress())) {
                            SERVER_IP = ip.getHostAddress();
                        }
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return SERVER_IP;
    }

    /**
     * 获取公网IP
     *
     * @return
     * @throws IOException
     */
    public static String getServerIp() throws IOException {
        InputStream ins = null;
        try {
            URL url = new URL("https://ip.cn/");
            URLConnection con = url.openConnection();
            con.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");

            ins = con.getInputStream();
            InputStreamReader isReader = new InputStreamReader(ins, "utf-8");
            BufferedReader bReader = new BufferedReader(isReader);
            StringBuffer webContent = new StringBuffer();
            String str;
            while ((str = bReader.readLine()) != null) {
                webContent.append(str);
            }
            int start = webContent.indexOf("<code>") + 6;
            int end = webContent.indexOf("</code");
            return webContent.substring(start, end);
        } finally {
            if (ins != null) {
                ins.close();
            }
        }
    }

    /**
     * 获取网口上下行速率
     *
     * @param input
     * @return
     */
    public static String[] readInLine(BufferedReader input) {
        String rxResult = "";
        String txResult = "";
        StringTokenizer tokenStat;
        try {
            input.readLine();
            input.readLine();
            input.readLine();
            input.readLine();
            tokenStat = new StringTokenizer(input.readLine());
            tokenStat.nextToken();
            rxResult = tokenStat.nextToken();
            txResult = tokenStat.nextToken();
        } catch (Exception e) {
            e.printStackTrace();
        }
        String arr[] = {rxResult, txResult};
        return arr;
    }

    /**
     * Java运行Linux命令
     *
     * @param CMD
     * @return
     */
    public static String runCommand(String CMD) {
        String info = "";
        try {
            Process pos = Runtime.getRuntime().exec(CMD);
            pos.waitFor();
            InputStreamReader isr = new InputStreamReader(pos.getInputStream());
            LineNumberReader lnr = new LineNumberReader(isr);
            String line = "";
            while ((line = lnr.readLine()) != null) {
                info = info + line + "\n";
            }
            pos.destroy();
        } catch (IOException e) {
            info = e.toString();
        } catch (Exception e) {
            info = e.toString();
        }
        return info;
    }

    /**
     * 格式化浮点数保留两位小数
     *
     * @param d
     * @return
     */
    private static double formatNumber(double d) {
        return Double.parseDouble(new Formatter().format("%.2f", d).toString());
    }

}
