

package com.yami.shop.common.response;

import lombok.Data;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author 北易航
 */
@Data
public class ServerResponse<T> implements Serializable {


    private int code;

    private String msg;

    private T obj;

    public boolean isSuccess(){
        return Objects.equals(ResponseCode.SUCCESS, this.code);
    }


}
