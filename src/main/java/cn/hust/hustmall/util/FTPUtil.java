package cn.hust.hustmall.util;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

/**
 * @program: hustmall
 * @author: yaopeng
 * @create: 2019-11-01 17:47
 **/

@Slf4j
@Data
public class FTPUtil {

    private static String ftpServerIp = PropertiesUtil.getProperty("ftp.server.ip");
    private static String ftpUser = PropertiesUtil.getProperty("ftp.user");
    private static String ftpPass = PropertiesUtil.getProperty("ftp.pass");

    private String ip;
    private Integer port;
    private String user;
    private String password;
    private FTPClient ftpClient;

    public FTPUtil(String ip, Integer port, String user, String password) {
        this.ip = ip;
        this.port = port;
        this.user = user;
        this.password = password;
    }

     public static boolean upload(List<File> fileList) throws IOException {
            //1.创建连接对象，并连接ftp服务器
            log.info("开始连接服务器，准备上传文件");
            FTPUtil ftpUtil = new FTPUtil(ftpServerIp,21,ftpUser,ftpPass);
            //2.上传文件
         boolean result = ftpUtil.upload("img", fileList);
         log.info("文件上传ftp服务器结束");
            return result;

     }

    private boolean upload(String remotePath,List<File> fileList) throws IOException {
         boolean isSuccess = true;
        FileInputStream fileInputStream  = null;

         if(connectServer(this.ip,this.port,this.user,this.password)){
             try {
                 ftpClient.changeWorkingDirectory(remotePath);
                 ftpClient.setBufferSize(1024);
                 ftpClient.setControlEncoding("UTF-8");
                 ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
                 ftpClient.enterLocalPassiveMode();
                 for(File file : fileList){
                     fileInputStream = new FileInputStream(file);
                     ftpClient.storeFile(file.getName(),fileInputStream);
                 }

             } catch (IOException e) {
                 log.error("上传文件异常，{}",e);
                 isSuccess = false;

             }finally {
                 fileInputStream.close();
                 ftpClient.disconnect();
             }
         }
        return isSuccess;
    }







    private boolean connectServer(String ip, Integer port, String user,String password) {

        boolean isSuccess = false;
        ftpClient = new FTPClient();
        try {
            ftpClient.connect(ip);
            isSuccess = ftpClient.login(user, password);

        } catch (IOException e) {
            log.error("连接FTP服务器异常");
        }
        return isSuccess;


    }



}
