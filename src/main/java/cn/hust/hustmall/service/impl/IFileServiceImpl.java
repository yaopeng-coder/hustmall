package cn.hust.hustmall.service.impl;

import cn.hust.hustmall.service.IFileService;
import cn.hust.hustmall.util.FTPUtil;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * 文件上传服务
 * @program: hustmall
 * @author: yaopeng
 * @create: 2019-11-01 17:29
 **/
@Service
@Slf4j
public class IFileServiceImpl implements IFileService{


    public String upload(MultipartFile file, String path){

        //1.得到文件名，并得到他的扩展名，然后用uuid替换文件名
        String fileName = file.getOriginalFilename();
        //得到扩展名
        String fileExtendName = fileName.substring(fileName.lastIndexOf(".") + 1);
        String uploadFileName = UUID.randomUUID().toString()+"."+fileExtendName;


        //2.判断路径是否存在，不存在则创建文件夹，fileDir是文件夹
        File fileDir = new File(path);
        if(!fileDir.exists()){
            fileDir.setWritable(true);
            //mkdirs可以一次性创建很多目录
            fileDir.mkdirs();
        }

        //3.上传文件,targetFile是文件
        File targetFile = new File(path,uploadFileName);
        try {


            file.transferTo(targetFile);

            //4.上传ftp服务器，并删除原来的文件
            FTPUtil.upload(Lists.newArrayList(targetFile));
        //    targetFile.delete();
        } catch (IOException e) {
            log.error("文件传输异常，{}",e);
            return null;
        }


        return targetFile.getName();

    }
}


