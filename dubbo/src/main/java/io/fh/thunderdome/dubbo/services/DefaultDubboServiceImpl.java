package io.fh.thunderdome.dubbo.services;

public class DefaultDubboServiceImpl implements DubboService {

  //  public Mono<String> requestReply(String str) {
  @Override
  public String requestReply(String str) {
    return str;
  }
}