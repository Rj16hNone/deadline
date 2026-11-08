package com.cx.helper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/** 数据模型与三块归属（未完成 / 超时未交 / 待批改）。 */
final class Tasks {
    private Tasks() {
    }

    static final String OPEN = "open";
    static final String OVERDUE = "overdue";
    static final String DONE = "done";
    static final String[] ORDER = {OPEN, OVERDUE, DONE};

    /** 到期超过这么多天还没交的，不再显示（免得陈年旧账一直挂着）。 */
    static final long STALE_MS = 30L * 24 * 60 * 60 * 1000;

    static String label(String s) {
        if (OVERDUE.equals(s)) {
            return "超时未交";
        }
        if (DONE.equals(s)) {
            return "待批改";
        }
        return "未完成";
    }

    static int rank(String s) {
        for (int i = 0; i < ORDER.length; i++) {
            if (ORDER[i].equals(s)) {
                return i;
            }
        }
        return ORDER.length;
    }

    /**
     * done    待批改（作业已交 / 考试已交卷，等老师批）
     * overdue 未完成且截止时间已过
     * open    未完成但还没到期；拿不到时间的也算这里（宁可当作待办）
     */
    static String state(T t, long now) {
        if (t.done) {
            return DONE;
        }
        Date end = Parse.fromIso(t.deadlineIso);
        if (end != null) {
            return end.getTime() < now ? OVERDUE : OPEN;
        }
        String st = nz(t.examStatus) + " " + nz(t.status);
        if (st.contains("已结束")) {
            return OVERDUE;
        }
        return OPEN;
    }

    /**
     * 到期超过一个月、仍没提交的算陈年旧账，不展示。
     * 只对「超时未交」生效：已交的（待批改）不管多旧都留着；拿不到时间的也留着。
     */
    static boolean stale(T t, long now) {
        if (!OVERDUE.equals(t.state)) {
            return false;
        }
        Date end = Parse.fromIso(t.deadlineIso);
        return end != null && now - end.getTime() >= STALE_MS;
    }

    static String nz(String s) {
        return s == null ? "" : s;
    }

    static final class T {
        String type = "作业";
        String course = "";
        String name = "";
        String status = "";
        String deadline = "";
        String deadlineIso = "";
        String startIso = "";
        String remain = "";
        String link = "";
        String examStatus = "";
        String score = "";
        boolean done;
        boolean approx;
        String state = OPEN;
    }

    static final class Course {
        String name = "";
        String courseId = "";
        String classId = "";
        String cpi = "";
        String workEnc = "";
        String enc = "";
        String openc = "";
        String examEnc = "";
        String token = "";
        String style = "";
        String url = "";
        boolean retired;
    }

    static final class Item {
        String name = "";
        String status = "";
        String remain = "";
        String link = "";
        boolean done;
    }

    static final class Res {
        final List<T> tasks = new ArrayList<>();
        final List<String> errors = new ArrayList<>();
        int courses;
        int[] counts = new int[3];
        int hidden;   // 超期一个月以上、被隐掉的条数
        String hint = "";

        int count(String state) {
            return counts[rank(state)];
        }
    }
}
