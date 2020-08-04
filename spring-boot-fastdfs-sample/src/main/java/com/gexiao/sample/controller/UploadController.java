package com.gexiao.sample.controller;

import com.gexiao.sample.service.UploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.UUID;

@RestController
public class UploadController {

    @Autowired
    private UploadService uploadService;

    @GetMapping("hello")
    public String hello() {
        return "hello,world";
    }

    @PostMapping("upload")
    public String upload(MultipartFile file) throws Exception {
        if (file != null) {
            String filename = file.getOriginalFilename();
            String suffix = filename.substring(filename.lastIndexOf(".") + 1);
            if (!suffix.equalsIgnoreCase("jpg") &&
                    !suffix.equalsIgnoreCase("png") &&
                    !suffix.equalsIgnoreCase("jpeg")
            ) {
                return "图片格式不正确";
            }
            String uploadPath = uploadService.upload(file, suffix);
            System.out.println("uploadPath = " + uploadPath);
            return uploadPath;
        }
        return "文件不能为空";
    }

    /**
     * 下载文件
     *
     * @param groupName 组名
     * @param path      fastDFS的文件相对路径
     * @param suffix    下载文件的后缀名
     * @param response
     * @throws Exception
     */
    @GetMapping("download")
    public void download(String groupName,
                         String path,
                         String suffix,
                         HttpServletResponse response) throws Exception {
        String fileName = UUID.randomUUID().toString() + "." + suffix;
        byte[] bytes1 = uploadService.download(groupName, path);
        // 设置响应头，控制浏览器下载该文件
        response.setHeader("content-disposition", "attachment;filename="
                + URLEncoder.encode(fileName, "UTF-8"));
        response.setHeader("Content-Type", "application/octet-stream");
        response.setHeader("Connection", "close");
        response.setHeader("Accept-Ranges", "bytes");
        try (ServletOutputStream os = response.getOutputStream()) {
            os.write(bytes1);
        }
    }

    /**
     * 删除文件
     *
     * @param groupName 组名
     * @param path      fastDFS的文件相对路径
     */
    @GetMapping("delete")
    public void deleteFile(String groupName, String path) {
        uploadService.deleteFile(groupName, path);
    }
}
