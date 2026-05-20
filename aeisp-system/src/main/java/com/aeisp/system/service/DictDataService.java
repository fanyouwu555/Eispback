package com.aeisp.system.service;

import com.aeisp.system.dto.DictDataQueryRequest;
import com.aeisp.system.entity.SysDictData;
import com.aeisp.system.vo.DictDataVO;

import java.util.List;

public interface DictDataService {

    List<DictDataVO> listByDictCode(String dictCode);

    SysDictData getById(Long id);

    boolean createData(SysDictData dictData);

    boolean updateData(SysDictData dictData);

    boolean deleteData(Long id);

    List<DictDataVO> listByDictCodeWithPage(String dictCode);
}