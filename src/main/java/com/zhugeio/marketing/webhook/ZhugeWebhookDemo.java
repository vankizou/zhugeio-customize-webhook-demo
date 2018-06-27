package com.zhugeio.marketing.webhook;

import static spark.Spark.*;

import java.util.logging.Logger;
import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONObject;
import spark.utils.StringUtils;

/**
 * 基于Spark web的诸葛智能触达自定义webhook示例
 */
public class ZhugeWebhookDemo {

  private static final String HEADER_REQ_ID = "X-ZhugeIO-ReqID";

  private static final String HEADER_SIGN = "X-ZhugeIO-Sign";

  private static final String FIELD_ACTIVITY = "activity";

  private static final String FIELD_USER_PROPERTIES = "user_properties";

  private static final String FIELD_TRIGGER_EVENT = "trigger_event";

  private static final String FIELD_PARAMS = "params";

  private static final String FIELD_PUSH_INFO = "push_info";

  private static final String FIELD_EVENT_EID = "$eid";

  private static final String SECRET = "a4523b4cade2";

  private static final Logger logger = Logger.getLogger("webhook_demo");

  public static void main(String[] args) {
    // 目前webhook只支持POST请求
    post("/v1/webook_demo", (request, response) -> {
      // 验证sign是否正确
      // 默认不需要验证sign，如果需要，请您先向诸葛的技术支持人员提供一个secret key
      String requestId = request.headers(HEADER_REQ_ID);
      String requestSign = request.headers(HEADER_SIGN);
      if (!checkSign(requestId, requestSign)) {
        response.status(403);
        return "invalid sign";
      }

      // 从请求体中获取需要的数据
      final JSONObject requestData = new JSONObject(request.body());

      // 获取活动信息
      final JSONObject activityInfo = requestData.getJSONObject(FIELD_ACTIVITY);
      logger.info("received activity info: " + activityInfo);

      // 获取用户属性
      final JSONObject userProperties = requestData.getJSONObject(FIELD_USER_PROPERTIES);
      logger.info("user properties: " + userProperties);

      // 获取触发事件，只有触发类和转化类才能获取触发事件
      if (requestData.has(FIELD_TRIGGER_EVENT)) {
        final JSONObject triggerEvent = requestData.getJSONObject(FIELD_TRIGGER_EVENT);
        logger.info("trigger event: " + triggerEvent);
        final String eid = triggerEvent.getString(FIELD_EVENT_EID);
        logger.info("The $eid is " + eid);
      }

      // 获取自定义参数
      final JSONObject params = requestData.getJSONObject(FIELD_PARAMS);
      logger.info("params: " + params);

      // 获取推送信息（只当自定义webhook类型为push时，才会有该值）
      final JSONObject pushInfo = requestData.getJSONObject(FIELD_PUSH_INFO);
      logger.info("pushInfo: " + pushInfo);

      // 如果处理没有问题，那么返回200状态码
      return "ok";
    });
  }

  /**
   * 验证sign是否正确
   * @param requestId
   * @param requestSign
   * @return
   */
  private static boolean checkSign(String requestId, String requestSign) {
    if (StringUtils.isEmpty(requestSign)) {
      return false;
    }
    // sign的计算：将secrect_key和本次的request id结合起来，计算一个SHA1后跟header中的Sign进行比较
    String rightSign = DigestUtils.sha1Hex(String.format("%s_%s", SECRET, requestId));
    return rightSign.equals(requestSign);
  }
}
