package org.sugar.media.service.monitor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.NetworkIF;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;

import java.lang.management.ManagementFactory;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * Date:2025/01/01 16:27:51
 * Author：Tobin
 * Description:
 */

@Service
@EnableScheduling
@Slf4j
public class MonitorService {


    @Value("${server.ip}")
    private String localIp;


    public static final DecimalFormat RATE_DECIMAL_FORMAT = new DecimalFormat("#.##");

    private static final String[] UNITS = {"B", "KB", "MB", "GB", "TB", "PB"};
    private static final int UNIT_THRESHOLD = 1024; // 换算单位为1024


    private long[] prevTicks;

    private long prevBytesSent = 0;
    private long prevBytesRecv = 0;

    private Map<String, Object> currCpuInfoMap = new HashMap<>();


    public String formatByte(long bytes) {
        if (bytes < UNIT_THRESHOLD) {
            return bytes + " B";
        }

        int unitIndex = 0;
        double size = (double) bytes;
        while (size >= UNIT_THRESHOLD && unitIndex < UNITS.length - 1) {
            size /= UNIT_THRESHOLD;
            unitIndex++;
        }

        // 保留两位小数
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        return decimalFormat.format(size) + " " + UNITS[unitIndex];
    }


    public MonitorService() {
        SystemInfo systemInfo = new SystemInfo();
        CentralProcessor processor = systemInfo.getHardware().getProcessor();

        prevTicks = processor.getSystemCpuLoadTicks();
    }


    public void setNetwork() {

    }

    //    @Async
//    @Scheduled(fixedRate = 1000)
    public void getNetwork() throws InterruptedException {

        SystemInfo systemInfo = new SystemInfo();
        List<NetworkIF> networkIFs = systemInfo.getHardware().getNetworkIFs();

        // 找到活跃的网络接口
        NetworkIF network = networkIFs.stream().filter(n -> Arrays.asList(n.getIPv4addr()).contains(this.localIp)).findFirst().orElse(null);

        if (network != null) {


            long bytesSent = network.getBytesSent();
            long bytesRecv = network.getBytesRecv();


            long uploadSpeed = (bytesSent - prevBytesSent); // KB/s
            long downloadSpeed = (bytesRecv - prevBytesRecv); // KB/s


            prevBytesSent = bytesSent;
            prevBytesRecv = bytesRecv;


            Map<String, Object> map = new HashMap<>();

            map.put("uploadSpeed", this.formatByte(uploadSpeed));
            map.put("downloadSpeed", this.formatByte(downloadSpeed));


        } else {
            System.out.println("No active network interface found!");
        }
    }

    @Async
    @Scheduled(fixedRate = 1000)
    public Map<String, Object> printlnCpuInfo() throws InterruptedException {

        Map<String, Object> map = new HashMap<>();

        SystemInfo systemInfo = new SystemInfo();
        CentralProcessor processor = systemInfo.getHardware().getProcessor();

        long[] ticks = processor.getSystemCpuLoadTicks();


//        if (prevTicks == null) return null;
        long user = ticks[CentralProcessor.TickType.USER.getIndex()] - prevTicks[CentralProcessor.TickType.USER.getIndex()];
        long nice = ticks[CentralProcessor.TickType.NICE.getIndex()] - prevTicks[CentralProcessor.TickType.NICE.getIndex()];
        long sys = ticks[CentralProcessor.TickType.SYSTEM.getIndex()] - prevTicks[CentralProcessor.TickType.SYSTEM.getIndex()];
        long idle = ticks[CentralProcessor.TickType.IDLE.getIndex()] - prevTicks[CentralProcessor.TickType.IDLE.getIndex()];
        long iowait = ticks[CentralProcessor.TickType.IOWAIT.getIndex()] - prevTicks[CentralProcessor.TickType.IOWAIT.getIndex()];
        long irq = ticks[CentralProcessor.TickType.IRQ.getIndex()] - prevTicks[CentralProcessor.TickType.IRQ.getIndex()];
        long softirq = ticks[CentralProcessor.TickType.SOFTIRQ.getIndex()] - prevTicks[CentralProcessor.TickType.SOFTIRQ.getIndex()];
        long steal = ticks[CentralProcessor.TickType.STEAL.getIndex()] - prevTicks[CentralProcessor.TickType.STEAL.getIndex()];
        long totalCpu = user + nice + sys + idle + iowait + irq + softirq + steal;


        prevTicks = ticks;
        // cpu核数
        map.put("totalCpu", processor.getLogicalProcessorCount());


        // cpu系统使用率
        map.put("cpuSysUse", RATE_DECIMAL_FORMAT.format(sys * 1.0 / totalCpu));

        // cpu用户使用率
        map.put("cpuUserUse", RATE_DECIMAL_FORMAT.format(user * 1.0 / totalCpu));


        // cpu当前使用率
        map.put("cpuUseRate", RATE_DECIMAL_FORMAT.format(1.0 - (idle * 1.0 / totalCpu)));

        currCpuInfoMap = map;
        return map;
    }


    public Map<String, Object> getCurrCpuInfo() {
        return this.currCpuInfoMap;
    }

