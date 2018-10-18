<%@ page import="java.util.Map" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="org.fuwushe.common.statuscode.ServiceError" %>
<%@ page import="org.fuwushe.common.utils.I18NUtil" %>
<%@ page import="com.alibaba.fastjson.JSON" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    Map<String, Object> returnMessageMap = new HashMap<String, Object>();
    returnMessageMap.put("message", I18NUtil.getMessage("err"));
    returnMessageMap.put("status", ServiceError.NOTFOUND.getCode());
    returnMessageMap.put("data",null);
    response.getWriter().print(JSON.toJSON(returnMessageMap));
%>
