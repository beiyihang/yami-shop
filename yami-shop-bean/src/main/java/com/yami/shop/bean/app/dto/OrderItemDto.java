

package com.yami.shop.bean.app.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author 北易航
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class OrderItemDto extends ProductItemDto implements Serializable {

}
