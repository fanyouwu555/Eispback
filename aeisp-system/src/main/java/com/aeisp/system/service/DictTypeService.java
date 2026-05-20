package com.aeisp.system.service;

import com.aeisp.common.PageResult;
import com.aeisp.system.dto.DictTypeQueryRequest;
import com.aeisp.system.entity.SysDictType;
import com.aeisp.system.vo.DictDataVO;
import com.aeisp.system.vo.DictTypeVO;

import java.util.List;

public interface DictTypeService {

    PageResult<DictTypeVO> listByPage(DictTypeQueryRequest request);

    List<DictTypeVO> listAll();

    SysDictType getById(Long id);

    SysDictType getByDictCode(String dictCode);

    boolean createType(SysDictType dictType);

    boolean updateType(SysDictType dictType);

    boolean deleteType(Long id);

    List<DictDataVO> getDictData(String dictCode);
}