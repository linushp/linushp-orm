package com.github.linushp.orm.utils;

import com.github.linushp.orm.model.SqlNdArgs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SQLFormatUtils {

    private static final Pattern PATTERN_REPLACE = Pattern.compile("\\$\\{[a-zA-Z_]+[0-9a-zA-Z_]+}");
    private static final Pattern PATTERN_REPLACE_VALUE = Pattern.compile("#\\{[a-zA-Z_]+[0-9a-zA-Z_]+}");


    public static SqlNdArgs formatSQLAndArgs(String sql, Map<String, ?> map) {
        sql = sql.trim();
        sql = sql.replaceAll("\n", " ");


        //1.将${XXX}替换成常量
        Matcher matcher = PATTERN_REPLACE.matcher(sql);

        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String x = matcher.group();
            x = x.substring(2, x.length() - 1);
            String v = (String) map.get(x);
            matcher.appendReplacement(sb, v);
        }
        matcher.appendTail(sb);
        String sql2 = sb.toString();


        //2.将#{XXX}替换成？
        Matcher matcher2 = PATTERN_REPLACE_VALUE.matcher(sql2);
        StringBuffer sb2 = new StringBuffer();
        List<Object> args = new ArrayList<>();

        while (matcher2.find()) {
            String argName = matcher2.group();
            argName = argName.substring(2, argName.length() - 1);

            Object arg = map.get(argName);
            args.add(arg);


            matcher2.appendReplacement(sb2, "?");
        }

        matcher2.appendTail(sb2);


        String resultSql = sb2.toString();

        return new SqlNdArgs(resultSql, args.toArray());
    }


}
