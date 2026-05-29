package com.aeisp.library.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface LibraryStorageService {

    String storeZip(Long resourceId, String versionNo, MultipartFile file);

    void extractZip(String zipPath, String destDir);

    List<String> listFiles(Long resourceId, String versionNo);

    byte[] readFile(Long resourceId, String versionNo, String filePath);

    void deleteVersionFiles(Long resourceId, String versionNo);

    void deleteResourceFiles(Long resourceId);

    String getZipAbsolutePath(Long resourceId, String versionNo);
}
