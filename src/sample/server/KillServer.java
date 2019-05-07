package sample.server;

import javafx.scene.control.TextArea;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author: xfp
 * @Date: 2019/5/7 14:05
 * @Version 1.0
 */
public class KillServer {
    private Set<Integer> ports;
    private String port;
    private TextArea area;

    public KillServer(String port,TextArea area) {
        area.clear();
        this.port = port;
        ports = new HashSet<>();
        this.area = area;
    }

    public void init(){
        area.appendText("正在查找" + port + "端口号的进程!\n");
        String[] split = port.split(",");
        for (String spid : split) {
            try{
                int pid = Integer.parseInt(spid);
                ports.add(pid);
            }catch(Exception e){
                area.appendText("错误的端口号，请输入一个或者多个端口，以英文逗号隔开\n");
            }
        }

        area.appendText("need kill " + ports.size() + " num\n");
        for (Integer pid : ports) {
            start(pid);
        }
        area.appendText("清理完毕，程序即将退出\n");
    }

    public void start(int port){
        Runtime runtime = Runtime.getRuntime();
        try {
            //查找进程号
            Process p = runtime.exec("cmd /c netstat -ano | findstr \""+port+"\"");
            InputStream inputStream = p.getInputStream();
            List<String> read = read(inputStream, "UTF-8");
            if(read.size() == 0){
                area.appendText("找不到该端口的进程\n");
            }else{
                for (String string : read) {
                    area.appendText(string +"\n");
                }
                area.appendText("找到"+read.size()+"个进程，正在准备清理\n");
                kill(read);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 验证此行是否为指定的端口，因为 findstr命令会是把包含的找出来，例如查找80端口，但是会把8099查找出来
     * @param str
     * @return
     */
    private boolean validPort(String str){
        Pattern pattern = Pattern.compile("^ *[a-zA-Z]+ +\\S+");
        Matcher matcher = pattern.matcher(str);

        matcher.find();
        String find = matcher.group();
        int spstart = find.lastIndexOf(":");
        find = find.substring(spstart + 1);

        int port = 0;
        try {
            port = Integer.parseInt(find);
        } catch (NumberFormatException e) {
            area.appendText("查找到错误的端口:" + find +"\n");
            return false;
        }
        if(this.ports.contains(port)){
            return true;
        }else{
            return false;
        }
    }

    /**
     * 更换为一个Set，去掉重复的pid值
     * @param data
     */
    public void kill(List<String> data){
        Set<Integer> pids = new HashSet<>();
        for (String line : data) {
            int offset = line.lastIndexOf(" ");
            String spid = line.substring(offset);
            spid = spid.replaceAll(" ", "");
            int pid = 0;
            try {
                pid = Integer.parseInt(spid);
            } catch (NumberFormatException e) {
                area.appendText("获取的进程号错误:" + spid +"\n");
            }
            pids.add(pid);
        }
        killWithPid(pids);
    }

    /**
     * 一次性杀除所有的端口
     * @param pids
     */
    public void killWithPid(Set<Integer> pids){
        for (Integer pid : pids) {
            try {
                Process process = Runtime.getRuntime().exec("taskkill /F /pid "+pid+"");
                InputStream inputStream = process.getInputStream();
                String txt = readTxt(inputStream, "GBK");
                area.appendText(txt);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private List<String> read(InputStream in,String charset) throws IOException{
        List<String> data = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, charset));
        String line;
        while((line = reader.readLine()) != null){
            boolean validPort = validPort(line);
            if(validPort){
                data.add(line);
            }
        }
        reader.close();
        return data;
    }
    public String readTxt(InputStream in,String charset) throws IOException{
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, charset));
        StringBuffer sb = new StringBuffer();
        String line;
        while((line = reader.readLine()) != null){
            sb.append(line);
        }
        reader.close();
        return sb.toString();
    }
}
