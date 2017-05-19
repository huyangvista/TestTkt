

import vdll.utils.String.StringGet;
import vdll.utils.io.FileOperate;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hocean on 2017/5/10.  1365676089
 */
public class FindTkt {
    private String fileNamf = "1365676089";

    //数据库字段
    private static final String f_TicketNumber = "grossrefund_amount";
    private static final String f_grossrefund_amount = "grossrefund_amount";
    private static final String f_refundtickettax = "refundtickettax";
    private static final String f_ticketstatus = "ticketstatus";
    private static final String f_dedu = "dedu";
    private static final String f_realrefundticketfee = "realrefundticketfee";
    private static final String f_rfnb = "rfnb";
    private static final String f_rtime = "rtime";

    //XML标签
    private static final String t_TicketNumber = "TicketNumber";
    private static final String t_grossrefund_amount = "Amount";
    private static final String t_refundtickettax = "";
    private static final String t_ticketstatus = "R";
    private static final String t_dedu = "DEDU";
    private static final String t_realrefundticketfee = "TFAR";
    private static final String t_rfnb = "RFNB";
    private static final String t_rtime = "TIME";

    //获取到的参数值
    public String TicketNumber = "";
    private String grossrefund_amount = "";
    private String refundtickettax = "";  //calc  refundtickettax = realrefundticketfee + dedu - grossrefund_amoun
    private String ticketstatus = "R";  //def
    private String dedu = "";
    private String realrefundticketfee = "";   // 除以100
    private String rfnb = "";
    private String rtime = "";


    //SQL
    public String sql_grossrefund_amount = "";
    public String sql_refundtickettax = "";
    public String sql_ticketstatus = "";
    public String sql_dedu = "";
    public String sql_realrefundticketfee = "";
    public String sql_rfnb = "";
    public String sql_rtime = "";

    public boolean tag_r = false;


    private void main(String[] args) {
        //MySqlString.CreateDemo(mySql);
    }

    private List<File> find(File dir) {
        List<File> list = new ArrayList<File>();
        find(list, dir);
        return list;
    }

    private void find(List<File> list, File dir) {
        if (list.size() > 0) {
            return;
        }
        File[] fs = dir.listFiles();
        for (int i = 0; i < fs.length; i++) {
            File f = fs[i];
            if (f.isDirectory()) {
                find(list, f);
            } else {
                // 20170509095200
                if (f.getName().contains(fileNamf)) {
                    //System.out.println(f.getName());
                    list.add(f);
                }
            }
        }
    }

    private String getTag(String text, String t) {
        List<String> lt = StringGet.getTagIn(text, "<" + t + ">", "</" + t + ">");
        if (lt.size() > 0) {
            return lt.get(lt.size() - 1).trim();
        }
        return "";
    }

    private String getUpdate(String tktNumber, String f, String v) {
        return String.format("UPDATE T_BLUESKY_ORD_TKTDATA  set %s = '%s'   where ticketnumber='%s';", f, v, tktNumber);
    }

