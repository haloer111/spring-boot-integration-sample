package com.gexiao.sample.service.impl;

import com.gexiao.sample.service.UploadService;
import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.domain.proto.storage.DownloadByteArray;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UploadServiceImpl implements UploadService {
    @Autowired
    private FastFileStorageClient fastClient;

    @Override
    public String upload(MultipartFile file, String fileExtName) throws Exception {
        StorePath storePath = fastClient.uploadFile(file.getInputStream(), file.getSize(), fileExtName, null);
        return storePath.getFullPath();
    }

    @Override
    public byte[] download(String groupName, String path) {
        return fastClient.downloadFile(groupName, path, new DownloadByteArray());
    }

    @Override
    public void deleteFile(String groupName, String path) {
        fastClient.deleteFile(groupName, path);
    }


}
