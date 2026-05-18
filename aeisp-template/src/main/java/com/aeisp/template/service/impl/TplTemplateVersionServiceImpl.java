package com.aeisp.template.service.impl;

import com.aeisp.common.exception.BizException;
import com.aeisp.template.dto.vo.TplTemplateVersionVO;
import com.aeisp.template.entity.TplTemplate;
import com.aeisp.template.entity.TplTemplateVersion;
import com.aeisp.template.mapper.TplTemplateMapper;
import com.aeisp.template.mapper.TplTemplateVersionMapper;
import com.aeisp.template.service.TemplateStorageService;
import com.aeisp.template.service.TplTemplateVersionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 模板版本 Service 实现类。
 *
 * @author AEISP Team
 */
@Service
@RequiredArgsConstructor
public class TplTemplateVersionServiceImpl implements TplTemplateVersionService {

    private final TplTemplateVersionMapper versionMapper;
    private final TplTemplateMapper templateMapper;
    private final TemplateStorageService templateStorageService;

    @Override
    public TplTemplateVersion getById(Long versionId) {
        return versionMapper.selectById(versionId);
    }

    @Override
    public List<TplTemplateVersionVO> listVersions(Long templateId) {
        List<TplTemplateVersion> list = versionMapper.selectByTemplateId(templateId);
        List<TplTemplateVersionVO> voList = new ArrayList<>();
        for (TplTemplateVersion v : list) {
            TplTemplateVersionVO vo = new TplTemplateVersionVO();
            BeanUtils.copyProperties(v, vo);
            voList.add(vo);
        }
        return voList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteVersion(Long versionId) {
        TplTemplateVersion version = versionMapper.selectById(versionId);
        if (version == null) {
            throw new BizException("版本不存在");
        }
        TplTemplate template = templateMapper.selectById(version.getTemplateId());
        if (template != null && versionId.equals(template.getCurrentVersionId())) {
            throw new BizException("不能删除当前正在使用的版本");
        }

        // 删除物理文件
        templateStorageService.deleteVersionFiles(version.getTemplateId(), version.getVersionNo());

        // 逻辑删除
        return versionMapper.deleteById(versionId) > 0;
    }
}
