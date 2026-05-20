package com.aeisp.template.controller;

import com.aeisp.common.Result;
import com.aeisp.template.entity.TplTemplatePreviewImage;
import com.aeisp.template.mapper.TplTemplatePreviewImageMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 模板预览图片管理 Controller。
 *
 * @author AEISP Team
 */
@Tag(name = "模板预览图片管理", description = "模板预览图片的增删改查")
@RestController
@RequestMapping("/api/v1/templates/{templateId}/preview-images")
@RequiredArgsConstructor
public class TplTemplatePreviewImageController {

    private final TplTemplatePreviewImageMapper previewImageMapper;

    /**
     * 查询模板预览图片列表。
     */
    @GetMapping
    @Operation(summary = "预览图片列表", description = "查询指定模板的所有预览图片")
    public Result<List<TplTemplatePreviewImage>> list(@PathVariable Long templateId) {
        List<TplTemplatePreviewImage> list = previewImageMapper.selectByTemplateId(templateId);
        return Result.success(list);
    }

    /**
     * 添加预览图片。
     */
    @PostMapping
    @Operation(summary = "添加预览图片", description = "为模板添加一张预览图片")
    public Result<Void> add(@PathVariable Long templateId,
                            @RequestParam String imageUrl,
                            @RequestParam(defaultValue = "0") Integer sortOrder) {
        TplTemplatePreviewImage image = new TplTemplatePreviewImage();
        image.setTemplateId(templateId);
        image.setImageUrl(imageUrl);
        image.setSortOrder(sortOrder);
        previewImageMapper.insert(image);
        return Result.success();
    }

    /**
     * 删除预览图片。
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除预览图片", description = "删除指定预览图片")
    public Result<Void> delete(@PathVariable Long id) {
        previewImageMapper.deleteById(id);
        return Result.success();
    }

    /**
     * 更新预览图片排序。
     */
    @PutMapping("/{id}/sort")
    @Operation(summary = "更新排序", description = "更新预览图片的展示顺序")
    public Result<Void> updateSort(@PathVariable Long id, @RequestParam Integer sortOrder) {
        TplTemplatePreviewImage image = new TplTemplatePreviewImage();
        image.setId(id);
        image.setSortOrder(sortOrder);
        previewImageMapper.updateById(image);
        return Result.success();
    }
}
