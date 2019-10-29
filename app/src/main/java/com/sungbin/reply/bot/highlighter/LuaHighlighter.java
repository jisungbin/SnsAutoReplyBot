package com.sungbin.reply.bot.highlighter;

import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;

import com.sungbin.reply.bot.utils.Utils;

public class LuaHighlighter {

    final String[] blueData = {"Bot", "NIL", "function", "return", "end", "local", "if", "then", "else", "switch", "elseif", "for", "while", "do", "break", "continue", "case", "in", "with", "true", "false", "new", "null", "undefined", "typeof", "delete", "try", "catch", "finally", "prototype", "this", "super", "default", "print", "repeat", "console"};
    final String[] redData = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};

    private int blue = Color.argb(255, 21, 101, 192);
    private int red = Color.argb(255, 191, 54, 12);
    private int green = Color.argb(255, 139, 195, 74);
    private int brown = Color.argb(255, 255, 160, 0);

    public LuaHighlighter(Context ctx){
        if(!Utils.readData(ctx, "brown", "null").equals("null")) brown = Color.parseColor(Utils.readData(ctx, "brown", "null"));
        if(!Utils.readData(ctx, "green", "null").equals("null")) green = Color.parseColor(Utils.readData(ctx, "green", "null"));
        if(!Utils.readData(ctx, "blue", "null").equals("null")) blue = Color.parseColor(Utils.readData(ctx, "blue", "null"));
        if(!Utils.readData(ctx, "red", "null").equals("null")) red = Color.parseColor(Utils.readData(ctx, "red", "null"));
    }

    public void apply(Editable s) {
        String str = s.toString();
        if (str.length() == 0) return;
        ForegroundColorSpan spans[] = s.getSpans(0, s.length(), ForegroundColorSpan.class);
        for (int n = 0; n < spans.length; n++) {
            s.removeSpan(spans[n]);
        }
        highlightForLua(s, str);
        codeHighlight(s, str);
    }

    private void codeHighlight(Editable s, String str) {
        for (int n = 0; n < blueData.length; n++) {
            int start = 0;
            while (start >= 0) {
                int index = str.indexOf(blueData[n], start);
                int end = index + blueData[n].length();
                if (index >= 0) {
                    if (s.getSpans(index, end, ForegroundColorSpan.class).length == 0 && isSeperated(str, index, end - 1))
                        if(n>0) s.setSpan(new ForegroundColorSpan(blue),
                                index, end,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        else s.setSpan(new ForegroundColorSpan(blue),
                                index, end,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                } else {
                    end = -1;
                }
                start = end;
            }
        }
        for (int n = 0; n < redData.length; n++) {
            int start = 0;
            while (start >= 0) {
                int index = str.indexOf(redData[n], start);
                int end = index + 1;
                if (index >= 0) {
                    if (s.getSpans(index, end, ForegroundColorSpan.class).length == 0 && checkNumber(str, index))
                        s.setSpan(new ForegroundColorSpan(red),
                                index, end,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                } else {
                    end = -1;
                }
                start = end;
            }
        }
    }

    private void highlightForLua(Editable s, String str){
        int start = 0;
        while (start >= 0) {
            int index = str.indexOf("--[[", start);
            int end = str.indexOf("]]", index + 4);
            if (index >= 0 && end >= 0) {
                s.setSpan(new ForegroundColorSpan(green),
                        index, end + 2,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {
                end = -5;
            }
            start = end + 2;
        }

        start = 0;
        while (start >= 0) {
            int index = str.indexOf("--", start);
            int end = str.indexOf("\n", index + 1);
            if (index >= 0 && end >= 0) {
                s.setSpan(new ForegroundColorSpan(green),
                        index, end,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {
                end = -1;
            }
            start = end;
        }

        start = 0;
        while (start >= 0) {
            int index = str.indexOf("\"", start);
            ForegroundColorSpan[] span = s.getSpans(index, index + 1, ForegroundColorSpan.class);
            while(index>0&&str.charAt(index-1)=='\\'||span.length>0) {
                index = str.indexOf("\"", index + 1);
                span = s.getSpans(index, index + 1, ForegroundColorSpan.class);
            }

            int end = str.indexOf("\"", index + 1);
            while(end>0&&str.charAt(end-1)=='\\') {
                end = str.indexOf("\"", end + 1);
            }

            if (index >= 0 && end >= 0) {
                span = s.getSpans(index, end + 1, ForegroundColorSpan.class);
                if (span.length > 0) {
                    if (str.substring(index + 1, end).contains("--[[") && str.substring(index + 1, end).contains("]]")) {
                        for(int n=0;n<span.length;n++) {
                            s.removeSpan(span[n]);
                        }
                        s.setSpan(new ForegroundColorSpan(Color.argb(255, 255, 160, 0)),
                                index, end + 1,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    } else if (str.substring(index + 1, end).contains("--")) {
                        span = s.getSpans(index, str.indexOf("\n", end), ForegroundColorSpan.class);
                        for(int n=0;n<span.length;n++) {
                            s.removeSpan(span[n]);
                        }
                        s.setSpan(new ForegroundColorSpan(brown),
                                index, end + 1,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                } else {
                    s.setSpan(new ForegroundColorSpan(brown),
                            index, end + 1,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            } else {
                end = -5;
            }
            start = end + 1;
        }
        start = 0;
        while (start >= 0) {

            int index = str.indexOf("'", start);
            ForegroundColorSpan[] span = s.getSpans(index, index + 1, ForegroundColorSpan.class);
            while(index>0&&str.charAt(index-1)=='\\'||span.length>0){
                index = str.indexOf("'", index+1);
                span = s.getSpans(index, index + 1, ForegroundColorSpan.class);
            }

            int end = str.indexOf("'", index + 1);
            while(end>0&&str.charAt(end-1)=='\\'){
                end = str.indexOf("'", end + 1);
            }
            if (index >= 0 && end >= 0) {
                span = s.getSpans(index, end + 1, ForegroundColorSpan.class);
                if (span.length > 0) {
                    if (str.substring(index + 1, end).contains("--[[") && str.substring(index + 1, end).contains("]]")) {
                        for(int n=0;n<span.length;n++) {
                            s.removeSpan(span[n]);
                        }
                        s.setSpan(new ForegroundColorSpan(Color.argb(255, 255, 160, 0)),
                                index, end + 1,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    } else if (str.substring(index + 1, end).contains("--")) {
                        span = s.getSpans(index, str.indexOf("\n", end), ForegroundColorSpan.class);
                        for(int n=0;n<span.length;n++) {
                            s.removeSpan(span[n]);
                        }
                        s.setSpan(new ForegroundColorSpan(brown),
                                index, end + 1,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                } else {
                    s.setSpan(new ForegroundColorSpan(brown),
                            index, end + 1,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            } else {
                end = -5;
            }
            start = end + 1;
        }
    }

    private boolean checkNumber(String str, int index) {
        int start = getStartPos(str, index);
        int end = getEndPos(str, index);
        if (str.charAt(end - 1) == '.') return false;
        if (start == 0) {
            if (str.charAt(start) == '.') return false;
            return isNumber(str.substring(start, end));
        } else {
            if (str.charAt(start + 1) == '.') return false;
            return isNumber(str.substring(start + 1, end));
        }
    }

    private static boolean isSplitPoint(char ch) {
        if (ch == '\n') return true;
        return " []{}()=-*/%&|!?:;,<>=^~".contains(ch + "");
    }

    public void allBlack(Editable e){
        e.setSpan(new ForegroundColorSpan(Color.BLACK), 0, e.toString().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private int getStartPos(String str, int index) {
        while (index > 0) {
            if (isSplitPoint(str.charAt(index))) return index;
            index--;
        }
        return 0;
    }

    private int getEndPos(String str, int index) {
        while (str.length() > index) {
            if (isSplitPoint(str.charAt(index))) return index;
            index++;
        }
        return str.length();
    }

    private boolean isSeperated(String str, int start, int end) {
        boolean front = false;
        char[] points = " []{}()+-*/%&|!?:;,<>=^~.".toCharArray();
        if (start == 0) {
            front = true;
        } else if (str.charAt(start - 1) == '\n') {
            front = true;
        } else {
            for (int n = 0; n < points.length; n++) {
                if (str.charAt(start - 1) == points[n]) {
                    front = true;
                    break;
                }
            }
        }
        if (front) {
            try {
                if (str.charAt(end + 1) == '\n') {
                    return true;
                } else {
                    for (int n = 0; n < points.length; n++) {
                        if (str.charAt(end + 1) == points[n]) return true;
                    }
                }
            } catch (Exception e) {
                return true;
            }
        }
        return false;
    }

    private boolean isNumber(String value) {
        try {
            double a = Double.valueOf(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

