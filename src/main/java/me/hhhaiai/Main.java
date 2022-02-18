package me.hhhaiai;

import me.hhhaiai.org.json.JSONArray;
import me.hhhaiai.org.json.JSONObject;
import me.hhhaiai.utils.FileUtils;
import me.hhhaiai.utils.Logs;
import me.hhhaiai.utils.TextUtils;

import java.io.File;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

/**
 * @Copyright © 2022 sanbo Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2022/2/17 5:00 PM
 * @author: sanbo
 */
public class Main {


    public static void main(String[] args) {
        test();
//        prepareDir();
//        parserResultAndSaveFile();
    }

    private static void parserResultAndSaveFile() {
        int totalCount = supportAppInfo.size() + notSupportAppNames.size();
        StringBuilder sb = new StringBuilder();
        sb
                .append("二分法覆盖率:").append(chu(supportAppInfo.size(), totalCount)).append("\r\n")
                .append("二分法支持详情:").append(supportAppInfo.size()).append("/").append(totalCount).append("\r\n")
        ;
        if (notSupportAppNames != null && notSupportAppNames.size() > 0) {
            sb.append("二分法覆盖不支持列表[").append(notSupportAppNames.size()).append("]:\r\n")
                    .append("\t").append(new JSONArray(notSupportAppNames).toString()).append("\r\n")
            ;
        }

        // parser sp
        for (int i = 0; i < supportAppInfo.size(); i++) {
            Rinfo info = new Rinfo();

        }


        System.out.println(sb.toString());
        FileUtils.saveTextToFile("result.txt", sb.toString(), false);


    }

    private static float getFloat(int a, int total) {
        DecimalFormat df = new DecimalFormat("0.0000");
        String s = df.format((float) a / total);
        return Float.parseFloat(s);
    }

    private static String chu(int a, int b) {
        NumberFormat percent = NumberFormat.getPercentInstance();
        percent.setMaximumFractionDigits(2);
        double per = new BigDecimal((float) a / b)
                .setScale(4, BigDecimal.ROUND_HALF_UP)
                .doubleValue();
        return percent.format(per);
    }


    /**
     * parser dir get files.
     */
    private static void prepareDir() {
        File f = new File("data/");
        File[] files = f.listFiles();
        for (int i = 0; i < files.length; i++) {
            parserOneApp(files[i]);
        }
    }

    private static void test() {
//        File f = new File("data/net-com.sankuai.meituan.txt");
        File f = new File("data/net-cn.xiaochuankeji.tieba.txt");
        parserOneApp(f);
        parserResultAndSaveFile();

    }

    /**
     * parser one package
     *
     * @param file
     */
    private static void parserOneApp(File file) {
        if (file == null || !file.exists()) {
            Logs.e("[" + file + "] has some error!");
            return;
        }

        List<String> lines = FileUtils.readForArray(file.getAbsolutePath());
        if (lines == null || lines.size() < 1) {
            Logs.e("Has no lines");
            return;
        }
        String pkgName = "", appName = "", versionInfo = "";
        Map<String, Integer> noCloseCountMap = new HashMap<String, Integer>();
        Map<String, Integer> closeCountMap = new HashMap<String, Integer>();
        List<String> totalStatus = new ArrayList<String>();

        for (int i = 0; i < lines.size(); i++) {
            try {
                String line = lines.get(i);
                if (TextUtils.isEmpty(line)) {
                    continue;
                }
                line = line.substring(0, line.length() - 1);
                // System.out.println(line);
            
                JSONObject obj = new JSONObject(line);
                if (obj == null || obj.length() < 1) {
                    continue;
                }
                if (TextUtils.isEmpty(pkgName) || TextUtils.isEmpty(appName) || TextUtils.isEmpty(versionInfo)) {
                    pkgName = new String(obj.optString("Pkg", ""));
                    appName = new String(obj.optString("Name", ""));
                    versionInfo = new String(obj.optString("Version", ""));
//                    Logs.i("pkgName:" + pkgName + "; appName:" + appName + " ;versionInfo:" + versionInfo);
                }
                parserOneAppOneLine(obj, noCloseCountMap, closeCountMap, totalStatus
                );

            } catch (Throwable e) {
                e.printStackTrace();
                continue;
            }
        }

//        Logs.i(" pkgName:" + pkgName + "; appName:" + appName + " ;versionInfo:" + versionInfo + " ;noCloseCountMap:" + noCloseCountMap.size()
//                + " ;closeCount:" + closeCount.size() + " ;totalStatus:" + totalStatus.size()
//        );

        boolean isAlive = false, isClose = false;
        //check support
        for (int i = 0; i < totalStatus.size(); i++) {
            String status = totalStatus.get(i);
            if (!isAlive) {
                if (STATUS_ALIVE.contains(status)) {
                    isAlive = true;
                }
            }
            if (!isClose) {
                if (STATUS_CLOSE.contains(status)) {
                    isClose = true;
                }
            }
        }
        if (isAlive && isClose) {
            Rinfo r = new Rinfo();
            r.appName = appName;
            r.pkgName = pkgName;
            r.versionInfo = versionInfo;

            r.aliveCount = getValueCount(noCloseCountMap);
            r.totalCount = getValueCount(noCloseCountMap) + getValueCount(closeCountMap);
            r.aliveStatus = new HashMap<String, Integer>(noCloseCountMap);
            r.closeStatus = new HashMap<String, Integer>(closeCountMap);
            r.allStatus = new ArrayList<String>(totalStatus);
            supportAppInfo.add(r);
        } else {
            notSupportAppNames.add(pkgName);
        }

    }

