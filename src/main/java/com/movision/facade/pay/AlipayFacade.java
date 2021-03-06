package com.movision.facade.pay;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeAppPayRequest;
import com.alipay.api.request.AlipayTradeFastpayRefundQueryRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeFastpayRefundQueryResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.movision.common.constant.PhotoConstant;
import com.movision.facade.index.FacadePhoto;
import com.movision.fsearch.utils.StringUtil;
import com.movision.mybatis.afterservice.entity.Afterservice;
import com.movision.mybatis.afterservice.service.AfterServcieServcie;
import com.movision.mybatis.orders.entity.Orders;
import com.movision.mybatis.orders.service.OrderService;
import com.movision.mybatis.pay.AlipayContent;
import com.movision.mybatis.photo.service.PhotoService;
import com.movision.mybatis.photoOrder.entity.PhotoOrder;
import com.movision.mybatis.photoOrder.service.PhotoOrderService;
import com.movision.mybatis.record.service.RecordService;
import com.movision.mybatis.subOrder.entity.SubOrder;
 import com.movision.mybatis.user.service.UserService;
import com.movision.utils.AlipayInputAssemblyUtils;
 import com.movision.utils.UpdateOrderPayBack;
import org.apache.commons.collections.map.HashedMap;
 import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.movision.utils.propertiesLoader.AlipayPropertiesLoader;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @Author shuxf
 * @Date 2017/3/14 10:25
 */
@Service
public class AlipayFacade {

    private static Logger log = LoggerFactory.getLogger(AlipayFacade.class);

    @Autowired
    private OrderService orderService;

    @Autowired
    private UpdateOrderPayBack updateOrderPayBack;

    @Autowired
    private UserService userService;

    @Autowired
    private RecordService recordService;

    @Autowired
    private AfterServcieServcie afterServcieServcie;

    @Autowired
    private AlipayInputAssemblyUtils alipayInputAssemblyUtils;

    @Autowired
    private PhotoOrderService photoOrderService;

    @Autowired
    private FacadePhoto facadePhoto;

    /**
     * 拼装支付宝支付请求入参
     *
     * @return
     */
    public Map<String, Object> packagePayParam(String ordersid) throws UnsupportedEncodingException, AlipayApiException {

        Map<String, Object> map = new HashMap<>();//用于返回结果的map

        String[] ordersidstr = ordersid.split(",");
        int[] ids = new int[ordersidstr.length];
        for (int i = 0; i < ordersidstr.length; i++) {
            ids[i] = Integer.parseInt(ordersidstr[i]);
        }

        //根据订单id查询所有主订单列表
        List<Orders> ordersList = orderService.queryOrdersListByIds(ids);

        if (null != ordersList && ordersList.size() == ordersidstr.length) {//传入的订单均存在且均为待支付的情况下

            String alipaygateway = AlipayPropertiesLoader.getValue("alipay_gateway");//支付宝请求网关
            //String alipublickey = AlipayPropertiesLoader.getValue("alipay_public_key");//支付宝公钥（请求接口入参目前未用到）

            List<AlipayContent> alipayContentList = new ArrayList<>();//alipay返回content实体列表

            for (int j = 0; j < ids.length; j++) {

                double totalamount = 0;//当前订单的实际支付总金额

                if (ordersList.get(j).getId() == ids[j]) {
                    //计算实际支付总金额
                    totalamount = ordersList.get(j).getMoney();
                    if (ordersList.get(j).getIsdiscount() == 1) {
                        totalamount = totalamount - ordersList.get(j).getDiscouponmoney();
                    }
                    if (null != ordersList.get(j).getDispointmoney()) {
                        totalamount = totalamount - ordersList.get(j).getDispointmoney();
                    }
                }
                String body = String.valueOf(ids[j]);
                log.info("打印订单号为：" + ids[j] + "的实际需要支付的总金额=================================>" + totalamount);

                //根据订单id查询订单中的所有子订单列表（商品列表）
                List<SubOrder> subOrderList = orderService.queryAllSubOrderList(ids[j]);
                //将子订单中的商品ID用,号连接成字符串
                StringBuffer subjectstr = new StringBuffer();
                for (int i = 0; i < subOrderList.size(); i++) {
                    subjectstr.append(subOrderList.get(i).getGoodsid() + ",");
                }
                String subject = subjectstr.toString().substring(0, subjectstr.toString().length() - 1);//去除末尾逗号

                //拼接支付宝入参 参数一：支付的总金额，参数二：订单号，参数三：商品id逗号分隔
                alipayContentList = alipayInputAssemblyUtils.assembleAlipayIintoGinseng(totalamount, body, subject);

            }

            map.put("code", 200);
            map.put("URL", alipaygateway);
            map.put("METHOD", "POST");
            map.put("CONTENT", alipayContentList);

        } else if (null == ordersList || ordersList.size() != ordersidstr.length) {

            map.put("code", 300);

        }
        return map;
    }


