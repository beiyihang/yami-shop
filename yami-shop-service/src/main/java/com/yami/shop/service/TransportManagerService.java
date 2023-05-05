

package com.yami.shop.service;

import com.yami.shop.bean.app.dto.ProductItemDto;
import com.yami.shop.bean.model.UserAddr;

/**
 * @author 北易航
 */
public interface TransportManagerService {
	/**
	 * 计算运费
	 * @param productItem
	 * @param userAddr
	 * @return
	 */
	Double calculateTransfee(ProductItemDto productItem, UserAddr userAddr);
}
