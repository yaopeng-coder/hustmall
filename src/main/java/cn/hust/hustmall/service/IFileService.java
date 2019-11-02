package cn.hust.hustmall.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * @program: hustmall
 * @author: yaopeng
 * @create: 2019-11-01 17:29
 **/
public interface IFileService {
    String upload(MultipartFile file, String path);
}
