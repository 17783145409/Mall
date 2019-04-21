package com.mmall.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.pojo.Category;
import com.mmall.service.ICategoryService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * ClassName: CategoryServiceImpl
 *
 * @author HoleLin
 * @version 1.0
 * @date 2019/4/21
 */
@Service("iCategoryService")
public class CategoryServiceImpl implements ICategoryService {
	private Logger mLogger = LoggerFactory.getLogger(CategoryServiceImpl.class);

	@Autowired
	private CategoryMapper mCategoryMapper;


	@Override
	public ServerResponse addCategory(String categoryName, Integer parentId) {
		if (parentId == null || StringUtils.isBlank(categoryName)) {
			return ServerResponse.createByErrorMessage("添加品类参数错误");
		}
		Category category = new Category();
		category.setName(categoryName);
		category.setParentId(parentId);
		// 这个分类是可用的
		category.setStatus(true);
		int rowCount = mCategoryMapper.insert(category);
		if (rowCount > 0) {
			return ServerResponse.createBySuccessMessage("添加品类成功");
		} else {
			return ServerResponse.createByErrorMessage("添加品类失败");
		}
	}


	@Override
	public ServerResponse updateCategoryName(Integer categoryId, String categoryName) {
		if (categoryId == null || StringUtils.isBlank(categoryName)) {
			return ServerResponse.createByErrorMessage("更新品类参数错误");
		}
		Category category = new Category();
		category.setId(categoryId);
		category.setName(categoryName);
		int rowCount = mCategoryMapper.updateByPrimaryKeySelective(category);
		if (rowCount > 0) {
			return ServerResponse.createBySuccessMessage("更新品类名字成功");
		}
		return ServerResponse.createByErrorMessage("更新品类名字失败");
	}

	@Override
	public ServerResponse<List<Category>> getChildrenParallelCategory(Integer categoryId) {
		List<Category> categoryList = mCategoryMapper.selectCategoryChildrenByParentId(categoryId);
		if (CollectionUtils.isEmpty(categoryList)) {
			mLogger.info("未找到当前分类的子分类");
		}
		return ServerResponse.createBySuccess(categoryList);
	}


	@Override
	public ServerResponse selectCategoryAndChildrenById(Integer categoryId) {
		Set<Category> categorySet = Sets.newHashSet();
		findChildrenCategory(categorySet, categoryId);
		List<Integer> categoryIdList = Lists.newArrayList();
		if (categoryId != null) {
			for (Category categoryItem : categorySet) {
				categoryIdList.add(categoryItem.getId());
			}
		}
		return ServerResponse.createBySuccess(categoryIdList);
	}

	private Set<Category> findChildrenCategory(Set<Category> categorySet, Integer categoryId) {
		Category category = mCategoryMapper.selectByPrimaryKey(categoryId);
		if (category != null) {
			categorySet.add(category);
		}
		// 查找子节点
		List<Category> categoryList = mCategoryMapper.selectCategoryChildrenByParentId(categoryId);
		for (Category categoryItem : categoryList) {
			findChildrenCategory(categorySet, categoryItem.getId());
		}
		return categorySet;


	}
}
