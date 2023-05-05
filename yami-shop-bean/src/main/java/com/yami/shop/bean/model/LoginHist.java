

package com.yami.shop.bean.model;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author 北易航
 */
@Data
@TableName("tz_login_hist")
public class LoginHist implements Serializable {
    /**
     * ID
     */
    @TableId

    private Long id;

    /**
     * 地区
     */
    private String area;

    /**
     * 国家
     */
    private String country;

    /**
     * 用户id
     */

    private String userId;

    /**
     * IP
     */
    private String ip;

    /**
     * 时间
     */

    private Date loginTime;

}