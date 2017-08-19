package com.yanhuahealth.healthlauncher.model.weather;

/**
 * Created by Administrator on 2016/3/7.
 */
public class TodayWeather {

    // 白天天气编号
    private String fa;

    // 晚上天气编号
    private String fb;

    // 白天天气温度
    private String fc;

    // 晚上天气温度
    private String fd;

    // 白天风向编号
    private String fe;

    // 晚上天向编号
    private String ff;

    // 白天风力编号
    private String fg;

    // 晚上风力编号
    private String fh;

    // 日出日落时间
    private String fi;

    public String getFa() {
        return fa;
    }

    public void setFa(String fa) {
        this.fa = fa;
    }

    public String getFb() {
        return fb;
    }

    public void setFb(String fb) {
        this.fb = fb;
    }

    public String getFc() {
        return fc;
    }

    public void setFc(String fc) {
        this.fc = fc;
    }

    public String getFd() {
        return fd;
    }

    public void setFd(String fd) {
        this.fd = fd;
    }

    public String getFe() {
        return fe;
    }

    public void setFe(String fe) {
        this.fe = fe;
    }

    public String getFf() {
        return ff;
    }

    public void setFf(String ff) {
        this.ff = ff;
    }

    public String getFg() {
        return fg;
    }

    public void setFg(String fg) {
        this.fg = fg;
    }

    public String getFh() {
        return fh;
    }

    public void setFh(String fh) {
        this.fh = fh;
    }

    public String getFi() {
        return fi;
    }

    public void setFi(String fi) {
        this.fi = fi;
    }

    @Override
    public String toString() {
        return "TodayWeather{" +
                "fa='" + fa + '\'' +
                ", fb='" + fb + '\'' +
                ", fc='" + fc + '\'' +
                ", fd='" + fd + '\'' +
                ", fe='" + fe + '\'' +
                ", ff='" + ff + '\'' +
                ", fg='" + fg + '\'' +
                ", fh='" + fh + '\'' +
                ", fi='" + fi + '\'' +
                '}';
    }
}
