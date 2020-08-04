package com.gexiao.sample.service;

import org.springframework.web.multipart.MultipartFile;

public interface UploadService {

    public String upload(MultipartFile file, String fileExtName) throws Exception;
    public byte[] download(String groupName, String path);
    public void deleteFile(String groupName, String path);
}
