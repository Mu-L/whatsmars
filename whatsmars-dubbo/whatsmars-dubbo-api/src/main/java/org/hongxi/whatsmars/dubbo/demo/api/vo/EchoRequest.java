package org.hongxi.whatsmars.dubbo.demo.api.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class EchoRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -2080533871181613308L;
    private String message;
}