    public Map<String, String> getMemoryInfo() {
        System.out.println("----------------主机内存信息----------------");

        Map<String, String> map = new HashMap<>();
        SystemInfo systemInfo = new SystemInfo();
        GlobalMemory memory = systemInfo.getHardware().getMemory();
        //总内存
        long totalByte = memory.getTotal();
        //剩余
        long availableByte = memory.getAvailable();
        System.out.println("总内存 = " + this.formatByte(totalByte));
        map.put("totalMemory", this.formatByte(totalByte));

        // 已用内存
        map.put("useMemory", this.formatByte(totalByte - availableByte));

        // 剩余内存
        map.put("remainingMemory", this.formatByte(availableByte));
        // 内存使用率
        map.put("memoryUsage", RATE_DECIMAL_FORMAT.format((totalByte - availableByte) * 1.0 / totalByte));

        return map;

    }

    public Map<String, String> getSysInfo() {
        System.out.println("----------------操作系统信息----------------");

        HashMap<String, String> map = new HashMap<>();
        Properties props = System.getProperties();

        //系统名称
        String osName = props.getProperty("os.name");
        map.put("osName", osName);
        //架构名称
        String osArch = props.getProperty("os.arch");

        map.put("osArch", osArch);

        SystemInfo si = new SystemInfo();
        OperatingSystem os = si.getOperatingSystem();

        map.put("osInfo", os.toString());

        return map;
    }

    public Map<String, String> getJvmInfo() {
        System.out.println("----------------jvm信息----------------");

        Map<String, String> map = new HashMap<>();
        Properties props = System.getProperties();
        Runtime runtime = Runtime.getRuntime();

        log.info("runtime info:{}", runtime.totalMemory());
        //jvm总内存
        long jvmTotalMemoryByte = runtime.totalMemory();
        //jvm最大可申请
        long jvmMaxMemoryByte = runtime.maxMemory();
        //空闲空间
        long freeMemoryByte = runtime.freeMemory();

        //jdk版本
        String jdkVersion = props.getProperty("java.version");
        //jdk路径
        String jdkHome = props.getProperty("java.home");
        // jvm 运行时间
        long time = ManagementFactory.getRuntimeMXBean().getStartTime();
        LocalDateTime jvmStartTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault());
        Duration duration = Duration.between(jvmStartTime, LocalDateTime.now());

        System.out.println("java版本 = " + jdkVersion);

        map.put("jdkVersion", jdkVersion);
        map.put("jdkHome", jdkHome);
//        System.out.println("jdkHome = " + jdkHome);
//        System.out.println("jvm内存总量 = " + this.formatByte(jvmTotalMemoryByte));
        map.put("jvmTotalMemory", this.formatByte(jvmTotalMemoryByte));

//        System.out.println("jvm最大可用内存总数 = " + this.formatByte(jvmMaxMemoryByte));
//        map.put("jvmTotalMemory", this.formatByte(jvmMaxMemoryByte));

        // 使用空间
        map.put("jvmUseMemory", this.formatByte(jvmTotalMemoryByte - freeMemoryByte));
        // 空闲空间
        map.put("jvmFreeMemory", this.formatByte(freeMemoryByte));

        // jvm内存使用率
        map.put("jvmMemory", RATE_DECIMAL_FORMAT.format((jvmTotalMemoryByte - freeMemoryByte) * 1.0 / jvmTotalMemoryByte));

        map.put("jvmRuntime", this.genTime(duration.toSeconds()));
        return map;
    }

    public void getThread() {
        System.out.println("----------------线程信息----------------");
        ThreadGroup currentGroup = Thread.currentThread().getThreadGroup();

        while (currentGroup.getParent() != null) {
            // 返回此线程组的父线程组
            currentGroup = currentGroup.getParent();
        }
        //此线程组中活动线程的估计数
        int noThreads = currentGroup.activeCount();

        Thread[] lstThreads = new Thread[noThreads];
        //把对此线程组中的所有活动子组的引用复制到指定数组中。
        currentGroup.enumerate(lstThreads);
        for (Thread thread : lstThreads) {
            System.out.println("线程数量：" + noThreads + " 线程id：" + thread.getId() + " 线程名称：" + thread.getName() + " 线程状态：" + thread.getState());
        }
    }

    public List<Map> getDiskInfo() {
        System.out.println("----------------磁盘信息----------------");
        SystemInfo si = new SystemInfo();
        OperatingSystem os = si.getOperatingSystem();
        FileSystem fileSystem = os.getFileSystem();
        List<OSFileStore> fileStores = fileSystem.getFileStores();
        List<Map> list = new ArrayList<>();
        for (OSFileStore fs : fileStores) {

            Map<String, Object> diskInfo = new LinkedHashMap<>();
            diskInfo.put("name", fs.getName());
            diskInfo.put("total", fs.getTotalSpace() > 0 ? this.formatByte(fs.getTotalSpace()) : "?");
            long used = fs.getTotalSpace() - fs.getUsableSpace();
            diskInfo.put("available", this.formatByte(fs.getUsableSpace()));
            diskInfo.put("used", this.formatByte(used));
            diskInfo.put("usageRate", RATE_DECIMAL_FORMAT.format(used * 1.0 / fs.getTotalSpace()));
            list.add(diskInfo);
        }

        return list;
    }


    public String genTime(Long totalSeconds) {


        long days = totalSeconds / (24 * 60 * 60);
        long hours = (totalSeconds % (24 * 60 * 60)) / (60 * 60);
        long minutes = (totalSeconds % (60 * 60)) / 60;
        long seconds = totalSeconds % 60;

        return days + " 天 " + hours + " 小时 " + minutes + " 分钟 " + seconds + " 秒";
    }

}
