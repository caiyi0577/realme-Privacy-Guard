package com.caiyi.rpg;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ReportStyle {
    public static SpannableString format(String text,Context context) {
        boolean dark=(context.getResources().getConfiguration().uiMode&Configuration.UI_MODE_NIGHT_MASK)==Configuration.UI_MODE_NIGHT_YES;
        int red=dark?Color.rgb(255,107,107):Color.rgb(183,28,28);
        SpannableString styled=new SpannableString(text);
        Matcher matches=Pattern.compile("(?m)^状态：阻断失败[^\\r\\n]*|^  失败：[^\\r\\n]*|阻断失败 [1-9][0-9]* 项").matcher(text);
        while(matches.find()) {
            styled.setSpan(new ForegroundColorSpan(red),matches.start(),matches.end(),Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            styled.setSpan(new StyleSpan(Typeface.BOLD),matches.start(),matches.end(),Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return styled;
    }
    public static String verify(Context context) {
        String sample="状态：阻断（规则就绪）\n状态：阻断失败（关键规则缺失）\n状态：未阻断（未勾选）\n状态：未运行（待启动）\n阻断失败 1 项";
        SpannableString text=format(sample,context);
        int at=sample.indexOf("状态：阻断失败");
        ForegroundColorSpan[] red=text.getSpans(at,at+7,ForegroundColorSpan.class);
        if(red.length!=1||Color.red(red[0].getForegroundColor())<=Color.green(red[0].getForegroundColor()))return "FAIL: missing red failure status";
        StyleSpan[] bold=text.getSpans(at,at+7,StyleSpan.class);
        if(bold.length!=1||bold[0].getStyle()!=Typeface.BOLD)return "FAIL: missing bold failure status";
        for(String normal:new String[]{"状态：阻断（","状态：未阻断（","状态：未运行（"}) {
            int start=sample.indexOf(normal);
            if(text.getSpans(start,start+normal.length(),ForegroundColorSpan.class).length!=0)return "FAIL: normal status colored red";
        }
        int total=sample.indexOf("阻断失败 1 项");
        if(text.getSpans(total,sample.length(),ForegroundColorSpan.class).length!=1)return "FAIL: failure count not red";
        if(format("阻断失败 0 项",context).getSpans(0,8,ForegroundColorSpan.class).length!=0)return "FAIL: zero failure count colored red";
        return "PASS: failure status and nonzero count are red and bold; other states and zero count stay normal";
    }
    private ReportStyle(){ }
}
