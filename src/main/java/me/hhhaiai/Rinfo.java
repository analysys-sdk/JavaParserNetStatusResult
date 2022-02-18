package me.hhhaiai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Copyright © 2022 sanbo Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2022/2/17 6:52 PM
 * @author: sanbo
 */
public  class Rinfo {
    public static String appName = "", pkgName = "", versionInfo = "";
    public static Map<String, Integer> closeStatus = new HashMap<String, Integer>();
    public static Map<String, Integer> aliveStatus = new HashMap<String, Integer>();
    public static int aliveCount= 0;
    public static int totalCount= 0;
    public static List<String> allStatus = new ArrayList<String>();
}