    private static int getValueCount(Map<String, Integer> noCloseCountMap) {
        //@TODO

        return 0;
    }


    /**
     * parser one line: one net req
     *
     * @param obj
     * @param noCloseCount
     * @param closeCount
     * @param totalStatus
     */
    private static void parserOneAppOneLine(JSONObject obj, Map<String, Integer> noCloseCount
            , Map<String, Integer> closeCount, List<String> totalStatus) {
        JSONObject cs = obj.optJSONObject("CodeStatistic");
        if (cs == null || cs.length() < 1) {
            return;
        }
        // get alive setup
        String st = obj.optString("Phase", "END");
        for (String statusInfo : cs.keySet()) {
            if (TextUtils.isEmpty(statusInfo)) {
                Logs.e("statusInfo is null!");
                continue;
            }
            int count = cs.optInt(statusInfo, -1);
            if (count < 1) {
                Logs.e("count is less than 1. -->" + count);
                continue;
            }
            if ("END".equalsIgnoreCase(st)) {
                // end status: add  to closeCount
                addToList(closeCount, statusInfo, count);
            } else {
                //other: add  to noCloseCount
                addToList(noCloseCount, statusInfo, count);
            }
            // add to totalStatus
            if (!totalStatus.contains(statusInfo)) {
                totalStatus.add(statusInfo);
            }
        }

    }

    /**
     * tools method. add status into map
     *
     * @param closeCountMap
     * @param statusInfo
     * @param count
     */
    private static void addToList(Map<String, Integer> closeCountMap, String statusInfo, int count) {
        // makesure can work
        if (closeCountMap == null) {
            closeCountMap = new HashMap<String, Integer>();
        }
        // has key
        if (closeCountMap.containsKey(statusInfo)) {
            //add
            int lastCount = closeCountMap.get(statusInfo);
            closeCountMap.put(statusInfo, lastCount + count);
        } else {
            // not has key
            closeCountMap.put(statusInfo, count);
        }
    }


    private static final List<Rinfo> supportAppInfo = new ArrayList<Rinfo>();
    private static final List<String> notSupportAppNames = new ArrayList<String>();
    /**
     * LISTEN(10)、SYNC-RECEIVED(3)、SYNC-SENT(2)、ESTABLISHED（1）
     */
    private static final List<String> STATUS_ALIVE = Arrays.asList("1", "2", "3", "10");
    /**
     * FIN-WAIT-1(4)、 FIN-WAIT-2(5)、CLOSING(11)、TIME-WAIT(6)、CLOSED(7)、CLOSE-WAIT(8)、LAST-ACK(9)
     */
    private static final List<String> STATUS_CLOSE = Arrays.asList("4", "5", "6", "7", "8", "9", "11");


}