    /**
     * 支付宝支付后，APP前台同步通知接口
     */
    public int alipayback(String resultStatus, String result) throws AlipayApiException, ParseException {
        int flag = 0;//设置标志位

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式

        if (resultStatus.equals("9000") || resultStatus.equals("8000") || resultStatus.equals("4000") || resultStatus.equals("6001") ||
                resultStatus.equals("6002") || resultStatus.equals("6004")) {
            //解析json
            JSONObject jSONObject = JSONObject.parseObject(result);
            String sign = (String) jSONObject.get("sign");//签名（用于验签）
            String sign_type = (String) jSONObject.get("sign_type");//签名类型
            String alipay_trade_app_pay_response = jSONObject.getString("alipay_trade_app_pay_response");//签名原始字符串
            String alipublickey = AlipayPropertiesLoader.getValue("alipay_public_key");//支付宝公钥
            String charsets = "GBK";

            //校验签名
            boolean signVerified = AlipaySignature.rsaCheck(alipay_trade_app_pay_response, sign, alipublickey, charsets, sign_type);

            if (signVerified) {
                //验签通过
                //解析原始字符串，持久化存储处理结果
                JSONObject jObject = JSONObject.parseObject(alipay_trade_app_pay_response);
                String code = (String) jObject.get("code");//结果码
                String msg = (String) jObject.get("msg");//处理结果的描述
                String app_id = (String) jObject.get("app_id");//支付宝分配给开发者的应用Id（未使用）
                String out_trade_no = (String) jObject.get("out_trade_no");//商户网站唯一订单号
                String trade_no = (String) jObject.get("trade_no");//该交易在支付宝系统中的交易流水号,最长64位
                String total_amount = (String) jObject.get("total_amount");//该笔订单的资金总额，单位为RMB-Yuan。取值范围为[0.01,100000000.00]，精确到小数点后两位。
                String seller_id = (String) jObject.get("seller_id");//收款支付宝账号对应的支付宝唯一用户号。以2088开头的纯16位数字（未使用）
                String charset = (String) jObject.get("charset");//编码格式（未使用）
                String timestamp = (String) jObject.get("timestamp");//时间

                Date intime = df.parse(timestamp);//转化后的时间

                if (code.equals("10000")) {
                    //接口调用成功,持久化存储
                    log.info("返回码code>>>>>>>>>>>" + code + ",处理结果>>>>>>>>>>>>>>" + msg);
                    log.info("订单实付总金额>>>>>>>>>>>>>>>>>>>>>>>>" + total_amount);
                    int tradenoarray = Integer.parseInt(out_trade_no);//获取主订单号

                    //更改订单状态，记录流水号、实际支付金额、交易时间、支付方式
                    int type = 1;//支付宝类型为1  微信为2
                    updateOrderPayBack.updateOrder(tradenoarray, trade_no, intime, type, total_amount);//公共方法（微信支付宝支付公用）

                } else if (code.equals("20000")) {
                    log.info("返回码code>>>>>>>>>>>" + code + ",处理结果>>>>>>>>>>>>>>" + msg);
                } else if (code.equals("20001")) {
                    log.info("返回码code>>>>>>>>>>>" + code + ",处理结果>>>>>>>>>>>>>>" + msg);
                } else if (code.equals("40001")) {
                    log.info("返回码code>>>>>>>>>>>" + code + ",处理结果>>>>>>>>>>>>>>" + msg);
                } else if (code.equals("40002")) {
                    log.info("返回码code>>>>>>>>>>>" + code + ",处理结果>>>>>>>>>>>>>>" + msg);
                } else if (code.equals("40004")) {
                    log.info("返回码code>>>>>>>>>>>" + code + ",处理结果>>>>>>>>>>>>>>" + msg);
                } else if (code.equals("40006")) {
                    log.info("返回码code>>>>>>>>>>>" + code + ",处理结果>>>>>>>>>>>>>>" + msg);
                }
                flag = 1;
            } else {
                //验签失败
                flag = -1;
            }
        }

        return flag;
    }


