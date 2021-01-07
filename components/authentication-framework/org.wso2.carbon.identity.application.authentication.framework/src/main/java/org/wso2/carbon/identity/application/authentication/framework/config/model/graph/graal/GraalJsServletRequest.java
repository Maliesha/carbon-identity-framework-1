package org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyObject;
import org.wso2.carbon.identity.application.authentication.framework.context.TransientObjectWrapper;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**
 * Javascript wrapper for Java level HTTPServletRequest.
 * This provides controlled access to HTTPServletRequest object via provided javascript native syntax.
 * e.g
 * var redirect_uri = context.request.params.redirect_uri
 * <p>
 * instead of
 * var userName = context.getRequest().getParameter("redirect_uri)
 * <p>
 * Also it prevents writing an arbitrary values to the respective fields, keeping consistency on runtime HTTPServletRequest.
 */

public class GraalJsServletRequest extends AbstractJSObjectWrapper<TransientObjectWrapper<HttpServletRequest>> implements ProxyObject {

    private static final Log LOG = LogFactory.getLog(GraalJsServletRequest.class);

    public GraalJsServletRequest(TransientObjectWrapper<HttpServletRequest> wrapped) {

        super(wrapped);
    }
    @Override
    public Object getMember(String name) {

        switch (name) {
            case FrameworkConstants.JSAttributes.JS_HEADERS:
                Map headers = new HashMap();
                Enumeration<String> headerNames = getRequest().getHeaderNames();
                if (headerNames != null) {
                    while (headerNames.hasMoreElements()) {
                        String headerName = headerNames.nextElement();
                        headers.put(headerName, getRequest().getHeader(headerName));
                    }
                }
                return new GraalJsWritableParameters(headers);
            case FrameworkConstants.JSAttributes.JS_PARAMS:
                return new GraalJsParameters(getRequest().getParameterMap());
            case FrameworkConstants.JSAttributes.JS_COOKIES:
                Map cookies = new HashMap();
                Cookie[] cookieArr = getRequest().getCookies();
                if (cookieArr != null) {
                    for (Cookie cookie : cookieArr) {
                        cookies.put(cookie.getName(), new GraalJsCookie(cookie));
                    }
                }
                return new GraalJsWritableParameters(cookies);
            case FrameworkConstants.JSAttributes.JS_REQUEST_IP:
                return IdentityUtil.getClientIpAddress(getRequest());
            default:
                return super.getMember(name);
        }
    }

    @Override
    public Object getMemberKeys() {

        return null;
    }

    @Override
    public boolean hasMember(String name) {

        if (getRequest() == null) {
            //Transient Object is null, hence no member access is possible.
            return false;
        }

        switch (name) {
            case FrameworkConstants.JSAttributes.JS_HEADERS:
                return getRequest().getHeaderNames() != null;
            case FrameworkConstants.JSAttributes.JS_PARAMS:
                return getRequest().getParameterMap() != null;
            case FrameworkConstants.JSAttributes.JS_COOKIES:
                return getRequest().getCookies() != null;
            case FrameworkConstants.JSAttributes.JS_REQUEST_IP:
                return true;
            default:
                return super.hasMember(name);
        }
    }

    @Override
    public void putMember(String key, Value value) {
        LOG.warn("Unsupported operation. Servlet Request is read only. Can't remove parameter " + key);
    }

    @Override
    public boolean removeMember(String key) {

        return false;
    }

    private HttpServletRequest getRequest() {
        TransientObjectWrapper<HttpServletRequest> transientObjectWrapper = getWrapped();
        return transientObjectWrapper.getWrapped();
    }
}
