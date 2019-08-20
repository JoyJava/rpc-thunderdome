package io.fh.thunderdome.dubbo.services;

import java.io.Serializable;

/**
 * User: fengHong Date: 2019-08-19 18:36
 */
public interface DubboService extends Serializable {

  //  Mono<String> requestReply(String string);
  String requestReply(String string);
}
