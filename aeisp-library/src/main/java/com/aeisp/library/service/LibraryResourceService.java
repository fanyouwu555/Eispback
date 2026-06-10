package com.aeisp.library.service;

import com.aeisp.common.PageResult;
import com.aeisp.library.dto.request.CreateLibraryRequest;
import com.aeisp.library.dto.request.LibraryQueryRequest;
import com.aeisp.library.dto.request.UpdateLibraryRequest;
import com.aeisp.library.dto.vo.LibResourceDetailVO;
import com.aeisp.library.dto.vo.LibResourceVO;

import java.util.List;

public interface LibraryResourceService {

    boolean createLibrary(CreateLibraryRequest request);

    boolean updateLibraryInfo(Long resourceId, UpdateLibraryRequest request);

    boolean deleteLibrary(Long resourceId);

    PageResult<LibResourceVO> listLibraries(LibraryQueryRequest request);

    LibResourceDetailVO getDetail(Long resourceId);

    List<LibResourceVO> listOnlineLibraries();
}