    /**支付宝交易退款
     *
     * @param ordersid
     * @return
     */
    public Map<String, Object> tradingARefund(String ordersid) throws AlipayApiException {
        double totalamount = 0;//订单实际支付总金额

        //根据订单id查询主订单
        Orders orders = orderService.getOrderById(Integer.parseInt(ordersid));
        Map<String, Object> contentmap = new HashedMap();
        if (null != orders) {//传入的订单均存在且均为待支付的情况下

            //获取实际退款金额
            totalamount = orders.getRealmoney();
            String transactionNumber = orders.getPaycode();

            SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String body = ordersid;
            log.info("订单:" + ordersid + "实际退款总金额=================================>" + totalamount);
            String app_id = AlipayPropertiesLoader.getValue("app_id");//获取配置文件中的APPID
            String appprivatekey = AlipayPropertiesLoader.getValue("private_key");//应用私钥（商户的私钥）
            String alipaygateway = AlipayPropertiesLoader.getValue("alipay_gateway");//支付宝请求网关
            String alipaygateway1 = "https://openapi.alipay.com/gateway.do";
            String alipublickey = AlipayPropertiesLoader.getValue("alipay_public_key");//支付宝公钥（请求接口入参目前未用到）


            String charset = "GBK";
            String format = "json";
            String sign_type = "RSA2";
            String method = "alipay.trade.app.pay";//App支付接口  alipay.trade.app.pay
            String version = "1.0";
            String timestamp = sd.format(new Date());


            AlipayClient alipayClient = new DefaultAlipayClient(alipaygateway, app_id, appprivatekey, format, charset, alipublickey, sign_type);
            AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
            request.setBizContent("{" +
                    "\"out_trade_no\":\"" + body + "\"," +  //订单支付时传入的商户订单号,不能和 trade_no同时为空。
                    "\"trade_no\":\"" + transactionNumber + "\"," +  //支付宝交易号，和商户订单号不能同时为空
                    "\"refund_amount\":" + totalamount + "," +  //需要退款的金额，该金额不能大于订单金额,单位为元，支持两位小数
                    "\"refund_reas on\":\"\"," +  //退款的原因说明
                    "\"out_request_no\":\"\"," +  //标识一次退款请求，同一笔交易多次退款需要保证唯一，如需部分退款，则此参数必传。
                    "\"operator_id\":\"\"," +  //商户的操作员编号
                    "\"store_id\":\"\"," +  //商户的门店编号
                    "\"terminal_id\":\"\"" +   //商户的终端编号
                    "}");
            AlipayTradeRefundResponse response = alipayClient.execute(request);
            if (response.isSuccess()) {
                System.out.println("调用成功" + response.getBody());
                //使用的优惠券和积分返还

                if (orders.getIsdiscount() == 1) {//是否使用优惠券
                    orderService.updateOrderDiscount(orders.getCouponsid());//返还优惠券
                }
                if (orders.getDispointmoney() != null && orders.getDispointmoney() > 0) {//是否使用了积分
                    Map m = new HashedMap();
                    m.put("dispointmoney", orders.getDispointmoney());
                    m.put("userid", orders.getUserid());
                    m.put("orderid", orders.getId());
                    Integer integral = userService.queryUserUseIntegral(m);//查询订单对应用户使用的积分
                    m.put("integral", integral);
                    userService.updateUserPoints(m);//把积分返还
                    m.put("intime", new Date());//生成时间
                    recordService.addIntegralRecord(m);//增加用户积分操作记录
                    orderService.updateOrderByIntegral(orders.getId());//修改订单状态
                }
                Afterservice afterservice = new Afterservice();
                afterservice.setOrderid(orders.getId());//订单id
                afterservice.setAddressid(orders.getAddressid());//配送地址
                afterservice.setAmountdue(orders.getRealmoney());//应退款金额
                afterservice.setAfterstatue(2);//售后类型退款
                afterservice.setAftersalestatus(1);//销售状态
                afterservice.setProcessingstatus(2);//处理状态
                afterservice.setProcessingtime(new Date());//处理时间
                afterservice.setUserid(orders.getUserid());//用户id
                afterservice.setRemark(orders.getRemark());//留言
                afterServcieServcie.insertAfterInformation(afterservice);//插入售后详情

                contentmap.put("code", 200);
                contentmap.put("msg", response.getMsg());
                contentmap.put("type", response);
            } else {
                String code = response.getCode();
                String msg = response.getSubMsg();
                System.out.println("调用失败" + response.getBody());
                if (code.equals("20000")) {
                    log.info("返回码code>>>>>>>>>>>" + code + ",处理结果>>>>>>>>>>>>>>" + msg);
                } else if (code.equals("20001")) {
                    log.info("返回码code>>>>>>>>>>>" + code + ",处理结果>>>>>>>>>>>>>>" + msg);
                } else if (code.equals("40001")) {
                    log.info("返回码code>>>>>>>>>>>" + code + ",处理结果>>>>>>>>>>>>>>" + msg);
                } else if (code.equals("40002")) {
                    log.info("返回码code>>>>>>>>>>>" + code + ",处理结果>>>>>>>>>>>>>>" + msg);
                } else if (code.equals("40004")) {
                    log.info("返回码code>>>>>>>>>>>" + code + ",处理结果>>>>>>>>>>>>>>" + msg);
                } else if (code.equals("40006")) {
                    log.info("返回码code>>>>>>>>>>>" + code + ",处理结果>>>>>>>>>>>>>>" + msg);
                }
                contentmap.put("code", 300);
                contentmap.put("msg", msg);
                contentmap.put("type", response);
            }
        }
        return contentmap;
    }

