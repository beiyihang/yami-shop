

package com.yami.shop.common.util;

import cn.hutool.core.util.PageUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

/**
 * @author 北易航
 */
@Data
public class PageAdapter{

    private int begin;

    private int size;

    public PageAdapter(Page page) {
        // 将页码和每页大小转换为起始位置和数量
        int[] startEnd = PageUtil.transToStartEnd((int) page.getCurrent() - 1, (int) page.getSize());
        // 将起始位置赋值给 begin
        this.begin = startEnd[0];
        // 将每页大小赋值给 size
        this.size = (int) page.getSize();
    }

}
