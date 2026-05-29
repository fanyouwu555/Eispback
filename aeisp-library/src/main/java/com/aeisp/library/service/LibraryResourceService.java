package com.aeisp.library.service;

import com.aeisp.common.PageResult;
import com.aeisp.library.dto.request.CreateLibraryRequest;
import com.aeisp.library.dto.request.LibraryQueryRequest;
import com.aeisp.library.dto.request.UpdateLibraryRequest;
import com.aeisp.library.dto.vo.LibResourceDetailVO;
import com.aeisp.library.dto.vo.LibResourceVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface LibraryResourceService {

    boolean createLibrary(CreateLibraryRequest request);

    boolean updateLibraryInfo(Long resourceId, UpdateLibraryRequest request);

    boolean uploadNewVersion(Long resourceId, MultipartFile zipFile, String versionNo, String changelog);

    boolean rollbackVersion(Long resourceId, Long versionId);

    boolean toggleStatus(Long resourceId, Integer status);

    boolean deleteLibrary(Long resourceId);

    PageResult<LibResourceVO> listLibraries(LibraryQueryRequest request);

    LibResourceDetailVO getDetail(Long resourceId);

    List<LibResourceVO> listOnlineLibraries();

    void incrementDownloadCount(Long resourceId);
}