    /**
     * 支付宝支付订单的查询
     *
     * @param orderid
     * @return
     * @throws AlipayApiException
     */
    public Map alipayTradeQuery(String orderid) throws AlipayApiException {

        Map<String, Object> contentmap = new HashedMap();
        //根据订单id查询所有主订单列表
        Orders orders = orderService.getOrderById(Integer.parseInt(orderid));

        if (null != orders) {//传入的订单均存在且均为待支付的情况下

            String transactionNumber = orders.getPaycode();

            SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String app_id = AlipayPropertiesLoader.getValue("app_id");//获取配置文件中的APPID
            String appprivatekey = AlipayPropertiesLoader.getValue("private_key");//应用私钥（商户的私钥）
            String alipaygateway = AlipayPropertiesLoader.getValue("alipay_gateway");//支付宝请求网关
            String alipublickey = AlipayPropertiesLoader.getValue("alipay_public_key");//支付宝公钥（请求接口入参目前未用到）
            String charset = "GBK";
            String format = "json";
            String sign_type = "RSA2";

            AlipayClient alipayClient = new DefaultAlipayClient(alipaygateway, app_id, appprivatekey, format, charset, alipublickey, sign_type);
            AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
            request.setBizContent("{" +
                    "    \"out_trade_no\":\"\"," +//订单支付时传入的商户订单号
                    "    \"trade_no\":\"" + transactionNumber + "\"" +//支付宝交易号
                    "  }");
            AlipayTradeQueryResponse response = alipayClient.execute(request);
            if (response.isSuccess()) {
                System.out.println("调用成功");
                contentmap.put("code", 200);
                contentmap.put("msg", response.getSubMsg());
                if (response.getTradeStatus() == "WAIT_BUYER_PAY") {
                    contentmap.put("status", "交易创建，等待买家付款");
                } else if (response.getTradeStatus() == "TRADE_CLOSED") {
                    contentmap.put("status", "未付款交易超时关闭，或支付完成后全额退款");
                } else if (response.getTradeStatus() == "TRADE_SUCCESS") {
                    contentmap.put("status", "交易支付成功");
                } else if (response.getTradeStatus() == "TRADE_FINISHED") {
                    contentmap.put("status", "交易结束，不可退款");
                }
                contentmap.put("resault", request);
            } else {
                System.out.println("调用失败");
                contentmap.put("code", 300);
                contentmap.put("msg", response.getSubMsg());
                contentmap.put("resault", request);
            }
        }
        return contentmap;
    }