    public String find(String fileNamf) {
        try {
            this.fileNamf = fileNamf;
            List<String> sqls = new ArrayList<String>();

            List<File> list = find(new File("D:\\dpdata"));
            sqls.add("-- 搜索 -> " + fileNamf);
            sqls.add("-- 快速找到：" + list.size() + "个文件（如果是多个文件的话可能不全）");
            String text = "";
            if (list.size() > 0) {
                File file = list.get(0);
                sqls.add("-- 解析文件：" + file.getName());
                text = FileOperate.readTxt(file.getPath());
            }

            // String tag = "<TIME>000</TIME> sadasd001222fgsdfg<TIME>000</TIME>sdfasdfasdfa";

            grossrefund_amount = getTag(text, t_grossrefund_amount);
            dedu = getTag(text, t_dedu);
            realrefundticketfee = getTag(text, t_realrefundticketfee);
            rfnb = getTag(text, t_rfnb);
            rtime = getTag(text, t_rtime);
            TicketNumber = getTag(text, t_TicketNumber);
            String r = getTag(text, "TicketStatus");
            sqls.add("-- 票号 " + TicketNumber);
            sqls.add("-- 票状态 " + r);


            double dGrossrefund_amoun = 0;
            double dDedu = 0;
            double dRealrefundticketfee = 0;
            double dRefundtickettax = 0;
            if (isNumber(grossrefund_amount)) {
                dGrossrefund_amoun = Double.parseDouble(grossrefund_amount);
            }
            if (isNumber(dedu)) {
                dDedu = Double.parseDouble(dedu);
            }
            if (isNumber(realrefundticketfee)) {
                dRealrefundticketfee = Double.parseDouble(realrefundticketfee) / 100;
                realrefundticketfee = dRealrefundticketfee + "";
            }
            dRefundtickettax = dRealrefundticketfee + dDedu - dGrossrefund_amoun;
            refundtickettax = dRefundtickettax + "";

            rtime = rtime.substring(0, 10) + " " + rtime.substring(11, 19);

            if (!r.equals("r") && !r.equals("R")) {
                System.err.println("--  票状态不是退票状态。");
                sqls.add("--  【【票状态不是退票状态。】】");
                tag_r = false;
            } else {
                tag_r = true;
            }

            sqls.add("-- 按最后找到的标签值,生成语句： ");
            sqls.add("");
            sqls.add(sql_grossrefund_amount = getUpdate(TicketNumber, f_grossrefund_amount, grossrefund_amount));
            sqls.add(sql_refundtickettax = getUpdate(TicketNumber, f_refundtickettax, refundtickettax));
            sqls.add(sql_ticketstatus = getUpdate(TicketNumber, f_ticketstatus, ticketstatus));
            sqls.add(sql_dedu = getUpdate(TicketNumber, f_dedu, dedu));
            sqls.add(sql_realrefundticketfee = getUpdate(TicketNumber, f_realrefundticketfee, realrefundticketfee));
            sqls.add(sql_rfnb = getUpdate(TicketNumber, f_rfnb, rfnb));
            sqls.add(sql_rtime = getUpdate(TicketNumber, f_rtime, rtime));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < sqls.size(); i++) {
                sb.append(sqls.get(i));
                sb.append("\r\n");
            }
            return sb.toString();
        } catch (Exception e) {
            tag_r = false;
            return "解析文件异常";
        }
    }


    public static boolean isNumber(Object o) {
        if (o == null) return false;
        String str = o.toString().trim();
        int len = str.length();
        int po = 0;
        char point = '.';
        for (int i = len - 1; i >= 0; i--) {
            char c = str.charAt(i);
            if (!Character.isDigit(c)) {
                if (c == point) {
                    if (++po > 1) {
                        return false;
                    }
                } else {
                    return false;
                }
            }
        }
        if (po <= 0) {
            return len >= 1;
        } else if (po <= 1) {
            return len >= 2;
        } else {
            return false;
        }
    }
}


/*
select rfnb, realrefundticketfee, ticketstatus, rtime, dedu, zval, cair
from T_BLUESKY_ORD_TKTDATA where ticketnumber = '1373781861';

UPDATE T_BLUESKY_ORD_TKTDATA  set grossrefund_amount=880.00        where ticketnumber='1373781861';
UPDATE T_BLUESKY_ORD_TKTDATA  set refundtickettax=50.00            where ticketnumber='1373781861';
UPDATE T_BLUESKY_ORD_TKTDATA  set ticketstatus='R'                 where ticketnumber='1373781861';
UPDATE T_BLUESKY_ORD_TKTDATA  set dedu=264.00                      where ticketnumber='1373781861';
UPDATE T_BLUESKY_ORD_TKTDATA  set realrefundticketfee=666.00       where ticketnumber='1373781861';
UPDATE T_BLUESKY_ORD_TKTDATA  set rfnb = '167698493'               where ticketnumber='1373781861';
UPDATE T_BLUESKY_ORD_TKTDATA  set rtime = '2017-05-10 11:41:00'    where ticketnumber='1373781861';

UPDATE T_BLUESKY_ORD_TKTDATA  set grossrefund_amount = '880.00'   where ticketnumber='1373781861';
UPDATE T_BLUESKY_ORD_TKTDATA  set refundtickettax = '50.0'   where ticketnumber='1373781861';
UPDATE T_BLUESKY_ORD_TKTDATA  set ticketstatus = 'R'   where ticketnumber='1373781861';
UPDATE T_BLUESKY_ORD_TKTDATA  set dedu = '264.00      '   where ticketnumber='1373781861';
UPDATE T_BLUESKY_ORD_TKTDATA  set realrefundticketfee = '66600'   where ticketnumber='1373781861';
UPDATE T_BLUESKY_ORD_TKTDATA  set rfnb = '167698493'   where ticketnumber='1373781861';
UPDATE T_BLUESKY_ORD_TKTDATA  set rtime = '2017-05-10T11:41:00Z'   where ticketnumber='1373781861';

梁志福，Mate 2017/5/10 13:55:34
refundtickettax = realrefundticketfee + dedu - grossrefund_amoun
 */