    /**
     * 支付宝交易退款查询
     *
     * @param orderid
     * @return
     * @throws AlipayApiException
     */
    public Map tradingRefundQuery(String orderid) throws AlipayApiException {

        Map map = new HashedMap();

        //根据订单id查询所有主订单列表
        Orders orders = orderService.getOrderById(Integer.parseInt(orderid));

        if (StringUtil.isNotEmpty(orderid)) {//如果传入的订单号不为空
            SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String app_id = AlipayPropertiesLoader.getValue("app_id");//获取配置文件中的APPID
            String appprivatekey = AlipayPropertiesLoader.getValue("private_key");//应用私钥（商户的私钥）
            String alipaygateway = AlipayPropertiesLoader.getValue("alipay_gateway");//支付宝请求网关
            String alipublickey = AlipayPropertiesLoader.getValue("alipay_public_key");//支付宝公钥（请求接口入参目前未用到）
            String charset = "GBK";
            String format = "json";
            String sign_type = "RSA2";

            String outrequestno = orderid;
            String tradingAccount = orders.getId().toString();

            AlipayClient alipayClient = new DefaultAlipayClient(alipaygateway, app_id, appprivatekey, format, charset, alipublickey, sign_type);
            AlipayTradeFastpayRefundQueryRequest request = new AlipayTradeFastpayRefundQueryRequest();
            request.setBizContent("{" +
                    "    \"trade_no\":\"\"," +
                    "    \"out_trade_no\":\"" + tradingAccount + "\"," +
                    "    \"out_request_no\":\"" + outrequestno + "\"" +
                    "  }");
            AlipayTradeFastpayRefundQueryResponse response = alipayClient.execute(request);
            if (response.isSuccess()) {
                System.out.println("调用成功");
                System.out.println(response.getBody() + ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                map.put("code", 200);
                map.put("msg", response.getSubMsg());
                map.put("tradeno", response.getTradeNo());//支付宝交易号
                map.put("outtradeno", response.getOutTradeNo());//创建交易传入的商户订单号
                map.put("outrequestno", response.getOutRequestNo());//本笔退款对应的退款请求号
                map.put("refundreason", response.getRefundReason());//发起退款时，传入的退款原因
                map.put("totalamount", response.getTotalAmount());//该笔退款所对应的交易的订单金额
                map.put("refundamount", response.getRefundAmount());//本次退款请求，对应的退款金额
            } else {
                System.out.println(response.getBody() + "<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
                map.put("code", 300);
                map.put("msg", response.getSubMsg());
                map.put("tradeno", response.getTradeNo());//支付宝交易号
                map.put("outtradeno", response.getOutTradeNo());//创建交易传入的商户订单号
                map.put("outrequestno", response.getOutRequestNo());//本笔退款对应的退款请求号
                map.put("refundreason", response.getRefundReason());//发起退款时，传入的退款原因
                map.put("totalamount", response.getTotalAmount());//该笔退款所对应的交易的订单金额
                map.put("refundamount", response.getRefundAmount());//本次退款请求，对应的退款金额
            }
        }
        return map;
    }


    /**
     * 支付宝拼装参数（约拍）
     * @param ordersid
     * @return
     * @throws UnsupportedEncodingException
     * @throws AlipayApiException
     */
    public Map<String, Object> packagePhotoPayParam(String ordersid) throws UnsupportedEncodingException, AlipayApiException {
        Map<String,Object> map = new HashMap();
        PhotoOrder photoOrder=photoOrderService.selectOrder(Integer.parseInt(ordersid));
        if(photoOrder!=null){
            String alipaygateway = AlipayPropertiesLoader.getValue("alipay_gateway");//支付宝请求网关
            //String alipublickey = AlipayPropertiesLoader.getValue("alipay_public_key");//支付宝公钥（请求接口入参目前未用到）

            List<AlipayContent> alipayContentList = new ArrayList<>();//alipay返回content实体列表


                double totalamount = photoOrder.getMoney();//当前订单的实际支付总金额

                log.info("打印订单号为：" + ordersid + "的实际需要支付的总金额=================================>" + totalamount);


                //拼接支付宝入参 参数一：支付的总金额，参数二：订单号，参数三：商品id逗号分隔
                alipayContentList = alipayInputAssemblyUtils.assembleAlipayIintoGinseng(totalamount, ordersid, null);


            map.put("code", 200);
            map.put("URL", alipaygateway);
            map.put("METHOD", "POST");
            map.put("CONTENT", alipayContentList);

        } else if (null == ordersid) {

            map.put("code", 300);

        }
        return map;

    }
      /**
     * 支付宝支付后，APP前台同步通知接口(约拍)
     */
    public int alipaybackPhoto(String resultStatus, String result) throws AlipayApiException, ParseException {
        int flag = 0;//设置标志位
//        Map<String, String> params = new HashMap<String, String>();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
         if (resultStatus.equals("9000") || resultStatus.equals("8000") || resultStatus.equals("4000") || resultStatus.equals("6001") ||
                resultStatus.equals("6002") || resultStatus.equals("6004")) {
            //解析json
            JSONObject jSONObject = JSONObject.parseObject(result);
            String sign = (String) jSONObject.get("sign");//签名（用于验签）
            String sign_type = (String) jSONObject.get("sign_type");//签名类型
            String alipay_trade_app_pay_response = jSONObject.getString("alipay_trade_app_pay_response");//签名原始字符串
            String alipublickey = AlipayPropertiesLoader.getValue("alipay_public_key");//支付宝公钥
            String charsets = "GBK";
            log.info(alipay_trade_app_pay_response);
//             params.put("alipay_trade_app_pay_response",alipay_trade_app_pay_response);
//            params.put("sign",sign);

             //校验签名
            boolean signVerified = AlipaySignature.rsaCheck(alipay_trade_app_pay_response, sign, alipublickey, charsets, sign_type);
            //boolean signVerified =AlipaySignature.rsaCheckV2(params, alipublickey,charsets);
               if (signVerified) {
                //验签通过
                //解析原始字符串，持久化存储处理结果
                JSONObject jObject = JSONObject.parseObject(alipay_trade_app_pay_response);
                String code = (String) jObject.get("code");//结果码
                String msg = (String) jObject.get("msg");//处理结果的描述
                String app_id = (String) jObject.get("app_id");//支付宝分配给开发者的应用Id（未使用）
                String out_trade_no = (String) jObject.get("out_trade_no");//商户网站唯一订单号
                String trade_no = (String) jObject.get("trade_no");//该交易在支付宝系统中的交易流水号,最长64位
                String total_amount = (String) jObject.get("total_amount");//该笔订单的资金总额，单位为RMB-Yuan。取值范围为[0.01,100000000.00]，精确到小数点后两位。
                String seller_id = (String) jObject.get("seller_id");//收款支付宝账号对应的支付宝唯一用户号。以2088开头的纯16位数字（未使用）
                String charset = (String) jObject.get("charset");//编码格式（未使用）
                String timestamp = (String) jObject.get("timestamp");//时间

                Date intime = df.parse(timestamp);//转化后的时间

                if (code.equals("10000")) {
                    //接口调用成功,持久化存储
                    log.info("返回码code>>>>>>>>>>>" + code + ",处理结果>>>>>>>>>>>>>>" + msg);
                    log.info("订单实付总金额>>>>>>>>>>>>>>>>>>>>>>>>" + total_amount);
                    int tradenoarray = Integer.parseInt(out_trade_no);//获取主订单号

                    //更改订单状态，记录流水号、实际支付金额、交易时间、支付方式
                    int type = 1;//支付宝类型为1  微信为2
                    facadePhoto.updateOrderType(tradenoarray, trade_no, intime, type, total_amount);//公共方法（微信支付宝支付公用）
                } else if (code.equals("20000")) {
                    log.info("返回码code>>>>>>>>>>>" + code + ",处理结果>>>>>>>>>>>>>>" + msg);
                } else if (code.equals("20001")) {
                    log.info("返回码code>>>>>>>>>>>" + code + ",处理结果>>>>>>>>>>>>>>" + msg);
                } else if (code.equals("40001")) {
                    log.info("返回码code>>>>>>>>>>>" + code + ",处理结果>>>>>>>>>>>>>>" + msg);
                } else if (code.equals("40002")) {
                    log.info("返回码code>>>>>>>>>>>" + code + ",处理结果>>>>>>>>>>>>>>" + msg);
                } else if (code.equals("40004")) {
                    log.info("返回码code>>>>>>>>>>>" + code + ",处理结果>>>>>>>>>>>>>>" + msg);
                } else if (code.equals("40006")) {
                    log.info("返回码code>>>>>>>>>>>" + code + ",处理结果>>>>>>>>>>>>>>" + msg);
                }
                flag = 1;
            } else {
                //验签失败
                flag = -1;
            }
        }

        return flag;
    }



}
